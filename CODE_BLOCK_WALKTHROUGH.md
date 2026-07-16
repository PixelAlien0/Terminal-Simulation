# Terminal Simulation: Code Block Walkthrough

This walkthrough is for the current multi-file GitHub version. It shows a real
piece of source code, then explains what that piece does, what calls it, and what
changes afterward.

For long pixel-art or Swing-construction methods, the walkthrough shows the
important executable lines and groups repetitive drawing/setup statements under
comments. The repository source remains the exact complete code; no behavior is
invented or omitted from the explanation.

Use this answer pattern during the defense:

> "This block belongs to this class. It receives this input, changes this data,
> and returns or causes this result. The next method in the flow is..."

## 1. How the whole application connects

```text
TerminalSimulation.main
    -> creates the Swing window
    -> constructor creates SimulationEngine and TerminalPanel
    -> Swing Timer calls updateFrame
    -> updateFrame calls SimulationEngine.update
    -> engine changes queues, passengers, buses, states, and coordinates
    -> repaint asks TerminalPanel.paintComponent to draw the new state
```

The files have separate jobs:

| File | Main job |
|---|---|
| `PassengerNode` | One queue node: passenger data plus `next` link |
| `PassengerQueue` | Front/rear node management and FIFO operations |
| `PassengerState`, `BusState` | Valid workflow states |
| `Person` | Passenger data, movement, and sprite |
| `Bus` | Bus data, seats, boarding queue, and sprite |
| `SimulationConfig` | Shared constants |
| `SimulationEngine` | All simulation rules |
| `TerminalPanel` | Drawing only |
| `TerminalSimulation` | Window, timer, controls, dialogs, and log |

## 2. `PassengerNode.java`

### Entire node class

```java
final class PassengerNode {
    final Person passenger;
    PassengerNode next;

    PassengerNode(Person passenger) {
        this.passenger = passenger;
    }
}
```

What this block does:

- `passenger` is the data stored inside the node.
- `next` points to the next node in the queue.
- `next == null` means this node is currently the rear.
- The constructor saves the passenger; Java initializes `next` to null.
- `final class` means the node class cannot be inherited.
- `final Person passenger` means a node cannot later be changed to hold another
  passenger.

This is a real DSA node because it stores both data and a link:

```text
[P1 | next] -> [P2 | next] -> [P3 | null]
```

## 3. `PassengerQueue.java`

### Queue fields

```java
final class PassengerQueue {
    private PassengerNode front;
    private PassengerNode rear;
    private int size;
```

What this block does:

- `front` points to the node that must leave next.
- `rear` points to the last node added.
- `size` remembers how many nodes exist.
- All three are private so other classes cannot replace queue links directly.
- An empty queue has null front, null rear, and size zero.

### Enqueue: add at the rear

```java
void enqueue(Person passenger) {
    if (passenger == null) {
        throw new IllegalArgumentException("Passenger cannot be null");
    }

    PassengerNode newNode = new PassengerNode(passenger);
    if (rear == null) {
        front = newNode;
        rear = newNode;
    } else {
        rear.next = newNode;
        rear = newNode;
    }
    size++;
}
```

What this block does:

1. Rejects null so every node contains a real passenger.
2. Wraps the passenger in a new node.
3. If the queue is empty, the new node is both front and rear.
4. Otherwise, the old rear links to the new node.
5. `rear` moves to the new last node.
6. Size increases.

Before and after:

```text
Before: front -> P1 -> P2 <- rear
enqueue(P3)
After:  front -> P1 -> P2 -> P3 <- rear
```

No traversal is needed, so enqueue is O(1). Passenger creation and regular bus
boarding call this method.

### Dequeue: remove from the front

```java
Person dequeue() {
    if (front == null) {
        return null;
    }

    PassengerNode removedNode = front;
    front = front.next;
    removedNode.next = null;
    size--;

    if (front == null) {
        rear = null;
    }
    return removedNode.passenger;
}
```

What this block does:

1. Returns null if nobody is waiting.
2. Saves the current front node.
3. Moves front to the second node.
4. Disconnects the removed node.
5. Decreases size.
6. If no node remains, rear must also become null.
7. Returns the removed passenger.

This is FIFO because the passenger who entered earliest leaves first. Ticket
completion and regular bus boarding call this method. It is O(1).

### Peek: inspect without removing

```java
Person peek() {
    return front == null ? null : front.passenger;
}
```

What this block does:

- Empty queue: return null.
- Non-empty queue: return the front passenger.
- It does not change links or size.
- Ticket service uses peek while the front passenger walks to and buys at the
  booth.

### Remove a requested passenger for CRUD

```java
boolean remove(Person passenger) {
    PassengerNode previous = null;
    PassengerNode current = front;

    while (current != null) {
        if (current.passenger == passenger) {
            if (previous == null) {
                front = current.next;
            } else {
                previous.next = current.next;
            }
            if (current == rear) {
                rear = previous;
            }
            current.next = null;
            size--;
            return true;
        }
        previous = current;
        current = current.next;
    }
    return false;
}
```

What this block does:

- Normal service uses dequeue, but Delete Passenger may target any node.
- `current` searches the nodes; `previous` stays one node behind.
- If previous is null, the target is the front, so front moves.
- A middle removal makes the previous node skip the target.
- If the target is rear, rear moves backward to previous.
- The removed node is disconnected and size decreases.
- True means found; false means not in this queue.

This special administrative operation is O(n). It does not change the normal
FIFO service rule.

### Contains

```java
boolean contains(Person passenger) {
    PassengerNode current = front;
    while (current != null) {
        if (current.passenger == passenger) {
            return true;
        }
        current = current.next;
    }
    return false;
}
```

What this block does:

- Walks from front through every `next` link.
- Uses `==` because the engine stores and checks the exact same `Person` object.
- Tests use it to make sure deleted passengers do not remain in bus queues.

### Empty, size, and front-node access

```java
boolean isEmpty() {
    return front == null;
}

int size() {
    return size;
}

PassengerNode frontNode() {
    return front;
}
```

What these blocks do:

- `isEmpty` checks the front in O(1).
- `size` returns the stored counter in O(1).
- `frontNode` lets layout and test code traverse the chain without changing
  service order.

## 4. State enums

### `PassengerState.java`: passenger states

```java
enum PassengerState {
    WALKING_TO_TICKET, WAITING_FOR_TICKET, BUYING_TICKET,
    WALKING_TO_PLATFORM, WAITING_ON_PLATFORM,
    MOVING_TO_BAY_LINE, WAITING_IN_BAY_LINE,
    WALKING_TO_BUS, SEATED_IN_BUS
}
```

What this block does:

- Restricts a passenger to nine valid stages.
- Prevents misspelled state strings.
- Lets the engine use a switch to react to arrival.
- These are states, not queue nodes.

### `BusState.java`: bus states

```java
enum BusState {
    ARRIVING, LOADING, WAITING_FOR_DEPARTURE, DEPARTING, DEPARTED
}
```

This restricts buses to their five lifecycle stages. Because this is a separate
enum, Java will not let a passenger receive a bus state.

## 5. `Person.java`

### Passenger fields

```java
final String id;
String destination;
final boolean isPriority;
final int arrivalOrder;

int x;
int y;
private int targetX;
private int targetY;
private int animationFrame;
private int animationSlow;

PassengerState state = PassengerState.WALKING_TO_TICKET;
Bus assignedBus;
int ticketTimerMs;
```

What this block stores:

