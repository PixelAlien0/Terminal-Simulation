# Terminal Simulation Complete Code Walkthrough

This document explains every Java file and every meaningful section of code in
the project. It is written for group members who are still learning Java and
need to explain how the current program works during the defense.

For a version that shows each source snippet before explaining it, see
[CODE_BLOCK_WALKTHROUGH.md](CODE_BLOCK_WALKTHROUGH.md).

The project has one main assigned data structure: a **Queue implemented with
linked passenger nodes**. Other structures only support the simulation:

- `PassengerQueue` is the assigned FIFO queue.
- `PassengerNode` provides the links used by the queue.
- `ArrayList` stores active objects that need searching or filtered selection.
- `Person[]` represents the fixed 20 seats of a bus.
- Enums represent states; they are not data structures.

## 1. Project files and responsibilities

| File | Responsibility |
|---|---|
| `src/PassengerNode.java` | One linked node containing a passenger and the next-node link |
| `src/PassengerQueue.java` | Custom node-based FIFO queue operations |
| `src/PassengerState.java` | Valid passenger workflow states |
| `src/BusState.java` | Valid bus lifecycle states |
| `src/Person.java` | Passenger data, movement, and pixel-art drawing |
| `src/Bus.java` | Bus data, fixed seats, FIFO boarding queue, and drawing |
| `src/SimulationConfig.java` | Named timing, capacity, window, and layout constants |
| `src/SimulationEngine.java` | All terminal rules and live simulation collections |
| `src/TerminalPanel.java` | Pixel-art view of the current engine state |
| `src/TerminalSimulation.java` | Swing window, controls, dialogs, timer, and log |
| `test/PassengerQueueTest.java` | Direct tests of node links and queue behavior |
| `test/SimulationEngineTest.java` | Integration, cleanup, long-run, and rendering tests |

The application follows this direction:

```text
Buttons and Swing Timer
          |
          v
TerminalSimulation
          |
          v
SimulationEngine ----> Person / Bus / PassengerQueue
          |
          v
TerminalPanel reads the latest state and draws it
```

The panel does not decide who receives a ticket or boards a bus. Those decisions
belong to the engine.

## 2. The custom queue and node

### `PassengerNode.java`

This is the physical link used to build the queue:

```java
final class PassengerNode {
    final Person passenger;
    PassengerNode next;

    PassengerNode(Person passenger) {
        this.passenger = passenger;
    }
}
```

#### `final class PassengerNode`

`final` prevents another class from extending `PassengerNode`. The project only
needs this one simple node design.

#### `final Person passenger`

This is the data stored by the node. It points to one `Person` object. `final`
means the node will not be changed to hold a different passenger later.

#### `PassengerNode next`

This points to the following node. A value of `null` means there is no following
node, so the current node is at the rear of the queue.

#### Constructor

The constructor receives a passenger and stores it. Java automatically leaves
`next` as `null` until the queue links the node.

The node chain looks like this:

```text
front
  |
  v
[P1 | next] -> [P2 | next] -> [P3 | null]
                                      ^
                                      |
                                     rear
```

This is a real DSA node because it stores data and a link. It is different from
a state enum, which only identifies what an object is currently doing.

### `PassengerQueue.java`

This class controls the nodes. Other classes request queue operations instead of
manually reconnecting nodes.

#### Queue fields

```java
private PassengerNode front;
private PassengerNode rear;
private int size;
```

- `front` points to the passenger who must leave next.
- `rear` points to the most recently added passenger.
- `size` stores the number of nodes so `size()` can answer in O(1) time.
- An empty queue has `front == null`, `rear == null`, and `size == 0`.

The fields are private so only `PassengerQueue` can replace its front/rear
links. `frontNode()` later exposes the first node for read-only layout traversal;
engine code must not change the links.

#### `enqueue(Person passenger)`

Purpose: add one passenger at the rear.

1. Reject `null`, because a node without a passenger would make no sense.
2. Create `new PassengerNode(passenger)`.
3. If `rear == null`, the queue was empty, so both `front` and `rear` become the
   new node.
4. Otherwise, connect the old rear with `rear.next = newNode`.
5. Move `rear` to the new node.
6. Increment `size`.

Example:

```text
Before enqueue(P3): front -> P1 -> P2 <- rear
After enqueue(P3):  front -> P1 -> P2 -> P3 <- rear
```

There is no traversal, so enqueue is O(1).

#### `dequeue()`

Purpose: remove and return the front passenger.

1. If `front == null`, return `null` because the queue is empty.
2. Save the old front node as `removedNode`.
3. Move `front` to `front.next`.
4. Disconnect the removed node with `removedNode.next = null`.
5. Decrement `size`.
6. If the new front is null, the last node was removed, so set `rear = null` too.
7. Return the removed node's passenger.

Example:

```text
Before: front -> P1 -> P2 -> P3 <- rear
After:  front -> P2 -> P3 <- rear       returned P1
```

Dequeue is O(1) and enforces FIFO: first in, first out.

#### `peek()`

