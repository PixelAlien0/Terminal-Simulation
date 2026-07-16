
# PassengerNode

## Actual structure

```java
final class PassengerNode {
    final Person passenger;
    PassengerNode next;

    PassengerNode(Person passenger) {
        this.passenger = passenger;
    }
}
```

Source: [TerminalSimulation](https://github.com/PixelAlien0/Terminal-Simulation/blob/main/src/TerminalSimulation.java)

| Part | Meaning |
|---|---|
| `passenger` | The stored `Person`; `final` means this field is not reassigned after construction. |
| `next` | The link to the next node; it changes when nodes are attached or removed. |
| constructor | Stores the passenger. Java leaves `next` as `null` initially. |

<details>
<summary><strong>What makes this a linked node rather than just a wrapper?</strong></summary>

The `next` field connects one `PassengerNode` to another, forming a chain.

</details>

<details>
<summary><strong>Why is `next` not `final`?</strong></summary>

Queue operations must change links during `enqueue` and `remove`.

</details>

Next: [PassengerQueue](https://github.com/PixelAlien0/Terminal-Simulation/blob/main/Defense/04%20-%20Classes/PassengerQueue.md)
