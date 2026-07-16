
# Dequeue Trace

Source: [TerminalSimulation](https://github.com/PixelAlien0/Terminal-Simulation/blob/main/src/TerminalSimulation.java) → `dequeue()`

## Normal case

```text
Before: front -> [P1] -> [P2] -> [P3] <- rear

removedNode = front
front = front.next
removedNode.next = null
size--

After:  [P1 | null] returned
        front -> [P2] -> [P3] <- rear
```

Clearing `removedNode.next` detaches the removed node from the live chain.

## Last-node case

```text
Before: front/rear -> [P1 | null]
front = front.next  // null
rear = null
size = 0
```

> [!IMPORTANT]
> **Empty invariant**
> After the final node is removed, both `front` and `rear` must be `null`.

## Empty case

If `front == null`, the method returns `null` and changes nothing.

<details>
<summary><strong>Why O(1)?</strong></summary>

It only reads and changes `front` and possibly `rear`; it never traverses the nodes.

</details>

Related: [Queue Mental Model](https://github.com/PixelAlien0/Terminal-Simulation/blob/main/Defense/02%20-%20Queue/Queue%20Mental%20Model.md#invariants) · [Queue Test Evidence](https://github.com/PixelAlien0/Terminal-Simulation/blob/main/Defense/06%20-%20Evidence/Queue%20Test%20Evidence.md)
