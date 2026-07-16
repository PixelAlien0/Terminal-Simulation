// Single-file version: all project classes are kept in this source file.
// Only TerminalSimulation is public because Java permits one public top-level class per file.

import java.awt.BasicStroke;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.event.ActionListener;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.RenderingHints;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.function.Consumer;
import java.util.Iterator;
import java.util.List;
import java.util.Random;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextArea;
import javax.swing.SwingUtilities;
import javax.swing.table.DefaultTableModel;
import javax.swing.Timer;

// -----------------------------------------------------------------------------
// TerminalSimulation.java
// -----------------------------------------------------------------------------
@SuppressWarnings("serial")
public final class TerminalSimulation extends JFrame {
    private static final String[] DESTINATIONS = {"Davao", "Tagum"};
    private final JTextArea log = new JTextArea(10, 30);
    private final SimulationEngine engine;
    private final TerminalPanel terminalPanel;
    private JButton stopBusesButton;
    private JButton ticketButton;
    private long lastFrameNanos;
    private int accumulatedMs;

    public TerminalSimulation() {
        super("Terminal Simulation - Pixel Art Edition");
        log.setEditable(false);
        log.setBackground(new Color(40, 40, 40));
        log.setForeground(new Color(220, 220, 220));
        log.setFont(new Font("Monospaced", Font.PLAIN, 12));

        engine = new SimulationEngine(this::appendLog);
        terminalPanel = new TerminalPanel(engine);
        JScrollPane logPane = new JScrollPane(log);
        logPane.setPreferredSize(new Dimension(300, 0));

        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLayout(new BorderLayout());
        add(terminalPanel, BorderLayout.CENTER);
        add(logPane, BorderLayout.EAST);
        add(createControls(), BorderLayout.SOUTH);
        setSize(SimulationConfig.WINDOW_WIDTH, SimulationConfig.WINDOW_HEIGHT);
        setMinimumSize(new Dimension(1000, 650));
        setLocationRelativeTo(null);

        engine.initialize();
        lastFrameNanos = System.nanoTime();
        new Timer(SimulationConfig.FRAME_DELAY_MS, event -> updateFrame()).start();
    }

    private JPanel createControls() {
        JPanel controls = new JPanel(new FlowLayout(FlowLayout.CENTER, 15, 10));
        controls.setBackground(new Color(30, 30, 30));
        controls.add(button("➕ Regular Passenger", new Color(60, 100, 60),
                event -> engine.createRandomPassenger(false)));
        controls.add(button("⭐ Priority Passenger", new Color(100, 50, 150),
                event -> engine.createRandomPassenger(true)));

        stopBusesButton = button("🛑 Stop Buses", new Color(180, 60, 60),
                event -> toggleBuses());
        ticketButton = button("🚫 Close Ticket Booth", new Color(200, 100, 50),
                event -> toggleTicketBooth());
        controls.add(stopBusesButton);
        controls.add(ticketButton);
        controls.add(button("Create Passenger", new Color(0, 150, 0),
                event -> createPassenger()));
        controls.add(button("List Passengers", new Color(0, 100, 200),
                event -> showPassengers()));
        controls.add(button("Update Passenger", new Color(200, 150, 0),
                event -> updatePassenger()));
        controls.add(button("Delete Passenger", new Color(200, 50, 50),
                event -> deletePassenger()));
        controls.add(button("Add Bus", new Color(0, 120, 200), event -> addBus()));
        controls.add(button("Delete Bus", new Color(200, 60, 60), event -> deleteBus()));
        return controls;
    }

    private JButton button(String text, Color color, ActionListener action) {
        JButton button = new JButton(text);
        button.setBackground(color);
        button.setForeground(Color.WHITE);
        button.setFont(new Font("Monospaced", Font.BOLD, 12));
        button.setFocusPainted(false);
        button.setBorder(BorderFactory.createLineBorder(Color.WHITE, 2));
        button.addActionListener(action);
        return button;
    }

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

    private void toggleBuses() {
        boolean stopped = engine.toggleBusesStopped();
        stopBusesButton.setText(stopped ? "▶ Resume Buses" : "🛑 Stop Buses");
        stopBusesButton.setBackground(
                stopped ? new Color(200, 150, 50) : new Color(180, 60, 60));
    }

