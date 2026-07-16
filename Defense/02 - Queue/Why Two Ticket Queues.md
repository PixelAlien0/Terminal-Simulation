
# Why Two Ticket Queues

`SimulationEngine` owns two `TicketLane` objects:

```java
private final TicketLane priorityLane = ...;
private final TicketLane regularLane = ...;
private final TicketLane[] ticketLanes = {priorityLane, regularLane};
```

Each lane contains its own custom `PassengerQueue`. `createPassenger` chooses the lane with `lane(priority).queue.enqueue(passenger)`.

## Does priority break FIFO?

No. Each lane remains FIFO internally. In `update`, the engine calls `updateTicketLane` for both lanes in array order, and each lane independently peeks and dequeues its own front passenger.

> [!CAUTION]
> **Do not claim**
> “The project uses Java's `PriorityQueue`.” It does not.

> [!NOTE]
> **Separate boarding rule**
> At buses, `loadBus` first searches the platform for a matching priority passenger and reserves a seat immediately. It then may move a matching regular passenger through the bus's FIFO `boardingLine`.

See [TerminalSimulation](https://github.com/PixelAlien0/Terminal-Simulation/blob/main/src/TerminalSimulation.java) methods `lane`, `update`, `updateTicketLane`, and `loadBus`.
