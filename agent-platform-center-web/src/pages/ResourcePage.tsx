import { useQuery } from '@tanstack/react-query';
import { Button, Drawer, Form, Input, Select, Space, Table, Tag, Typography, message } from 'antd';
import type { ColumnsType } from 'antd/es/table';
import { useState } from 'react';
import type { CreatePlatformResourceRequest, PlatformResource } from '../types/agent';

interface ResourcePageProps {
  title: string;
  description: string;
  createLabel: string;
  queryKey: string;
  queryFn: () => Promise<PlatformResource[]>;
  createFn: (payload: CreatePlatformResourceRequest) => Promise<PlatformResource>;
}

const columns: ColumnsType<PlatformResource> = [
  {
    title: 'Name',
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
    title: 'Type',
    dataIndex: 'type',
    key: 'type',
    width: 120,
    render: (type: string) => <Tag color="blue">{type}</Tag>,
  },
  {
    title: 'Version',
    dataIndex: 'version',
    key: 'version',
    width: 120,
  },
  {
    title: 'Tags',
    dataIndex: 'tags',
    key: 'tags',
    render: (tags: string[]) => (
      <Space wrap>
        {tags.map((tag) => (
          <Tag key={tag}>{tag}</Tag>
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
];

export default function ResourcePage(props: ResourcePageProps) {
  const [open, setOpen] = useState(false);
  const [form] = Form.useForm<CreatePlatformResourceRequest>();
  const { data = [], isLoading, refetch } = useQuery({
    queryKey: [props.queryKey],
    queryFn: props.queryFn,
  });

  async function handleCreate(values: CreatePlatformResourceRequest) {
    await props.createFn(values);
    message.success(`${props.title.slice(0, -1)} created`);
    setOpen(false);
    form.resetFields();
    await refetch();
  }

  return (
    <section className="page-stack">
      <div className="page-title-row">
        <div>
          <Typography.Title level={2}>{props.title}</Typography.Title>
          <Typography.Text type="secondary">{props.description}</Typography.Text>
        </div>
        <Button type="primary" onClick={() => setOpen(true)}>{props.createLabel}</Button>
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
        title={props.createLabel}
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
          initialValues={{ version: 'v1', tags: [], content: '', schemaText: '' }}
          onFinish={handleCreate}
        >
          <Form.Item
            label="Name"
            name="name"
            rules={[{ required: true, message: 'Please enter a name' }]}
          >
            <Input />
          </Form.Item>
          <Form.Item
            label="Version"
            name="version"
            rules={[{ required: true, message: 'Please enter a version' }]}
          >
            <Input />
          </Form.Item>
          <Form.Item
            label="Description"
            name="description"
            rules={[{ required: true, message: 'Please enter a description' }]}
          >
            <Input.TextArea rows={4} />
          </Form.Item>
          <Form.Item label="Tags" name="tags">
            <Select mode="tags" tokenSeparators={[',']} />
          </Form.Item>
          <Form.Item label="Content" name="content">
            <Input.TextArea rows={6} placeholder="Prompt body, Skill instructions, MCP connection notes, or Tool implementation notes." />
          </Form.Item>
          <Form.Item label="Schema" name="schemaText">
            <Input.TextArea rows={6} placeholder="{&quot;input&quot;:{},&quot;output&quot;:{}}" />
          </Form.Item>
        </Form>
      </Drawer>
    </section>
  );
}
