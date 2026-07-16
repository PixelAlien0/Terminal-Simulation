---
tags: [terminal-simulation, active-recall, questions]
---

# Rapid Recall Questions

Try to answer aloud before opening each answer.

<details><summary>1. What is a queue?</summary>
A linear data structure that normally adds at the rear and removes from the front.
</details>

<details><summary>2. What does FIFO mean?</summary>
First In, First Out. The earliest enqueued passenger is normally the earliest dequeued passenger.
</details>

<details><summary>3. What is stored in PassengerNode?</summary>
One Person reference and a `next` reference to the following PassengerNode.
</details>

<details><summary>4. Why does PassengerQueue store front and rear?</summary>
`front` identifies the next passenger to serve. `rear` lets enqueue attach a new node in O(1) time.
</details>

<details><summary>5. What special case occurs when enqueueing into an empty queue?</summary>
Both front and rear must point to the new node.
</details>

<details><summary>6. What special case occurs when dequeue removes the last node?</summary>
After front becomes null, rear must also become null.
</details>

<details><summary>7. Why is remove usually O(n)?</summary>
The method may need to follow nodes from the front until it finds the requested passenger.
</details>

<details><summary>8. Why use a custom queue?</summary>
The project was assigned queues, and the custom implementation visibly demonstrates nodes, links, front, rear, FIFO, and link repair.
</details>

<details><summary>9. Why also keep an ArrayList of passengers?</summary>
It represents all active passengers for drawing, searching, listing, and CRUD. The queue represents service order.
</details>

<details><summary>10. What is an enum?</summary>
A Java type containing a fixed set of named values. It prevents invalid or misspelled state values.
</details>

<details><summary>11. What does SimulationEngine do?</summary>
It owns passengers, buses, queues, timing, ticket service, boarding rules, movement, and cleanup.
</details>

<details><summary>12. What does TerminalPanel do?</summary>
It reads the engine's current state and draws the terminal, buses, passengers, boards, and decorations.
</details>

<details><summary>13. What does TerminalSimulation do?</summary>
It creates the JFrame, controls, dialogs, event log, timer, engine, and drawing panel.
</details>

<details><summary>14. Why use SwingUtilities.invokeLater?</summary>
Swing expects interface creation and updates on its Event Dispatch Thread.
</details>

<details><summary>15. What is repaint?</summary>
It requests Swing to draw the panel again. Swing later calls paintComponent using current engine data.
</details>

<details><summary>16. How does priority service preserve FIFO?</summary>
Regular and priority passengers have separate FIFO queues. The engine may choose priority first, but order within each queue stays FIFO.
</details>

<details><summary>17. How is a passenger matched to a bus?</summary>
The engine considers destination, waiting/boarding state, priority rules, and available bus capacity.
</details>

<details><summary>18. What happens when a bus is deleted?</summary>
Its passengers are detached and returned to the platform, then the bus is removed and its bay becomes available.
</details>

<details><summary>19. Why are constants placed in SimulationConfig?</summary>
It avoids scattered magic numbers and gives timing, capacity, and layout values one clear location.
</details>

<details><summary>20. What tests prove the DSA works?</summary>
PassengerQueueTest checks node links, FIFO order, and removal repairs. SimulationEngineTest checks higher-level rules and cleanup.
</details>