Returns the front passenger without removing anything. It returns null when the
queue is empty. Ticket processing uses this to inspect the current client.

#### `remove(Person passenger)`

Normal queue service removes only the front through `dequeue`. This extra method
exists because CRUD allows the user to delete a specific passenger who might be
in the middle of a queue.

The method keeps `previous` and `current` node references while traversing:

- Removing the front changes `front`.
- Removing a middle node changes `previous.next`.
- Removing the rear changes `rear` to `previous`.
- Every successful removal disconnects the old node and decrements `size`.
- If the passenger is not found, it returns false.

It compares object references with `==` because the engine passes the exact
`Person` object stored in the node. This special CRUD removal is O(n).

#### `contains(Person passenger)`

Traverses from front to rear and returns true if a node stores that exact
passenger. Tests use it to ensure cleanup removed bus references. It is O(n).

#### `isEmpty()`

Returns `front == null`. This is O(1).

#### `size()`

Returns the stored node count. This is O(1) because it does not traverse.

#### `frontNode()`

Returns the front node so the engine and tests can walk the chain:

```java
PassengerNode current = queue.frontNode();
while (current != null) {
    Person passenger = current.passenger;
    current = current.next;
}
```

This traversal is needed to give every waiting passenger a different screen
coordinate. It does not remove or reorder the queue.

## 3. State enum files

### `PassengerState.java`

This enum lists the only allowed passenger states:

| State | Meaning |
|---|---|
| `WALKING_TO_TICKET` | Moving from the entrance to a ticket-line position |
| `WAITING_FOR_TICKET` | At the line position and waiting for service |
| `BUYING_TICKET` | Front passenger is completing the ticket timer |
| `WALKING_TO_PLATFORM` | Ticket complete; moving to a platform spot |
| `WAITING_ON_PLATFORM` | Available for a matching bus to select |
| `MOVING_TO_BAY_LINE` | Regular passenger walking to a bus queue |
| `WAITING_IN_BAY_LINE` | Regular passenger waiting in FIFO order |
| `WALKING_TO_BUS` | A seat is reserved and the passenger is walking to it |
| `SEATED_IN_BUS` | Passenger reached the seat coordinate |

An enum prevents invalid text and spelling mistakes. It is not a node and does
not replace the queue.

### `BusState.java`

| State | Meaning |
|---|---|
| `ARRIVING` | Bus is moving from the right toward a bay |
| `LOADING` | Bus selects and boards matching passengers |
| `WAITING_FOR_DEPARTURE` | Doors are closed during a short buffer |
| `DEPARTING` | Bus is moving right out of the terminal |
| `DEPARTED` | Bus is off-screen and ready for cleanup |

Because `PassengerState` and `BusState` are different enum types, Java prevents
a passenger from accidentally receiving a bus state.

## 4. `Person.java`

`Person` is both the passenger model and the passenger sprite.

### Identity and terminal fields

| Field | Meaning |
|---|---|
| `id` | Unique label such as P23 |
| `destination` | Davao or Tagum; can change through Update Passenger |
| `isPriority` | Selects ticket lane and boarding rule |
| `arrivalOrder` | Number drawn on the passenger |
| `state` | Current `PassengerState` |
| `assignedBus` | Bus reference during a boarding/seat assignment, otherwise null |
| `ticketTimerMs` | Remaining ticket transaction time |

ID, priority type, and arrival order are final because they do not change after
creation. Destination changes through CRUD, so it is not final.

### Position and animation fields

- `x`, `y`: current screen coordinate.
- `targetX`, `targetY`: coordinate selected by the engine.
- `animationFrame`: current walking-art frame from 0 to 3.
- `animationSlow`: slows the visual animation relative to movement updates.

Targets and animation counters are private because only `Person` should directly
control them.

### Constructor

The constructor copies the supplied identity, destination, starting coordinate,
type, and order. It also sets the initial target equal to the starting position.
The field declaration already initializes state as `WALKING_TO_TICKET`.

### `setTarget(int targetX, int targetY)`

Stores the next destination coordinate. It does not move immediately. Queue and
platform positioning helpers call this method.

### `stepTowardTarget()`

This method moves x and y by at most `PASSENGER_SPEED` pixels per engine step.

- `Math.min` is used while increasing a coordinate.
- `Math.max` is used while decreasing a coordinate.
- These prevent moving past the target.
- If at least one coordinate changed, the animation counter advances.
- Every third moving call advances `animationFrame` and wraps it with `% 4`.
- The return value is true only when both coordinates equal the target.

The method controls movement only. It does not decide which state comes next;
`SimulationEngine.movePassengers` handles that rule.

### `draw(Graphics2D graphics)`

The drawing method is organized into visual layers:

1. Calculate a one-pixel body bob from `animationFrame`.
2. Select hair and shirt colors based on priority type.
3. Draw a translucent oval shadow.
4. Draw alternating legs using `legOffset`.
5. Draw the shirt/body and black outline.
6. Draw the arrival number on the shirt.
7. Draw skin, head outline, and hair rectangles.
8. Draw a star for priority passengers.
9. Draw the first destination letter.
10. If state is `BUYING_TICKET`, draw a progress bar based on
    `ticketTimerMs / TICKET_SERVICE_MS`.

