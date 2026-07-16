
# Vocabulary

| Term | Project-specific meaning |
|---|---|
| **Queue** | A linear structure where normal insertion is at the rear and removal is at the front. |
| **FIFO** | First In, First Out within one queue. |
| **Node** | A `PassengerNode` containing a `Person` reference and a `next` reference. |
| **Front** | The next node inspected or removed. |
| **Rear** | The final node, used for constant-time insertion. |
| **Invariant** | A condition that must remain true, such as an empty queue having both `front` and `rear` equal to `null`. |
| **Traversal** | Following `next` references from node to node. |
| **State** | A named stage stored in `PassengerState` or `BusState`. |
| **Enum** | A Java type with a fixed set of allowed values. |
| **EDT** | Swing's Event Dispatch Thread, where the UI is created and timer actions run. |
| **Repaint** | A request for Swing to call `paintComponent` later. |
| **CRUD** | Create, Read, Update, Delete operations exposed by the UI and engine. |

> [!WARNING]
> **Important distinction**
> The master `ArrayList<Person>` represents all active passengers. A `PassengerQueue` represents a particular FIFO service order. They are not interchangeable.

Related: [Queue Mental Model](https://github.com/PixelAlien0/Terminal-Simulation/blob/main/Defense/02%20-%20Queue/Queue%20Mental%20Model.md) · [Application Architecture](https://github.com/PixelAlien0/Terminal-Simulation/blob/main/Defense/03%20-%20Program%20Flow/Application%20Architecture.md)
