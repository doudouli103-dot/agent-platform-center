import { SendOutlined } from '@ant-design/icons';
import { useQuery } from '@tanstack/react-query';
import { Button, Input, Select, Space, Timeline, Typography } from 'antd';
import { useMemo, useState } from 'react';
import { createRun, fetchAgents } from '../api/client';
import type { TraceEvent } from '../types/agent';

const { TextArea } = Input;

export default function PlaygroundPage() {
  const { data: agents = [] } = useQuery({ queryKey: ['agents'], queryFn: fetchAgents });
  const [agentId, setAgentId] = useState('agent-java-architect');
  const [message, setMessage] = useState('分析这个 Spring Boot 项目，找出架构问题并给出修改建议。');
  const [answer, setAnswer] = useState('');
  const [events, setEvents] = useState<TraceEvent[]>([]);
  const [running, setRunning] = useState(false);

  const agentOptions = useMemo(
    () => agents.map((agent) => ({ label: agent.name, value: agent.id })),
    [agents],
  );

  async function runAgent() {
    setRunning(true);
    setAnswer('');
    setEvents([]);

    const run = await createRun(agentId, message);
    const source = new EventSource(run.eventsUrl);

    const addEvent = (name: string, data: string) => {
      setEvents((current) => [...current, { name, data, time: new Date().toLocaleTimeString() }]);
    };

    ['run.started', 'skill.selected', 'mcp.started', 'mcp.result', 'mcp.completed', 'gateway.selected', 'trace.completed', 'run.completed'].forEach((name) => {
      source.addEventListener(name, (event) => {
        addEvent(name, (event as MessageEvent).data);
        if (name === 'run.completed') {
          setRunning(false);
          source.close();
        }
      });
    });

    source.addEventListener('model.token', (event) => {
      const payload = JSON.parse((event as MessageEvent).data) as { text: string };
      setAnswer((current) => current + payload.text);
      addEvent('model.token', payload.text);
    });

    source.onerror = () => {
      addEvent('stream.error', 'SSE connection closed unexpectedly');
      setRunning(false);
      source.close();
    };
  }

  return (
    <section className="playground-grid">
      <div className="builder-panel">
        <div className="page-title-row compact">
          <div>
            <Typography.Title level={2}>Playground</Typography.Title>
            <Typography.Text type="secondary">Run an Agent and inspect streaming output with trace events.</Typography.Text>
          </div>
        </div>
        <Space direction="vertical" size={16} className="full-width">
          <Select
            value={agentId}
            options={agentOptions}
            onChange={setAgentId}
            className="full-width"
          />
          <TextArea
            value={message}
            onChange={(event) => setMessage(event.target.value)}
            rows={8}
          />
          <Button type="primary" icon={<SendOutlined />} loading={running} onClick={runAgent}>
            Run
          </Button>
        </Space>
      </div>

      <div className="result-panel">
        <Typography.Title level={4}>Agent Output</Typography.Title>
        <div className="answer-box">{answer || 'Output will stream here.'}</div>
      </div>

      <div className="trace-panel">
        <Typography.Title level={4}>Execution Trace</Typography.Title>
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
    </section>
  );
}
