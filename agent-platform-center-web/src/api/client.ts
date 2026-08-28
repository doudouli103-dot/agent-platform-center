import axios from 'axios';
import type { AgentDefinition, CreateAgentRequest, CreatePlatformResourceRequest, PlatformResource, RunRecord, RunResponse } from '../types/agent';

export const api = axios.create({
  baseURL: '/api',
  timeout: 15000,
});

export async function fetchAgents(): Promise<AgentDefinition[]> {
  const response = await api.get<AgentDefinition[]>('/agents');
  return response.data;
}

export async function createAgent(payload: CreateAgentRequest): Promise<AgentDefinition> {
  const response = await api.post<AgentDefinition>('/agents', payload);
  return response.data;
}

export async function fetchPrompts(): Promise<PlatformResource[]> {
  const response = await api.get<PlatformResource[]>('/prompts');
  return response.data;
}

export async function fetchSkills(): Promise<PlatformResource[]> {
  const response = await api.get<PlatformResource[]>('/skills');
  return response.data;
}

export async function fetchMcpServers(): Promise<PlatformResource[]> {
  const response = await api.get<PlatformResource[]>('/mcp-servers');
  return response.data;
}

export async function fetchTools(): Promise<PlatformResource[]> {
  const response = await api.get<PlatformResource[]>('/tools');
  return response.data;
}

export async function fetchModels(): Promise<PlatformResource[]> {
  const response = await api.get<PlatformResource[]>('/models');
  return response.data;
}

export async function createPrompt(payload: CreatePlatformResourceRequest): Promise<PlatformResource> {
  const response = await api.post<PlatformResource>('/prompts', payload);
  return response.data;
}

export async function createSkill(payload: CreatePlatformResourceRequest): Promise<PlatformResource> {
  const response = await api.post<PlatformResource>('/skills', payload);
  return response.data;
}

export async function createMcpServer(payload: CreatePlatformResourceRequest): Promise<PlatformResource> {
  const response = await api.post<PlatformResource>('/mcp-servers', payload);
  return response.data;
}

export async function createTool(payload: CreatePlatformResourceRequest): Promise<PlatformResource> {
  const response = await api.post<PlatformResource>('/tools', payload);
  return response.data;
}

export async function createModel(payload: CreatePlatformResourceRequest): Promise<PlatformResource> {
  const response = await api.post<PlatformResource>('/models', payload);
  return response.data;
}

export async function createRun(agentId: string, message: string): Promise<RunResponse> {
  const response = await api.post<RunResponse>('/runs', { agentId, message });
  return response.data;
}

export async function fetchRuns(): Promise<RunRecord[]> {
  const response = await api.get<RunRecord[]>('/runs');
  return response.data;
}