These drawing statements create pixel art. They do not change queue order or
simulation rules.

## 5. `Bus.java`

`Bus` stores one bus's route, location, lifecycle, seats, and boarding queue.

### Fixed identity fields

| Field | Meaning |
|---|---|
| `busId` | Display ID such as Davao Exp A (B1) |
| `destination` | Route accepted by the bus |
| `bayId` | Terminal bay 1-4 |
| `capacity` | Fixed from `SimulationConfig.BUS_CAPACITY` |
| `y` | Fixed vertical bay coordinate |

### Changing bus fields

- `x`: changes while arriving and departing.
- `state`: starts as `ARRIVING`.
- `countdownMs`: begins at 60,000 ms.
- `departureBufferMs`: begins at 4,800 ms.
- `countdownStarted`: becomes true after a passenger fully reaches a seat.
- `boardingElapsedMs`: tracks the 400 ms regular boarding interval.

### Passenger storage

```java
final Person[] seats = new Person[capacity];
final PassengerQueue boardingLine = new PassengerQueue();
```

The seat array is fixed because a bus has exactly 20 seat positions. A null
array entry is empty. `boardingLine` is the custom FIFO queue: regular
passengers enqueue at the rear and board by dequeuing from the front.

### Constructor

Copies the ID, destination, bay, and starting position. Other fields already
have their initial values at declaration.

### `getRandomEmptySeat(Random random)`

1. Choose a random starting array index.
2. Check at most 20 positions.
3. Wrap the index with `(start + offset) % capacity`.
4. Return the first null seat or -1 if every position is occupied.

It is O(20), effectively O(1) because capacity never grows.

### `isFull()`

Calls `getPassengerCount()` and compares it with capacity.

### `getPassengerCount()`

Scans the seat array and counts non-null references. A passenger counts as soon
as the seat is reserved, even while walking to the coordinate.

### `getSeatCoordinate(int seatIndex)`

Converts a seat index into a column and row, then adds the bus x/y offset. When
the bus moves, the same calculation gives the seat's new screen coordinate.

### `draw(Graphics2D graphics)`

1. Return without drawing if state is `DEPARTED`.
2. Select green Tagum colors or blue Davao colors.
3. Draw the shadow and three wheels.
4. Draw the rounded bus body and outline.
5. Draw lower trim and route stripe.
6. Draw four windows.
7. Draw the door and door window.
8. Draw bus ID and `[occupied/capacity]`.
9. During loading, draw the remaining countdown after it has started.

The private bus width/height constants are drawing measurements, not terminal
capacity.

## 6. `SimulationConfig.java`

This class contains only named constants. Its constructor is private so nobody
creates a useless `SimulationConfig` object.

### Frame and window constants

- `FRAME_DELAY_MS = 16`: one fixed simulation step and approximate timer delay.
- `MAX_FRAME_CATCH_UP_MS = 250`: maximum elapsed time added after a late frame.
- `WINDOW_WIDTH = 1380`, `WINDOW_HEIGHT = 720`: initial frame size.
- `LOG_MAX_LINES = 500`: prevents unlimited log growth.
- `MAX_PASSENGERS = 160`: cap for automatic passenger creation.

### Simulation constants

- `PASSENGER_SPEED = 4`: pixels moved per fixed step.
- `BUS_CAPACITY = 20`: length of every seat array.
- `BUS_ARRIVAL_INTERVAL_MS = 19_200`: automatic bus interval.
- `PASSENGER_SPAWN_INTERVAL_MS = 1_120`: automatic passenger interval.
- `TICKET_SERVICE_MS = 320`: one ticket purchase.
- `TICKET_TRANSACTION_DELAY_MS = 320`: pause before the next client.
- `BOARDING_INTERVAL_MS = 400`: time between regular dequeues.
- `BUS_LOADING_COUNTDOWN_MS = 60_000`: loading countdown after seating begins.
- `BUS_DEPARTURE_BUFFER_MS = 4_800`: door-closing pause.

### Queue layout constants

- `TICKET_BOOTH_X`: x coordinate used by the front ticket client.
- `PRIORITY_QUEUE_Y` and `REGULAR_QUEUE_Y`: separate line rows.
- `TICKET_QUEUE_SPACING`: horizontal distance between visible queue positions.

Named constants make the rules readable and prevent repeated unexplained
numbers.

## 7. `SimulationEngine.java`

This is the rules layer. It owns all live simulation data and is the largest
logic file.

### Imports

- `Point` carries seat coordinates.
- `ArrayList` stores platform passengers, master passengers, buses, and helper
  results.
- `Arrays.fill` clears seat arrays.
- `Collections.unmodifiableList` gives read-only collection views to the UI.
- `Iterator` safely removes a matching platform passenger during traversal.
- `Random` controls route/type/bay/seat variation.
- `Consumer<String>` lets the engine send text to any logger supplied by the
  window or tests.

