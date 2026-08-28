import { useQuery } from '@tanstack/react-query';
import { Button, Drawer, Form, Input, Select, Space, Table, Tag, Typography, message } from 'antd';
import type { ColumnsType } from 'antd/es/table';
import { useState } from 'react';
import { createAgent, fetchAgents, fetchMcpServers, fetchModels, fetchPrompts, fetchSkills, fetchTools } from '../api/client';
import type { AgentDefinition, CreateAgentRequest } from '../types/agent';

interface AgentsPageProps {
  onChat: (agentId: string) => void;
}

export default function AgentsPage({ onChat }: AgentsPageProps) {
  const [open, setOpen] = useState(false);
  const [form] = Form.useForm<CreateAgentRequest>();
  const { data = [], isLoading, refetch } = useQuery({
    queryKey: ['agents'],
    queryFn: fetchAgents,
  });
  const { data: prompts = [] } = useQuery({ queryKey: ['prompts'], queryFn: fetchPrompts });
  const { data: models = [] } = useQuery({ queryKey: ['models'], queryFn: fetchModels });
  const { data: skills = [] } = useQuery({ queryKey: ['skills'], queryFn: fetchSkills });
  const { data: mcpServers = [] } = useQuery({ queryKey: ['mcpServers'], queryFn: fetchMcpServers });
  const { data: tools = [] } = useQuery({ queryKey: ['tools'], queryFn: fetchTools });

  async function handleCreate(values: CreateAgentRequest) {
    const agent = await createAgent(values);
    message.success('Agent created');
    setOpen(false);
    form.resetFields();
    await refetch();
    onChat(agent.id);
  }

  const columns: ColumnsType<AgentDefinition> = [
    {
      title: 'Agent',
      dataIndex: 'name',
      key: 'name',
      render: (name, record) => (
        <div className="table-primary">
          <strong>{name}</strong>
          <span>{record.description}</span>
        </div>
      ),
    },
    {
      title: 'Model',
      dataIndex: 'model',
      key: 'model',
      width: 180,
    },
    {
      title: 'Prompt',
      dataIndex: 'promptVersion',
      key: 'promptVersion',
      width: 220,
    },
    {
      title: 'Skills',
      dataIndex: 'skills',
      key: 'skills',
      render: (skills: string[]) => (
        <Space wrap>
          {skills.map((skill) => (
            <Tag key={skill}>{skill}</Tag>
          ))}
        </Space>
      ),
    },
    {
      title: 'MCP',
      dataIndex: 'mcpServers',
      key: 'mcpServers',
      render: (servers: string[]) => (
        <Space wrap>
          {servers.map((server) => (
            <Tag color="cyan" key={server}>{server}</Tag>
          ))}
        </Space>
      ),
    },
    {
      title: 'Tools',
      dataIndex: 'tools',
      key: 'tools',
      render: (tools: string[]) => (
        <Space wrap>
          {(tools || []).map((tool) => (
            <Tag color="purple" key={tool}>{tool}</Tag>
          ))}
        </Space>
      ),
    },
    {
      title: 'Status',
      dataIndex: 'status',
      key: 'status',
      width: 110,
      render: (status: string) => <Tag color="gold">{status}</Tag>,
    },
    {
      title: 'Action',
      key: 'action',
      width: 110,
      render: (_, record) => (
        <Button size="small" onClick={() => onChat(record.id)}>
          Use
        </Button>
      ),
    },
  ];

  return (
    <section className="page-stack">
      <div className="page-title-row">
        <div>
          <Typography.Title level={2}>Agents</Typography.Title>
          <Typography.Text type="secondary">Configure runtime-ready Agents from model, prompt, skill, and MCP bindings.</Typography.Text>
        </div>
        <Button type="primary" onClick={() => setOpen(true)}>New Agent</Button>
      </div>
      <Table
        rowKey="id"
        columns={columns}
        dataSource={data}
        loading={isLoading}
        pagination={false}
        bordered
      />
      <Drawer
        title="New Agent"
        width={520}
        open={open}
        onClose={() => setOpen(false)}
        destroyOnClose
        extra={
          <Space>
            <Button onClick={() => setOpen(false)}>Cancel</Button>
            <Button type="primary" onClick={() => form.submit()}>Save</Button>
          </Space>
        }
      >
        <Form
          form={form}
          layout="vertical"
          initialValues={{
            model: 'model-qwen3-coder-next@v1',
            promptVersion: 'prompt-java-architect@v1',
            skills: ['skill-java-review@v1'],
            mcpServers: ['mcp-filesystem@v1'],
            tools: ['tool-shell@v1'],
          }}
          onFinish={handleCreate}
        >
          <Form.Item
            label="Name"
            name="name"
            rules={[{ required: true, message: 'Please enter an Agent name' }]}
          >
            <Input placeholder="Code Reviewer" />
          </Form.Item>
          <Form.Item
            label="Description"
            name="description"
            rules={[{ required: true, message: 'Please enter a description' }]}
          >
            <Input.TextArea rows={3} placeholder="Review Java code and produce practical suggestions." />
          </Form.Item>
          <Form.Item
            label="Model"
            name="model"
            rules={[{ required: true, message: 'Please select a model' }]}
          >
            <Select
              options={models.map((model) => ({
                label: `${model.name} @ ${model.version}`,
                value: `${model.id}@${model.version}`,
              }))}
            />
          </Form.Item>
          <Form.Item
            label="Prompt"
            name="promptVersion"
            rules={[{ required: true, message: 'Please select a prompt' }]}
          >
            <Select
              options={prompts.map((prompt) => ({
                label: `${prompt.name} @ ${prompt.version}`,
                value: `${prompt.id}@${prompt.version}`,
              }))}
            />
          </Form.Item>
          <Form.Item label="Skills" name="skills">
            <Select
              mode="multiple"
              options={skills.map((skill) => ({
                label: `${skill.name} @ ${skill.version}`,
                value: `${skill.id}@${skill.version}`,
              }))}
            />
          </Form.Item>
          <Form.Item label="MCP Servers" name="mcpServers">
            <Select
              mode="multiple"
              options={mcpServers.map((server) => ({
                label: `${server.name} @ ${server.version}`,
                value: `${server.id}@${server.version}`,
              }))}
            />
          </Form.Item>
          <Form.Item label="Tools" name="tools">
            <Select
              mode="multiple"
              options={tools.map((tool) => ({
                label: `${tool.name} @ ${tool.version}`,
                value: `${tool.id}@${tool.version}`,
              }))}
            />
          </Form.Item>
        </Form>
      </Drawer>
    </section>
  );
}
