
# Project in 60 Seconds

> [!IMPORTANT]
> **Say this in your own words**
> Our project is a Java Swing bus-terminal simulation built to demonstrate our assigned data structure: a custom node-based FIFO queue. `PassengerNode` stores a passenger and a link to the next node. `PassengerQueue` maintains `front`, `rear`, and `size`, and supports `enqueue`, `dequeue`, `peek`, `remove`, and other helpers. `SimulationEngine` applies the terminal rules, `TerminalPanel` draws the current state, and `TerminalSimulation` connects the engine to the Swing window and timer. During an update, passengers and buses can spawn, ticket queues are processed, passengers move and board, and departed buses are cleaned up. The result demonstrates FIFO ordering, linked nodes, state transitions, searching, removal, and a graphical real-world use of queues.

## Fifteen-second version

> The application is a Java Swing terminal simulation centered on a custom linked FIFO passenger queue. The queue controls service order, the engine controls the rules, and the panel displays the changing state.

## Where is the DSA?

| Evidence | What it proves |
|---|---|
| [TerminalSimulation](https://github.com/PixelAlien0/Terminal-Simulation/blob/main/src/TerminalSimulation.java) | Each node stores a `Person` and a `next` link. |
| [TerminalSimulation](https://github.com/PixelAlien0/Terminal-Simulation/blob/main/src/TerminalSimulation.java) | The custom queue manages `front`, `rear`, `size`, and link repair. |
| [TerminalSimulation](https://github.com/PixelAlien0/Terminal-Simulation/blob/main/src/TerminalSimulation.java) | Ticket lanes and bus boarding lines use the queue. |
| [PassengerQueueTest](https://github.com/PixelAlien0/Terminal-Simulation/blob/main/test/PassengerQueueTest.java) | FIFO order, links, edge cases, and removal are tested. |

<details>
<summary><strong>What should I emphasize if the teacher interrupts me?</strong></summary>

Emphasize that the queue is custom and node-based, then point to `PassengerQueue.enqueue`, `dequeue`, and `remove`.

</details>

## Continue

[Follow the study route](https://github.com/PixelAlien0/Terminal-Simulation/blob/main/Defense/01%20-%20Start%20Here/Defense%20Study%20Route.md) · [Learn the queue](https://github.com/PixelAlien0/Terminal-Simulation/blob/main/Defense/02%20-%20Queue/Queue%20Mental%20Model.md) · [Open the architecture canvas](https://github.com/PixelAlien0/Terminal-Simulation/blob/main/Defense/03%20-%20Program%20Flow/Application%20Architecture.md)
