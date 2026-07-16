
# Passenger Journey

```mermaid
stateDiagram-v2
    [*] --> WALKING_TO_TICKET
    WALKING_TO_TICKET --> WAITING_FOR_TICKET: reaches queue target
    WAITING_FOR_TICKET --> BUYING_TICKET: front reaches booth
    BUYING_TICKET --> WALKING_TO_PLATFORM: timer reaches zero
    WALKING_TO_PLATFORM --> WAITING_ON_PLATFORM: reaches lounge slot
    WAITING_ON_PLATFORM --> WALKING_TO_BUS: priority / seat reserved
    WAITING_ON_PLATFORM --> MOVING_TO_BAY_LINE: regular selected
    MOVING_TO_BAY_LINE --> WAITING_IN_BAY_LINE: reaches line target
    WAITING_IN_BAY_LINE --> WALKING_TO_BUS: dequeued and seat reserved
    WALKING_TO_BUS --> SEATED_IN_BUS: reaches seat
    SEATED_IN_BUS --> [*]: bus departs and cleanup runs
```

## Method trace

| Stage | Method responsible |
|---|---|
| Create and ticket enqueue | `createPassenger` |
| Ticket service | `updateTicketLane` |
| Send to lounge | `sendToPlatform` |
| Match destination/type/state | `takePlatformPassenger` |
| Regular boarding order | `bus.boardingLine.enqueue/dequeue` |
| Seat assignment | `reserveSeat` |
| Arrival state changes | `movePassengers` |
| Final removal | `removeDepartedBuses` |

> [!IMPORTANT]
> **Three matching conditions**
> `takePlatformPassenger` requires matching priority type, state `WAITING_ON_PLATFORM`, and matching destination.

Open visually: [Passenger Journey](https://github.com/PixelAlien0/Terminal-Simulation/blob/main/Defense/03%20-%20Program%20Flow/Passenger%20Journey.md)
