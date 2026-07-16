
# Limitations

Good defense answers acknowledge tradeoffs without attacking the project.

## Simplified terminal rules

The simulation omits reservations, traffic, detailed schedules, and multiple agents.

**Improvement:** Add selected rules only if the queue-focused learning goal stays clear.

## No persistence

Closing the application loses passengers, buses, and logs.

**Improvement:** Save and restore an engine snapshot. Persistence was outside the assigned queue topic.

## Random runs

Random spawning makes demonstrations less repeatable.

**Improvement:** Accept a fixed random seed or add a deterministic demonstration mode, as the tests already do with `Random(7)`.

## Swing-thread workload

The timer and simulation run on Swing's event thread. The current work is small, but expensive future operations could pause the UI.

**Improvement:** Keep lightweight state changes on the EDT and move genuinely heavy work to a safe background worker.

<details>
<summary><strong>Scope defense</strong></summary>

The goal was not a production transport system. It was to demonstrate a custom node-based FIFO queue clearly inside a working Java Swing simulation.

</details>