- Identity: ID, destination, type, and displayed arrival number.
- Position: current x/y and private movement target.
- Animation: frame and slowdown counter.
- Workflow: passenger state, assigned bus, and ticket time.
- Destination is not final because Update Passenger may change it.

### Constructor

```java
Person(String id, String destination, int startX, int startY,
       boolean isPriority, int arrivalOrder) {
    this.id = id;
    this.destination = destination;
    this.x = startX;
    this.y = startY;
    this.targetX = startX;
    this.targetY = startY;
    this.isPriority = isPriority;
    this.arrivalOrder = arrivalOrder;
}
```

The constructor copies the supplied starting data. Initial target equals the
start so the person stays still until the engine assigns a queue position.

### Set movement target

```java
void setTarget(int targetX, int targetY) {
    this.targetX = targetX;
    this.targetY = targetY;
}
```

This changes where the passenger should walk. It does not move immediately.

### Move toward the target

```java
boolean stepTowardTarget() {
    int oldX = x;
    int oldY = y;

    if (x < targetX) {
        x = Math.min(x + SimulationConfig.PASSENGER_SPEED, targetX);
    } else if (x > targetX) {
        x = Math.max(x - SimulationConfig.PASSENGER_SPEED, targetX);
    }

    if (y < targetY) {
        y = Math.min(y + SimulationConfig.PASSENGER_SPEED, targetY);
    } else if (y > targetY) {
        y = Math.max(y - SimulationConfig.PASSENGER_SPEED, targetY);
    }

    if (x != oldX || y != oldY) {
        animationSlow++;
        if (animationSlow >= 3) {
            animationFrame = (animationFrame + 1) % 4;
            animationSlow = 0;
        }
    }

    return x == targetX && y == targetY;
}
```

What this block does:

- Moves each coordinate by at most four pixels per fixed engine update.
- `Math.min` and `Math.max` prevent overshooting.
- Advances the walking animation only when position changed.
- Wraps animation frames from 3 back to 0.
- Returns true when the passenger reached the target.
- `SimulationEngine.movePassengers` uses true to change the state.

### Draw the passenger

```java
void draw(Graphics2D graphics) {
    int bob = animationFrame % 2 == 0 ? 1 : 0;
    int renderY = y + bob;
    Color skin = new Color(210, 160, 120);
    Color hair = isPriority ? new Color(200, 180, 50) : new Color(170, 50, 50);
    Color shirt = isPriority ? new Color(100, 50, 150) : new Color(60, 100, 60);
    // Remaining statements draw shadow, legs, body, number, head,
    // priority star, destination letter, and ticket progress bar.
}
```

The full method uses rectangles, ovals, colors, and fonts to create the pixel-art
sprite. The drawing sections are:

1. Shadow below the person.
2. Alternating legs based on `animationFrame`.
3. Shirt and outline.
4. Arrival number.
5. Head and hair.
6. Priority star and route letter.
7. Ticket progress bar only while `BUYING_TICKET`.

Drawing reads passenger state; it does not change queue order.

## 6. `Bus.java`

### Bus fields

```java
final String busId;
final String destination;
final int bayId;
final int capacity = SimulationConfig.BUS_CAPACITY;

int x;
final int y;
BusState state = BusState.ARRIVING;
int countdownMs = SimulationConfig.BUS_LOADING_COUNTDOWN_MS;
int departureBufferMs = SimulationConfig.BUS_DEPARTURE_BUFFER_MS;
boolean countdownStarted;
final Person[] seats = new Person[capacity];
final PassengerQueue boardingLine = new PassengerQueue();
int boardingElapsedMs;
```

What this block stores:

- Fixed bus identity, route, bay, capacity, and y coordinate.
- Changing x coordinate and lifecycle state.
- Loading and departure timers.
- A fixed 20-position seat array.
- A custom node-based FIFO queue for regular boarders.

### Constructor

```java
Bus(String busId, String destination, int bayId, int startX, int startY) {
    this.busId = busId;
    this.destination = destination;
    this.bayId = bayId;
    this.x = startX;
    this.y = startY;
}
```

This saves identity and starting position. Field declarations already set the
initial state, timers, seats, and queue.

### Find a random empty seat

```java
int getRandomEmptySeat(Random random) {
    int start = random.nextInt(capacity);
    for (int offset = 0; offset < capacity; offset++) {
        int index = (start + offset) % capacity;
        if (seats[index] == null) {
            return index;
        }
    }
    return -1;
}
```

What this block does:

1. Chooses a random seat index to begin searching.
2. Checks at most every seat once.
3. `% capacity` wraps from the final index back to zero.
4. A null seat is empty, so its index is returned.
5. `-1` means no empty seat exists.

Example with capacity 20: if start is 18, the checked order begins 18, 19, 0,
1, 2, and continues until a null seat is found. Because capacity is fixed at 20,
this is effectively O(1).

### Check whether the bus is full

```java
boolean isFull() {
    return getPassengerCount() == capacity;
}
```

This calls the counting method and compares the result with 20. It avoids
duplicating the seat-counting loop.

### Count seat references

```java
int getPassengerCount() {
    int count = 0;
    for (Person passenger : seats) {
        if (passenger != null) {
            count++;
        }
    }
    return count;
}
```

Every non-null array entry is a reserved or occupied seat. A passenger begins
counting when `reserveSeat` stores the reference, even while walking to the bus.

### Convert seat index to screen position

```java
Point getSeatCoordinate(int seatIndex) {
    int column = seatIndex / 4;
    int row = seatIndex % 4;
    return new Point(x + 15 + column * 30, y + 15 + row * 15);
}
```

Division chooses one of five columns; remainder chooses one of four rows. The
bus x/y offset makes the coordinate move with the bus.

### Draw the bus

```java
void draw(Graphics2D graphics) {
    if (state == BusState.DEPARTED) {
        return;
    }

    Color body = destination.equals("Tagum")
            ? new Color(34, 139, 34)
            : new Color(30, 80, 160);
    // Remaining statements draw shadow, wheels, body, trim, windows,
    // door, bus ID, capacity, and countdown.
}
```

The first guard hides a departed bus. Tagum is green and Davao is blue. The
remaining pixel-art sections draw the vehicle and show `[passengers/capacity]`.
The countdown appears only while loading after somebody is seated.

## 7. `SimulationConfig.java`

### All configuration groups

```java
static final int FRAME_DELAY_MS = 16;
static final int MAX_FRAME_CATCH_UP_MS = 250;

static final int WINDOW_WIDTH = 1380;
static final int WINDOW_HEIGHT = 720;
static final int LOG_MAX_LINES = 500;
static final int MAX_PASSENGERS = 160;

static final int PASSENGER_SPEED = 4;
static final int BUS_CAPACITY = 20;
static final int BUS_ARRIVAL_INTERVAL_MS = 19_200;
static final int PASSENGER_SPAWN_INTERVAL_MS = 1_120;
static final int TICKET_SERVICE_MS = 320;
static final int TICKET_TRANSACTION_DELAY_MS = 320;
static final int BOARDING_INTERVAL_MS = 400;
static final int BUS_LOADING_COUNTDOWN_MS = 60_000;
static final int BUS_DEPARTURE_BUFFER_MS = 4_800;

static final int TICKET_BOOTH_X = 180;
static final int PRIORITY_QUEUE_Y = 340;
static final int REGULAR_QUEUE_Y = 380;
static final int TICKET_QUEUE_SPACING = 28;
```

What this block does:

- Gives names to timing, capacity, limit, window, and layout values.
- Keeps the same rule from being written as different numbers in different
  methods.
- Underscores improve readability: `60_000` is the integer 60000.