    private void toggleTicketBooth() {
        boolean open = engine.toggleTicketBooth();
        ticketButton.setText(open ? "🚫 Close Ticket Booth" : "✅ Open Ticket Booth");
        ticketButton.setBackground(
                open ? new Color(200, 100, 50) : new Color(60, 180, 80));
    }

    private void createPassenger() {
        String type = choose("Create Passenger", "Choose Passenger Type",
                new String[]{"Regular", "Priority"});
        if (type == null) {
            return;
        }
        String destination = chooseDestination("Create Passenger");
        if (destination != null) {
            engine.createPassengerWithLog("Priority".equals(type), destination);
        }
    }

    private void showPassengers() {
        DefaultTableModel model = new DefaultTableModel(
                new String[]{"ID", "Destination", "Type", "State", "Assigned Bus"}, 0) {
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
                    passenger.assignedBus == null ? "—" : passenger.assignedBus.busId
            });
        }
        JTable table = new JTable(model);
        table.setAutoCreateRowSorter(true);
        JScrollPane pane = new JScrollPane(table);
        pane.setPreferredSize(new Dimension(720, 420));
        JOptionPane.showMessageDialog(this, pane, "Current Passengers",
                JOptionPane.INFORMATION_MESSAGE);
    }

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
        String[] ids = new String[buses.size()];
        for (int index = 0; index < ids.length; index++) {
            ids[index] = buses.get(index).busId;
        }
        String selected = choose("Delete Bus",
                "Passengers on the deleted bus will return to the platform.", ids);
        if (selected != null) {
            engine.removeBus(selected);
        }
    }

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

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new TerminalSimulation().setVisible(true));
    }
}

// -----------------------------------------------------------------------------
// SimulationConfig.java
// -----------------------------------------------------------------------------
final class SimulationConfig {
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

    private SimulationConfig() {
    }
}

// -----------------------------------------------------------------------------
// PassengerState.java
// -----------------------------------------------------------------------------
enum PassengerState {
    WALKING_TO_TICKET, WAITING_FOR_TICKET, BUYING_TICKET,
    WALKING_TO_PLATFORM, WAITING_ON_PLATFORM,
    MOVING_TO_BAY_LINE, WAITING_IN_BAY_LINE,
    WALKING_TO_BUS, SEATED_IN_BUS
}

// -----------------------------------------------------------------------------
// BusState.java
// -----------------------------------------------------------------------------
enum BusState {
    ARRIVING, LOADING, WAITING_FOR_DEPARTURE, DEPARTING, DEPARTED
}

// -----------------------------------------------------------------------------
// PassengerNode.java
// -----------------------------------------------------------------------------
final class PassengerNode {
    final Person passenger;
    PassengerNode next;

    PassengerNode(Person passenger) {
        this.passenger = passenger;
    }
}

// -----------------------------------------------------------------------------
// PassengerQueue.java
// -----------------------------------------------------------------------------
final class PassengerQueue {
    private PassengerNode front;
    private PassengerNode rear;
    private int size;

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

    Person peek() {
        return front == null ? null : front.passenger;
    }

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

    boolean isEmpty() {
        return front == null;
    }

    int size() {
        return size;
    }

    PassengerNode frontNode() {
        return front;
    }
}

// -----------------------------------------------------------------------------
// Person.java
// -----------------------------------------------------------------------------
final class Person {
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

    Person(String id, String destination, int startX, int startY, boolean isPriority, int arrivalOrder) {
        this.id = id;
        this.destination = destination;
        this.x = startX;
        this.y = startY;
        this.targetX = startX;
        this.targetY = startY;
        this.isPriority = isPriority;
        this.arrivalOrder = arrivalOrder;
    }

    void setTarget(int targetX, int targetY) {
        this.targetX = targetX;
        this.targetY = targetY;
    }

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

