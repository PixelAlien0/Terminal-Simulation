import java.awt.Point;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Iterator;
import java.util.IdentityHashMap;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;
import java.util.function.Consumer;

final class SimulationEngine {
    private final LinkedList<Person> priorityTicketQueue = new LinkedList<Person>();
    private final LinkedList<Person> regularTicketQueue = new LinkedList<Person>();
    private final LinkedList<Person> platformQueue = new LinkedList<Person>();
    private final List<Person> workingPassengers = new ArrayList<Person>();
    private final List<Bus> activeBuses = new ArrayList<Bus>();

    private final Random random;
    private final Consumer<String> logger;

    private int passengerCounter;
    private int busCounter = 1;
    private int passengerSpawnElapsedMs;
    private int busArrivalElapsedMs;
    private int priorityTransactionDelayMs;
    private int regularTransactionDelayMs;
    private boolean busesStopped;
    private boolean ticketBoothOpen = true;

    SimulationEngine(Consumer<String> logger) {
        this(new Random(), logger);
    }

    SimulationEngine(Random random, Consumer<String> logger) {
        this.random = random;
        this.logger = logger == null ? new Consumer<String>() {
            @Override
            public void accept(String message) {
                // Intentionally empty for tests and headless use.
            }
        } : logger;
    }

    void initialize() {
        log("[SYSTEM] Simulation started...");
        for (int index = 0; index < 22; index++) {
            generateAutoPassenger();
        }
    }

