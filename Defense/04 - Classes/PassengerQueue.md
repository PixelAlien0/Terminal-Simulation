
# PassengerQueue

Source: [TerminalSimulation](https://github.com/PixelAlien0/Terminal-Simulation/blob/main/src/TerminalSimulation.java)

## Fields

| Field | Responsibility |
|---|---|
| `front` | First node and next removal point. |
| `rear` | Final node and next attachment point. |
| `size` | Stored number of nodes. |

## Public-to-package operations

| Method | Behavior |
|---|---|
| `enqueue` | Reject null, create node, attach at rear. |
| `dequeue` | Remove front or return null when empty. |
| `peek` | Return front passenger without removal. |
| `remove` | Find an exact passenger and repair links. |
| `contains` | Traverse for an exact passenger. |
| `isEmpty` | Check `front == null`. |
| `size` | Return maintained count. |
| `frontNode` | Expose traversal start to engine positioning and tests. |

## Where it is used

- `SimulationEngine.TicketLane.queue`: regular and priority ticket order.
- `Bus.boardingLine`: regular passengers waiting to enter one bus.

<details>
<summary><strong>Why is `PassengerQueue` custom rather than `java.util.Queue`?</strong></summary>

The assignment is about queues, and the implementation exposes nodes, links, front/rear handling, FIFO behavior, and removal repair directly.

</details>

Deep dives: [Enqueue Trace](https://github.com/PixelAlien0/Terminal-Simulation/blob/main/Defense/02%20-%20Queue/Enqueue%20Trace.md) · [Dequeue Trace](https://github.com/PixelAlien0/Terminal-Simulation/blob/main/Defense/02%20-%20Queue/Dequeue%20Trace.md) · [Remove and Link Repair](https://github.com/PixelAlien0/Terminal-Simulation/blob/main/Defense/02%20-%20Queue/Remove%20and%20Link%20Repair.md)
