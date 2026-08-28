import { SendOutlined } from '@ant-design/icons';
import { useQuery } from '@tanstack/react-query';
import { Button, Descriptions, Input, Select, Space, Tag, Timeline, Typography } from 'antd';
import { useEffect, useMemo, useState } from 'react';
import { createRun, fetchAgents, fetchMcpServers, fetchPrompts, fetchSkills, fetchTools } from '../api/client';
import type { AgentDefinition, PlatformResource, TraceEvent } from '../types/agent';

const { TextArea } = Input;

interface ChatPageProps {
  initialAgentId?: string;
}

interface ChatMessage {
  id: string;
  role: 'user' | 'assistant';
  content: string;
}

interface RunResult {
  runId?: string;
  status: 'idle' | 'running' | 'completed' | 'error';
  gateway?: Record<string, unknown>;
  trace?: Record<string, unknown>;
  startedAt?: string;
  completedAt?: string;
}

export default function ChatPage({ initialAgentId }: ChatPageProps) {
  const { data: agents = [] } = useQuery({ queryKey: ['agents'], queryFn: fetchAgents });
  const { data: prompts = [] } = useQuery({ queryKey: ['prompts'], queryFn: fetchPrompts });
  const { data: skills = [] } = useQuery({ queryKey: ['skills'], queryFn: fetchSkills });
  const { data: mcpServers = [] } = useQuery({ queryKey: ['mcpServers'], queryFn: fetchMcpServers });
  const { data: tools = [] } = useQuery({ queryKey: ['tools'], queryFn: fetchTools });
  const [agentId, setAgentId] = useState(initialAgentId || 'agent-java-architect');
  const [input, setInput] = useState('帮我分析当前需求，并给出下一步实现建议。');
  const [messages, setMessages] = useState<ChatMessage[]>([]);
  const [events, setEvents] = useState<TraceEvent[]>([]);
  const [runResult, setRunResult] = useState<RunResult>({ status: 'idle' });
  const [running, setRunning] = useState(false);

  useEffect(() => {
    if (initialAgentId) {
      setAgentId(initialAgentId);
    }
  }, [initialAgentId]);

  const selectedAgent = useMemo(
    () => agents.find((agent) => agent.id === agentId),
    [agents, agentId],
  );

  const agentOptions = useMemo(
    () => agents.map((agent) => ({ label: agent.name, value: agent.id })),
    [agents],
  );

  async function sendMessage() {
    const content = input.trim();
    if (!content || running) {
      return;
    }

    const userMessage: ChatMessage = { id: `user-${Date.now()}`, role: 'user', content };
    const assistantId = `assistant-${Date.now()}`;
    setMessages((current) => [...current, userMessage, { id: assistantId, role: 'assistant', content: '' }]);
    setEvents([]);
    setInput('');
    setRunning(true);

    const run = await createRun(agentId, content);
    setRunResult({
      runId: run.runId,
      status: 'running',
      startedAt: new Date().toLocaleTimeString(),
    });
    const source = new EventSource(run.eventsUrl);

    const addEvent = (name: string, data: string) => {
      setEvents((current) => [...current, { name, data, time: new Date().toLocaleTimeString() }]);
    };

    ['run.started', 'skill.selected', 'mcp.started', 'mcp.completed', 'gateway.selected', 'trace.completed', 'run.completed'].forEach((name) => {
      source.addEventListener(name, (event) => {
        const data = (event as MessageEvent).data;
        addEvent(name, data);
        updateRunResult(name, data);
        if (name === 'run.completed') {
          setRunning(false);
          source.close();
        }
      });
    });

    source.addEventListener('model.token', (event) => {
      const payload = JSON.parse((event as MessageEvent).data) as { text: string };
      setMessages((current) => current.map((item) => (
        item.id === assistantId ? { ...item, content: item.content + payload.text } : item
      )));
      addEvent('model.token', payload.text);
    });

    source.onerror = () => {
      addEvent('stream.error', 'SSE connection closed unexpectedly');
      setRunResult((current) => ({ ...current, status: 'error', completedAt: new Date().toLocaleTimeString() }));
      setRunning(false);
      source.close();
    };
  }

  function updateRunResult(eventName: string, data: string) {
    const payload = parseEventData(data);
    if (eventName === 'gateway.selected') {
      setRunResult((current) => ({ ...current, gateway: payload }));
    }
    if (eventName === 'trace.completed') {
      setRunResult((current) => ({ ...current, trace: payload }));
    }
    if (eventName === 'run.completed') {
      setRunResult((current) => ({ ...current, status: 'completed', completedAt: new Date().toLocaleTimeString() }));
    }
  }

  return (
    <section className="chat-layout">
      <div className="chat-main-panel">
        <div className="page-title-row compact">
          <div>
            <Typography.Title level={2}>Chat</Typography.Title>
            <Typography.Text type="secondary">Use a configured Agent directly with its model, prompt, skill, MCP, and tool bindings.</Typography.Text>
          </div>
        </div>

        <Space direction="vertical" size={12} className="full-width">
          <Select value={agentId} options={agentOptions} onChange={setAgentId} className="full-width" />

          {selectedAgent && (
            <div className="agent-binding-strip">
              <Tag color="blue">{selectedAgent.model}</Tag>
              <Tag color="green">{selectedAgent.promptVersion}</Tag>
              {selectedAgent.skills.map((skill) => <Tag key={skill}>{skill}</Tag>)}
              {selectedAgent.mcpServers.map((server) => <Tag color="cyan" key={server}>{server}</Tag>)}
              {selectedAgent.tools.map((tool) => <Tag color="purple" key={tool}>{tool}</Tag>)}
            </div>
          )}
        </Space>

        <div className="chat-thread">
          {messages.length === 0 && (
            <div className="chat-empty">
              <Typography.Text type="secondary">Select an Agent and start chatting.</Typography.Text>
            </div>
          )}
          {messages.map((item) => (
            <div key={item.id} className={`chat-message ${item.role}`}>
              <strong>{item.role === 'user' ? 'You' : selectedAgent?.name || 'Agent'}</strong>
              <p>{item.content || 'Thinking...'}</p>
            </div>
          ))}
        </div>

        <div className="chat-composer">
          <TextArea
            value={input}
            onChange={(event) => setInput(event.target.value)}
            onPressEnter={(event) => {
              if (!event.shiftKey) {
                event.preventDefault();
                void sendMessage();
              }
            }}
            rows={3}
          />
          <Button type="primary" icon={<SendOutlined />} loading={running} onClick={sendMessage}>
            Send
          </Button>
        </div>
      </div>

      <div className="chat-side-panel">
        <AgentResultPanel
          agent={selectedAgent}
          prompts={prompts}
          skills={skills}
          mcpServers={mcpServers}
          tools={tools}
          runResult={runResult}
        />

        <div className="chat-trace-panel">
          <Typography.Title level={4}>Trace</Typography.Title>
          <Timeline
            items={events.map((event) => ({
              children: (
                <div className="trace-item">
                  <strong>{event.name}</strong>
                  <span>{event.time}</span>
                  <code>{event.data}</code>
                </div>
              ),
            }))}
          />
        </div>
      </div>
    </section>
  );
}

