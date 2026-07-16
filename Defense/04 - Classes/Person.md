
# Person

Source: [TerminalSimulation](https://github.com/PixelAlien0/Terminal-Simulation/blob/main/src/TerminalSimulation.java) · states: [TerminalSimulation](https://github.com/PixelAlien0/Terminal-Simulation/blob/main/src/TerminalSimulation.java)

## Data groups

| Group | Examples |
|---|---|
| Identity | `id`, `destination`, `isPriority`, `arrivalOrder` |
| Position | `x`, `y`, private target coordinates |
| Simulation | `state`, `assignedBus`, `ticketTimerMs` |
| Animation | frame and slowdown counters |

## Behavior

- `setTarget` changes the destination coordinate.
- `stepTowardTarget` moves by at most `PASSENGER_SPEED` on each axis and returns whether the target was reached.
- `draw` renders the passenger and ticket progress; it does not change queue membership.

<details>
<summary><strong>Who changes passenger states after movement completes?</strong></summary>

`SimulationEngine.movePassengers`, based on the passenger's current state when `stepTowardTarget()` returns `true`.

</details>