    void draw(Graphics2D graphics) {
        int bob = animationFrame % 2 == 0 ? 1 : 0;
        int renderY = y + bob;
        Color skin = new Color(210, 160, 120);
        Color hair = isPriority ? new Color(200, 180, 50) : new Color(170, 50, 50);
        Color shirt = isPriority ? new Color(100, 50, 150) : new Color(60, 100, 60);
        Color pants = new Color(50, 50, 80);

        graphics.setColor(new Color(0, 0, 0, 60));
        graphics.fillOval(x - 6, y + 12, 12, 6);

        graphics.setColor(pants);
        int leg = animationFrame % 4;
        int legOffset = leg == 0 || leg == 2 ? 2 : -2;
        graphics.fillRect(x - 4, renderY + 8, 3, 5 + legOffset);
        graphics.fillRect(x + 1, renderY + 8, 3, 5 - legOffset);

        graphics.setColor(shirt);
        graphics.fillRect(x - 5, renderY, 10, 9);
        graphics.setColor(Color.BLACK);
        graphics.drawRect(x - 5, renderY, 10, 9);

        graphics.setColor(Color.WHITE);
        graphics.setFont(new Font("SansSerif", Font.BOLD, 8));
        String number = String.valueOf(arrivalOrder);
        graphics.drawString(number, x + (number.length() > 1 ? -4 : -2), renderY + 8);

        graphics.setColor(skin);
        graphics.fillRect(x - 4, renderY - 8, 8, 8);
        graphics.setColor(Color.BLACK);
        graphics.drawRect(x - 4, renderY - 8, 8, 8);

        graphics.setColor(hair);
        graphics.fillRect(x - 5, renderY - 10, 10, 4);
        graphics.fillRect(x - 6, renderY - 8, 3, 4);
        graphics.fillRect(x + 3, renderY - 8, 3, 4);

        graphics.setFont(new Font("Monospaced", Font.BOLD, 10));
        if (isPriority) {
            graphics.setColor(Color.YELLOW);
            graphics.drawString("★", x - 6, y - 12);
        }
        graphics.setColor(Color.WHITE);
        graphics.drawString(destination.substring(0, 1), x + 2, y - 12);

        if (state == PassengerState.BUYING_TICKET) {
            graphics.setColor(Color.BLACK);
            graphics.drawRect(x - 8, renderY - 20, 16, 4);
            graphics.setColor(Color.YELLOW);
            double completed = 1.0 - ticketTimerMs / (double) SimulationConfig.TICKET_SERVICE_MS;
            int fill = (int) Math.round(14 * Math.max(0.0, Math.min(1.0, completed)));
            graphics.fillRect(x - 7, renderY - 19, fill, 3);
        }
    }
}

// -----------------------------------------------------------------------------
// Bus.java
// -----------------------------------------------------------------------------
final class Bus {
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

    private static final int BUS_WIDTH = 180;
    private static final int BUS_HEIGHT = 80;

    Bus(String busId, String destination, int bayId, int startX, int startY) {
        this.busId = busId;
        this.destination = destination;
        this.bayId = bayId;
        this.x = startX;
        this.y = startY;
    }

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

    boolean isFull() {
        return getPassengerCount() == capacity;
    }

    int getPassengerCount() {
        int count = 0;
        for (Person passenger : seats) {
            if (passenger != null) {
                count++;
            }
        }
        return count;
    }

    Point getSeatCoordinate(int seatIndex) {
        int column = seatIndex / 4;
        int row = seatIndex % 4;
        return new Point(x + 15 + column * 30, y + 15 + row * 15);
    }