interface AgentResultPanelProps {
  agent?: AgentDefinition;
  prompts: PlatformResource[];
  skills: PlatformResource[];
  mcpServers: PlatformResource[];
  tools: PlatformResource[];
  runResult: RunResult;
}

function AgentResultPanel({ agent, prompts, skills, mcpServers, tools, runResult }: AgentResultPanelProps) {
  return (
    <div className="chat-result-panel">
      <Typography.Title level={4}>Agent Result Data</Typography.Title>
      <Descriptions size="small" column={1} bordered>
        <Descriptions.Item label="Agent">{agent?.name || '-'}</Descriptions.Item>
        <Descriptions.Item label="Run ID">{runResult.runId || '-'}</Descriptions.Item>
        <Descriptions.Item label="Status">
          <Tag color={runStatusColor(runResult.status)}>{runResult.status}</Tag>
        </Descriptions.Item>
        <Descriptions.Item label="Model">{agent?.model || '-'}</Descriptions.Item>
        <Descriptions.Item label="Gateway">{stringifyValue(runResult.gateway) || '-'}</Descriptions.Item>
        <Descriptions.Item label="Trace">{stringifyValue(runResult.trace) || '-'}</Descriptions.Item>
      </Descriptions>

      {agent && (
        <Space direction="vertical" size={12} className="full-width binding-detail-list">
          <BindingGroup title="Prompt" values={[agent.promptVersion]} resources={prompts} color="green" />
          <BindingGroup title="Skills" values={agent.skills} resources={skills} color="default" />
          <BindingGroup title="MCP Servers" values={agent.mcpServers} resources={mcpServers} color="cyan" />
          <BindingGroup title="Tools" values={agent.tools} resources={tools} color="purple" />
        </Space>
      )}
    </div>
  );
}

interface BindingGroupProps {
  title: string;
  values: string[];
  resources: PlatformResource[];
  color: string;
}

function BindingGroup({ title, values, resources, color }: BindingGroupProps) {
  return (
    <div className="binding-group">
      <Typography.Text type="secondary">{title}</Typography.Text>
      <Space wrap>
        {values.length === 0 && <Tag>None</Tag>}
        {values.map((value) => {
          const resource = findBoundResource(value, resources);
          return (
            <Tag color={color} key={value}>
              {resource ? `${resource.name} @ ${resource.version}` : value}
            </Tag>
          );
        })}
      </Space>
    </div>
  );
}

function findBoundResource(binding: string, resources: PlatformResource[]) {
  const [id, version] = binding.split('@');
  return resources.find((resource) => resource.id === id && (!version || resource.version === version));
}

function parseEventData(data: string): Record<string, unknown> {
  try {
    return JSON.parse(data) as Record<string, unknown>;
  } catch {
    return { value: data };
  }
}

function stringifyValue(value?: Record<string, unknown>) {
  if (!value) {
    return '';
  }
  return JSON.stringify(value);
}

function runStatusColor(status: RunResult['status']) {
  if (status === 'completed') {
    return 'green';
  }
  if (status === 'running') {
    return 'blue';
  }
  if (status === 'error') {
    return 'red';
  }
  return 'default';
}
