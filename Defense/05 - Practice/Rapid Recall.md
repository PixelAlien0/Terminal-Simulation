
# Rapid Recall

Say each answer aloud before expanding it.

<details>
<summary><strong>1. What does FIFO mean in this project?</strong></summary>

First In, First Out within one `PassengerQueue`: the earliest enqueued passenger is normally the earliest dequeued passenger.

</details>

<details>
<summary><strong>2. What does a `PassengerNode` store?</strong></summary>

One `Person` reference and a `next` reference to the following node.

</details>

<details>
<summary><strong>3. Why store both `front` and `rear`?</strong></summary>

`front` gives direct access to the next removal; `rear` gives direct access to the insertion point. This makes normal dequeue and enqueue O(1).

</details>

<details>
<summary><strong>4. What is the empty enqueue edge case?</strong></summary>

Both `front` and `rear` must point to the new node.

</details>

<details>
<summary><strong>5. What is the final dequeue edge case?</strong></summary>

When advancing `front` makes it `null`, `rear` must also be set to `null`.

</details>

<details>
<summary><strong>6. Why is `remove` O(n)?</strong></summary>

It may follow `next` through every node before finding the exact passenger or proving it is absent.

</details>

<details>
<summary><strong>7. Why use `==` in `remove` and `contains`?</strong></summary>

The engine wants the exact `Person` object reference already stored, not merely another object with similar field values.

</details>

<details>
<summary><strong>8. Where is the custom queue used?</strong></summary>

In each regular/priority `TicketLane` and in every bus's regular-passenger `boardingLine`.

</details>

<details>
<summary><strong>9. Does priority use `java.util.PriorityQueue`?</strong></summary>

No. There are separate custom FIFO ticket queues, and bus loading applies an explicit priority rule.

</details>

<details>
<summary><strong>10. Why also keep `ArrayList<Person>`?</strong></summary>

It stores all active passengers for lookup, drawing, listing, and CRUD; queues represent particular service orders.

</details>

<details>
<summary><strong>11. What does `SimulationEngine` own?</strong></summary>

Rules and changing data: queues, platform, passengers, buses, timing, state changes, boarding, and cleanup.

</details>

<details>
<summary><strong>12. What does `TerminalPanel` own?</strong></summary>

Rendering of the current state. It does not choose who is served or boards.

</details>

<details>
<summary><strong>13. What does `TerminalSimulation` own?</strong></summary>

The JFrame, controls, dialogs, log, Swing timer, and connection between UI, engine, and panel.

</details>

<details>
<summary><strong>14. What happens during one engine update?</strong></summary>

Spawning, bus updates, priority and regular ticket-lane updates, then passenger movement.

</details>

<details>
<summary><strong>15. What makes a platform passenger eligible for a bus?</strong></summary>

Matching priority type requested by the caller, state `WAITING_ON_PLATFORM`, and matching destination.

</details>

<details>
<summary><strong>16. When does the bus countdown begin?</strong></summary>

After at least one seat contains a passenger whose state has reached `SEATED_IN_BUS`.

</details>

<details>
<summary><strong>17. What does `repaint` do?</strong></summary>

It requests a future Swing repaint; Swing later calls `paintComponent`.

</details>

<details>
<summary><strong>18. What happens when a passenger is deleted?</strong></summary>

The engine removes the passenger from ticket/platform queues, boarding lines or seats, then from the master list, and repositions affected collections.

</details>

<details>
<summary><strong>19. What happens when a bus is deleted?</strong></summary>

Boarding and seated passengers are collected once, detached, and sent back to the platform; the bus is removed.

</details>

<details>
<summary><strong>20. What proves the queue works?</strong></summary>

`PassengerQueueTest` checks links, FIFO, middle/front/rear removal, empty reuse, and null rejection.

</details>

Record missed answers in [Mistake Log](https://github.com/PixelAlien0/Terminal-Simulation/blob/main/Defense/05%20-%20Practice/Mistake%20Log.md).