    void draw(Graphics2D graphics) {
        if (state == BusState.DEPARTED) {
            return;
        }

        Color body = destination.equals("Tagum") ? new Color(34, 139, 34) : new Color(30, 80, 160);
        Color trim = destination.equals("Tagum") ? new Color(20, 80, 20) : new Color(15, 40, 80);
        Color stripe = destination.equals("Tagum") ? new Color(255, 215, 0) : new Color(210, 210, 210);
        Color windowBlue = new Color(109, 165, 255);

        graphics.setColor(new Color(0, 0, 0, 50));
        graphics.fillOval(x, y + BUS_HEIGHT - 10, BUS_WIDTH, 20);
        graphics.setColor(new Color(30, 30, 30));
        graphics.fillRoundRect(x + 20, y + BUS_HEIGHT - 10, 24, 24, 8, 8);
        graphics.fillRoundRect(x + 50, y + BUS_HEIGHT - 10, 24, 24, 8, 8);
        graphics.fillRoundRect(x + BUS_WIDTH - 45, y + BUS_HEIGHT - 10, 24, 24, 8, 8);

        graphics.setColor(body);
        graphics.fillRoundRect(x, y, BUS_WIDTH, BUS_HEIGHT, 10, 10);
        graphics.setColor(Color.BLACK);
        graphics.setStroke(new BasicStroke(3));
        graphics.drawRoundRect(x, y, BUS_WIDTH, BUS_HEIGHT, 10, 10);
        graphics.setStroke(new BasicStroke(1));

        graphics.setColor(trim);
        graphics.fillRect(x + 2, y + BUS_HEIGHT - 20, BUS_WIDTH - 4, 18);
        graphics.setColor(stripe);
        graphics.fillRect(x + 2, y + BUS_HEIGHT - 32, BUS_WIDTH - 4, 12);

        graphics.setColor(windowBlue);
        for (int index = 0; index < 4; index++) {
            graphics.fillRect(x + 10 + index * 30, y + 15, 25, 25);
            graphics.setColor(Color.BLACK);
            graphics.drawRect(x + 10 + index * 30, y + 15, 25, 25);
            graphics.setColor(windowBlue);
        }

        graphics.setColor(Color.WHITE);
        graphics.fillRect(x + 135, y + 15, 30, 45);
        graphics.setColor(Color.BLACK);
        graphics.drawRect(x + 135, y + 15, 30, 45);
        graphics.setColor(windowBlue);
        graphics.fillRect(x + 140, y + 20, 20, 20);
        graphics.setColor(Color.BLACK);
        graphics.drawRect(x + 140, y + 20, 20, 20);

        graphics.setColor(Color.WHITE);
        graphics.setFont(new Font("Monospaced", Font.BOLD, 12));
        graphics.drawString(busId + " [" + getPassengerCount() + "/" + capacity + "]", x + 5, y - 10);

        if (state == BusState.LOADING && countdownStarted) {
            graphics.setColor(Color.GREEN);
            int secondsLeft = (int) Math.ceil(countdownMs / 1000.0);
            graphics.drawString("⏱ " + secondsLeft + "s", x + 5, y - 25);
        }
    }
}

