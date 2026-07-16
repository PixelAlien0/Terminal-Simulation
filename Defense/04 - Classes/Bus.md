
# Bus

Source: [TerminalSimulation](https://github.com/PixelAlien0/Terminal-Simulation/blob/main/src/TerminalSimulation.java) · states: [TerminalSimulation](https://github.com/PixelAlien0/Terminal-Simulation/blob/main/src/TerminalSimulation.java)

## Important fields

- Identity: `busId`, `destination`, `bayId`
- Lifecycle: `state`, countdown, departure buffer
- Capacity: fixed `Person[] seats`
- Queue: custom `PassengerQueue boardingLine`
- Position: `x`, `y`

## Important methods

| Method | Purpose |
|---|---|
| `getRandomEmptySeat` | Starts at a random index and circularly scans all seats. |
| `isFull` | Compares passenger count with capacity. |
| `getPassengerCount` | Counts non-null seat references. |
| `getSeatCoordinate` | Converts a seat index to a screen point. |
| `draw` | Renders the bus and loading countdown. |

> [!NOTE]
> A passenger can already occupy a seat-array reference while still in `WALKING_TO_BUS`. `SEATED_IN_BUS` is set only after the passenger reaches the seat coordinate.

See [Bus Journey](https://github.com/PixelAlien0/Terminal-Simulation/blob/main/Defense/03%20-%20Program%20Flow/Bus%20Journey.md).
