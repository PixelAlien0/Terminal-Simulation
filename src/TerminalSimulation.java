import javax.swing.*;
import java.awt.*;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedList;
import java.util.List;
import java.util.Random;

public class TerminalSimulation extends JFrame {
    enum PassengerState {
        WALKING_TO_TICKET, WAITING_FOR_TICKET, BUYING_TICKET,
        WALKING_TO_PLATFORM, WAITING_ON_PLATFORM,
        MOVING_TO_BAY_LINE, WAITING_IN_BAY_LINE,
        WALKING_TO_BUS, SEATED_IN_BUS,
        LEAVING_TERMINAL
    }
    enum BusState { ARRIVING, LOADING, WAITING_FOR_DEPARTURE, DEPARTING, DEPARTED }
   
    static class Person {
        String id;
        String destination;
        boolean isPriority;
        int arrivalOrder;
        int x, y;
        int targetX, targetY;
        int speed = 4;
        int animationFrame = 0;
        int animationSlow = 0;
        PassengerState state;
        Bus assignedBus = null;
        int ticketTimer = 0;

        public Person(String id, String destination, int startX, int startY, boolean isPriority, int arrivalOrder) {
            this.id = id;
            this.destination = destination;
            this.x = startX;
            this.y = startY;
            this.isPriority = isPriority;
            this.arrivalOrder = arrivalOrder;
            this.state = PassengerState.WALKING_TO_TICKET;
        }

        public void setTarget(int tx, int ty) {
            this.targetX = tx;
            this.targetY = ty;
        }

        public boolean stepTowardTarget() {
            boolean arrivedX = false;
            boolean arrivedY = false;
            if (x < targetX) x = Math.min(x + speed, targetX);
            else if (x > targetX) x = Math.max(x - speed, targetX);
            else arrivedX = true;
            if (y < targetY) y = Math.min(y + speed, targetY);
            else if (y > targetY) y = Math.max(y - speed, targetY);
            else arrivedY = true;
            if (!arrivedX || !arrivedY) {
                animationSlow++;
                if (animationSlow >= 3) {
                    animationFrame = (animationFrame + 1) % 4;
                    animationSlow = 0;
                }
            }
            return (arrivedX && arrivedY);
        }

        public void draw(Graphics2D g2) {
            if (state == PassengerState.LEAVING_TERMINAL && y > 680) return;
            int bob = (animationFrame % 2 == 0) ? 1 : 0;
            int renderY = y + bob;
            Color skin = new Color(210, 160, 120);
            Color hair = isPriority ? new Color(200, 180, 50) : new Color(170, 50, 50);
            Color shirt = isPriority ? new Color(100, 50, 150) : new Color(60, 100, 60);
            Color pants = new Color(50, 50, 80);
            Color outline = Color.BLACK;
            g2.setColor(new Color(0, 0, 0, 60));
            g2.fillOval(x - 6, y + 12, 12, 6);
            g2.setColor(pants);
            int leg = animationFrame % 4;
            int legOffset = (leg == 0 || leg == 2) ? 2 : -2;
            g2.fillRect(x - 4, renderY + 8, 3, 5 + legOffset);
            g2.fillRect(x + 1, renderY + 8, 3, 5 - legOffset);
            g2.setColor(shirt);
            g2.fillRect(x - 5, renderY, 10, 9);
            g2.setColor(outline);
            g2.drawRect(x - 5, renderY, 10, 9);
            g2.setColor(Color.WHITE);
            g2.setFont(new Font("SansSerif", Font.BOLD, 8));
            String numStr = String.valueOf(arrivalOrder);
            int textOffsetX = (numStr.length() > 1) ? -4 : -2;
            g2.drawString(numStr, x + textOffsetX, renderY + 8);
            g2.setColor(skin);
            g2.fillRect(x - 4, renderY - 8, 8, 8);
            g2.setColor(outline);
            g2.drawRect(x - 4, renderY - 8, 8, 8);
            g2.setColor(hair);
            g2.fillRect(x - 5, renderY - 10, 10, 4);
            g2.fillRect(x - 6, renderY - 8, 3, 4);
            g2.fillRect(x + 3, renderY - 8, 3, 4);
            g2.setFont(new Font("Monospaced", Font.BOLD, 10));
            if (isPriority) {
                g2.setColor(Color.YELLOW);
                g2.drawString("★", x - 6, y - 12);
            }
            g2.setColor(Color.WHITE);
            g2.drawString(destination.substring(0, 1), x + 2, y - 12);
            if (state == PassengerState.BUYING_TICKET) {
                g2.setColor(Color.BLACK);
                g2.drawRect(x - 8, renderY - 20, 16, 4);
                g2.setColor(Color.YELLOW);
                int fill = (int)(14 * ((20 - ticketTimer) / 20.0));
                g2.fillRect(x - 7, renderY - 19, Math.max(0, fill), 3);
            }
        }
    }

    static class Bus {
        String busId;
        String destination;
        int bayId;
        int capacity = 20;
        int x, y;
        BusState state = BusState.ARRIVING;
        int countdownTicks = 3600;
        int departureBufferTicks = 300;
        boolean countdownStarted = false;
        Person[] seats = new Person[capacity];
        List<Person> boardingLine = new ArrayList<>();
        int busWidth = 180;
        int busHeight = 80;
        int boardingTicker = 0;

