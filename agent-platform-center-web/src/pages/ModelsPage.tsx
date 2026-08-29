import { ApiOutlined, CheckCircleOutlined, PlusOutlined } from '@ant-design/icons';
import { useQuery } from '@tanstack/react-query';
import { Button, Drawer, Form, Input, Select, Space, Table, Tag, Typography, message } from 'antd';
import type { ColumnsType } from 'antd/es/table';
import { useMemo, useState } from 'react';
import { createModel, fetchModels } from '../api/client';
import type { CreatePlatformResourceRequest, PlatformResource } from '../types/agent';
import { toModelApiProfile } from '../utils/modelCapabilities';

export default function ModelsPage() {
  const [open, setOpen] = useState(false);
  const [selectedModel, setSelectedModel] = useState<PlatformResource | null>(null);
  const [form] = Form.useForm<CreatePlatformResourceRequest>();
  const { data = [], isLoading, refetch } = useQuery({
    queryKey: ['models'],
    queryFn: fetchModels,
  });

  const selectedProfile = useMemo(
    () => selectedModel ? toModelApiProfile(selectedModel) : null,
    [selectedModel],
  );

  async function handleCreate(values: CreatePlatformResourceRequest) {
    await createModel(values);
    message.success('Model created');
    setOpen(false);
    form.resetFields();
    await refetch();
  }

  const columns: ColumnsType<PlatformResource> = [
    {
      title: 'Model',
      dataIndex: 'name',
      key: 'name',
      render: (name, record) => {
        const profile = toModelApiProfile(record);
        return (
          <div className="table-primary">
            <strong>{name}</strong>
            <span>{record.description}</span>
            <code>{profile.model}</code>
          </div>
        );
      },
    },
    {
      title: 'Provider',
      key: 'provider',
      width: 180,
      render: (_, record) => <Tag color="cyan">{toModelApiProfile(record).provider}</Tag>,
    },
    {
      title: 'API Capability',
      key: 'capabilities',
      render: (_, record) => (
        <Space wrap>
          {toModelApiProfile(record).capabilities.map((capability) => (
            <Tag key={capability.key} icon={<CheckCircleOutlined />} color={capability.streaming ? 'purple' : 'green'}>
              {capability.label}
            </Tag>
          ))}
        </Space>
      ),
    },
    {
      title: 'Provider URL',
      key: 'gateway',
      width: 240,
      render: (_, record) => <code>{toModelApiProfile(record).baseUrl}</code>,
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
        <Button size="small" icon={<ApiOutlined />} onClick={() => setSelectedModel(record)}>
          API
        </Button>
      ),
    },
  ];

  return (
    <section className="page-stack">
      <div className="page-title-row">
        <div>
          <Typography.Title level={2}>Models</Typography.Title>
          <Typography.Text type="secondary">View chat capabilities through tenx-ai-gateway and media capabilities through tenx-ai-media-service.</Typography.Text>
        </div>
        <Button type="primary" icon={<PlusOutlined />} onClick={() => setOpen(true)}>
          New Model
        </Button>
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
        title="Model API Capability"
        width={620}
        open={Boolean(selectedModel)}
        onClose={() => setSelectedModel(null)}
      >
        {selectedModel && selectedProfile && (
          <Space direction="vertical" size={18} className="full-width">
            <div className="model-api-summary">
              <div>
                <Typography.Text type="secondary">Model</Typography.Text>
                <strong>{selectedModel.name}</strong>
                <code>{selectedProfile.model}</code>
              </div>
              <div>
                <Typography.Text type="secondary">Provider</Typography.Text>
                <strong>{selectedProfile.provider}</strong>
                <code>{selectedProfile.baseUrl}</code>
              </div>
            </div>

            <div className="api-capability-list">
              {selectedProfile.capabilities.map((capability) => (
                <div className="api-capability-row" key={capability.key}>
                  <Tag color="blue">{capability.method}</Tag>
                  <strong>{capability.path}</strong>
                  <span>{capability.label}</span>
                  {capability.streaming && <Tag color="purple">stream</Tag>}
                </div>
              ))}
            </div>

            <div>
              <Typography.Title level={5}>Request</Typography.Title>
              <pre className="code-block">{selectedProfile.requestExample}</pre>
            </div>
          </Space>
        )}
      </Drawer>

      <Drawer
        title="New Model"
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
            version: 'v1',
            tags: ['gateway'],
            content: '',
            schemaText: '{\n  "provider": "tenx-ai-gateway",\n  "model": "qwen3-coder-next",\n  "baseUrl": "http://127.0.0.1:8088",\n  "capabilities": ["chat", "stream"]\n}',
          }}
          onFinish={handleCreate}
        >
          <Form.Item label="Name" name="name" rules={[{ required: true, message: 'Please enter a name' }]}>
            <Input />
          </Form.Item>
          <Form.Item label="Version" name="version" rules={[{ required: true, message: 'Please enter a version' }]}>
            <Input />
          </Form.Item>
          <Form.Item label="Description" name="description" rules={[{ required: true, message: 'Please enter a description' }]}>
            <Input.TextArea rows={4} />
          </Form.Item>
          <Form.Item label="Tags" name="tags">
            <Select mode="tags" tokenSeparators={[',']} />
          </Form.Item>
          <Form.Item label="Schema" name="schemaText">
            <Input.TextArea rows={10} />
          </Form.Item>
        </Form>
      </Drawer>
    </section>
  );
}
