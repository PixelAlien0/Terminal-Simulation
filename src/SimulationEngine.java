import java.awt.Point;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Random;
import java.util.Set;
import java.util.function.Consumer;

final class SimulationEngine {
    private static final class TicketLane {
        final LinkedList<Person> queue = new LinkedList<Person>();
        final int y;
        final int rowDirection;
        int delayMs;

        TicketLane(int y, int rowDirection) {
            this.y = y;
            this.rowDirection = rowDirection;
        }
    }

    private final TicketLane priorityLane =
            new TicketLane(SimulationConfig.PRIORITY_QUEUE_Y, -1);
    private final TicketLane regularLane =
            new TicketLane(SimulationConfig.REGULAR_QUEUE_Y, 1);
    private final TicketLane[] ticketLanes = {priorityLane, regularLane};
    private final LinkedList<Person> platform = new LinkedList<Person>();
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

    SimulationEngine(Consumer<String> logger) {
        this(new Random(), logger);
    }

    SimulationEngine(Random random, Consumer<String> logger) {
        this.random = random;
        this.logger = logger == null ? message -> { } : logger;
    }

    void initialize() {
        log("[SYSTEM] Simulation started...");
        for (int index = 0; index < 22; index++) {
            createRandomPassenger(random.nextFloat() < 0.20f);
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
        lane(priority).queue.add(passenger);
        passengers.add(passenger);
        positionTicketQueues();
        return passenger;
    }

    Person createPassengerWithLog(boolean priority, String destination) {
        Person passenger = createPassenger(priority, destination);
        log("[CRUD] Created " + (priority ? "Priority" : "Regular")
                + " passenger " + passenger.id + " → " + passenger.destination);
        return passenger;
    }

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

    boolean updatePassengerDestination(String id, String destination) {
        Person passenger = findPassenger(id);
        if (passenger == null) {
            return false;
        }
        passenger.destination = normalizeDestination(destination);
        if (detachFromBuses(passenger)) {
            sendToPlatform(passenger);
        }
        log("[CRUD] Updated " + passenger.id + " destination to " + passenger.destination);
        return true;
    }

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

    void update(int elapsedMs) {
        updateSpawning(elapsedMs);
        updateBuses(elapsedMs);
        for (TicketLane ticketLane : ticketLanes) {
            updateTicketLane(ticketLane, elapsedMs);
        }
        movePassengers();
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
            positionTicketQueues();
        }
        return ticketBoothOpen;
    }

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

    private TicketLane lane(boolean priority) {
        return priority ? priorityLane : regularLane;
    }

    private void updateSpawning(int elapsedMs) {
        passengerSpawnMs += elapsedMs;
        if (passengerSpawnMs >= SimulationConfig.PASSENGER_SPAWN_INTERVAL_MS) {
            passengerSpawnMs %= SimulationConfig.PASSENGER_SPAWN_INTERVAL_MS;
            if (ticketBoothOpen && passengers.size() < SimulationConfig.MAX_PASSENGERS) {
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
                bus.boardingLine.add(regular);
                positionBoardingLine(bus);
            }
        }

        if (!bus.boardingLine.isEmpty() && !bus.isFull()) {
            bus.boardingElapsedMs += elapsedMs;
            if (bus.boardingElapsedMs >= SimulationConfig.BOARDING_INTERVAL_MS) {
                reserveSeat(bus, bus.boardingLine.remove(0));
                positionBoardingLine(bus);
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
        } else {
            bus.countdownMs = Math.max(0, bus.countdownMs - elapsedMs);
        }

        if (bus.isFull() || bus.countdownStarted && bus.countdownMs == 0) {
            List<Person> waiting = new ArrayList<Person>(bus.boardingLine);
            bus.boardingLine.clear();
            for (Person passenger : waiting) {
                sendToPlatform(passenger);
            }
            bus.state = BusState.WAITING_FOR_DEPARTURE;
            log("[BUS] " + bus.busId + " closed doors. Preparing to depart.");
        }
    }

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
            passenger.ticketTimerMs = Math.max(0, passenger.ticketTimerMs - elapsedMs);
            if (passenger.ticketTimerMs == 0) {
                ticketLane.queue.remove();
                sendToPlatform(passenger);
                positionTicketQueues();
                ticketLane.delayMs = SimulationConfig.TICKET_TRANSACTION_DELAY_MS;
            }
        }
    }

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

    private void returnBusPassengers(Bus bus) {
        Set<Person> returning = new LinkedHashSet<Person>(bus.boardingLine);
        for (Person passenger : bus.seats) {
            if (passenger != null) {
                returning.add(passenger);
            }
        }
        bus.boardingLine.clear();
        Arrays.fill(bus.seats, null);
        for (Person passenger : returning) {
            if (passengers.contains(passenger)) {
                sendToPlatform(passenger);
            }
        }
    }

    private void sendToPlatform(Person passenger) {
        removeFromQueues(passenger);
        if (!platform.contains(passenger)) {
            platform.add(passenger);
        }
        passenger.assignedBus = null;
        passenger.state = PassengerState.WALKING_TO_PLATFORM;
        positionPlatform();
    }

    private void positionTicketQueues() {
        for (TicketLane ticketLane : ticketLanes) {
            int index = 0;
            for (Person passenger : ticketLane.queue) {
                int column = index % 7;
                int row = index / 7;
                passenger.setTarget(
                        SimulationConfig.TICKET_BOOTH_X
                                - column * SimulationConfig.TICKET_QUEUE_SPACING,
                        ticketLane.y + ticketLane.rowDirection * row * 18
                );
                index++;
            }
        }
    }

    private void positionPlatform() {
        int index = 0;
        for (Person passenger : platform) {
            int slot = index % SimulationConfig.MAX_PASSENGERS;
            int bay = slot / 40;
            int withinBay = slot % 40;
            passenger.setTarget(280 + withinBay % 10 * 24, 35 + bay * 155 + withinBay / 10 * 18);
            index++;
        }
    }

    private void positionBoardingLine(Bus bus) {
        for (int index = 0; index < bus.boardingLine.size(); index++) {
            bus.boardingLine.get(index).setTarget(bus.x - 30 - index * 18, bus.y + 45);
        }
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

    private String cleanId(String id) {
        if (id == null || id.trim().isEmpty()) {
            return null;
        }
        return id.trim();
    }

    private void log(String message) {
        logger.accept(message);
    }
}
