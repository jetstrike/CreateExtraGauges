import json

path = r"C:\Users\DMills\.gemini\antigravity-ide\brain\2232a745-a1d8-4872-b2d7-cc56bda09bd5\.system_generated\logs\transcript.jsonl"
with open(path, "r", encoding="utf-8") as f:
    for line in f:
        data = json.loads(line)
        if "task-1475" in line:
            print(json.dumps(data, indent=2))
