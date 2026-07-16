---
tags: [terminal-simulation, limitations, improvements]
---

# Honest Limitations and Improvements

Good defense answers acknowledge tradeoffs without attacking the project.

## Limitation: simulation rules are simplified

The terminal models useful queue behavior but not every real-world situation, such as multiple ticket agents, reservations, traffic, or detailed schedules.

**Possible improvement:** add more rules only after preserving the queue-focused learning goal.

## Limitation: state is not saved

Closing the program loses passengers, buses, and logs.

**Possible improvement:** save and load a simulation snapshot from a file. This was left out because persistence is outside the assigned queue topic.

## Limitation: random behavior makes runs different

Random spawning improves the simulation but can make exact demonstrations harder to repeat.

**Possible improvement:** accept a fixed random seed or add a deterministic demonstration mode.

## Limitation: UI and simulation share the Swing event thread

The project is small enough for this design, but expensive future operations could make the interface pause.

**Possible improvement:** keep lightweight state updates on Swing's thread and move genuinely heavy work to a background worker with safe UI updates.

## Why the project is still appropriately scoped

> The goal was not to build a production transport system. The goal was to demonstrate a node-based FIFO queue clearly inside a working Java Swing simulation. The current scope supports that goal while remaining explainable for defense.