// -----------------------------------------------------------------------------
// SimulationEngine.java
// -----------------------------------------------------------------------------
final class SimulationEngine {
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
        lane(priority).queue.enqueue(passenger);
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
                if (passenger != null && passenger.state == PassengerState.SEATED_IN_BUS) {
                    bus.countdownStarted = true;
                    break;
                }
            }
        } else {
            bus.countdownMs = Math.max(0, bus.countdownMs - elapsedMs);
        }

        if (bus.isFull() || bus.countdownStarted && bus.countdownMs == 0) {
            while (!bus.boardingLine.isEmpty()) {
                sendToPlatform(bus.boardingLine.dequeue());
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
                ticketLane.queue.dequeue();
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
        int index = 0;
        PassengerNode current = bus.boardingLine.frontNode();
        while (current != null) {
            current.passenger.setTarget(bus.x - 30 - index * 18, bus.y + 45);
            current = current.next;
            index++;
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

// -----------------------------------------------------------------------------
// TerminalPanel.java
// -----------------------------------------------------------------------------
@SuppressWarnings("serial")
final class TerminalPanel extends JPanel {
    private final SimulationEngine engine;
    private final List<EnvironmentDecoration> decorations =
            new ArrayList<EnvironmentDecoration>();

    TerminalPanel(SimulationEngine engine) {
        this.engine = engine;
        setBackground(new Color(148, 128, 73));
        setPreferredSize(new Dimension(1050, 620));
        generateDecorations();
    }

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

            List<Person> passengers = new ArrayList<Person>(engine.passengers());
            passengers.sort(Comparator.comparingInt(passenger -> passenger.y));
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

    private void generateDecorations() {
        Random random = new Random();
        for (int bay = 0; bay < 4; bay++) {
            int y = 30 + bay * 155;
            decorations.add(new EnvironmentDecoration("TrashCan", 500, y + 20));
        }
        for (int index = 0; index < 20; index++) {
            decorations.add(new EnvironmentDecoration(
                    "Flower",
                    random.nextInt(230),
                    random.nextInt(620)
            ));
        }
    }

    private void drawTerminalBackground(Graphics2D canvas) {
        canvas.setColor(new Color(81, 81, 81));
        canvas.fillRect(600, 0, Math.max(0, getWidth() - 600), getHeight());

        canvas.setColor(Color.WHITE);
        canvas.setStroke(new BasicStroke(
                4,
                BasicStroke.CAP_BUTT,
                BasicStroke.JOIN_MITER,
                10,
                new float[]{20, 20},
                0
        ));
        for (int bay = 0; bay < 4; bay++) {
            canvas.drawLine(600, 100 + bay * 155, getWidth(), 100 + bay * 155);
        }
        canvas.setStroke(new BasicStroke(1));

        canvas.setColor(new Color(191, 143, 94));
        canvas.fillRect(250, 0, 350, getHeight());
        canvas.setColor(new Color(160, 110, 60));
        for (int y = 0; y < getHeight(); y += 30) {
            canvas.drawLine(250, y, 600, y);
        }
        canvas.drawLine(600, 0, 600, getHeight());

        for (EnvironmentDecoration decoration : decorations) {
            decoration.draw(canvas);
        }
    }

    private void drawBayWaitingAreas(Graphics2D canvas) {
        for (int bay = 0; bay < 4; bay++) {
            int y = 30 + bay * 155;
            canvas.setColor(new Color(60, 65, 110));
            canvas.fillRect(350, y, 120, 70);
            canvas.setColor(Color.BLACK);
            canvas.drawRect(350, y, 120, 70);

            canvas.setColor(new Color(110, 60, 40));
            canvas.fillRect(360, y + 40, 60, 20);
            canvas.setColor(new Color(80, 40, 20));
            canvas.fillRect(360, y + 45, 60, 3);
            canvas.fillRect(360, y + 50, 60, 3);
            canvas.fillRect(360, y + 55, 60, 3);
            canvas.setColor(Color.BLACK);
            canvas.drawRect(360, y + 40, 60, 20);

            canvas.setColor(Color.WHITE);
            canvas.fillRect(430, y + 10, 30, 40);
            canvas.setColor(Color.BLACK);
            canvas.drawRect(430, y + 10, 30, 40);
        }
    }

    private void drawTicketBooth(Graphics2D canvas) {
        canvas.setColor(new Color(120, 120, 120));
        canvas.fillRect(150, 310, 60, 80);
        canvas.setColor(Color.BLACK);
        canvas.drawRect(150, 310, 60, 80);
        canvas.setColor(new Color(170, 200, 255));
        canvas.fillRect(160, 330, 40, 45);
        canvas.setColor(Color.BLACK);
        canvas.drawString("TICKETS", 155, 325);

        Person priorityClient = engine.ticketClient(true);
        Person regularClient = engine.ticketClient(false);
        boolean priorityActive = priorityClient != null
                && priorityClient.state == PassengerState.BUYING_TICKET;
        boolean regularActive = regularClient != null
                && regularClient.state == PassengerState.BUYING_TICKET;

        Color idle = engine.isTicketBoothOpen() ? new Color(200, 200, 50) : Color.RED;
        drawBoothLight(canvas, 195, 335, priorityActive ? Color.GREEN : idle);
        drawBoothLight(canvas, 195, 362, regularActive ? Color.GREEN : idle);

        if (!engine.isTicketBoothOpen()) {
            canvas.setColor(Color.RED);
            canvas.setFont(new Font("Monospaced", Font.BOLD, 12));
            canvas.drawString("CLOSED", 158, 358);
        }
    }

    private void drawBoothLight(Graphics2D canvas, int x, int y, Color color) {
        canvas.setColor(color);
        canvas.fillRect(x, y, 5, 8);
        canvas.setColor(Color.BLACK);
        canvas.drawRect(x, y, 5, 8);
    }

    private void drawScheduleBoard(Graphics2D canvas) {
        int boardX = 290;
        int boardY = Math.max(500, getHeight() - 110);
        int boardWidth = 280;
        int boardHeight = 100;

        canvas.setColor(new Color(60, 65, 75));
        canvas.fillRect(boardX, boardY, boardWidth, boardHeight);
        canvas.setColor(new Color(100, 110, 120));
        canvas.fillRect(boardX, boardY, boardWidth, 5);
        canvas.fillRect(boardX, boardY, 5, boardHeight);
        canvas.setColor(new Color(30, 35, 45));
        canvas.fillRect(boardX, boardY + boardHeight - 5, boardWidth, 5);
        canvas.fillRect(boardX + boardWidth - 5, boardY, 5, boardHeight);

        int screenX = boardX + 10;
        int screenY = boardY + 10;
        int screenWidth = boardWidth - 20;
        int screenHeight = boardHeight - 20;
        canvas.setColor(new Color(5, 35, 75));
        canvas.fillRect(screenX, screenY, screenWidth, screenHeight);
        canvas.setColor(new Color(10, 45, 90));
        for (int y = screenY; y < screenY + screenHeight; y += 4) {
            canvas.drawLine(screenX, y, screenX + screenWidth, y);
        }

        canvas.setColor(new Color(0, 220, 255));
        canvas.setFont(new Font("Monospaced", Font.BOLD, 17));
        canvas.drawString("DEPARTURE SCHEDULE", screenX + 40, screenY + 18);
        canvas.setFont(new Font("Monospaced", Font.BOLD, 12));

        for (int bay = 1; bay <= 4; bay++) {
            drawScheduleRow(canvas, bay, screenX, screenY + 35 + (bay - 1) * 14);
        }
    }

    private void drawScheduleRow(Graphics2D canvas, int bay, int x, int y) {
        String bayText = "BAY " + bay;
        String status = "NO BUS";
        Color statusColor = new Color(50, 90, 140);

        for (Bus bus : engine.buses()) {
            if (bus.bayId != bay) {
                continue;
            }
            bayText += " - " + bus.destination.toUpperCase();
            if (bus.state == BusState.ARRIVING) {
                status = "ARRIVING";
                statusColor = new Color(255, 210, 0);
            } else if (bus.state == BusState.LOADING) {
                status = "LOADING";
                statusColor = new Color(0, 255, 50);
            } else if (bus.state == BusState.WAITING_FOR_DEPARTURE) {
                status = "CLOSING";
                statusColor = new Color(255, 130, 50);
            } else if (bus.state == BusState.DEPARTING) {
                status = "DEPARTING";
                statusColor = new Color(255, 80, 80);
            }
            break;
        }

        canvas.setColor(new Color(0, 160, 255));
        canvas.fillRect(x + 15, y - 8, 6, 6);
        canvas.setColor(new Color(210, 230, 255));
        canvas.drawString(bayText, x + 28, y);
        canvas.setColor(statusColor);
        canvas.drawString(status, x + 185, y);
    }

    private void drawStatusPanel(Graphics2D canvas) {
        canvas.setColor(new Color(0, 0, 0, 150));
        canvas.fillRect(10, 10, 200, 105);
        canvas.setColor(Color.WHITE);
        canvas.setFont(new Font("Monospaced", Font.BOLD, 14));
        canvas.drawString("TERMINAL SIM", 20, 28);
        canvas.setFont(new Font("Monospaced", Font.PLAIN, 12));
        canvas.drawString("Prio Queue: " + engine.ticketCount(true), 20, 48);
        canvas.drawString("Reg Queue: " + engine.ticketCount(false), 20, 66);
        canvas.drawString("Lounge Queue: " + engine.platformCount(), 20, 84);
        canvas.drawString(
                "Booth: " + (engine.isTicketBoothOpen() ? "OPEN" : "CLOSED"),
                20,
                102
        );
    }
}

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
            graphics.setColor(new Color(230, 100, 150));
            graphics.fillOval(x, y - 3, 6, 6);
            graphics.fillOval(x - 3, y, 6, 6);
            graphics.fillOval(x + 3, y, 6, 6);
            graphics.fillOval(x, y + 3, 6, 6);
            graphics.setColor(Color.YELLOW);
            graphics.fillOval(x + 1, y + 1, 4, 4);
        } else {
            graphics.setColor(new Color(200, 200, 200));
            graphics.fillRoundRect(x, y, 22, 35, 5, 5);
            graphics.setColor(new Color(150, 150, 150));
            for (int offset = 4; offset <= 16; offset += 6) {
                graphics.fillRect(x + offset, y + 5, 2, 25);
            }
            graphics.setColor(Color.BLACK);
            graphics.drawRoundRect(x, y, 22, 35, 5, 5);
            graphics.setColor(new Color(180, 180, 180));
            graphics.fillOval(x - 2, y - 5, 26, 10);
            graphics.setColor(Color.BLACK);
            graphics.drawOval(x - 2, y - 5, 26, 10);
        }
    }
}