### Nested `TicketLane`

Each logical ticket lane contains:

- one custom `PassengerQueue`;
- the lane y coordinate;
- `rowDirection` for wrapping long visual queues upward or downward;
- its own post-transaction `delayMs`.

The priority and regular lanes use the same algorithm without duplicating the
entire ticket method.

### Engine collections

```text
priorityLane.queue  = node-based FIFO ticket queue
regularLane.queue   = node-based FIFO ticket queue
platform            = filtered waiting list, not claimed as strict FIFO
passengers          = master list of all active passengers
buses               = active buses
bus.boardingLine    = node-based FIFO queue owned by each bus
bus.seats           = fixed array owned by each bus
```

The platform is a list because a bus searches for the first passenger matching
three conditions: destination, priority type, and `WAITING_ON_PLATFORM`. This
can skip unmatched people, so calling the whole platform a strict FIFO queue
would be inaccurate.

### Counters and flags

- `passengerCounter` creates unique P numbers.
- `busCounter` creates unique B numbers.
- `passengerSpawnMs` and `busSpawnMs` accumulate elapsed time.
- `busesStopped` pauses all bus rules.
- `ticketBoothOpen` pauses ticket work and automatic passenger generation.

### Constructors

The normal constructor creates a new random generator. The second constructor
accepts a `Random` and logger, allowing tests to use `new Random(7)` for
repeatability. A null logger becomes an empty lambda so the engine can always
call `log` safely.

### `initialize()`

Logs startup and creates 22 random passengers. Each random float has a 20%
chance of producing a priority passenger.

### Passenger creation methods

#### `createRandomPassenger(boolean priority)`

Selects Davao or Tagum randomly and delegates to `createPassenger`.

#### `createPassenger(boolean priority, String destination)`

1. Increment the ID counter.
2. Normalize the route.
3. Construct a `Person` at the entrance.
4. Select the priority or regular `TicketLane`.
5. Call `queue.enqueue(passenger)` at the rear.
6. Add the same object to the master passenger list.
7. Recalculate visible queue targets.

The master list owns active membership; the queue owns ticket-service order.

#### `createPassengerWithLog(...)`

Uses the normal creation method and then writes a CRUD event.

### Passenger lookup and CRUD

#### `findPassenger(String id)`

Trims the requested ID and scans the master list. Comparison ignores case. It
returns the matching object or null.

#### `updatePassengerDestination(...)`

1. Find the passenger.
2. Normalize and store the new route.
3. Call `detachFromBuses`.
4. If an assignment was found, send the passenger back to the platform.
5. Log the update.

A passenger still in a ticket queue keeps their FIFO position. An assigned
passenger is removed from the old bus so they cannot travel on the wrong route.

#### `removePassenger(String id)`

1. Find the passenger.
2. Remove their node from both ticket queues and remove them from the platform.
3. Remove them from every bus boarding queue and seat.
4. Remove them from the master list.
5. Recalculate affected positions.
6. Log deletion.

Normal service remains FIFO. The queue's O(n) `remove` is only used for this
explicit CRUD operation.

### Bus creation and deletion

#### `addBus(String destination)`

Builds a free-bay list, chooses one randomly, delegates to `addBusAtBay`, logs,
and returns the new bus. It returns null if all four bays are occupied.

#### `addBusAtBay(String destination, int bay)`

Validates bay 1-4 and vacancy, normalizes the route, creates the line name and
A/B label, constructs the bus at x = 1400, and adds it to `buses`.

This method is also useful for tests because they can request a specific bay.

#### `removeBus(String id)`

Finds and removes the active bus, calls `returnBusPassengers`, and logs. Returning
passengers are detached and sent to the platform instead of becoming ghosts.

### Main update and controls

#### `update(int elapsedMs)`

Every fixed step runs in this exact order:

1. `updateSpawning`
2. `updateBuses`
3. `updateTicketLane` for priority
4. `updateTicketLane` for regular
5. `movePassengers`

#### `toggleBusesStopped()`

Flips the boolean, logs the new condition, and returns it so the button can
change its label.

#### `toggleTicketBooth()`

Flips booth state, logs, and refreshes targets when reopening.

### Read-only engine methods

- `passengers()`: unmodifiable view of active passengers.
- `platformPassengers()`: unmodifiable view used by tests.
- `buses()`: unmodifiable active-bus view.
- `ticketCount(boolean)`: custom queue size.
- `ticketClient(boolean)`: custom queue peek.
- `platformCount()`: waiting-list count.
- `isTicketBoothOpen()`: used by ticket-booth drawing.

These methods allow reading without allowing the UI to directly add/remove
engine data.

### `lane(boolean priority)`

Returns `priorityLane` when true and `regularLane` when false.

### Automatic spawning

#### `updateSpawning(int elapsedMs)`

- Adds elapsed time to the passenger timer.
- At the interval, keeps the remainder with `%`.
- Creates an automatic passenger only if the booth is open and active passenger
  count is below 160.
