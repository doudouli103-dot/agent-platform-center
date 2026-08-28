package com.lijunwei.agentplatformcenter.api.service;

import com.lijunwei.agentplatformcenter.api.model.AgentDefinition;
import com.lijunwei.agentplatformcenter.api.model.RunRecord;
import com.lijunwei.agentplatformcenter.api.model.RunRequest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Service;

import java.sql.Timestamp;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
public class RunHistoryService {
    private final JdbcTemplate jdbcTemplate;

    public RunHistoryService(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public void createRun(String runId, RunRequest request, Optional<AgentDefinition> agent) {
        jdbcTemplate.update(
                "insert into run_record (id, agent_id, agent_name, model, user_message, assistant_output, status, gateway_data, trace_data) values (?, ?, ?, ?, ?, ?, ?, ?, ?)",
                runId,
                request.getAgentId(),
                agent.map(AgentDefinition::getName).orElse(""),
                agent.map(AgentDefinition::getModel).orElse(""),
                request.getMessage(),
                "",
                "created",
                "",
                ""
        );
    }

    public void markRunning(String runId) {
        jdbcTemplate.update("update run_record set status = ? where id = ?", "running", runId);
    }

    public void recordEvent(String runId, String eventName, String eventData) {
        jdbcTemplate.update(
                "insert into run_event (id, run_id, event_name, event_data) values (?, ?, ?, ?)",
                "event-" + UUID.randomUUID().toString().substring(0, 8),
                runId,
                eventName,
                eventData
        );
    }

    public void appendAssistantOutput(String runId, String text) {
        String current = jdbcTemplate.queryForObject(
                "select assistant_output from run_record where id = ?",
                String.class,
                runId
        );
        jdbcTemplate.update(
                "update run_record set assistant_output = ? where id = ?",
                defaultString(current) + defaultString(text),
                runId
        );
    }

    public void updateGatewayData(String runId, String gatewayData) {
        jdbcTemplate.update("update run_record set gateway_data = ? where id = ?", defaultString(gatewayData), runId);
    }

    public void markCompleted(String runId, String traceData) {
        jdbcTemplate.update(
                "update run_record set status = ?, trace_data = ?, completed_at = current_timestamp where id = ?",
                "completed",
                defaultString(traceData),
                runId
        );
    }

    public void markFailed(String runId, String errorMessage) {
        jdbcTemplate.update(
                "update run_record set status = ?, trace_data = ?, completed_at = current_timestamp where id = ?",
                "failed",
                defaultString(errorMessage),
                runId
        );
    }

    public List<RunRecord> listRuns() {
        return jdbcTemplate.query(
                "select id, agent_id, agent_name, model, user_message, assistant_output, status, gateway_data, trace_data, created_at, completed_at from run_record order by created_at desc, id desc limit 50",
                runRecordMapper()
        );
    }

    private RowMapper<RunRecord> runRecordMapper() {
        return (rs, rowNum) -> new RunRecord(
                rs.getString("id"),
                rs.getString("agent_id"),
                rs.getString("agent_name"),
                rs.getString("model"),
                rs.getString("user_message"),
                rs.getString("assistant_output"),
                rs.getString("status"),
                rs.getString("gateway_data"),
                rs.getString("trace_data"),
                toIsoString(rs.getTimestamp("created_at")),
                toIsoString(rs.getTimestamp("completed_at"))
        );
    }

    private String toIsoString(Timestamp timestamp) {
        if (timestamp == null) {
            return "";
        }
        return timestamp.toInstant().toString();
    }

    private String defaultString(String value) {
        return value == null ? "" : value;
    }
}