        public Bus(String busId, String destination, int bayId, int startX, int startY) {
            this.busId = busId;
            this.destination = destination;
            this.bayId = bayId;
            this.x = startX;
            this.y = startY;
        }

        public int getRandomEmptySeat() {
            List<Integer> seatIndices = new ArrayList<>();
            for (int i = 0; i < capacity; i++) seatIndices.add(i);
            Collections.shuffle(seatIndices);
            for (int seatIndex : seatIndices) {
                if (seats[seatIndex] == null) return seatIndex;
            }
            return -1;
        }

        public boolean isFull() {
            for (Person p : seats) if (p == null) return false;
            return true;
        }

        public int getPassengerCount() {
            int count = 0;
            for (Person p : seats) if (p != null) count++;
            return count;
        }

        public Point getSeatCoordinate(int seatIndex) {
            int row = seatIndex / 4;
            int col = seatIndex % 4;
            return new Point(x + 15 + (row * 30), y + 15 + (col * 15));
        }

        public void draw(Graphics2D g2) {
            if (state == BusState.DEPARTED) return;
            Color mainBodyColor = destination.equals("Tagum") ? new Color(34, 139, 34) : new Color(30, 80, 160);
            Color trimColor = destination.equals("Tagum") ? new Color(20, 80, 20) : new Color(15, 40, 80);
            Color stripeColor = destination.equals("Tagum") ? new Color(255, 215, 0) : new Color(210, 210, 210);
            Color windowBlue = new Color(109, 165, 255);
            Color tireBlack = new Color(30, 30, 30);
            Color outline = Color.BLACK;

            g2.setColor(new Color(0, 0, 0, 50));
            g2.fillOval(x, y + busHeight - 10, busWidth, 20);
            g2.setColor(tireBlack);
            g2.fillRoundRect(x + 20, y + busHeight - 10, 24, 24, 8, 8);
            g2.fillRoundRect(x + 50, y + busHeight - 10, 24, 24, 8, 8);
            g2.fillRoundRect(x + busWidth - 45, y + busHeight - 10, 24, 24, 8, 8);

            g2.setColor(mainBodyColor);
            g2.fillRoundRect(x, y, busWidth, busHeight, 10, 10);
            g2.setColor(outline);
            g2.setStroke(new BasicStroke(3));
            g2.drawRoundRect(x, y, busWidth, busHeight, 10, 10);
            g2.setStroke(new BasicStroke(1));

            g2.setColor(trimColor);
            g2.fillRect(x + 2, y + busHeight - 20, busWidth - 4, 18);
            g2.setColor(stripeColor);
            g2.fillRect(x + 2, y + busHeight - 32, busWidth - 4, 12);

            g2.setColor(windowBlue);
            for (int i = 0; i < 4; i++) {
                g2.fillRect(x + 10 + (i * 30), y + 15, 25, 25);
                g2.setColor(outline);
                g2.drawRect(x + 10 + (i * 30), y + 15, 25, 25);
                g2.setColor(windowBlue);
            }

            g2.setColor(Color.WHITE);
            g2.fillRect(x + 135, y + 15, 30, 45);
            g2.setColor(outline);
            g2.drawRect(x + 135, y + 15, 30, 45);
            g2.setColor(windowBlue);
            g2.fillRect(x + 140, y + 20, 20, 20);
            g2.setColor(outline);
            g2.drawRect(x + 140, y + 20, 20, 20);

            g2.setColor(Color.WHITE);
            g2.setFont(new Font("Monospaced", Font.BOLD, 12));
            g2.drawString(busId + " [" + getPassengerCount() + "/" + capacity + "]", x + 5, y - 10);
           
            if (state == BusState.LOADING) {
                g2.setColor(Color.GREEN);
                if (countdownStarted) {
                    int secsLeft = (int) Math.ceil(countdownTicks / 60.0);
                    g2.drawString("⏱ " + secsLeft + "s", x + 5, y - 25);
                }
            }
        }
    }

    static class EnvironmentDecoration {
        String type; int x, y;
        public EnvironmentDecoration(String type, int x, int y) {
            this.type = type; this.x = x; this.y = y;
        }
        public void draw(Graphics2D g2) {
            if (type.equals("Flower")) {
                g2.setColor(new Color(230, 100, 150));
                g2.fillOval(x, y-3, 6, 6); g2.fillOval(x-3, y, 6, 6);
                g2.fillOval(x+3, y, 6, 6); g2.fillOval(x, y+3, 6, 6);
                g2.setColor(Color.YELLOW);
                g2.fillOval(x+1, y+1, 4, 4);
            } else if (type.equals("TrashCan")) {
                g2.setColor(new Color(200, 200, 200));
                g2.fillRoundRect(x, y, 22, 35, 5, 5);
                g2.setColor(new Color(150, 150, 150));
                g2.fillRect(x + 4, y + 5, 2, 25);
                g2.fillRect(x + 10, y + 5, 2, 25);
                g2.fillRect(x + 16, y + 5, 2, 25);
                g2.setColor(Color.BLACK);
                g2.drawRoundRect(x, y, 22, 35, 5, 5);
                g2.setColor(new Color(180, 180, 180));
                g2.fillOval(x - 2, y - 5, 26, 10);
                g2.setColor(Color.BLACK);
                g2.drawOval(x - 2, y - 5, 26, 10);
            }
        }
    }