### Private constructor

```java
private SimulationConfig() {
}
```

The class contains only static constants, so nobody should create an instance.

## 8. `SimulationEngine.java`

This is the rules class. It owns the live passengers, platform, buses, and two
ticket queues.

### Ticket-lane helper

```java
private static final class TicketLane {
    final PassengerQueue queue = new PassengerQueue();
    final int y;
    final int rowDirection;
    int delayMs;

    TicketLane(int y, int rowDirection) {
        this.y = y;
        this.rowDirection = rowDirection;
    }
}
```

What this block does:

- Gives each ticket line its own custom queue.
- Stores its screen row and visual wrapping direction.
- Stores a separate delay after each ticket transaction.
- Lets one `updateTicketLane` method process both lines.

### Engine collections and flags

```java
private final TicketLane priorityLane =
        new TicketLane(SimulationConfig.PRIORITY_QUEUE_Y, -1);
private final TicketLane regularLane =
        new TicketLane(SimulationConfig.REGULAR_QUEUE_Y, 1);
private final TicketLane[] ticketLanes = {priorityLane, regularLane};
private final List<Person> platform = new ArrayList<Person>();
private final List<Person> passengers = new ArrayList<Person>();
private final List<Bus> buses = new ArrayList<Bus>();
private final Random random;
private final Consumer<String> logger;

private int passengerCounter;
private int busCounter = 1;
private int passengerSpawnMs;
private int busSpawnMs;
private boolean busesStopped;
private boolean ticketBoothOpen = true;
```

What this block stores:

- Two node-based FIFO ticket queues.
- `platform`: filtered waiting list; buses may skip unmatched routes/types.
- `passengers`: master list of every active passenger.
- `buses`: all active buses.
- Random generator, logger, unique-ID counters, spawn timers, and pause flags.

### Constructors and test injection

```java
SimulationEngine(Consumer<String> logger) {
    this(new Random(), logger);
}

SimulationEngine(Random random, Consumer<String> logger) {
    this.random = random;
    this.logger = logger == null ? message -> { } : logger;
}
```

The application uses the first constructor. Tests use the second with
`new Random(7)` so random choices repeat. A null logger becomes an action that
does nothing, preventing null checks throughout the engine.

### Initial data

```java
void initialize() {
    log("[SYSTEM] Simulation started...");
    for (int index = 0; index < 22; index++) {
        createRandomPassenger(random.nextFloat() < 0.20f);
    }
}
```

This logs startup and creates 22 passengers. Each has a 20% priority chance.

### Create a random passenger

```java
Person createRandomPassenger(boolean priority) {
    return createPassenger(priority,
            random.nextBoolean() ? "Davao" : "Tagum");
}
```

The caller supplies passenger type; this method randomly chooses the route and
delegates to the main creation method.

### Create and enqueue a passenger

```java
Person createPassenger(boolean priority, String destination) {
    passengerCounter++;
    Person passenger = new Person(
            "P" + passengerCounter,
            normalizeDestination(destination),
            10,
            350 + random.nextInt(30),
            priority,
            passengerCounter
    );
    lane(priority).queue.enqueue(passenger);
    passengers.add(passenger);
    positionTicketQueues();
    return passenger;
}
```

What this block does:

1. Creates the next P number.
2. Normalizes the route.
3. Creates the person near the left entrance.
4. Selects the priority or regular lane.
5. Enqueues at that queue's rear.
6. Adds the same object to the master list.
7. Recalculates queue targets.

### Creation with a log message

```java
Person createPassengerWithLog(boolean priority, String destination) {
    Person passenger = createPassenger(priority, destination);
    log("[CRUD] Created " + (priority ? "Priority" : "Regular")
            + " passenger " + passenger.id + " -> " + passenger.destination);
    return passenger;
}
```

The detailed CRUD button uses this wrapper. Quick passenger buttons use the
random method without a CRUD message.

### Find a passenger by ID

```java
Person findPassenger(String id) {
    String wanted = cleanId(id);
    if (wanted != null) {
        for (Person passenger : passengers) {
            if (passenger.id.equalsIgnoreCase(wanted)) {
                return passenger;
            }
        }
    }
    return null;
}
```

This trims input, scans the master list, ignores letter case, and returns either
the exact Person object or null. Complexity is O(n).

### Update destination safely

```java
boolean updatePassengerDestination(String id, String destination) {
    Person passenger = findPassenger(id);
    if (passenger == null) {
        return false;
    }
    passenger.destination = normalizeDestination(destination);
    if (detachFromBuses(passenger)) {
        sendToPlatform(passenger);
    }
    log("[CRUD] Updated " + passenger.id
            + " destination to " + passenger.destination);
    return true;
}
```

What this block does:

- Rejects an unknown ID.
- Changes to a supported route.
- If the person was assigned to a bus, removes the boarding node or seat and
  returns them to the platform.
- Prevents a Tagum passenger from staying on a Davao bus.

### Delete a passenger safely

```java
boolean removePassenger(String id) {
    Person passenger = findPassenger(id);
    if (passenger == null) {
        return false;
    }
    removeFromQueues(passenger);
    detachFromBuses(passenger);
    passengers.remove(passenger);
    positionTicketQueues();
    positionPlatform();
    log("[CRUD] Deleted passenger " + passenger.id);
    return true;
}
```

This removes every reference: ticket nodes, platform entry, bus boarding node,
seat, bus assignment, and master-list entry. Repositioning closes visible gaps.

### Add a bus at any free bay

```java
Bus addBus(String destination) {
    List<Integer> freeBays = freeBays();
    if (freeBays.isEmpty()) {
        return null;
    }
    int bay = freeBays.get(random.nextInt(freeBays.size()));
    Bus bus = addBusAtBay(destination, bay);
    log("[BUS] Added " + bus.busId + " at Bay " + bay);
    return bus;
}
```

This finds available bay numbers, randomly selects one, delegates construction,
and returns null when all four are occupied.

### Build a bus at a requested bay

```java
Bus addBusAtBay(String destination, int bay) {
    if (bay < 1 || bay > 4 || !isBayFree(bay)) {
        return null;
    }
    String normalized = normalizeDestination(destination);
    String line = normalized.equals("Davao") ? "Davao Exp" : "Tagum Met";
    char label = bay == 1 || bay == 3 ? 'A' : 'B';
    Bus bus = new Bus(
            line + " " + label + " (B" + busCounter++ + ")",
            normalized,
            bay,
            1400,
            20 + (bay - 1) * 155
    );
    buses.add(bus);
    return bus;
}
```

This validates the bay, constructs its display ID and y coordinate, starts it
off-screen at x 1400, and adds it to active buses. Tests use this method to
create predictable buses.

### Delete a bus safely

```java
boolean removeBus(String id) {
    Bus bus = findBus(id);
    if (bus == null) {
        return false;
    }
    buses.remove(bus);
    returnBusPassengers(bus);
    log("[BUS] Deleted bus: " + bus.busId);
    return true;
}
```

The bus is removed, but its passengers are not abandoned. The cleanup helper
returns its boarding and seated passengers to the platform.

### One complete engine step

```java
void update(int elapsedMs) {
    updateSpawning(elapsedMs);
    updateBuses(elapsedMs);
    for (TicketLane ticketLane : ticketLanes) {
        updateTicketLane(ticketLane, elapsedMs);
    }
    movePassengers();
}
```

The order is important: spawning, buses, both ticket queues, then movement and
arrival-state transitions.

### Pause controls

