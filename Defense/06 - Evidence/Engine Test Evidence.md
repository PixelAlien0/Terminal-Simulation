
# Engine Test Evidence

Source: [SimulationEngineTest](https://github.com/PixelAlien0/Terminal-Simulation/blob/main/test/SimulationEngineTest.java)

| Test area | What is checked |
|---|---|
| Passenger deletion | Master list, bus seat, and engine invariants are repaired. |
| Bus deletion | Passenger loses the bus reference and returns to the platform. |
| Destination update | Assigned passenger is detached and requeued for the new destination. |
| ID lookup | Surrounding spaces and case differences are accepted. |
| Long simulation | Known references and bus capacity remain valid over 7,500 ticks. |
| Headless rendering | `TerminalPanel` can paint into a `BufferedImage` without opening a window. |

The tests inject `new Random(7)` to make behavior repeatable.

<details>
<summary><strong>What invariant is checked for passengers on a bus?</strong></summary>

Every passenger in a boarding line or seat must exist in the engine's master passenger list and must reference that same bus through `assignedBus`.

</details>
