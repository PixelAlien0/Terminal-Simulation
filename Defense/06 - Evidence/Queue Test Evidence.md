
# Queue Test Evidence

Source: [PassengerQueueTest](https://github.com/PixelAlien0/Terminal-Simulation/blob/main/test/PassengerQueueTest.java)

| Test | Evidence produced |
|---|---|
| `queueUsesLinkedNodesAndFifoOrder` | P1 → P2 → P3 links, rear terminates at null, size is 3, peek is P1, dequeue order is P1/P2/P3. |
| `removalRepairsFrontMiddleAndRearLinks` | Specific removal works for middle, front, and rear without losing the remaining node. |
| `emptyQueueCanBeReused` | Final dequeue produces a valid empty queue that can accept another passenger. |
| `nullPassengerIsRejected` | `enqueue(null)` throws `IllegalArgumentException`. |

> [!IMPORTANT]
> **Defense phrasing**
> The test does not merely check returned values; it follows `frontNode().next` to prove the implementation really uses a linked node chain.

Run commands are in [README](https://github.com/PixelAlien0/Terminal-Simulation/blob/main/README.md).
