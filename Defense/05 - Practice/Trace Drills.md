
# Trace Drills

## Drill A — Create a regular passenger

Without looking, name the collections, state, target, and methods involved from button click to ticket service.

<details>
<summary><strong>Answer</strong></summary>

A UI action calls `createRandomPassenger(false)` or the CRUD dialog calls `createPassengerWithLog(false, destination)`. Both reach `createPassenger`, which constructs a `Person` in `WALKING_TO_TICKET`, enqueues it in `regularLane.queue`, adds it to the master `passengers` list, and calls `positionTicketQueues`. `movePassengers` changes it to `WAITING_FOR_TICKET` after it reaches its target. `updateTicketLane` serves only the front node.

</details>

## Drill B — Remove the middle queue node

Draw P1 → P2 → P3, then remove P2. Which references and values change?

<details>
<summary><strong>Answer</strong></summary>

Traversal reaches P2 with `previous` at P1. The code assigns `previous.next = current.next`, so P1 links to P3; clears P2's `next`; decrements `size`; and returns `true`. `front` and `rear` do not change.

</details>

## Drill C — Ticket service completes

Trace the exact calls after the front passenger's `ticketTimerMs` reaches zero.

<details>
<summary><strong>Answer</strong></summary>

`updateTicketLane` calls `queue.dequeue`, `sendToPlatform(passenger)`, `positionTicketQueues`, and sets the lane transaction delay. `sendToPlatform` removes stale queue references, adds the passenger to `platform` if absent, clears `assignedBus`, sets `WALKING_TO_PLATFORM`, and calls `positionPlatform`.

</details>

## Drill D — Regular passenger boards

How is regular boarding different from priority boarding?

<details>
<summary><strong>Answer</strong></summary>

`loadBus` can reserve a matching priority passenger immediately. A matching regular passenger is first assigned to the bus, set to `MOVING_TO_BAY_LINE`, and enqueued in the bus's custom `boardingLine`. After the boarding interval, the front regular passenger is dequeued and passed to `reserveSeat`.

</details>

## Drill E — Delete a seated passenger

Why is removing only from the master list incorrect?

<details>
<summary><strong>Answer</strong></summary>

The bus seat would retain an unknown passenger reference. `removePassenger` therefore calls `removeFromQueues` and `detachFromBuses` before removing from `passengers`; `detachFromBuses` checks boarding lines and every seat and clears `assignedBus`.

</details>

## Drill F — Bus departs

When are its passengers removed from the simulation?

<details>
<summary><strong>Answer</strong></summary>

`moveBus` marks the bus `DEPARTED` after it moves beyond the window. `removeDepartedBuses` then clears each seated passenger's `assignedBus`, removes that passenger from the master list, clears the seat array, and removes the bus.

</details>