    class TerminalPanel extends JPanel {
        public TerminalPanel() { setBackground(new Color(148, 128, 73)); }
        private void drawScheduleBoard(Graphics2D g2) {
            int bx = 290; int by = 550; int bw = 280; int bh = 110;
            g2.setColor(new Color(90, 95, 100));
            g2.fillRect(bx + 35, by + bh, 20, 50);
            g2.fillRect(bx + bw - 55, by + bh, 20, 50);
            g2.fillRect(bx + 35, by + bh + 25, bw - 70, 12);
            g2.setColor(new Color(50, 55, 60));
            g2.fillRect(bx + 48, by + bh, 7, 50);
            g2.fillRect(bx + bw - 42, by + bh, 7, 50);
            g2.fillRect(bx + 35, by + bh + 32, bw - 70, 5);
            g2.setColor(new Color(60, 65, 75));
            g2.fillRect(bx, by, bw, bh);
            g2.setColor(new Color(100, 110, 120));
            g2.fillRect(bx, by, bw, 5);
            g2.fillRect(bx, by, 5, bh);
            g2.setColor(new Color(30, 35, 45));
            g2.fillRect(bx, by + bh - 5, bw, 5);
            g2.fillRect(bx + bw - 5, by, 5, bh);
            int sx = bx + 10; int sy = by + 10; int sw = bw - 20; int sh = bh - 20;
            g2.setColor(new Color(5, 35, 75));
            g2.fillRect(sx, sy, sw, sh);
            g2.setColor(new Color(10, 45, 90));
            for (int i = sy; i < sy + sh; i += 4) g2.drawLine(sx, i, sx + sw, i);
            g2.setColor(new Color(0, 220, 255));
            g2.setFont(new Font("Monospaced", Font.BOLD, 17));
            g2.drawString("DEPARTURE SCHEDULE", sx + 40, sy + 18);
            g2.setFont(new Font("Monospaced", Font.BOLD, 12));
            for (int bay = 1; bay <= 4; bay++) {
                String bayText = "BAY " + bay;
                String statusText = "NO BUS";
                Color statusColor = new Color(50, 90, 140);
                for (Bus b : activeBuses) {
                    if (b.bayId == bay) {
                        bayText += " - " + b.destination.toUpperCase();
                        if (b.state == BusState.ARRIVING) { statusText = "ARRIVING "; statusColor = new Color(255, 210, 0); }
                        else if (b.state == BusState.LOADING) { statusText = "LOADING "; statusColor = new Color(0, 255, 50); }
                        else if (b.state == BusState.WAITING_FOR_DEPARTURE) { statusText = "DEPARTING"; statusColor = new Color(255, 80, 80); }
                        else if (b.state == BusState.DEPARTING) { statusText = "DEPARTED "; statusColor = new Color(150, 150, 150); }
                        break;
                    }
                }
                int rowY = sy + 35 + ((bay - 1) * 14);
                g2.setColor(new Color(0, 160, 255));
                g2.fillRect(sx + 15, rowY - 8, 6, 6);
                g2.setColor(new Color(210, 230, 255));
                g2.drawString(bayText, sx + 28, rowY);
                g2.setColor(statusColor);
                g2.drawString(statusText, sx + 185, rowY);
            }
        }

        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            Graphics2D g2 = (Graphics2D) g;
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setColor(new Color(81, 81, 81));
            g2.fillRect(600, 0, 780, getHeight());
            g2.setColor(Color.WHITE);
            g2.setStroke(new BasicStroke(4, BasicStroke.CAP_BUTT, BasicStroke.JOIN_MITER, 10, new float[]{20, 20}, 0));
            for(int i=0; i<4; i++) g2.drawLine(600, 100 + (i * 155), getWidth(), 100 + (i * 155));
            g2.setStroke(new BasicStroke(1));

            g2.setColor(new Color(191, 143, 94));
            g2.fillRect(250, 0, 350, getHeight());
            g2.setColor(new Color(160, 110, 60));
            for(int i=0; i<getHeight(); i+=30) g2.drawLine(250, i, 600, i);
            g2.drawLine(600, 0, 600, getHeight());

            for (EnvironmentDecoration decor : randomStuff) decor.draw(g2);

            for(int i=0; i<4; i++) {
                int sy = 30 + (i * 155);
                g2.setColor(new Color(60, 65, 110));
                g2.fillRect(350, sy, 120, 70);
                g2.setColor(Color.BLACK);
                g2.drawRect(350, sy, 120, 70);
                g2.setColor(new Color(110, 60, 40));
                g2.fillRect(360, sy + 40, 60, 20);
                g2.setColor(new Color(80, 40, 20));
                g2.fillRect(360, sy + 45, 60, 3);
                g2.fillRect(360, sy + 50, 60, 3);
                g2.fillRect(360, sy + 55, 60, 3);
                g2.setColor(Color.BLACK);
                g2.drawRect(360, sy + 40, 60, 20);
                g2.setColor(Color.WHITE);
                g2.fillRect(430, sy + 10, 30, 40);
                g2.setColor(Color.BLACK);
                g2.drawRect(430, sy + 10, 30, 40);
            }

