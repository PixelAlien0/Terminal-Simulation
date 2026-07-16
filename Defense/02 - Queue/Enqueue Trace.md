
# Enqueue Trace

Source: [TerminalSimulation](https://github.com/PixelAlien0/Terminal-Simulation/blob/main/src/TerminalSimulation.java) → `enqueue(Person passenger)`

## Normal case: add P3

```text
Before: front -> [P1] -> [P2] <- rear

1. newNode = [P3 | null]
2. rear.next = newNode
3. rear = newNode
4. size++

After:  front -> [P1] -> [P2] -> [P3] <- rear
```

## Empty-queue case

```text
Before: front = null, rear = null, size = 0
Create: [P1 | null]
After:  front ─┐
               ├──> [P1 | null]
        rear  ──┘
        size = 1
```

The code checks `rear == null`. It sets **both** `front` and `rear` to the new node.

> [!IMPORTANT]
> **Why O(1)?**
> The queue already stores `rear`, so it does not traverse the chain to find the last node.

<details>
<summary><strong>What would break if only `rear` were set for the first passenger?</strong></summary>

`front` would remain `null`, so `peek` and `dequeue` would act as if the queue were empty.

</details>

Related: [Dequeue Trace](https://github.com/PixelAlien0/Terminal-Simulation/blob/main/Defense/02%20-%20Queue/Dequeue%20Trace.md) · [PassengerQueueTest](https://github.com/PixelAlien0/Terminal-Simulation/blob/main/test/PassengerQueueTest.java)