- Advances the bus timer only when fewer than four buses exist and operations
  are not stopped.
- Spawns a bus when the bus interval is reached.

#### `spawnBus()`

Chooses a free bay. Automatic buses use Davao for bays 1-2 and Tagum for bays
3-4, then a log message is written.

#### `freeBays()` and `isBayFree(int bay)`

`freeBays` checks numbers 1 through 4. `isBayFree` scans active buses and rejects
a bay already used by a bus.

#### `findBus(String id)`

Uses the same trimmed, case-insensitive style as passenger lookup.

### Bus lifecycle

#### `updateBuses(int elapsedMs)`

Returns immediately when buses are stopped. Otherwise it switches on each bus
state:

- `ARRIVING`: subtract 8 from x; at 620, switch to `LOADING`.
- `LOADING`: call `loadBus`.
- `WAITING_FOR_DEPARTURE`: reduce the buffer; at zero, log and depart.
- `DEPARTING`: call `moveBus`.
- Other states need no action in the switch.

After the loop, it calls `removeDepartedBuses`.

#### `loadBus(Bus bus, int elapsedMs)`

This method applies boarding priority and queue behavior:

1. If a physical seat is free, take one matching priority passenger from the
   platform and reserve a seat immediately.
2. Calculate reserved capacity as seat references plus boarding-queue nodes.
3. If reserved capacity is below 20, take one matching regular passenger.
4. Assign the bus, change state to `MOVING_TO_BAY_LINE`, and enqueue the regular
   passenger at the boarding queue's rear.
5. Reposition the boarding queue by traversing its nodes.
6. When 400 ms has accumulated, dequeue the front regular passenger and reserve
   their seat. This is the second clear FIFO use in the project.
7. Start the 60-second countdown after at least one seat passenger reaches
   `SEATED_IN_BUS`.
8. When full or timed out, dequeue any remaining boarding passengers back to the
   platform and change to `WAITING_FOR_DEPARTURE`.

Priority passengers bypass the regular boarding queue, but they still need a
ticket, matching route, platform arrival, empty seat, and movement to the bus.

#### `takePlatformPassenger(String destination, boolean priority)`

Iterates over the platform list. It selects the first entry whose:

- `isPriority` matches;
- state is `WAITING_ON_PLATFORM`;
- destination equals the bus destination.

`Iterator.remove()` safely removes the current list entry, platform targets are
recalculated, and the passenger is returned. Null means no match.

#### `reserveSeat(Bus bus, Person passenger)`

Gets an empty index. If none exists, it sends the passenger back to the platform.
Otherwise it changes state to `WALKING_TO_BUS`, assigns the bus, puts the
reference in the seat array, calculates the seat coordinate, and sets the
movement target.

#### `moveBus(Bus bus)`

Adds 6 to bus x. For each non-null seat, recalculates and assigns the passenger's
x/y so they travel with the bus. Past the window plus 200 pixels, state becomes
`DEPARTED`.

#### `removeDepartedBuses()`

Uses a bus iterator. For each departed bus, it clears seat passengers'
`assignedBus`, removes them from the master passenger list, clears all seats,
and removes the bus through the iterator.

### Ticket processing

#### `updateTicketLane(TicketLane ticketLane, int elapsedMs)`

1. Reduce a positive post-transaction delay and return.
2. Return if the booth is closed or the custom queue is empty.
3. Use `peek()` to inspect the front passenger without removing them.
4. If waiting, target the booth coordinate.
5. At the booth, change state to `BUYING_TICKET` and initialize 320 ms.
6. Reduce the ticket timer.
7. At zero, call `dequeue()` to remove exactly the front passenger.
8. Send that passenger to the platform, reposition the remaining nodes, and set
   the lane's 320 ms transaction delay.

This is the primary FIFO demonstration: passengers enqueue at the rear, the
front is served through peek, and completion dequeues the front.

### Passenger movement

#### `movePassengers()`

Calls `stepTowardTarget` for every active passenger. If not yet at the target,
the loop continues. At arrival it performs these state transitions:

```text
WALKING_TO_TICKET  -> WAITING_FOR_TICKET
WALKING_TO_PLATFORM -> WAITING_ON_PLATFORM
MOVING_TO_BAY_LINE -> WAITING_IN_BAY_LINE
WALKING_TO_BUS -> SEATED_IN_BUS
```

### Cleanup helpers

#### `removeFromQueues(Person passenger)`

Calls the custom queue's specific-person removal for both ticket queues and
removes the passenger from the platform list.

#### `detachFromBuses(Person passenger)`

Tracks whether any assignment existed. For every bus it:

- removes the passenger's node from the boarding queue;
- repositions the remaining node chain when removal succeeds;
- scans the fixed seats and nulls matching references.

It always clears `assignedBus` and reports whether anything was found.

#### `returnBusPassengers(Bus bus)`

1. Dequeue the entire boarding line into a temporary return list.
2. Add non-null seat passengers that are not already in that list.
3. Clear all seat positions with `Arrays.fill`.
4. Send still-active passengers back to the platform.