            g2.setColor(new Color(120, 120, 120));
            g2.fillRect(150, 310, 60, 80);
            g2.setColor(Color.BLACK);
            g2.drawRect(150, 310, 60, 80);
            g2.setColor(new Color(170, 200, 255));
            g2.fillRect(160, 330, 40, 45);
            g2.setColor(Color.BLACK);
            g2.drawString("TICKETS", 155, 325);

            boolean prioAuth = !priorityTicketQueue.isEmpty() && priorityTicketQueue.peek().state == PassengerState.BUYING_TICKET;
            boolean regAuth = !regularTicketQueue.isEmpty() && regularTicketQueue.peek().state == PassengerState.BUYING_TICKET;
            Color prioLight = prioAuth ? Color.GREEN : (ticketBoothOpen ? new Color(200, 200, 50) : Color.RED);
            Color regLight = regAuth ? Color.GREEN : (ticketBoothOpen ? new Color(200, 200, 50) : Color.RED);
            g2.setColor(prioLight); g2.fillRect(195, 335, 5, 8); g2.setColor(Color.BLACK); g2.drawRect(195, 335, 5, 8);
            g2.setColor(regLight); g2.fillRect(195, 362, 5, 8); g2.setColor(Color.BLACK); g2.drawRect(195, 362, 5, 8);

            if (!ticketBoothOpen) {
                g2.setColor(Color.RED);
                g2.setFont(new Font("Monospaced", Font.BOLD, 12));
                g2.drawString("CLOSED", 158, 358);
            }

            drawScheduleBoard(g2);
            for (Bus b : activeBuses) b.draw(g2);

            List<Person> toDraw = new ArrayList<>(workingList);
            toDraw.sort(Comparator.comparingInt(p -> p.y));
            for (Person p : toDraw) {
                if (p.state != PassengerState.SEATED_IN_BUS) p.draw(g2);
            }

