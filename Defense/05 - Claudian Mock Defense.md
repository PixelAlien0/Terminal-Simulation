---
tags: [terminal-simulation, claudian, mock-defense]
---

# Claudian Mock Defense

## Best prompt: strict oral defense

Copy this into Claudian:

```text
Act as our strict Data Structures and Algorithms teacher during a project defense.

Use only this vault and the actual Terminal-Simulation source code. Ask me one question at a time. Focus on HOW the code works, especially PassengerNode, PassengerQueue, FIFO, enqueue, dequeue, remove, SimulationEngine, Swing Timer, state enums, boarding, cleanup, and time complexity.

Do not reveal the answer until I attempt it. After each answer:
1. Score it 0, 1, or 2.
2. State what was correct.
3. Correct missing or inaccurate parts using a specific Java file or method.
4. Ask one follow-up “how” or “why” question.

Keep the language understandable for first-time Java students. Start with: “Explain your project in one minute.”
```

## Queue-only drill

```text
Quiz me only about our custom node-based queue. Make me draw front, rear, nodes, and next references after enqueue, dequeue, and remove operations. Include empty-queue and one-node edge cases. Ask time complexity. One question at a time; wait for my answer.
```

## Trace-the-code drill

```text
Give me a scenario from our project, such as creating a regular passenger, ticket service, boarding, bus departure, deleting a passenger, or deleting a bus. Ask me to trace the exact classes and methods involved in order. Correct me using the real source files. Do not provide the trace before I try.
```

## Group practice

```text
We are a group preparing for defense. Ask one question for Member 1, then a related follow-up for Member 2, then ask Member 3 to correct or add details. Rotate across queue implementation, simulation flow, Swing UI, models, enums, testing, and limitations. Keep a score table.
```

## Anti-hallucination instruction

Add this whenever Claudian invents something:

```text
Do not assume. Before correcting us, search the actual src and test folders. Name the exact file and method supporting your correction. If the project does not contain something, say so clearly.
```