```java
boolean toggleBusesStopped() {
    busesStopped = !busesStopped;
    log("[SYSTEM] Bus Operations "
            + (busesStopped ? "Paused." : "Resumed."));
    return busesStopped;
}

boolean toggleTicketBooth() {
    ticketBoothOpen = !ticketBoothOpen;
    log("[SYSTEM] Ticket Booth "
            + (ticketBoothOpen ? "OPEN." : "CLOSED."));
    if (ticketBoothOpen) {
        positionTicketQueues();
    }
    return ticketBoothOpen;
}
```

Each method flips a flag, logs, and returns the new condition for button text.
Reopening the booth refreshes visible node positions.

### Read-only views and counts

```java
List<Person> passengers() {
    return Collections.unmodifiableList(passengers);
}

List<Person> platformPassengers() {
    return Collections.unmodifiableList(platform);
}

List<Bus> buses() {
    return Collections.unmodifiableList(buses);
}

int ticketCount(boolean priority) {
    return lane(priority).queue.size();
}

Person ticketClient(boolean priority) {
    return lane(priority).queue.peek();
}

int platformCount() {
    return platform.size();
}

boolean isTicketBoothOpen() {
    return ticketBoothOpen;
}
```

The unmodifiable views allow reading without outside `add` or `remove` calls.
Queue size and peek support the panel without exposing queue mutation.

### Select a ticket lane

```java
private TicketLane lane(boolean priority) {
    return priority ? priorityLane : regularLane;
}
```

True selects priority; false selects regular.

### Automatic spawning

```java
private void updateSpawning(int elapsedMs) {
    passengerSpawnMs += elapsedMs;
    if (passengerSpawnMs >= SimulationConfig.PASSENGER_SPAWN_INTERVAL_MS) {
        passengerSpawnMs %= SimulationConfig.PASSENGER_SPAWN_INTERVAL_MS;
        if (ticketBoothOpen
                && passengers.size() < SimulationConfig.MAX_PASSENGERS) {
            createRandomPassenger(random.nextFloat() < 0.20f);
        }
    }

    if (buses.size() < 4 && !busesStopped) {
        busSpawnMs += elapsedMs;
        if (busSpawnMs >= SimulationConfig.BUS_ARRIVAL_INTERVAL_MS) {
            busSpawnMs %= SimulationConfig.BUS_ARRIVAL_INTERVAL_MS;
            spawnBus();
        }
    }
}
```

This uses elapsed milliseconds, not assumed timer ticks. Automatic passengers
stop at 160 or while the booth is closed. Automatic buses stop at four or while
bus operations are paused.

### Automatic bus and free-bay helpers

```java
private void spawnBus() {
    List<Integer> freeBays = freeBays();
    if (freeBays.isEmpty()) {
        return;
    }
    int bay = freeBays.get(random.nextInt(freeBays.size()));
    Bus bus = addBusAtBay(bay <= 2 ? "Davao" : "Tagum", bay);
    log("[BUS] " + bus.busId + " arriving at Bay " + bay);
}

private List<Integer> freeBays() {
    List<Integer> result = new ArrayList<Integer>();
    for (int bay = 1; bay <= 4; bay++) {
        if (isBayFree(bay)) {
            result.add(bay);
        }
    }
    return result;
}

private boolean isBayFree(int bay) {
    for (Bus bus : buses) {
        if (bus.bayId == bay) {
            return false;
        }
    }
    return true;
}
```

Automatic route follows the bay group: 1-2 Davao, 3-4 Tagum. The helpers build
and validate the available bay numbers.

### Find a bus

```java
private Bus findBus(String id) {
    String wanted = cleanId(id);
    if (wanted != null) {
        for (Bus bus : buses) {
            if (bus.busId.equalsIgnoreCase(wanted)) {
                return bus;
            }
        }
    }
    return null;
}
```

This is the bus version of passenger lookup: trimmed and case-insensitive.

### Update bus states

```java
private void updateBuses(int elapsedMs) {
    if (busesStopped) {
        return;
    }
    for (Bus bus : buses) {
        switch (bus.state) {
            case ARRIVING:
                bus.x -= 8;
                if (bus.x <= 620) {
                    bus.x = 620;
                    bus.state = BusState.LOADING;
                }
                break;
            case LOADING:
                loadBus(bus, elapsedMs);
                break;
            case WAITING_FOR_DEPARTURE:
                bus.departureBufferMs -= elapsedMs;
                if (bus.departureBufferMs <= 0) {
                    bus.state = BusState.DEPARTING;
                    log("[BUS] " + bus.busId + " departing with "
                            + bus.getPassengerCount() + " passengers.");
                }
                break;
            case DEPARTING:
                moveBus(bus);
                break;
            default:
                break;
        }
    }
    removeDepartedBuses();
}
```

This is the bus state machine. Pausing returns before movement or timer changes.
Every active state has one action, then departed buses are cleaned.

### Load one bus

```java
private void loadBus(Bus bus, int elapsedMs) {
    if (!bus.isFull()) {
        Person priority = takePlatformPassenger(bus.destination, true);
        if (priority != null) {
            reserveSeat(bus, priority);
        }
    }

    int reserved = bus.getPassengerCount() + bus.boardingLine.size();
    if (reserved < bus.capacity) {
        Person regular = takePlatformPassenger(bus.destination, false);
        if (regular != null) {
            regular.state = PassengerState.MOVING_TO_BAY_LINE;
            regular.assignedBus = bus;
            bus.boardingLine.enqueue(regular);
            positionBoardingLine(bus);
        }
    }

    if (!bus.boardingLine.isEmpty() && !bus.isFull()) {
        bus.boardingElapsedMs += elapsedMs;
        if (bus.boardingElapsedMs >= SimulationConfig.BOARDING_INTERVAL_MS) {
            reserveSeat(bus, bus.boardingLine.dequeue());
            positionBoardingLine(bus);
            bus.boardingElapsedMs = 0;
        }
    }

    if (!bus.countdownStarted) {
        for (Person passenger : bus.seats) {
            if (passenger != null
                    && passenger.state == PassengerState.SEATED_IN_BUS) {
                bus.countdownStarted = true;
                break;
            }
        }
    } else {
        bus.countdownMs = Math.max(0, bus.countdownMs - elapsedMs);
    }

    if (bus.isFull()
            || bus.countdownStarted && bus.countdownMs == 0) {
        while (!bus.boardingLine.isEmpty()) {
            sendToPlatform(bus.boardingLine.dequeue());
        }
        bus.state = BusState.WAITING_FOR_DEPARTURE;
        log("[BUS] " + bus.busId
                + " closed doors. Preparing to depart.");
    }
}
```

What this block does:

1. Priority passengers get direct seat reservation first.
2. Seats plus boarding nodes are counted as reserved capacity.
3. One matching regular passenger enqueues at the bus line's rear.
4. Every 400 ms the front regular passenger dequeues and gets a seat.
5. Countdown starts after somebody physically reaches a seat.
6. Full or timed-out buses dequeue remaining waiting passengers back to the
   platform and close.

### Select a matching platform passenger

```java
private Person takePlatformPassenger(String destination, boolean priority) {
    Iterator<Person> iterator = platform.iterator();
    while (iterator.hasNext()) {
        Person passenger = iterator.next();
        if (passenger.isPriority == priority
                && passenger.state == PassengerState.WAITING_ON_PLATFORM
                && passenger.destination.equals(destination)) {
            iterator.remove();
            positionPlatform();
            return passenger;
        }
    }
    return null;
}
```

This is filtered selection, not global FIFO. It chooses the first entry matching
type, waiting state, and route. Iterator removal is safe during traversal.

