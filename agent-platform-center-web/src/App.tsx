import { ApiOutlined, CodeOutlined, CommentOutlined, FileTextOutlined, RobotOutlined, SettingOutlined, ThunderboltOutlined, ToolOutlined } from '@ant-design/icons';
import { Layout, Menu, Typography } from 'antd';
import type { MenuProps } from 'antd';
import { useState } from 'react';
import { createMcpServer, createPrompt, createSkill, createTool, fetchMcpServers, fetchPrompts, fetchSkills, fetchTools } from './api/client';
import AgentsPage from './pages/AgentsPage';
import ChatPage from './pages/ChatPage';
import ModelsPage from './pages/ModelsPage';
import PlaygroundPage from './pages/PlaygroundPage';
import ResourcePage from './pages/ResourcePage';

const { Header, Content, Sider } = Layout;

type PageKey = 'agents' | 'models' | 'prompts' | 'skills' | 'mcp' | 'tools' | 'chat' | 'playground' | 'settings';

const items: MenuProps['items'] = [
  { key: 'agents', icon: <RobotOutlined />, label: 'Agents' },
  { key: 'models', icon: <ApiOutlined />, label: 'Models' },
  { key: 'prompts', icon: <FileTextOutlined />, label: 'Prompts' },
  { key: 'skills', icon: <CodeOutlined />, label: 'Skills' },
  { key: 'mcp', icon: <ApiOutlined />, label: 'MCP' },
  { key: 'tools', icon: <ToolOutlined />, label: 'Tools' },
  { key: 'chat', icon: <CommentOutlined />, label: 'Chat' },
  { key: 'playground', icon: <ThunderboltOutlined />, label: 'Playground' },
  { key: 'settings', icon: <SettingOutlined />, label: 'Settings' },
];

export default function App() {
  const [page, setPage] = useState<PageKey>('agents');
  const [chatAgentId, setChatAgentId] = useState<string | undefined>();

  function openAgentChat(agentId: string) {
    setChatAgentId(agentId);
    setPage('chat');
  }

  return (
    <Layout className="app-shell">
      <Sider width={232} theme="light" className="app-sider">
        <div className="brand">
          <div className="brand-mark">AP</div>
          <div>
            <Typography.Title level={4} className="brand-title">
              Agent Platform
            </Typography.Title>
            <Typography.Text type="secondary">Center</Typography.Text>
          </div>
        </div>
        <Menu
          mode="inline"
          selectedKeys={[page]}
          items={items}
          onClick={({ key }) => setPage(key as PageKey)}
        />
      </Sider>
      <Layout>
        <Header className="app-header">
          <Typography.Title level={3}>Agent Platform Center</Typography.Title>
        </Header>
        <Content className="app-content">
          {page === 'agents' && <AgentsPage onChat={openAgentChat} />}
          {page === 'models' && <ModelsPage />}
          {page === 'prompts' && (
            <ResourcePage
              title="Prompts"
              description="Manage reusable system prompts and versioned instruction templates."
              createLabel="New Prompt"
              queryKey="prompts"
              queryFn={fetchPrompts}
              createFn={createPrompt}
            />
          )}
          {page === 'skills' && (
            <ResourcePage
              title="Skills"
              description="Register executable Agent capabilities with clear input and output contracts."
              createLabel="New Skill"
              queryKey="skills"
              queryFn={fetchSkills}
              createFn={createSkill}
            />
          )}
          {page === 'mcp' && (
            <ResourcePage
              title="MCP Servers"
              description="Connect Agent tools through stable MCP server contracts."
              createLabel="New MCP"
              queryKey="mcpServers"
              queryFn={fetchMcpServers}
              createFn={createMcpServer}
            />
          )}
          {page === 'tools' && (
            <ResourcePage
              title="Tools"
              description="Define tool capabilities that Agents can bind and execute through the runtime."
              createLabel="New Tool"
              queryKey="tools"
              queryFn={fetchTools}
              createFn={createTool}
            />
          )}
          {page === 'chat' && <ChatPage initialAgentId={chatAgentId} />}
          {page === 'playground' && <PlaygroundPage />}
          {page === 'settings' && (
            <section className="placeholder-panel">
              <Typography.Title level={4}>Settings</Typography.Title>
              <Typography.Text type="secondary">Model routing, secrets, and publishing controls will land after the V1 run loop.</Typography.Text>
            </section>
          )}
        </Content>
      </Layout>
    </Layout>
  );
}
