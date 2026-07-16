
# SimulationEngine

Source: [TerminalSimulation](https://github.com/PixelAlien0/Terminal-Simulation/blob/main/src/TerminalSimulation.java)

## Owns

- Two custom ticket queues inside `TicketLane` objects
- Platform `ArrayList<Person>`
- Master active-passenger list
- Bus list
- Random generator and logger
- Spawn counters and pause flags

## Main responsibilities

| Area | Methods |
|---|---|
| Creation and lookup | `initialize`, `createPassenger`, `findPassenger` |
| CRUD cleanup | `updatePassengerDestination`, `removePassenger`, `removeBus` |
| Heartbeat | `update` |
| Ticketing | `updateTicketLane`, `positionTicketQueues` |
| Boarding | `loadBus`, `takePlatformPassenger`, `reserveSeat` |
| Bus lifecycle | `updateBuses`, `moveBus`, `removeDepartedBuses` |
| Reference repair | `removeFromQueues`, `detachFromBuses`, `returnBusPassengers` |
| Movement | `movePassengers`, position helpers |

> [!IMPORTANT]
> **Core answer**
> The engine owns the rules and changing data. It does not draw the interface.

<details>
<summary><strong>Why return unmodifiable list views?</strong></summary>

UI code can inspect current passengers, platform members, and buses without directly adding or deleting entries behind the engine's rules.

</details>