### Reserve a seat

```java
private void reserveSeat(Bus bus, Person passenger) {
    int seatIndex = bus.getRandomEmptySeat(random);
    if (seatIndex < 0) {
        sendToPlatform(passenger);
        return;
    }
    passenger.state = PassengerState.WALKING_TO_BUS;
    passenger.assignedBus = bus;
    bus.seats[seatIndex] = passenger;
    Point seat = bus.getSeatCoordinate(seatIndex);
    passenger.setTarget(seat.x, seat.y);
}
```

The passenger enters the seat array before reaching the coordinate, preventing
another passenger from receiving the same seat. No seat means return to platform.

### Move and finish a bus

```java
private void moveBus(Bus bus) {
    bus.x += 6;
    for (int index = 0; index < bus.seats.length; index++) {
        Person passenger = bus.seats[index];
        if (passenger != null) {
            Point seat = bus.getSeatCoordinate(index);
            passenger.x = seat.x;
            passenger.y = seat.y;
        }
    }
    if (bus.x > SimulationConfig.WINDOW_WIDTH + 200) {
        bus.state = BusState.DEPARTED;
    }
}

private void removeDepartedBuses() {
    Iterator<Bus> iterator = buses.iterator();
    while (iterator.hasNext()) {
        Bus bus = iterator.next();
        if (bus.state == BusState.DEPARTED) {
            for (Person passenger : bus.seats) {
                if (passenger != null) {
                    passenger.assignedBus = null;
                    passengers.remove(passenger);
                }
            }
            Arrays.fill(bus.seats, null);
            iterator.remove();
        }
    }
}
```

`moveBus` keeps seated coordinates attached to the moving vehicle. Once
off-screen, cleanup removes completed passengers, clears seats, and safely
removes the bus.

### Process one ticket queue

```java
private void updateTicketLane(TicketLane ticketLane, int elapsedMs) {
    if (ticketLane.delayMs > 0) {
        ticketLane.delayMs = Math.max(0, ticketLane.delayMs - elapsedMs);
        return;
    }
    if (!ticketBoothOpen || ticketLane.queue.isEmpty()) {
        return;
    }

    Person passenger = ticketLane.queue.peek();
    if (passenger.state == PassengerState.WAITING_FOR_TICKET) {
        passenger.setTarget(SimulationConfig.TICKET_BOOTH_X, ticketLane.y);
    }
    if (passenger.x == SimulationConfig.TICKET_BOOTH_X
            && passenger.y == ticketLane.y
            && passenger.state != PassengerState.BUYING_TICKET) {
        passenger.state = PassengerState.BUYING_TICKET;
        passenger.ticketTimerMs = SimulationConfig.TICKET_SERVICE_MS;
    }
    if (passenger.state == PassengerState.BUYING_TICKET) {
        passenger.ticketTimerMs = Math.max(0,
                passenger.ticketTimerMs - elapsedMs);
        if (passenger.ticketTimerMs == 0) {
            ticketLane.queue.dequeue();
            sendToPlatform(passenger);
            positionTicketQueues();
            ticketLane.delayMs =
                    SimulationConfig.TICKET_TRANSACTION_DELAY_MS;
        }
    }
}
```

This is the clearest Queue algorithm in the application:

- `peek`: serve only the front.
- `dequeue`: remove that front after completion.
- Later passengers remain linked in the same FIFO order.

### Move passengers and change arrival states

```java
private void movePassengers() {
    for (Person passenger : passengers) {
        if (!passenger.stepTowardTarget()) {
            continue;
        }
        switch (passenger.state) {
            case WALKING_TO_TICKET:
                passenger.state = PassengerState.WAITING_FOR_TICKET;
                break;
            case WALKING_TO_PLATFORM:
                passenger.state = PassengerState.WAITING_ON_PLATFORM;
                break;
            case MOVING_TO_BAY_LINE:
                passenger.state = PassengerState.WAITING_IN_BAY_LINE;
                break;
            case WALKING_TO_BUS:
                passenger.state = PassengerState.SEATED_IN_BUS;
                break;
            default:
                break;
        }
    }
}
```

Movement belongs to Person; deciding the next workflow state belongs to Engine.
The switch runs only after the target is reached.

### Remove passenger references

```java
private void removeFromQueues(Person passenger) {
    priorityLane.queue.remove(passenger);
    regularLane.queue.remove(passenger);
    platform.remove(passenger);
}

private boolean detachFromBuses(Person passenger) {
    boolean found = passenger.assignedBus != null;
    for (Bus bus : buses) {
        if (bus.boardingLine.remove(passenger)) {
            found = true;
            positionBoardingLine(bus);
        }
        for (int index = 0; index < bus.seats.length; index++) {
            if (bus.seats[index] == passenger) {
                bus.seats[index] = null;
                found = true;
            }
        }
    }
    passenger.assignedBus = null;
    return found;
}
```

The first helper clears ticket nodes and platform membership. The second clears
every bus queue node and seat reference. This prevents ghost passengers.

### Return passengers from a deleted bus

```java
private void returnBusPassengers(Bus bus) {
    List<Person> returning = new ArrayList<Person>();
    while (!bus.boardingLine.isEmpty()) {
        Person passenger = bus.boardingLine.dequeue();
        if (!returning.contains(passenger)) {
            returning.add(passenger);
        }
    }
    for (Person passenger : bus.seats) {
        if (passenger != null && !returning.contains(passenger)) {
            returning.add(passenger);
        }
    }
    Arrays.fill(bus.seats, null);
    for (Person passenger : returning) {
        if (passengers.contains(passenger)) {
            sendToPlatform(passenger);
        }
    }
}
```

This drains the node queue, collects unique seat passengers, clears the bus, and
returns still-active people to the platform.

### Send one passenger to the platform

```java
private void sendToPlatform(Person passenger) {
    removeFromQueues(passenger);
    if (!platform.contains(passenger)) {
        platform.add(passenger);
    }
    passenger.assignedBus = null;
    passenger.state = PassengerState.WALKING_TO_PLATFORM;
    positionPlatform();
}
```

Old ticket/platform references are cleared first, preventing duplicates. The
passenger becomes unassigned and receives a platform movement target.

### Position the two node queues

```java
private void positionTicketQueues() {
    for (TicketLane ticketLane : ticketLanes) {
        int index = 0;
        PassengerNode current = ticketLane.queue.frontNode();
        while (current != null) {
            Person passenger = current.passenger;
            int column = index % 7;
            int row = index / 7;
            passenger.setTarget(
                    SimulationConfig.TICKET_BOOTH_X
                            - column * SimulationConfig.TICKET_QUEUE_SPACING,
                    ticketLane.y + ticketLane.rowDirection * row * 18
            );
            index++;
            current = current.next;
        }
    }
}
```

This visibly demonstrates node traversal. Starting at front, it follows `next`
and assigns a screen target without changing links or FIFO order.

### Position platform and bus boarding line

```java
private void positionPlatform() {
    int index = 0;
    for (Person passenger : platform) {
        int slot = index % SimulationConfig.MAX_PASSENGERS;
        int bay = slot / 40;
        int withinBay = slot % 40;
        passenger.setTarget(
                280 + withinBay % 10 * 24,
                35 + bay * 155 + withinBay / 10 * 18);
        index++;
    }
}

private void positionBoardingLine(Bus bus) {
    int index = 0;
    PassengerNode current = bus.boardingLine.frontNode();
    while (current != null) {
        current.passenger.setTarget(
                bus.x - 30 - index * 18,
                bus.y + 45);
        current = current.next;
        index++;
    }
}
```

