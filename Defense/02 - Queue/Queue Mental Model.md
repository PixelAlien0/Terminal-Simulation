
# Queue Mental Model

```text
front                                      rear
  |                                          |
  v                                          v
[P1 | next] -> [P2 | next] -> [P3 | null]
```

Each box is a [PassengerNode](https://github.com/PixelAlien0/Terminal-Simulation/blob/main/Defense/02%20-%20Queue/PassengerNode.md). The queue remembers the first and last node; each node remembers only the following node.

## Invariants

> [!IMPORTANT]
> **Conditions that must remain true**
> 1. `size` equals the number of reachable nodes.
> 2. If the queue is empty, `front == null` and `rear == null`.
> 3. If the queue is not empty, `rear.next == null`.
> 4. Following `next` from `front` eventually reaches `rear`.

## FIFO

`enqueue` attaches at `rear`; `dequeue` removes from `front`. Therefore P1 leaves before P2 and P3.

> [!CAUTION]
> **Common mistake**
> FIFO describes order **within one queue**. The engine owns two ticket queues and may process both independently. This is not a Java `PriorityQueue`.

## Operations

| Operation | Main pointer action | Typical time |
|---|---|---:|
| `enqueue` | Attach after `rear` | O(1) |
| `dequeue` | Advance `front` | O(1) |
| `peek` | Read `front` | O(1) |
| `remove` | Traverse and reconnect | O(n) |
| `contains` | Traverse | O(n) |
| `size` | Return stored integer | O(1) |

Open visually: [Queue Mental Model](https://github.com/PixelAlien0/Terminal-Simulation/blob/main/Defense/02%20-%20Queue/Queue%20Mental%20Model.md)
