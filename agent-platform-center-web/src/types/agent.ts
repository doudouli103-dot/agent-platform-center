export interface AgentDefinition {
  id: string;
  name: string;
  description: string;
  model: string;
  promptVersion: string;
  skills: string[];
  mcpServers: string[];
  tools: string[];
  status: string;
}

export interface CreateAgentRequest {
  name: string;
  description: string;
  model: string;
  promptVersion: string;
  skills: string[];
  mcpServers: string[];
  tools: string[];
}

export interface CreatePlatformResourceRequest {
  name: string;
  version: string;
  description: string;
  tags: string[];
  content: string;
  schemaText: string;
}

export interface RunResponse {
  runId: string;
  status: string;
  eventsUrl: string;
}

export interface PlatformResource {
  id: string;
  name: string;
  type: string;
  version: string;
  description: string;
  tags: string[];
  content: string;
  schemaText: string;
  status: string;
}

export interface TraceEvent {
  name: string;
  data: string;
  time: string;
}