Platform uses ordinary list iteration because it is filtered waiting storage.
The boarding line follows real node links from front to rear.

### Normalize input and send logs

```java
private String normalizeDestination(String destination) {
    if ("Davao".equalsIgnoreCase(destination)) {
        return "Davao";
    }
    if ("Tagum".equalsIgnoreCase(destination)) {
        return "Tagum";
    }
    throw new IllegalArgumentException(
            "Unsupported destination: " + destination);
}

private String cleanId(String id) {
    if (id == null || id.trim().isEmpty()) {
        return null;
    }
    return id.trim();
}

private void log(String message) {
    logger.accept(message);
}
```

These helpers standardize the two routes, clean IDs, and forward messages to
the logger supplied by the window.

## 9. `TerminalPanel.java`

This class is the view. It reads the engine and draws; it does not enqueue,
dequeue, assign seats, or change workflow states.

### Panel fields and constructor

```java
private final SimulationEngine engine;
private final List<EnvironmentDecoration> decorations =
        new ArrayList<EnvironmentDecoration>();

TerminalPanel(SimulationEngine engine) {
    this.engine = engine;
    setBackground(new Color(148, 128, 73));
    setPreferredSize(new Dimension(1050, 620));
    generateDecorations();
}
```

The panel saves the engine reference, chooses a background/size, and generates
decorations once.

### Draw one complete frame

```java
@Override
protected void paintComponent(Graphics graphics) {
    super.paintComponent(graphics);
    Graphics2D canvas = (Graphics2D) graphics.create();
    try {
        canvas.setRenderingHint(
                RenderingHints.KEY_ANTIALIASING,
                RenderingHints.VALUE_ANTIALIAS_ON
        );
        drawTerminalBackground(canvas);
        drawBayWaitingAreas(canvas);
        drawTicketBooth(canvas);
        drawScheduleBoard(canvas);

        for (Bus bus : engine.buses()) {
            bus.draw(canvas);
        }

        List<Person> passengers =
                new ArrayList<Person>(engine.passengers());
        passengers.sort(
                Comparator.comparingInt(passenger -> passenger.y));
        for (Person passenger : passengers) {
            if (passenger.state != PassengerState.SEATED_IN_BUS) {
                passenger.draw(canvas);
            }
        }

        drawStatusPanel(canvas);
    } finally {
        canvas.dispose();
    }
}
```

What this block does:

1. Clears the old frame with `super.paintComponent`.
2. Creates an independent graphics context.
3. Enables smooth edges.
4. Draws static terminal layers.
5. Draws every active bus.
6. Copies passengers and sorts the copy by y for visual depth.
7. Hides seated walking sprites because they are inside buses.
8. Draws the status overlay.
9. Always disposes the copied graphics context.

### Generate decorations

```java
private void generateDecorations() {
    Random random = new Random();
    for (int bay = 0; bay < 4; bay++) {
        int y = 30 + bay * 155;
        decorations.add(
                new EnvironmentDecoration("TrashCan", 500, y + 20));
    }
    for (int index = 0; index < 20; index++) {
        decorations.add(new EnvironmentDecoration(
                "Flower",
                random.nextInt(230),
                random.nextInt(620)
        ));
    }
}
```

This makes four trash cans and 20 random flowers. They do not affect simulation
data.

### Draw terminal background

```java
private void drawTerminalBackground(Graphics2D canvas) {
    canvas.setColor(new Color(81, 81, 81));
    canvas.fillRect(600, 0, Math.max(0, getWidth() - 600), getHeight());
    // Draw dashed bay lines.
    // Draw terminal floor and horizontal floor lines.
    // Draw the road/floor boundary.
    // Draw each EnvironmentDecoration.
}
```

This method paints the road and floor before moving objects. `Math.max` avoids a
negative road width when the panel is small. Stroke settings create dashed bay
lines, then reset to a normal stroke.

### Draw four waiting areas

```java
private void drawBayWaitingAreas(Graphics2D canvas) {
    for (int bay = 0; bay < 4; bay++) {
        int y = 30 + bay * 155;
        // Draw waiting-room rectangle, bench slats, and bay sign.
    }
}
```

One loop reuses the same artwork at four y positions.

### Draw ticket booth and activity lights

```java
private void drawTicketBooth(Graphics2D canvas) {
    // Draw booth body, window, and TICKETS label.
    Person priorityClient = engine.ticketClient(true);
    Person regularClient = engine.ticketClient(false);
    boolean priorityActive = priorityClient != null
            && priorityClient.state == PassengerState.BUYING_TICKET;
    boolean regularActive = regularClient != null
            && regularClient.state == PassengerState.BUYING_TICKET;

    Color idle = engine.isTicketBoothOpen()
            ? new Color(200, 200, 50)
            : Color.RED;
    drawBoothLight(canvas, 195, 335,
            priorityActive ? Color.GREEN : idle);
    drawBoothLight(canvas, 195, 362,
            regularActive ? Color.GREEN : idle);
    // Draw CLOSED text when the engine reports closed.
}
```

Queue `peek` reaches the panel through `ticketClient`. Green means the front is
buying, yellow means open/idle, and red means closed.

### Draw one booth light

```java
private void drawBoothLight(
        Graphics2D canvas, int x, int y, Color color) {
    canvas.setColor(color);
    canvas.fillRect(x, y, 5, 8);
    canvas.setColor(Color.BLACK);
    canvas.drawRect(x, y, 5, 8);
}
```

This helper avoids repeating the same small rectangle code for both lines.

### Draw departure board

```java
private void drawScheduleBoard(Graphics2D canvas) {
    int boardX = 290;
    int boardY = Math.max(500, getHeight() - 110);
    int boardWidth = 280;
    int boardHeight = 100;
    // Draw frame, screen, scan lines, and heading.
    for (int bay = 1; bay <= 4; bay++) {
        drawScheduleRow(
                canvas, bay, boardX + 10,
                boardY + 45 + (bay - 1) * 14);
    }
}
```

This places the board near the bottom and delegates one row per bay.

### Draw one schedule row

```java
private void drawScheduleRow(
        Graphics2D canvas, int bay, int x, int y) {
    String bayText = "BAY " + bay;
    String status = "NO BUS";
    Color statusColor = new Color(50, 90, 140);

    for (Bus bus : engine.buses()) {
        if (bus.bayId != bay) {
            continue;
        }
        bayText += " - " + bus.destination.toUpperCase();
        // Select ARRIVING, LOADING, CLOSING, or DEPARTING color/text.
        break;
    }
    // Draw bayText and status.
}
```

This searches for the bus assigned to one bay and converts its state to display
text/color. `break` stops after the matching bus because bays are unique.

### Draw status panel

```java
private void drawStatusPanel(Graphics2D canvas) {
    // Draw translucent panel and title.
    canvas.drawString("Prio Queue: " + engine.ticketCount(true), 20, 48);
    canvas.drawString("Reg Queue: " + engine.ticketCount(false), 20, 66);
    canvas.drawString("Lounge Queue: " + engine.platformCount(), 20, 84);
    canvas.drawString(
            "Booth: "
                    + (engine.isTicketBoothOpen() ? "OPEN" : "CLOSED"),
            20,
            102
    );
}
```

This displays custom queue sizes and platform/booth status without mutating them.

### Environment decoration helper

