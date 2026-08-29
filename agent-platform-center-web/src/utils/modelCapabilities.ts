import type { PlatformResource } from '../types/agent';

export interface ModelApiCapability {
  key: string;
  label: string;
  method: string;
  path: string;
  streaming: boolean;
}

export interface ModelApiProfile {
  provider: string;
  model: string;
  baseUrl: string;
  apiKeyRequired: boolean;
  capabilities: ModelApiCapability[];
  requestExample: string;
}

interface ModelSchema {
  provider?: string;
  model?: string;
  baseUrl?: string;
  mediaBaseUrl?: string;
  apiKeyRequired?: boolean;
  capabilities?: string[];
}

const capabilityCatalog: Record<string, ModelApiCapability> = {
  chat: {
    key: 'chat',
    label: 'Chat Completions',
    method: 'POST',
    path: '/v1/chat/completions',
    streaming: false,
  },
  stream: {
    key: 'stream',
    label: 'Streaming Chat',
    method: 'POST',
    path: '/v1/chat/completions',
    streaming: true,
  },
  image: {
    key: 'image',
    label: 'Image Generation',
    method: 'POST',
    path: '/api/v1/images/generations',
    streaming: false,
  },
  video: {
    key: 'video',
    label: 'Video Generation',
    method: 'POST',
    path: '/api/v1/videos/generations',
    streaming: false,
  },
};

export function toModelApiProfile(resource: PlatformResource): ModelApiProfile {
  const schema = parseSchema(resource.schemaText);
  const model = schema.model || normalizeModelResourceId(resource.id);
  const capabilities = resolveCapabilities(schema.capabilities);
  const primaryCapability = capabilities[0] || capabilityCatalog.chat;
  const usesMediaService = capabilities.some((capability) => capability.key === 'image' || capability.key === 'video')
    && !capabilities.some((capability) => capability.key === 'chat' || capability.key === 'stream');
  const baseUrl = usesMediaService
    ? schema.mediaBaseUrl || schema.baseUrl || 'http://127.0.0.1:8092'
    : schema.baseUrl || 'http://127.0.0.1:8088';

  return {
    provider: schema.provider || (usesMediaService ? 'tenx-ai-media-service' : 'tenx-ai-gateway'),
    model,
    baseUrl,
    apiKeyRequired: schema.apiKeyRequired !== false,
    capabilities,
    requestExample: buildRequestExample(model, primaryCapability),
  };
}

function parseSchema(schemaText: string): ModelSchema {
  if (!schemaText || !schemaText.trim()) {
    return {};
  }
  try {
    return JSON.parse(schemaText) as ModelSchema;
  } catch {
    return {};
  }
}

function resolveCapabilities(capabilities?: string[]): ModelApiCapability[] {
  const keys = capabilities && capabilities.length > 0 ? capabilities : ['chat', 'stream'];
  return keys
    .map((key) => capabilityCatalog[key])
    .filter((capability): capability is ModelApiCapability => Boolean(capability));
}

function normalizeModelResourceId(id: string) {
  return id.replace(/^model-/, '');
}

function buildRequestExample(model: string, capability: ModelApiCapability) {
  const body = capability.key === 'image'
    ? { model, prompt: '生成一张产品架构图' }
    : capability.key === 'video'
      ? { model, prompt: '生成一个短视频片段' }
      : {
          model,
          stream: capability.streaming,
          messages: [
            { role: 'user', content: '分析这个 Spring Boot 项目' },
          ],
        };

  return JSON.stringify(body, null, 2);
}