The simple temporary list prevents duplicate returns without introducing a Set,
keeping the project focused on its assigned Queue structure.

#### `sendToPlatform(Person passenger)`

Removes old ticket/platform references, adds the passenger to the platform only
if absent, clears bus assignment, sets `WALKING_TO_PLATFORM`, and refreshes all
platform targets.

### Positioning helpers

#### `positionTicketQueues()`

For each ticket lane, start at `queue.frontNode()` and follow `current.next`.
The node index determines a seven-column wrapped visual layout. This is a visible
example of node traversal that does not change FIFO order.

#### `positionPlatform()`

Iterates the platform list and maps passengers into four bay-area groups with
rows and columns.

#### `positionBoardingLine(Bus bus)`

Starts at the bus queue's front node and follows `next`. Each later node receives
a target 18 pixels farther behind the bus.

### Validation and logging helpers

#### `normalizeDestination(String destination)`

Returns exactly `Davao` or `Tagum` while accepting different capitalization.
Any unsupported value throws `IllegalArgumentException`.

#### `cleanId(String id)`

Returns null for null/blank input; otherwise returns trimmed text.

#### `log(String message)`

Passes the message to the injected `Consumer<String>`.

## 8. `TerminalPanel.java`

`TerminalPanel extends JPanel`, which lets Swing place it in the frame and call
its painting method.

### Fields and constructor

- `engine`: read-only source of current passengers, buses, and counts.
- `decorations`: flowers and trash cans generated once.
- Constructor sets background and preferred size, then creates decorations.

### `paintComponent(Graphics graphics)`

This is called by Swing, not directly by the simulation.

1. `super.paintComponent` clears the previous frame.
2. `graphics.create()` makes a separate `Graphics2D` context.
3. Antialiasing is enabled.
4. Draw background, waiting areas, booth, and departure board.
5. Draw every bus.
6. Copy the master passenger view into a temporary list.
7. Sort that copy by y coordinate for visual depth.
8. Draw passengers except those already seated inside buses.
9. Draw the status panel.
10. Dispose the copied graphics context in `finally`.

Sorting the copy does not change queue links or engine passenger order.

### `generateDecorations()`

Adds four trash cans near bays and 20 randomly positioned flowers. These are
visual only.

### `drawTerminalBackground(Graphics2D canvas)`

Draws the road, dashed bay lines, terminal floor, floor lines, wall boundary,
and every decoration. `Math.max` prevents a negative road width in a small
window.

### `drawBayWaitingAreas(Graphics2D canvas)`

Loops four times and draws the bay waiting-room rectangle, bench, and sign at a
different y offset.

### `drawTicketBooth(Graphics2D canvas)`

Draws the booth and reads both front clients through `engine.ticketClient`.
Green lights mean a front passenger is buying a ticket. Yellow means open/idle;
red and `CLOSED` mean service is paused.

### `drawBoothLight(...)`

Small helper that draws one colored rectangle and outline.

### `drawScheduleBoard(Graphics2D canvas)`

Places the board near the bottom, draws its frame and scan lines, writes the
heading, and requests one schedule row for bays 1-4.

### `drawScheduleRow(...)`

Searches active buses for the requested bay. It selects status text/color from
the bus state and draws the bay, route, and status.

### `drawStatusPanel(Graphics2D canvas)`

Displays the two custom ticket queue sizes, platform waiting count, and ticket
booth state.

### `EnvironmentDecoration`

This small helper class is in the same file because it is used only by the
panel. The constructor stores type/x/y. `draw` either creates a flower from
ovals or a trash can from rounded rectangles, lines, and ovals.

## 9. `TerminalSimulation.java`

This public class extends `JFrame`, so it is the application window.

### Fields

- `DESTINATIONS`: the two dialog choices.
- `log`: right-side text area.
- `engine`: simulation rules and live data.
- `terminalPanel`: pixel-art view.
- two button references: needed because their text/color changes.
- `lastFrameNanos` and `accumulatedMs`: fixed-step timing.

### Constructor

1. Call the JFrame constructor with the title.
2. Style the log as non-editable monospaced text.
3. Construct the engine with `this::appendLog`.
4. Construct the panel with the engine.
5. Configure close behavior and `BorderLayout`.
6. Put the panel in the center, log on the east, controls on the south.
7. Set initial/minimum size and center the frame.
8. Call `engine.initialize()`.
9. Record current nanoseconds.
10. Start a Swing timer that calls `updateFrame` about every 16 ms.

### `createControls()`

Creates the bottom panel and all ten buttons. Lambda listeners call either a
small window helper or an engine method. The quick passenger buttons choose a
random route; the CRUD Create button asks for type and route.

### `button(...)`

Factory helper that applies consistent color, font, focus, border, and action
listener settings before returning a button.

### `updateFrame()`

