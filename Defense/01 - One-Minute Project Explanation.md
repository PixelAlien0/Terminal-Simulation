---
tags: [terminal-simulation, defense, summary]
---

# One-Minute Project Explanation

## Answer you should understand—not recite mechanically

Our project is a Java Swing bus-terminal simulation made for Data Structures and Algorithms. Our assigned structure is the queue, so the central feature is a custom node-based FIFO passenger queue.

`PassengerNode` stores one passenger and a link to the next node. `PassengerQueue` manages the `front`, `rear`, and `size`, and implements operations such as `enqueue`, `dequeue`, `peek`, and `remove`.

`SimulationEngine` owns the rules and changing data: passengers, buses, queues, ticket processing, boarding, and state changes. `TerminalPanel` draws the terminal, while `TerminalSimulation` creates the Swing window, controls, event log, and timer.

During every timer update, the engine may create passengers or buses, process ticket queues, move passengers, board buses, and update countdowns. The panel is then repainted so the screen shows the new state.

The project demonstrates FIFO ordering, linked nodes, state management, searching, removal, and a graphical simulation of how queues work in a real situation.

## Short version

> It is a Java Swing terminal simulation centered on our assigned data structure: a custom node-based FIFO queue. The queue controls passenger order, the engine controls the rules, and the panel displays the changing simulation.

## If the teacher asks, “Where is the DSA?”

Point to:

- `src/PassengerNode.java` — the linked node.
- `src/PassengerQueue.java` — the custom FIFO queue.
- `src/SimulationEngine.java` — where queues are used for ticket and boarding order.
- `test/PassengerQueueTest.java` — evidence that FIFO and link repair are tested.