    Person createRandomPassenger(boolean priority) {
        return createPassenger(priority, random.nextBoolean() ? "Davao" : "Tagum");
    }

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
        if (priority) {
            priorityTicketQueue.add(passenger);
        } else {
            regularTicketQueue.add(passenger);
        }
        workingPassengers.add(passenger);
        recalculateTicketQueueSpacings();
        return passenger;
    }

    Person createPassengerWithLog(boolean priority, String destination) {
        Person passenger = createPassenger(priority, destination);
        log("[CRUD] Created " + (priority ? "Priority" : "Regular")
                + " passenger " + passenger.id + " → " + passenger.destination);
        return passenger;
    }

    Person findPassenger(String id) {
        String normalizedId = normalizeId(id);
        if (normalizedId == null) {
            return null;
        }
        for (Person passenger : workingPassengers) {
            if (passenger.id.equalsIgnoreCase(normalizedId)) {
                return passenger;
            }
        }
        return null;
    }

    boolean updatePassengerDestination(String id, String destination) {
        Person passenger = findPassenger(id);
        if (passenger == null) {
            return false;
        }

        passenger.destination = normalizeDestination(destination);
        if (detachPassengerFromAllBuses(passenger)) {
            returnPassengerToPlatform(passenger);
        }
        log("[CRUD] Updated " + passenger.id + " destination to " + passenger.destination);
        return true;
    }

    boolean removePassenger(String id) {
        Person passenger = findPassenger(id);
        if (passenger == null) {
            return false;
        }

        priorityTicketQueue.remove(passenger);
        regularTicketQueue.remove(passenger);
        platformQueue.remove(passenger);
        detachPassengerFromAllBuses(passenger);
        workingPassengers.remove(passenger);
        recalculateTicketQueueSpacings();
        recalculatePlatformLoungeSpacings();
        log("[CRUD] Deleted passenger " + passenger.id);
        return true;
    }

    Bus addBus(String destination) {
        List<Integer> freeBays = getFreeBays();
        if (freeBays.isEmpty()) {
            return null;
        }

        int bay = freeBays.get(random.nextInt(freeBays.size()));
        Bus bus = addBusAtBay(destination, bay);
        log("[BUS] Added " + bus.busId + " at Bay " + bay);
        return bus;
    }

    Bus addBusAtBay(String destination, int bay) {
        if (bay < 1 || bay > 4 || isBayOccupied(bay)) {
            return null;
        }

        String normalizedDestination = normalizeDestination(destination);
        String lineName = normalizedDestination.equals("Davao") ? "Davao Exp" : "Tagum Met";
        char bayLabel = bay == 1 || bay == 3 ? 'A' : 'B';
        int targetY = 20 + (bay - 1) * 155;
        Bus bus = new Bus(
                lineName + " " + bayLabel + " (B" + busCounter++ + ")",
                normalizedDestination,
                bay,
                1400,
                targetY
        );
        activeBuses.add(bus);
        return bus;
    }

    Bus findBus(String id) {
        String normalizedId = normalizeId(id);
        if (normalizedId == null) {
            return null;
        }
        for (Bus bus : activeBuses) {
            if (bus.busId.equalsIgnoreCase(normalizedId)) {
                return bus;
            }
        }
        return null;
    }

    boolean removeBus(String id) {
        Bus bus = findBus(id);
        if (bus == null) {
            return false;
        }

        requeuePassengersFromBus(bus);
        activeBuses.remove(bus);
        log("[BUS] Deleted bus: " + bus.busId);
        return true;
    }

    void update(int elapsedMs) {
        updateAutomaticSpawning(elapsedMs);
        updateBuses(elapsedMs);
        priorityTransactionDelayMs = processTicketQueue(
                priorityTicketQueue,
                SimulationConfig.PRIORITY_QUEUE_Y,
                priorityTransactionDelayMs,
                elapsedMs
        );
        regularTransactionDelayMs = processTicketQueue(
                regularTicketQueue,
                SimulationConfig.REGULAR_QUEUE_Y,
                regularTransactionDelayMs,
                elapsedMs
        );
        updatePassengerMovement();
    }

    boolean toggleBusesStopped() {
        busesStopped = !busesStopped;
        log("[SYSTEM] Bus Operations " + (busesStopped ? "Paused." : "Resumed."));
        return busesStopped;
    }

    boolean toggleTicketBooth() {
        ticketBoothOpen = !ticketBoothOpen;
        log("[SYSTEM] Ticket Booth " + (ticketBoothOpen ? "OPEN." : "CLOSED."));
        if (ticketBoothOpen) {
            recalculateTicketQueueSpacings();
        }
        return ticketBoothOpen;
    }

    List<Person> getWorkingPassengers() {
        return Collections.unmodifiableList(workingPassengers);
    }

    List<Person> getPlatformPassengers() {
        return Collections.unmodifiableList(platformQueue);
    }

    List<Bus> getActiveBuses() {
        return Collections.unmodifiableList(activeBuses);
    }

    Person getPriorityTicketClient() {
        return priorityTicketQueue.peek();
    }

    Person getRegularTicketClient() {
        return regularTicketQueue.peek();
    }

    int getPriorityTicketQueueSize() {
        return priorityTicketQueue.size();
    }

    int getRegularTicketQueueSize() {
        return regularTicketQueue.size();
    }

    int getPlatformQueueSize() {
        return platformQueue.size();
    }

    boolean isBusesStopped() {
        return busesStopped;
    }

    boolean isTicketBoothOpen() {
        return ticketBoothOpen;
    }

    List<String> validateInvariants() {
        List<String> errors = new ArrayList<String>();
        Set<Person> knownPassengers = new LinkedHashSet<Person>(workingPassengers);
        Map<Person, Integer> locationCounts = new IdentityHashMap<Person, Integer>();
        validateQueue(priorityTicketQueue, knownPassengers, "priority ticket queue", errors);
        validateQueue(regularTicketQueue, knownPassengers, "regular ticket queue", errors);
        validateQueue(platformQueue, knownPassengers, "platform queue", errors);
        countLocations(priorityTicketQueue, locationCounts);
        countLocations(regularTicketQueue, locationCounts);
        countLocations(platformQueue, locationCounts);

        for (Bus bus : activeBuses) {
            for (Person passenger : bus.boardingLine) {
                validateBusPassenger(passenger, bus, knownPassengers, "boarding line", errors);
                incrementLocation(passenger, locationCounts);
            }
            for (Person passenger : bus.seats) {
                if (passenger != null) {
                    validateBusPassenger(passenger, bus, knownPassengers, "seat", errors);
                    incrementLocation(passenger, locationCounts);
                }
            }
        }
        for (Person passenger : workingPassengers) {
            Integer locations = locationCounts.get(passenger);
            if (locations == null || locations.intValue() != 1) {
                errors.add(passenger.id + " belongs to "
                        + (locations == null ? 0 : locations.intValue())
                        + " simulation locations; expected exactly 1");
            }
        }
        return errors;
    }

    private void generateAutoPassenger() {
        createRandomPassenger(random.nextFloat() < 0.20f);
    }

    private void updateAutomaticSpawning(int elapsedMs) {
        passengerSpawnElapsedMs += elapsedMs;
        if (passengerSpawnElapsedMs >= SimulationConfig.PASSENGER_SPAWN_INTERVAL_MS) {
            passengerSpawnElapsedMs %= SimulationConfig.PASSENGER_SPAWN_INTERVAL_MS;
            if (ticketBoothOpen) {
                generateAutoPassenger();
            }
        }

        if (activeBuses.size() < 4 && !busesStopped) {
            busArrivalElapsedMs += elapsedMs;
            if (busArrivalElapsedMs >= SimulationConfig.BUS_ARRIVAL_INTERVAL_MS) {
                busArrivalElapsedMs %= SimulationConfig.BUS_ARRIVAL_INTERVAL_MS;
                spawnRandomBus();
            }
        }
    }

    private void spawnRandomBus() {
        List<Integer> freeBays = getFreeBays();
        if (freeBays.isEmpty()) {
            return;
        }

        int bay = freeBays.get(random.nextInt(freeBays.size()));
        String destination = bay <= 2 ? "Davao" : "Tagum";
        Bus bus = addBusAtBay(destination, bay);
        log("[BUS] " + bus.busId + " arriving at Bay " + bay);
    }

    private List<Integer> getFreeBays() {
        List<Integer> freeBays = new ArrayList<Integer>();
        for (int bay = 1; bay <= 4; bay++) {
            if (!isBayOccupied(bay)) {
                freeBays.add(bay);
            }
        }
        return freeBays;
    }

    private boolean isBayOccupied(int bay) {
        for (Bus bus : activeBuses) {
            if (bus.bayId == bay) {
                return true;
            }
        }
        return false;
    }

    private void updateBuses(int elapsedMs) {
        for (Bus bus : activeBuses) {
            if (busesStopped) {
                continue;
            }

            if (bus.state == BusState.ARRIVING) {
                bus.x -= 8;
                if (bus.x <= 620) {
                    bus.x = 620;
                    bus.state = BusState.LOADING;
                }
            } else if (bus.state == BusState.LOADING) {
                updateLoadingBus(bus, elapsedMs);
            } else if (bus.state == BusState.WAITING_FOR_DEPARTURE) {
                bus.departureBufferMs -= elapsedMs;
                if (bus.departureBufferMs <= 0) {
                    bus.state = BusState.DEPARTING;
                    log("[BUS] " + bus.busId + " departing with "
                            + bus.getPassengerCount() + " passengers.");
                }
            } else if (bus.state == BusState.DEPARTING) {
                moveDepartingBus(bus);
            }
        }
        removeDepartedBuses();
    }

    private void updateLoadingBus(Bus bus, int elapsedMs) {
        moveOnePriorityPassengerToBus(bus);
        moveOneRegularPassengerToLine(bus);

        if (!bus.boardingLine.isEmpty() && !bus.isFull()) {
            bus.boardingElapsedMs += elapsedMs;
            if (bus.boardingElapsedMs >= SimulationConfig.BOARDING_INTERVAL_MS) {
                Person boarder = bus.boardingLine.remove(0);
                reserveSeat(bus, boarder);
                updateBusBayLineCoordinates(bus);
                bus.boardingElapsedMs = 0;
            }
        }

        if (!bus.countdownStarted) {
            for (Person passenger : bus.seats) {
                if (passenger != null && passenger.state == PassengerState.SEATED_IN_BUS) {
                    bus.countdownStarted = true;
                    break;
                }
            }
        }
        if (bus.countdownStarted) {
            bus.countdownMs = Math.max(0, bus.countdownMs - elapsedMs);
        }

        if (bus.isFull() || bus.countdownStarted && bus.countdownMs <= 0) {
            List<Person> waitingPassengers = new ArrayList<Person>(bus.boardingLine);
            bus.boardingLine.clear();
            for (Person passenger : waitingPassengers) {
                passenger.assignedBus = null;
                returnPassengerToPlatform(passenger);
            }
            recalculatePlatformLoungeSpacings();
            bus.state = BusState.WAITING_FOR_DEPARTURE;
            log("[BUS] " + bus.busId + " closed doors. Preparing to depart.");
        }
    }

    private void moveOnePriorityPassengerToBus(Bus bus) {
        if (bus.isFull()) {
            return;
        }
        for (int index = 0; index < platformQueue.size(); index++) {
            Person passenger = platformQueue.get(index);
            if (passenger.isPriority
                    && passenger.state == PassengerState.WAITING_ON_PLATFORM
                    && bus.destination.equals(passenger.destination)) {
                platformQueue.remove(index);
                reserveSeat(bus, passenger);
                recalculatePlatformLoungeSpacings();
                return;
            }
        }
    }

    private void moveOneRegularPassengerToLine(Bus bus) {
        if (bus.boardingLine.size() >= bus.capacity || bus.isFull()) {
            return;
        }
        for (int index = 0; index < platformQueue.size(); index++) {
            Person passenger = platformQueue.get(index);
            if (!passenger.isPriority
                    && passenger.state == PassengerState.WAITING_ON_PLATFORM
                    && bus.destination.equals(passenger.destination)) {
                platformQueue.remove(index);
                passenger.state = PassengerState.MOVING_TO_BAY_LINE;
                passenger.assignedBus = bus;
                bus.boardingLine.add(passenger);
                updateBusBayLineCoordinates(bus);
                recalculatePlatformLoungeSpacings();
                return;
            }
        }
    }

    private void reserveSeat(Bus bus, Person passenger) {
        int seatIndex = bus.getRandomEmptySeat(random);
        if (seatIndex < 0) {
            returnPassengerToPlatform(passenger);
            return;
        }

        passenger.state = PassengerState.WALKING_TO_BUS;
        passenger.assignedBus = bus;
        bus.seats[seatIndex] = passenger;
        Point seat = bus.getSeatCoordinate(seatIndex);
        passenger.setTarget(seat.x, seat.y);
    }

    private void moveDepartingBus(Bus bus) {
        bus.x += 6;
        for (int seatIndex = 0; seatIndex < bus.seats.length; seatIndex++) {
            Person passenger = bus.seats[seatIndex];
            if (passenger != null) {
                Point seat = bus.getSeatCoordinate(seatIndex);
                passenger.x = seat.x;
                passenger.y = seat.y;
            }
        }
        if (bus.x > SimulationConfig.WINDOW_WIDTH + 200) {
            bus.state = BusState.DEPARTED;
        }
    }

    private void removeDepartedBuses() {
        Iterator<Bus> iterator = activeBuses.iterator();
        while (iterator.hasNext()) {
            Bus bus = iterator.next();
            if (bus.state != BusState.DEPARTED) {
                continue;
            }

            for (Person passenger : bus.seats) {
                if (passenger != null) {
                    passenger.assignedBus = null;
                    workingPassengers.remove(passenger);
                }
            }
            Arrays.fill(bus.seats, null);
            iterator.remove();
        }
    }

    private int processTicketQueue(
            LinkedList<Person> queue,
            int queueY,
            int transactionDelayMs,
            int elapsedMs) {
        if (transactionDelayMs > 0) {
            return Math.max(0, transactionDelayMs - elapsedMs);
        }
        if (!ticketBoothOpen || queue.isEmpty()) {
            return transactionDelayMs;
        }

        Person passenger = queue.peek();
        if (passenger.state == PassengerState.WAITING_FOR_TICKET) {
            passenger.setTarget(SimulationConfig.TICKET_BOOTH_X, queueY);
        }
        if (passenger.x == SimulationConfig.TICKET_BOOTH_X
                && passenger.y == queueY
                && passenger.state != PassengerState.BUYING_TICKET) {
            passenger.state = PassengerState.BUYING_TICKET;
            passenger.ticketTimerMs = SimulationConfig.TICKET_SERVICE_MS;
        }
        if (passenger.state == PassengerState.BUYING_TICKET) {
            passenger.ticketTimerMs = Math.max(0, passenger.ticketTimerMs - elapsedMs);
            if (passenger.ticketTimerMs == 0) {
                queue.poll();
                passenger.state = PassengerState.WALKING_TO_PLATFORM;
                platformQueue.add(passenger);
                recalculatePlatformLoungeSpacings();
                recalculateTicketQueueSpacings();
                return SimulationConfig.TICKET_TRANSACTION_DELAY_MS;
            }
        }
        return transactionDelayMs;
    }

    private void updatePassengerMovement() {
        for (Person passenger : workingPassengers) {
            if (!passenger.stepTowardTarget()) {
                continue;
            }

            if (passenger.state == PassengerState.WALKING_TO_TICKET) {
                passenger.state = PassengerState.WAITING_FOR_TICKET;
            } else if (passenger.state == PassengerState.WALKING_TO_PLATFORM) {
                passenger.state = PassengerState.WAITING_ON_PLATFORM;
            } else if (passenger.state == PassengerState.MOVING_TO_BAY_LINE) {
                passenger.state = PassengerState.WAITING_IN_BAY_LINE;
            } else if (passenger.state == PassengerState.WALKING_TO_BUS) {
                passenger.state = PassengerState.SEATED_IN_BUS;
            }
        }
    }

    private void requeuePassengersFromBus(Bus bus) {
        Set<Person> affected = new LinkedHashSet<Person>(bus.boardingLine);
        for (Person passenger : bus.seats) {
            if (passenger != null) {
                affected.add(passenger);
            }
        }

        bus.boardingLine.clear();
        Arrays.fill(bus.seats, null);
        for (Person passenger : affected) {
            passenger.assignedBus = null;
            if (workingPassengers.contains(passenger)) {
                returnPassengerToPlatform(passenger);
            }
        }
        recalculatePlatformLoungeSpacings();
    }

    private boolean detachPassengerFromAllBuses(Person passenger) {
        boolean detached = passenger.assignedBus != null;
        for (Bus bus : activeBuses) {
            if (bus.boardingLine.remove(passenger)) {
                detached = true;
                updateBusBayLineCoordinates(bus);
            }
            for (int seatIndex = 0; seatIndex < bus.seats.length; seatIndex++) {
                if (bus.seats[seatIndex] == passenger) {
                    bus.seats[seatIndex] = null;
                    detached = true;
                }
            }
        }
        passenger.assignedBus = null;
        return detached;
    }

    private void returnPassengerToPlatform(Person passenger) {
        priorityTicketQueue.remove(passenger);
        regularTicketQueue.remove(passenger);
        if (!platformQueue.contains(passenger)) {
            platformQueue.add(passenger);
        }
        passenger.assignedBus = null;
        passenger.state = PassengerState.WALKING_TO_PLATFORM;
        recalculatePlatformLoungeSpacings();
    }

    private void recalculateTicketQueueSpacings() {
        positionTicketQueue(priorityTicketQueue, SimulationConfig.PRIORITY_QUEUE_Y, -1);
        positionTicketQueue(regularTicketQueue, SimulationConfig.REGULAR_QUEUE_Y, 1);
    }

    private void positionTicketQueue(List<Person> queue, int baseY, int rowDirection) {
        int visibleColumns = 7;
        int queueIndex = 0;
        for (Person passenger : queue) {
            if (passenger.state != PassengerState.WALKING_TO_TICKET
                    && passenger.state != PassengerState.WAITING_FOR_TICKET
                    && passenger.state != PassengerState.BUYING_TICKET) {
                continue;
            }

            int column = queueIndex % visibleColumns;
            int row = queueIndex / visibleColumns;
            int x = SimulationConfig.TICKET_BOOTH_X
                    - column * SimulationConfig.TICKET_QUEUE_SPACING;
            int y = baseY + rowDirection * row * 18;
            passenger.setTarget(x, y);
            queueIndex++;
        }
    }

    private void recalculatePlatformLoungeSpacings() {
        int index = 0;
        int columnsPerBay = 10;
        int rowsPerBay = 4;
        int slotsPerBay = columnsPerBay * rowsPerBay;
        int totalSlots = slotsPerBay * 4;

        for (Person passenger : platformQueue) {
            if (passenger.state != PassengerState.WAITING_ON_PLATFORM
                    && passenger.state != PassengerState.WALKING_TO_PLATFORM) {
                continue;
            }

            int slot = index % totalSlots;
            int bayGroup = slot / slotsPerBay;
            int slotWithinBay = slot % slotsPerBay;
            int x = 280 + slotWithinBay % columnsPerBay * 24;
            int y = 35 + bayGroup * 155 + slotWithinBay / columnsPerBay * 18;
            passenger.setTarget(x, y);
            index++;
        }
    }

    private void updateBusBayLineCoordinates(Bus bus) {
        int lineStartX = bus.x - 30;
        int lineY = bus.y + 45;
        for (int index = 0; index < bus.boardingLine.size(); index++) {
            bus.boardingLine.get(index).setTarget(lineStartX - index * 18, lineY);
        }
    }

    private void validateQueue(
            List<Person> queue,
            Set<Person> knownPassengers,
            String queueName,
            List<String> errors) {
        for (Person passenger : queue) {
            if (!knownPassengers.contains(passenger)) {
                errors.add(passenger.id + " is in the " + queueName + " but not the working list");
            }
        }
    }

    private void validateBusPassenger(
            Person passenger,
            Bus bus,
            Set<Person> knownPassengers,
            String location,
            List<String> errors) {
        if (!knownPassengers.contains(passenger)) {
            errors.add(passenger.id + " is in a bus " + location + " but not the working list");
        }
        if (passenger.assignedBus != bus) {
            errors.add(passenger.id + " has an incorrect " + location + " assignment");
        }
    }

    private void countLocations(
            List<Person> passengers,
            Map<Person, Integer> locationCounts) {
        for (Person passenger : passengers) {
            incrementLocation(passenger, locationCounts);
        }
    }

    private void incrementLocation(
            Person passenger,
            Map<Person, Integer> locationCounts) {
        Integer current = locationCounts.get(passenger);
        locationCounts.put(passenger, current == null ? 1 : current + 1);
    }

    private String normalizeDestination(String destination) {
        if ("Davao".equalsIgnoreCase(destination)) {
            return "Davao";
        }
        if ("Tagum".equalsIgnoreCase(destination)) {
            return "Tagum";
        }
        throw new IllegalArgumentException("Unsupported destination: " + destination);
    }

    private String normalizeId(String id) {
        if (id == null) {
            return null;
        }
        String normalized = id.trim();
        return normalized.isEmpty() ? null : normalized;
    }

    private void log(String message) {
        logger.accept(message);
    }
}