1. Measure elapsed nanoseconds.
2. Convert to milliseconds and prevent negative elapsed values.
3. Cap catch-up at 250 ms.
4. Add time to the accumulator.
5. While at least 16 ms remains, call `engine.update(16)` and subtract 16.
6. Request `terminalPanel.repaint()`.

Fixed steps keep queue/timer behavior consistent when Swing timer events arrive
slightly late.

### `appendLog(String message)`

Appends text, calculates how many lines exceed the 500-line limit, removes old
lines, and scrolls to the newest text. The catch resets safely if document
offset removal ever fails.

### Toggle methods

- `toggleBuses`: calls engine toggle, then changes Stop/Resume text and color.
- `toggleTicketBooth`: calls engine toggle, then changes Close/Open text and
  color.

### Passenger dialog methods

#### `createPassenger()`

Asks for regular/priority and destination. Cancelling returns without change.
Otherwise calls `createPassengerWithLog`.

#### `showPassengers()`

Creates a non-editable table model, adds one row per active passenger, enables
sorting, and shows it in a scroll pane. The assigned-bus column displays a dash
when null. The anonymous model overrides `isCellEditable` to always return
false, preventing the table from pretending that direct cell edits update the
engine.

#### `updatePassenger()`

Prompts for ID, confirms the passenger exists, asks for a new route, and calls
the engine update method.

#### `deletePassenger()`

Prompts for ID and calls `removePassenger`. False means no matching passenger,
so an error dialog appears.

### Bus dialog methods

#### `addBus()`

Asks for a route and calls the engine. Null from the engine means no free bay.

#### `deleteBus()`

Rejects an empty bus list, builds an ID options array, explains that passengers
will return to the platform, and calls engine removal for the chosen bus.

### Input helpers

- `chooseDestination`: calls the general choice helper with two routes.
- `choose`: wraps `JOptionPane.showInputDialog` with supplied options.
- `promptId`: trims input and converts cancel/blank to null.
- `showError`: displays an error dialog.

### `main(String[] args)`

Calls `SwingUtilities.invokeLater`, creates the frame on Swing's Event Dispatch
Thread, and makes it visible. Swing buttons, timer actions, and normal painting
therefore operate on the same UI thread.

## 10. `PassengerQueueTest.java`

This file tests the assigned data structure directly.

### `main` and `runAll`

`main` runs the checks and prints a success message. `runAll` allows the engine
test runner to include the same queue checks without printing twice.

### `queueUsesLinkedNodesAndFifoOrder()`

Enqueues P1, P2, and P3, then verifies:

- the front node stores P1;
- `next` connects P1 to P2 and P2 to P3;
- the rear node's next link is null;
- size and peek are correct;
- dequeue order is P1, P2, P3.

### `removalRepairsFrontMiddleAndRearLinks()`

Tests the special CRUD removal at the middle, front, and rear. It confirms that
the remaining node is still reachable and queue size is correct.

### `emptyQueueCanBeReused()`

Dequeues the only passenger, verifies empty behavior, then enqueues another
passenger. This proves both front and rear were reset correctly.

### `nullPassengerIsRejected()`

Calls `enqueue(null)` and expects `IllegalArgumentException`.

### Helpers

- `passenger`: creates small Person objects for queue tests.
- `check`: throws `AssertionError` when a condition is false.

## 11. `SimulationEngineTest.java`

This is the integration/regression runner. Its `main` first runs all direct queue
tests, then runs six engine/panel scenarios.

### `deletingPassengerClearsSeatAndWorkingList()`

Creates a passenger, gets them seated, deletes them, and verifies both the
master list and bus storage are clear.

### `deletingBusReturnsPassengerToPlatform()`

Seats a priority passenger, deletes the bus, and checks null assignment,
`WALKING_TO_PLATFORM`, and platform membership.

### `updatingAssignedPassengerRequeuesForNewDestination()`

Seats a Davao passenger, changes them to Tagum, and verifies removal from the
old seat and return to the platform.

### `identifiersAreTrimmedAndCaseInsensitive()`

Searches using lowercase text with surrounding spaces.

### `simulationMaintainsInvariantsOverTime()`

Runs 7,500 fixed updates. Every 100 ticks it verifies bus capacity and all
boarding-node/seat references.

### `terminalPanelPaintsHeadlessly()`

Creates a panel and paints it into a `BufferedImage` instead of opening a frame.
This catches rendering exceptions in automated tests.

### Engine-test helpers

- `newEngine`: provides deterministic `Random(7)` and an empty logger.
- `loadingBus`: creates a bus at a chosen bay and places it directly in loading
  state for faster tests.
- `waitUntilSeated`: waits for a specific state.
- `advanceUntil`: runs a maximum number of engine updates until a condition.
- `check`: throws an assertion failure.
- `isPassengerOnBus`: checks the node-based boarding queue and seat array.
- `checkEngineState`: builds the known-passenger set, traverses every boarding
  node, scans every seat, and checks back-references.

The test uses a `HashSet` only as a test assertion helper. It is not part of the
terminal algorithm being presented for the Queue assignment.

## 12. Complete runtime traces

### Program startup