```java
final class EnvironmentDecoration {
    private final String type;
    private final int x;
    private final int y;

    EnvironmentDecoration(String type, int x, int y) {
        this.type = type;
        this.x = x;
        this.y = y;
    }

    void draw(Graphics2D graphics) {
        if ("Flower".equals(type)) {
            // Draw petals and yellow center.
        } else {
            // Draw trash-can body, grooves, outline, and lid.
        }
    }
}
```

This class is in the panel file because it is used only for panel artwork.

## 10. `TerminalSimulation.java`

This is the public JFrame class: application startup, buttons, dialogs, timer,
and log.

### Window fields

```java
private static final String[] DESTINATIONS = {"Davao", "Tagum"};
private final JTextArea log = new JTextArea(10, 30);
private final SimulationEngine engine;
private final TerminalPanel terminalPanel;
private JButton stopBusesButton;
private JButton ticketButton;
private long lastFrameNanos;
private int accumulatedMs;
```

The engine and panel live as long as the frame. Two buttons need fields because
their labels change. The last two fields implement fixed-step timing.

### Window constructor

```java
public TerminalSimulation() {
    super("Terminal Simulation - Pixel Art Edition");
    // Style log.
    engine = new SimulationEngine(this::appendLog);
    terminalPanel = new TerminalPanel(engine);
    // Build scroll pane, frame layout, sizes, and controls.
    engine.initialize();
    lastFrameNanos = System.nanoTime();
    new Timer(SimulationConfig.FRAME_DELAY_MS,
            event -> updateFrame()).start();
}
```

What this block does:

1. Creates the window title and log.
2. Gives the engine a reference to `appendLog` without giving it the text area.
3. Gives the panel read access to the engine.
4. Places panel/log/controls in `BorderLayout`.
5. Creates starting passengers.
6. Starts the Swing timer.

### Create all controls

```java
private JPanel createControls() {
    JPanel controls = new JPanel(
            new FlowLayout(FlowLayout.CENTER, 15, 10));
    // Create quick passenger and pause buttons.
    // Create passenger CRUD and bus management buttons.
    // Each lambda calls its engine/window action.
    return controls;
}
```

This method defines ten buttons. A lambda such as
`event -> engine.createRandomPassenger(false)` means the click calls that method.

### Button factory

```java
private JButton button(
        String text, Color color, ActionListener action) {
    JButton button = new JButton(text);
    button.setBackground(color);
    button.setForeground(Color.WHITE);
    button.setFont(new Font("Monospaced", Font.BOLD, 12));
    button.setFocusPainted(false);
    button.setBorder(BorderFactory.createLineBorder(Color.WHITE, 2));
    button.addActionListener(action);
    return button;
}
```

This avoids repeating style and listener setup ten times.

### Fixed-step frame update

```java
private void updateFrame() {
    long now = System.nanoTime();
    accumulatedMs += (int) Math.min(
            SimulationConfig.MAX_FRAME_CATCH_UP_MS,
            Math.max(0L, (now - lastFrameNanos) / 1_000_000L)
    );
    lastFrameNanos = now;
    while (accumulatedMs >= SimulationConfig.FRAME_DELAY_MS) {
        engine.update(SimulationConfig.FRAME_DELAY_MS);
        accumulatedMs -= SimulationConfig.FRAME_DELAY_MS;
    }
    terminalPanel.repaint();
}
```

This measures real elapsed time, converts nanoseconds to milliseconds, caps a
late frame at 250 ms, runs consistent 16 ms steps, then requests drawing.

### Append and limit the event log

```java
private void appendLog(String message) {
    log.append(message + System.lineSeparator());
    int excess = log.getLineCount() - SimulationConfig.LOG_MAX_LINES - 1;
    if (excess > 0) {
        try {
            log.replaceRange("", 0, log.getLineEndOffset(excess - 1));
        } catch (Exception ignored) {
            log.setText(message + System.lineSeparator());
        }
    }
    log.setCaretPosition(log.getDocument().getLength());
}
```

This prevents unlimited text growth by deleting old lines beyond 500 and moves
the caret to the newest event.

### Toggle buttons

```java
private void toggleBuses() {
    boolean stopped = engine.toggleBusesStopped();
    stopBusesButton.setText(
            stopped ? "Resume Buses" : "Stop Buses");
    // Change button color.
}

private void toggleTicketBooth() {
    boolean open = engine.toggleTicketBooth();
    ticketButton.setText(
            open ? "Close Ticket Booth" : "Open Ticket Booth");
    // Change button color.
}
```

The engine owns the condition; the window only changes visual button feedback.

### Create Passenger dialog flow

```java
private void createPassenger() {
    String type = choose(
            "Create Passenger", "Choose Passenger Type",
            new String[]{"Regular", "Priority"});
    if (type == null) {
        return;
    }
    String destination = chooseDestination("Create Passenger");
    if (destination != null) {
        engine.createPassengerWithLog(
                "Priority".equals(type), destination);
    }
}
```

Cancel exits safely. Otherwise the selected type becomes a boolean and the
engine performs actual creation/enqueueing.

### List passengers

```java
private void showPassengers() {
    DefaultTableModel model = new DefaultTableModel(
            new String[]{"ID", "Destination", "Type", "State", "Assigned Bus"},
            0) {
        @Override
        public boolean isCellEditable(int row, int column) {
            return false;
        }
    };
    for (Person passenger : engine.passengers()) {
        model.addRow(new Object[]{
                passenger.id,
                passenger.destination,
                passenger.isPriority ? "PRIORITY" : "REGULAR",
                passenger.state,
                passenger.assignedBus == null
                        ? "-" : passenger.assignedBus.busId
        });
    }
    // Put the JTable in a scroll pane and show the dialog.
}
```

This is CRUD Read. The override prevents users from editing cells that would not
update the engine.

### Update and delete passenger dialogs

```java
private void updatePassenger() {
    String id = promptId("Enter Passenger ID to update:");
    if (id == null) {
        return;
    }
    if (engine.findPassenger(id) == null) {
        showError("Passenger not found!");
        return;
    }
    String destination = chooseDestination("Update " + id);
    if (destination != null) {
        engine.updatePassengerDestination(id, destination);
    }
}

private void deletePassenger() {
    String id = promptId("Enter Passenger ID to delete:");
    if (id != null && !engine.removePassenger(id)) {
        showError("Passenger not found!");
    }
}
```

The window validates dialog flow; the engine performs safe cleanup.

### Add and delete bus dialogs

```java
private void addBus() {
    String destination = chooseDestination("Add Bus");
    if (destination != null && engine.addBus(destination) == null) {
        showError("No free bays available!");
    }
}

private void deleteBus() {
    List<Bus> buses = engine.buses();
    if (buses.isEmpty()) {
        showError("No buses to delete!");
        return;
    }
    // Build a String[] of IDs and ask the user to choose one.
    // Call engine.removeBus(selected) for safe passenger return.
}
```

The UI handles choices/errors. The engine owns bay rules and deletion cleanup.

### Dialog helper methods

```java
private String chooseDestination(String title) {
    return choose(title, "Choose Destination", DESTINATIONS);
}

private String choose(String title, String message, String[] options) {
    return (String) JOptionPane.showInputDialog(
            this, message, title, JOptionPane.QUESTION_MESSAGE,
            null, options, options[0]);
}

private String promptId(String message) {
    String id = JOptionPane.showInputDialog(this, message);
    return id == null || id.trim().isEmpty() ? null : id.trim();
}

private void showError(String message) {
    JOptionPane.showMessageDialog(
            this, message, "Error", JOptionPane.ERROR_MESSAGE);
}
```

These centralize repeated Swing input and error behavior.

### Main method