            g2.setColor(new Color(0, 0, 0, 150));
            g2.fillRect(10, 10, 200, 105);
            g2.setColor(Color.WHITE);
            g2.setFont(new Font("Monospaced", Font.BOLD, 14));
            g2.drawString("TERMINAL SIM", 20, 28);
            g2.setFont(new Font("Monospaced", Font.PLAIN, 12));
            g2.drawString("Prio Queue: " + priorityTicketQueue.size(), 20, 48);
            g2.drawString("Reg Queue: " + regularTicketQueue.size(), 20, 66);
            g2.drawString("Lounge Queue: " + platformQueue.size(), 20, 84);
            g2.drawString("Booth: " + (ticketBoothOpen ? "OPEN" : "CLOSED"), 20, 102);
        }
    }

    private LinkedList<Person> priorityTicketQueue = new LinkedList<>();
    private LinkedList<Person> regularTicketQueue = new LinkedList<>();
    private LinkedList<Person> platformQueue = new LinkedList<>();
    private List<Person> workingList = new ArrayList<>();
    private List<Bus> activeBuses = new ArrayList<>();
    private List<EnvironmentDecoration> randomStuff = new ArrayList<>();
    private TerminalPanel visualCanvas;
    private Timer frameLoopTimer;
    private JTextArea logConsole;
    private int priorityTransactionDelay = 0;
    private int regularTransactionDelay = 0;
    private int globalPassengerCounter = 0;
    private int busNameIdIndex = 1;
    private int passengerSpawnTicker = 0;
    private int busArrivalDelayTicker = 0;
    private Random rand = new Random();
    private boolean busesStopped = false;
    private boolean ticketBoothOpen = true;
    private JButton btnStopTicketBooth;

    public TerminalSimulation() {
        setTitle("Terminal Simulation - Pixel Art Edition");
        setSize(1380, 720);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLayout(new BorderLayout());
        visualCanvas = new TerminalPanel();
        add(visualCanvas, BorderLayout.CENTER);
        logConsole = new JTextArea(10, 30);
        logConsole.setEditable(false);
        logConsole.setBackground(new Color(40, 40, 40));
        logConsole.setForeground(new Color(220, 220, 220));
        logConsole.setFont(new Font("Monospaced", Font.PLAIN, 12));
        add(new JScrollPane(logConsole), BorderLayout.EAST);

        JPanel buttonDeck = new JPanel();
        buttonDeck.setBackground(new Color(30, 30, 30));
        buttonDeck.setLayout(new FlowLayout(FlowLayout.CENTER, 15, 10));

        JButton btnAddRegular = new JButton("➕ Regular Passenger");
        JButton btnAddPriority = new JButton("⭐ Priority Passenger");
        JButton btnStopBuses = new JButton("🛑 Stop Buses");
        btnStopTicketBooth = new JButton("🚫 Close Ticket Booth");

        JButton btnCreate = new JButton("Create Passenger");
        JButton btnRead = new JButton("List Passengers");
        JButton btnUpdate = new JButton("Update Passenger");
        JButton btnDelete = new JButton("Delete Passenger");

        JButton btnAddBus = new JButton("Add Bus");
        JButton btnDeleteBus = new JButton("Delete Bus");

        styleButton(btnAddRegular, new Color(60, 100, 60));
        styleButton(btnAddPriority, new Color(100, 50, 150));
        styleButton(btnStopBuses, new Color(180, 60, 60));
        styleButton(btnStopTicketBooth, new Color(200, 100, 50));
        styleButton(btnCreate, new Color(0, 150, 0));
        styleButton(btnRead, new Color(0, 100, 200));
        styleButton(btnUpdate, new Color(200, 150, 0));
        styleButton(btnDelete, new Color(200, 50, 50));
        styleButton(btnAddBus, new Color(0, 120, 200));
        styleButton(btnDeleteBus, new Color(200, 60, 60));

        btnAddRegular.addActionListener(e -> injectCustomPassenger(false));
        btnAddPriority.addActionListener(e -> injectCustomPassenger(true));
        btnStopBuses.addActionListener(e -> {
            busesStopped = !busesStopped;
            if (busesStopped) {
                btnStopBuses.setText("▶ Resume Buses");
                btnStopBuses.setBackground(new Color(200, 150, 50));
                appendLog("[SYSTEM] Bus Operations Paused.");
            } else {
                btnStopBuses.setText("🛑 Stop Buses");
                btnStopBuses.setBackground(new Color(180, 60, 60));
                appendLog("[SYSTEM] Bus Operations Resumed.");
            }
        });
        btnStopTicketBooth.addActionListener(e -> {
            ticketBoothOpen = !ticketBoothOpen;
            if (!ticketBoothOpen) {
                btnStopTicketBooth.setText("✅ Open Ticket Booth");
                btnStopTicketBooth.setBackground(new Color(60, 180, 80));
                appendLog("[SYSTEM] Ticket Booth CLOSED.");
            } else {
                btnStopTicketBooth.setText("🚫 Close Ticket Booth");
                btnStopTicketBooth.setBackground(new Color(200, 100, 50));
                appendLog("[SYSTEM] Ticket Booth OPEN.");
                recalculateTicketQueueSpacings();
            }
        });

        btnCreate.addActionListener(e -> createPassenger());
        btnRead.addActionListener(e -> readPassengers());
        btnUpdate.addActionListener(e -> updatePassenger());
        btnDelete.addActionListener(e -> deletePassenger());
        btnAddBus.addActionListener(e -> addBus());
        btnDeleteBus.addActionListener(e -> deleteBus());

        buttonDeck.add(btnAddRegular);
        buttonDeck.add(btnAddPriority);
        buttonDeck.add(btnStopBuses);
        buttonDeck.add(btnStopTicketBooth);
        buttonDeck.add(btnCreate);
        buttonDeck.add(btnRead);
        buttonDeck.add(btnUpdate);
        buttonDeck.add(btnDelete);
        buttonDeck.add(btnAddBus);
        buttonDeck.add(btnDeleteBus);

        add(buttonDeck, BorderLayout.SOUTH);

        generateRandomStuff();
        initializeTerminalDataAssets();
        frameLoopTimer = new Timer(16, e -> executeUpdateTick());
        frameLoopTimer.start();
        setLocationRelativeTo(null);
    }

    private void styleButton(JButton btn, Color bg) {
        btn.setBackground(bg);
        btn.setForeground(Color.WHITE);
        btn.setFont(new Font("Monospaced", Font.BOLD, 12));
        btn.setFocusPainted(false);
        btn.setBorder(BorderFactory.createLineBorder(Color.WHITE, 2));
    }

    private void appendLog(String message) {
        logConsole.append(message + "\n");
        logConsole.setCaretPosition(logConsole.getDocument().getLength());
    }

    private void generateRandomStuff() {
        for(int i=0; i<4; i++) {
            int yPos = 30 + (i * 155);
            randomStuff.add(new EnvironmentDecoration("TrashCan", 500, yPos + 20));
        }
        for(int i=0; i<20; i++) {
            randomStuff.add(new EnvironmentDecoration("Flower", rand.nextInt(230), rand.nextInt(650)));
        }
    }

    private void initializeTerminalDataAssets() {
        appendLog("[SYSTEM] Simulation started...");
        for (int i = 0; i < 22; i++) generateAutoPassenger();
    }

    private void injectCustomPassenger(boolean isPriority) {
        globalPassengerCounter++;
        String id = "P" + globalPassengerCounter;
        String dest = rand.nextBoolean() ? "Davao" : "Tagum";
        Person p = new Person(id, dest, 10, 350 + rand.nextInt(30), isPriority, globalPassengerCounter);
        if (p.isPriority) priorityTicketQueue.add(p);
        else regularTicketQueue.add(p);
        workingList.add(p);
        recalculateTicketQueueSpacings();
    }

    private void generateAutoPassenger() {
        globalPassengerCounter++;
        String id = "P" + globalPassengerCounter;
        String dest = rand.nextBoolean() ? "Davao" : "Tagum";
        boolean isPriority = rand.nextFloat() < 0.20f;
        Person p = new Person(id, dest, 10, 350 + rand.nextInt(30), isPriority, globalPassengerCounter);
        if (p.isPriority) priorityTicketQueue.add(p);
        else regularTicketQueue.add(p);
        workingList.add(p);
        recalculateTicketQueueSpacings();
    }

    // ==================== PASSENGER CRUD ====================
    private void createPassenger() {
        String[] prioOptions = {"Regular", "Priority"};
        int prioChoice = JOptionPane.showOptionDialog(this,
                "Choose Passenger Type",
                "Create Passenger",
                JOptionPane.DEFAULT_OPTION,
                JOptionPane.QUESTION_MESSAGE,
                null,
                prioOptions,
                prioOptions[0]);

        if (prioChoice == -1) return;
        boolean isPriority = (prioChoice == 1);

        String[] destOptions = {"Davao", "Tagum"};
        int destChoice = JOptionPane.showOptionDialog(this,
                "Choose Destination",
                "Create Passenger",
                JOptionPane.DEFAULT_OPTION,
                JOptionPane.QUESTION_MESSAGE,
                null,
                destOptions,
                destOptions[0]);

        if (destChoice == -1) return;
        String dest = destOptions[destChoice];

        globalPassengerCounter++;
        String id = "P" + globalPassengerCounter;
        Person p = new Person(id, dest, 10, 350 + rand.nextInt(30), isPriority, globalPassengerCounter);

        if (isPriority) priorityTicketQueue.add(p);
        else regularTicketQueue.add(p);
        workingList.add(p);
        recalculateTicketQueueSpacings();

        appendLog("[CRUD] Created " + (isPriority ? "Priority" : "Regular") + " passenger " + id + " → " + dest);
    }

    private void readPassengers() {
        StringBuilder sb = new StringBuilder("=== Current Passengers ===\n");
        for (Person p : workingList) {
            sb.append(p.id).append(" | ").append(p.destination)
              .append(" | ").append(p.isPriority ? "PRIORITY" : "REGULAR")
              .append(" | State: ").append(p.state).append("\n");
        }
        JOptionPane.showMessageDialog(this, sb.toString(), "Passengers List", JOptionPane.INFORMATION_MESSAGE);
    }

    private void updatePassenger() {
        String id = JOptionPane.showInputDialog(this, "Enter Passenger ID to update:");
        if (id == null || id.trim().isEmpty()) return;

        Person target = null;
        for (Person p : workingList) {
            if (p.id.equals(id)) {
                target = p;
                break;
            }
        }

        if (target == null) {
            JOptionPane.showMessageDialog(this, "Passenger not found!", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        String[] destOptions = {"Davao", "Tagum"};
        int choice = JOptionPane.showOptionDialog(this,
                "Choose New Destination for " + id,
                "Update Passenger",
                JOptionPane.DEFAULT_OPTION,
                JOptionPane.QUESTION_MESSAGE,
                null,
                destOptions,
                destOptions[0]);

        if (choice == -1) return;

        String newDest = destOptions[choice];
        target.destination = newDest;
        appendLog("[CRUD] Updated " + id + " destination to " + newDest);
    }

    private void deletePassenger() {
        String id = JOptionPane.showInputDialog(this, "Enter Passenger ID to delete:");
        boolean removed = workingList.removeIf(p -> p.id.equals(id));
        if (removed) {
            priorityTicketQueue.removeIf(p -> p.id.equals(id));
            regularTicketQueue.removeIf(p -> p.id.equals(id));
            platformQueue.removeIf(p -> p.id.equals(id));
            appendLog("[CRUD] Deleted passenger " + id);
        } else {
            JOptionPane.showMessageDialog(this, "Passenger not found!", "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    // ==================== BUS CRUD ====================
    private void addBus() {
        String[] options = {"Davao", "Tagum"};
        int choice = JOptionPane.showOptionDialog(this,
                "Choose Bus Destination",
                "Add New Bus",
                JOptionPane.DEFAULT_OPTION,
                JOptionPane.QUESTION_MESSAGE,
                null,
                options,
                options[0]);

        if (choice == -1) return;

        String dest = options[choice];

        boolean[] bayOccupied = new boolean[5];
        for (Bus b : activeBuses) bayOccupied[b.bayId] = true;
        List<Integer> freeBays = new ArrayList<>();
        for (int bId = 1; bId <= 4; bId++) if (!bayOccupied[bId]) freeBays.add(bId);

        if (freeBays.isEmpty()) {
            JOptionPane.showMessageDialog(this, "No free bays available!", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        int chosenBay = freeBays.get(rand.nextInt(freeBays.size()));
        String lineName = dest.equals("Davao") ? "Davao Exp" : "Tagum Met";
        char bayLabel = (chosenBay == 1 || chosenBay == 3) ? 'A' : 'B';
        int targetYPos = 20 + ((chosenBay - 1) * 155);

        Bus newBus = new Bus(lineName + " " + bayLabel + " (B" + (busNameIdIndex++) + ")", dest, chosenBay, 1400, targetYPos);
        activeBuses.add(newBus);
        appendLog("[BUS] Added " + newBus.busId + " at Bay " + chosenBay);
    }

    private void deleteBus() {
        if (activeBuses.isEmpty()) {
            JOptionPane.showMessageDialog(this, "No buses to delete!", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        String busId = JOptionPane.showInputDialog(this, "Enter Bus ID to delete\n(e.g. Davao Exp A (B5)):");
        if (busId == null || busId.trim().isEmpty()) return;

        boolean removed = activeBuses.removeIf(b -> b.busId.equals(busId.trim()));
        if (removed) {
            appendLog("[BUS] Deleted bus: " + busId);
        } else {
            JOptionPane.showMessageDialog(this, "Bus not found!", "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void spawnRandomBus() {
        boolean[] bayOccupied = new boolean[5];
        for (Bus b : activeBuses) bayOccupied[b.bayId] = true;
        List<Integer> freeBays = new ArrayList<>();
        for (int bId = 1; bId <= 4; bId++) if (!bayOccupied[bId]) freeBays.add(bId);
        if (freeBays.isEmpty()) return;
        int chosenBay = freeBays.get(rand.nextInt(freeBays.size()));
        String dest = (chosenBay <= 2) ? "Davao" : "Tagum";
        String lineName = dest.equals("Davao") ? "Davao Exp" : "Tagum Met";
        char bayLabel = (chosenBay == 1 || chosenBay == 3) ? 'A' : 'B';
        int targetYPos = 20 + ((chosenBay - 1) * 155);
        Bus newBus = new Bus(lineName + " " + bayLabel + " (B" + (busNameIdIndex++) + ")", dest, chosenBay, 1400, targetYPos);
        activeBuses.add(newBus);
        appendLog("[BUS] " + newBus.busId + " arriving at Bay " + chosenBay);
    }

    private void recalculateTicketQueueSpacings() {
        int idx = 0;
        for (Person p : priorityTicketQueue) {
            if (p.state == PassengerState.WALKING_TO_TICKET || p.state == PassengerState.WAITING_FOR_TICKET || p.state == PassengerState.BUYING_TICKET) {
                p.setTarget(180 - (idx * 28), 340);
                idx++;
            }
        }
        idx = 0;
        for (Person p : regularTicketQueue) {
            if (p.state == PassengerState.WALKING_TO_TICKET || p.state == PassengerState.WAITING_FOR_TICKET || p.state == PassengerState.BUYING_TICKET) {
                p.setTarget(180 - (idx * 28), 380);
                idx++;
            }
        }
    }

    private void recalculatePlatformLoungeSpacings() {
        int orderIndex = 0;
        for (Person p : platformQueue) {
            if (p.state == PassengerState.WAITING_ON_PLATFORM || p.state == PassengerState.WALKING_TO_PLATFORM) {
                int bayGroup = (orderIndex / 12) % 4;
                int cx = 370 + ((orderIndex % 12) % 4 * 20);
                int cy = 70 + (bayGroup * 155) + ((orderIndex % 12) / 4 * 20);
                p.setTarget(cx, cy);
                orderIndex++;
            }
        }
    }

    private void updateBusBayLineCoordinates(Bus b) {
        int lineStartX = b.x - 30;
        int lineY = b.y + 45;
        for (int i = 0; i < b.boardingLine.size(); i++) {
            Person p = b.boardingLine.get(i);
            p.setTarget(lineStartX - (i * 18), lineY);
        }
    }

    private void executeUpdateTick() {
        passengerSpawnTicker++;
        if (passengerSpawnTicker > 70 && ticketBoothOpen) {
            generateAutoPassenger();
            passengerSpawnTicker = 0;
        } else if (passengerSpawnTicker > 70) {
            passengerSpawnTicker = 0;
        }

        if (activeBuses.size() < 4 && !busesStopped) {
            busArrivalDelayTicker++;
            if (busArrivalDelayTicker > 1200) {
                spawnRandomBus();
                busArrivalDelayTicker = 0;
            }
        }

        for (Bus b : activeBuses) {
            if (busesStopped) continue;
            if (b.state == BusState.ARRIVING) {
                b.x -= 8;
                if (b.x <= 620) b.state = BusState.LOADING;
            } else if (b.state == BusState.LOADING) {
                // Priority go straight
                for (int i = 0; i < platformQueue.size(); i++) {
                    Person p = platformQueue.get(i);
                    if (p.isPriority && p.state == PassengerState.WAITING_ON_PLATFORM && 
                        b.destination.equals(p.destination) && !b.isFull()) {
                        platformQueue.remove(i);
                        i--;
                        p.state = PassengerState.WALKING_TO_BUS;
                        int seatIdx = b.getRandomEmptySeat();
                        if (seatIdx != -1) {
                            p.assignedBus = b;
                            b.seats[seatIdx] = p;
                            Point seatCoord = b.getSeatCoordinate(seatIdx);
                            p.setTarget(seatCoord.x, seatCoord.y);
                        }
                        recalculatePlatformLoungeSpacings();
                        break;
                    }
                }

                // Regular passengers (max 20 in line)
                if (b.boardingLine.size() < 20) {
                    for (int i = 0; i < platformQueue.size(); i++) {
                        Person p = platformQueue.get(i);
                        if (!p.isPriority && p.state == PassengerState.WAITING_ON_PLATFORM && 
                            b.destination.equals(p.destination) && !b.isFull()) {
                            platformQueue.remove(i);
                            i--;
                            p.state = PassengerState.MOVING_TO_BAY_LINE;
                            p.assignedBus = b;
                            b.boardingLine.add(p);
                            updateBusBayLineCoordinates(b);
                            recalculatePlatformLoungeSpacings();
                            break;
                        }
                    }
                }

                if (!b.boardingLine.isEmpty() && !b.isFull()) {
                    b.boardingTicker++;
                    if (b.boardingTicker >= 25) {
                        Person boarder = b.boardingLine.remove(0);
                        int seatIdx = b.getRandomEmptySeat();
                        if (seatIdx != -1) {
                            boarder.state = PassengerState.WALKING_TO_BUS;
                            boarder.assignedBus = b;
                            b.seats[seatIdx] = boarder;
                            Point seatCoord = b.getSeatCoordinate(seatIdx);
                            boarder.setTarget(seatCoord.x, seatCoord.y);
                        }
                        updateBusBayLineCoordinates(b);
                        b.boardingTicker = 0;
                    }
                }

                if (!b.countdownStarted) {
                    for (Person p : b.seats) if (p != null && p.state == PassengerState.SEATED_IN_BUS) {
                        b.countdownStarted = true;
                        break;
                    }
                }
                if (b.countdownStarted && b.countdownTicks > 0) b.countdownTicks--;

                if (b.isFull() || (b.countdownStarted && b.countdownTicks <= 0)) {
                    b.boardingLine.forEach(p -> {
                        p.state = PassengerState.WALKING_TO_PLATFORM;
                        p.assignedBus = null;
                        platformQueue.add(p);
                    });
                    b.boardingLine.clear();
                    recalculatePlatformLoungeSpacings();
                    b.state = BusState.WAITING_FOR_DEPARTURE;
                    appendLog("[BUS] " + b.busId + " closed doors. Preparing to depart.");
                }
            } else if (b.state == BusState.WAITING_FOR_DEPARTURE) {
                b.departureBufferTicks--;
                if (b.departureBufferTicks <= 0) {
                    b.state = BusState.DEPARTING;
                    appendLog("[BUS] " + b.busId + " departing with " + b.getPassengerCount() + " passengers.");
                }
            } else if (b.state == BusState.DEPARTING) {
                b.x += 6;
                for (int i = 0; i < b.seats.length; i++) {
                    Person p = b.seats[i];
                    if (p != null) {
                        Point seatPos = b.getSeatCoordinate(i);
                        p.x = seatPos.x;
                        p.y = seatPos.y;
                    }
                }
                if (b.x > getWidth() + 200) b.state = BusState.DEPARTED;
            }
        }

        activeBuses.removeIf(b -> {
            if (b.state == BusState.DEPARTED) {
                for (Person p : b.seats) if (p != null) workingList.remove(p);
                return true;
            }
            return false;
        });

        if (priorityTransactionDelay > 0) priorityTransactionDelay--;
        else if (ticketBoothOpen && !priorityTicketQueue.isEmpty()) {
            Person client = priorityTicketQueue.peek();
            if (client.state == PassengerState.WAITING_FOR_TICKET) client.setTarget(180, 340);
            if (client.x == 180 && client.y == 340) {
                if (client.state != PassengerState.BUYING_TICKET) {
                    client.state = PassengerState.BUYING_TICKET;
                    client.ticketTimer = 20;
                }
            }
            if (client.state == PassengerState.BUYING_TICKET) {
                client.ticketTimer--;
                if (client.ticketTimer <= 0) {
                    priorityTicketQueue.poll();
                    client.state = PassengerState.WALKING_TO_PLATFORM;
                    platformQueue.add(client);
                    recalculatePlatformLoungeSpacings();
                    recalculateTicketQueueSpacings();
                    priorityTransactionDelay = 20;
                }
            }
        }

        if (regularTransactionDelay > 0) regularTransactionDelay--;
        else if (ticketBoothOpen && !regularTicketQueue.isEmpty()) {
            Person client = regularTicketQueue.peek();
            if (client.state == PassengerState.WAITING_FOR_TICKET) client.setTarget(180, 380);
            if (client.x == 180 && client.y == 380) {
                if (client.state != PassengerState.BUYING_TICKET) {
                    client.state = PassengerState.BUYING_TICKET;
                    client.ticketTimer = 20;
                }
            }
            if (client.state == PassengerState.BUYING_TICKET) {
                client.ticketTimer--;
                if (client.ticketTimer <= 0) {
                    regularTicketQueue.poll();
                    client.state = PassengerState.WALKING_TO_PLATFORM;
                    platformQueue.add(client);
                    recalculatePlatformLoungeSpacings();
                    recalculateTicketQueueSpacings();
                    regularTransactionDelay = 20;
                }
            }
        }

        for (Person p : workingList) {
            boolean arrived = p.stepTowardTarget();
            if (arrived) {
                switch (p.state) {
                    case WALKING_TO_TICKET: p.state = PassengerState.WAITING_FOR_TICKET; break;
                    case WALKING_TO_PLATFORM: p.state = PassengerState.WAITING_ON_PLATFORM; break;
                    case MOVING_TO_BAY_LINE: p.state = PassengerState.WAITING_IN_BAY_LINE; break;
                    case WALKING_TO_BUS: p.state = PassengerState.SEATED_IN_BUS; break;
                }
            }
        }
        workingList.removeIf(p -> p.state == PassengerState.LEAVING_TERMINAL && p.y > 680);
        visualCanvas.repaint();
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new TerminalSimulation().setVisible(true));
    }
}