```text
TerminalSimulation.main
-> SwingUtilities.invokeLater
-> TerminalSimulation constructor
-> SimulationEngine constructor
-> TerminalPanel constructor
-> engine.initialize
-> 22 passengers enqueue into two ticket queues
-> Swing Timer starts
```

### One timer event

```text
updateFrame
-> measure elapsed time
-> run zero or more engine.update(16) calls
   -> spawn rules
   -> bus rules
   -> priority ticket queue
   -> regular ticket queue
   -> passenger movement/state arrival
-> request repaint
-> Swing later calls paintComponent
```

### Regular passenger journey

```text
create Person
-> enqueue at rear of regular ticket queue
-> walk to queue target
-> wait
-> become front
-> peek during ticket service
-> dequeue after ticket completion
-> enter platform waiting list
-> matching bus selects passenger
-> enqueue at rear of that bus boarding queue
-> wait for earlier regular passengers
-> dequeue from front after boarding interval
-> reserve array seat
-> walk to seat
-> become seated
-> travel with departing bus
-> removed when bus has departed
```

### Priority passenger difference

```text
priority ticket queue still uses FIFO
-> platform waiting list
-> matching loading bus selects priority first
-> reserve seat directly
-> does not enter regular bus boarding queue
```

### Delete passenger

```text
find in master list
-> remove node from either ticket queue if present
-> remove from platform if present
-> remove node from every bus boarding queue if present
-> clear any matching seat
-> clear assignedBus
-> remove from master list
-> reposition affected passengers
```

### Delete bus

```text
find and remove bus
-> dequeue every boarding passenger
-> collect every seat passenger
-> clear seats
-> send active passengers back to platform
-> clear their assignedBus
```

## 13. Queue defense sheet

### Why two node classes?

`PassengerNode` represents one linked element. `PassengerQueue` owns the links
and guarantees FIFO operations. Separating them makes each responsibility clear.

### Where is FIFO demonstrated?

1. Regular passengers enqueue at the rear of their ticket line.
2. `peek` services only the front ticket passenger.
3. Ticket completion dequeues that front passenger.
4. Regular passengers enqueue at the rear of a bus boarding line.
5. The bus dequeues the front boarding passenger every 400 ms.

### Why store both front and rear?

Front makes dequeue O(1). Rear makes enqueue O(1). Without rear, enqueue would
need to traverse every node to find the end.

### Why reset rear after the final dequeue?

After the last node leaves, both front and rear must describe an empty queue. A
stale rear would make the next enqueue link to a removed node.

### Why is CRUD removal O(n)?

Deleting a requested ID can target any node, so the queue must walk from front
until it finds that passenger. This is a special administrative operation;
normal service remains O(1) FIFO dequeue.

### Why is the platform not called the main queue?

A bus skips unmatched destinations and passenger types. That is filtered
selection, not one global first-in-first-out service order. The actual ticket
and boarding lines are the project's strict queues.

### Why keep enums?

Nodes determine order; enums determine state. Removing enums would weaken type
safety without improving the queue implementation.

### Is this the same assignment as a singly linked list?

The physical links are singly linked, but the abstraction being demonstrated is
Queue. The project keeps `front` and `rear` and normally exposes FIFO enqueue,
peek, and dequeue behavior. A singly-linked-list assignment usually emphasizes
general insertion, deletion, and traversal at arbitrary positions. This project
uses traversal only for visual positioning and one special CRUD deletion.

## 14. Compile, run, and test

From the repository root in PowerShell:

```powershell
New-Item -ItemType Directory -Force out
$sources = (Get-ChildItem src -Filter *.java).FullName
javac --release 8 -encoding UTF-8 -d out $sources
java -cp out TerminalSimulation
```

Compile and run every test:

```powershell
$files = @(
    (Get-ChildItem src -Filter *.java).FullName
    (Get-ChildItem test -Filter *.java).FullName
)
javac --release 8 -encoding UTF-8 -d out $files
java -ea -cp out PassengerQueueTest
java -ea -cp out SimulationEngineTest
```

Expected output:

```text
PassengerQueueTest: all checks passed
SimulationEngineTest: all checks passed
```

## 15. Coverage checklist

After studying this walkthrough, every member should be able to explain:

- what data and link a `PassengerNode` stores;
- front and rear behavior for an empty, one-node, and multi-node queue;
- enqueue, dequeue, peek, size, contains, and special CRUD removal;
- why the queue is FIFO and where FIFO appears in the simulation;
- why the platform is a filtered list instead of the main queue;
- all passenger and bus states;
- Person movement and drawing responsibilities;
- bus seats, boarding queue, capacity, countdown, and drawing;
- every constant group;
- engine startup, update order, spawning, ticketing, boarding, cleanup, and
  positioning;
- panel drawing order and every drawing helper;
- window construction, buttons, dialogs, fixed-step timer, and log cap;
- what every direct queue test and engine regression test protects.

When answering a code question, use this pattern:

> "This method receives the action, it changes this field or collection, and
> the next update or drawing method produces this result."
