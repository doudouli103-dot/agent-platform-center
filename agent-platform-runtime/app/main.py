import asyncio
import json
from typing import AsyncIterator

from fastapi import FastAPI
from fastapi.responses import StreamingResponse
from pydantic import BaseModel


app = FastAPI(title="Agent Platform Runtime", version="0.1.0")


class RunRequest(BaseModel):
    agent_id: str
    message: str


@app.get("/health")
def health() -> dict:
    return {"status": "UP"}


@app.post("/runs")
def create_run(request: RunRequest) -> dict:
    return {
        "run_id": "runtime-run-demo",
        "agent_id": request.agent_id,
        "status": "created",
        "events_url": "/runs/runtime-run-demo/events",
    }


@app.get("/runs/{run_id}/events")
async def stream_run_events(run_id: str) -> StreamingResponse:
    async def events() -> AsyncIterator[str]:
        for name, payload in [
            ("run.started", {"runId": run_id}),
            ("skill.selected", {"skill": "java-review@v1"}),
            ("model.token", {"text": "Runtime stream is ready."}),
            ("run.completed", {"status": "completed"}),
        ]:
            yield "event: {}\ndata: {}\n\n".format(name, json.dumps(payload, ensure_ascii=False))
            await asyncio.sleep(0.35)

    return StreamingResponse(events(), media_type="text/event-stream")
