
# Queue Complexity

| Method | Time | Why |
|---|---:|---|
| `enqueue` | O(1) | Uses the stored `rear`. |
| `dequeue` | O(1) | Removes directly through `front`. |
| `peek` | O(1) | Reads the front passenger. |
| `isEmpty` | O(1) | Checks whether `front` is `null`. |
| `size` | O(1) | Returns a maintained counter. |
| `remove` | O(n) | May traverse every node. |
| `contains` | O(n) | May traverse every node. |
| `frontNode` | O(1) | Returns the stored front reference. |

## Space

The queue uses **O(n)** total space because it creates one node per queued passenger. Each individual `enqueue` adds O(1) extra space.

<details>
<summary><strong>Would enqueue still be O(1) without `rear`?</strong></summary>

Not with this singly linked design. Finding the last node from `front` would require O(n) traversal.

</details>

<details>
<summary><strong>Why store `size` instead of counting nodes each time?</strong></summary>

Maintaining the counter makes `size()` O(1), at the cost of updating it correctly during every insertion and removal.

</details>
