
# Remove and Link Repair

`remove(Person passenger)` supports cleanup when CRUD or bus operations remove a specific passenger who may not be at the front.

## Traversal variables

- `current` is the node being checked.
- `previous` is the node immediately before `current`.
- The code compares passenger references with `==` because it is locating the exact `Person` object already stored in the simulation.

## Middle removal

```text
Before: [P1] -> [P2] -> [P3]
                  ^ current
          ^ previous

previous.next = current.next

After:  [P1] ----------> [P3]
        [P2].next = null
```

## Edge cases

| Removed node | Required repair |
|---|---|
| Front | `front = current.next` |
| Middle | `previous.next = current.next` |
| Rear | `rear = previous` |
| Only node | Both front and rear become `null` through the front and rear cases |

Then the method clears `current.next`, decrements `size`, and returns `true`. If traversal finishes without a match, it returns `false`.

> [!IMPORTANT]
> **Complexity**
> O(n), because the target might be the last node or absent.

Evidence: [PassengerQueueTest](https://github.com/PixelAlien0/Terminal-Simulation/blob/main/test/PassengerQueueTest.java) · [Queue Test Evidence](https://github.com/PixelAlien0/Terminal-Simulation/blob/main/Defense/06%20-%20Evidence/Queue%20Test%20Evidence.md)