```java
public static void main(String[] args) {
    SwingUtilities.invokeLater(
            () -> new TerminalSimulation().setVisible(true));
}
```

Java starts here. `invokeLater` creates and shows Swing UI on the Event Dispatch
Thread.

## 11. `PassengerQueueTest.java`

### Test runner

```java
public static void main(String[] args) {
    runAll();
    System.out.println("PassengerQueueTest: all checks passed");
}

static void runAll() {
    queueUsesLinkedNodesAndFifoOrder();
    removalRepairsFrontMiddleAndRearLinks();
    emptyQueueCanBeReused();
    nullPassengerIsRejected();
}
```

This runs four direct tests of the assigned DSA.

### Node links and FIFO test

```java
queue.enqueue(first);
queue.enqueue(second);
queue.enqueue(third);

PassengerNode front = queue.frontNode();
check(front.passenger == first, "Front node should contain the first passenger");
check(front.next.passenger == second, "First node should link to the second node");
check(front.next.next.passenger == third, "Second node should link to the third node");
check(front.next.next.next == null, "Rear node should point to null");
check(queue.dequeue() == first, "First passenger should leave first");
check(queue.dequeue() == second, "Second passenger should leave second");
check(queue.dequeue() == third, "Third passenger should leave third");
```

This proves the physical node chain and FIFO output order.

### CRUD removal test

```java
check(queue.remove(second), "Middle passenger should be removable");
check(queue.remove(first), "Front passenger should be removable");
check(queue.remove(fourth), "Rear passenger should be removable");
```

This verifies the extra administrative removal repairs each possible link case.

### Empty reuse and null rejection

```java
check(queue.dequeue() == first, "Single passenger should be dequeued");
check(queue.isEmpty(), "Queue should be empty");
queue.enqueue(second);
check(queue.peek() == second, "Queue should work after becoming empty");

try {
    queue.enqueue(null);
} catch (IllegalArgumentException expected) {
    rejected = true;
}
```

These prove front/rear reset after the last node and invalid null data is blocked.

The helper `passenger` creates small model objects; `check` throws
`AssertionError` when a condition is false.

## 12. `SimulationEngineTest.java`

### Main regression runner

```java
public static void main(String[] args) {
    PassengerQueueTest.runAll();
    deletingPassengerClearsSeatAndWorkingList();
    deletingBusReturnsPassengerToPlatform();
    updatingAssignedPassengerRequeuesForNewDestination();
    identifiersAreTrimmedAndCaseInsensitive();
    simulationMaintainsInvariantsOverTime();
    terminalPanelPaintsHeadlessly();
    System.out.println("SimulationEngineTest: all checks passed");
}
```

This includes queue tests plus six integration/rendering scenarios.

### Cleanup scenarios

```java
// Seat a passenger, delete passenger, then check:
check(engine.findPassenger(passenger.id) == null,
        "Deleted passenger remains in working list");
check(!isPassengerOnBus(bus, passenger),
        "Deleted passenger remains in a bus seat");

// Seat a passenger, delete bus, then check:
check(passenger.assignedBus == null,
        "Passenger still references the deleted bus");
check(engine.platformPassengers().contains(passenger),
        "Passenger is missing from the platform");
```

These protect the two major ghost/stranded-passenger fixes.

### Destination and ID scenarios

```java
engine.updatePassengerDestination(passenger.id, "Tagum");
check("Tagum".equals(passenger.destination),
        "Destination was not updated");
check(!isPassengerOnBus(bus, passenger),
        "Updated passenger remains in old bus seat");

check(engine.findPassenger(
        "  " + passenger.id.toLowerCase() + "  ") == passenger,
        "Passenger ID lookup should ignore spaces and case");
```

These protect route cleanup and friendly lookup behavior.

### Long-run invariant test

```java
for (int tick = 0; tick < 7_500; tick++) {
    engine.update(SimulationConfig.FRAME_DELAY_MS);
    if (tick % 100 == 0) {
        checkEngineState(engine);
        for (Bus bus : engine.buses()) {
            check(bus.getPassengerCount() <= bus.capacity,
                    "Bus exceeded its passenger capacity");
        }
    }
}
```

This advances two simulated minutes and repeatedly checks references/capacity.

### Headless drawing test

```java
BufferedImage image = new BufferedImage(
        1050, 620, BufferedImage.TYPE_INT_ARGB);
Graphics2D graphics = image.createGraphics();
try {
    panel.paint(graphics);
} finally {
    graphics.dispose();
}
```

This verifies drawing without opening a real window.

### Test helpers

- `newEngine`: deterministic random seed and silent logger.
- `loadingBus`: creates a bus and skips it directly to loading for fast setup.
- `waitUntilSeated`: waits for a passenger state.
- `advanceUntil`: updates until a condition or maximum tick count.
- `check`: throws assertion failure.
- `isPassengerOnBus`: checks node queue and seat array.
- `checkEngineState`: traverses every boarding node and seat and verifies the
  passenger exists in the master list and points to the correct bus.

## 13. Complete feature traces

### Regular passenger

```text
button/automatic spawn
-> createPassenger
-> Passenger constructor
-> enqueue rear of regular ticket queue
-> positionTicketQueues traverses nodes
-> movePassengers walks person
-> updateTicketLane peeks front
-> ticket timer reaches zero
-> dequeue front
-> sendToPlatform
-> matching bus selects regular passenger
-> enqueue rear of bus boarding queue
-> 400 ms boarding interval
-> dequeue front
-> reserveSeat writes seat array
-> walk to seat
-> SEATED_IN_BUS
-> bus departs
-> removeDepartedBuses removes completed passenger
```

### Priority passenger

```text
enqueue priority ticket queue
-> same FIFO ticket process
-> platform
-> loadBus checks priority first
-> reserveSeat directly
-> skips regular bus boarding queue
```

### Passenger deletion

```text
UI prompt
-> engine.removePassenger
-> findPassenger
-> remove node from ticket queues if present
-> remove platform entry if present
-> remove node from each bus boarding queue if present
-> clear matching seat
-> clear assignedBus
-> remove from master list
-> reposition visible lines
```

### Bus deletion

```text
UI selection
-> engine.removeBus
-> remove active bus
-> dequeue every boarding passenger
-> collect every seat passenger
-> clear seats
-> send still-active passengers to platform
```

## 14. Compile, run, and test

```powershell
New-Item -ItemType Directory -Force out
$sources = (Get-ChildItem src -Filter *.java).FullName
javac --release 8 -encoding UTF-8 -d out $sources
java -cp out TerminalSimulation
```

Tests:

```powershell
$files = @(
    (Get-ChildItem src -Filter *.java).FullName
    (Get-ChildItem test -Filter *.java).FullName
)
javac --release 8 -encoding UTF-8 -d out $files
java -ea -cp out PassengerQueueTest
java -ea -cp out SimulationEngineTest
```

Expected:

```text
PassengerQueueTest: all checks passed
SimulationEngineTest: all checks passed
```

## 15. What to memorize versus understand

Do not memorize drawing coordinates or every color. Understand:

- which class owns each responsibility;
- how front/rear/next implement the queue;
- enqueue, peek, dequeue, and special CRUD removal;
- why ticket and boarding lines are FIFO;
- why platform selection is filtered rather than strict FIFO;
- passenger and bus state changes;
- how cleanup removes every reference;
- how timer update and repaint connect logic to drawing.

When shown any method, answer:

1. What inputs does it receive?
2. What field, queue, list, state, or coordinate does it change?
3. What does it return?
4. Who calls it?
5. What happens next?
