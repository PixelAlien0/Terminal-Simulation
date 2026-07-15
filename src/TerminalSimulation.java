import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextArea;
import javax.swing.SwingUtilities;
import javax.swing.Timer;
import javax.swing.table.DefaultTableModel;
import javax.swing.text.BadLocationException;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.util.List;

public final class TerminalSimulation extends JFrame {
    private final JTextArea logConsole = new JTextArea(10, 30);
    private final SimulationEngine engine;
    private final TerminalPanel visualCanvas;
    private final JButton stopBusesButton = new JButton("🛑 Stop Buses");
    private final JButton ticketBoothButton = new JButton("🚫 Close Ticket Booth");

    private Timer frameTimer;
    private long lastFrameNanos;
    private int updateAccumulatorMs;

    public TerminalSimulation() {
        super("Terminal Simulation - Pixel Art Edition");
        configureLogConsole();

        engine = new SimulationEngine(this::appendLog);
        visualCanvas = new TerminalPanel(engine);

        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLayout(new BorderLayout());
        add(visualCanvas, BorderLayout.CENTER);

        JScrollPane logScrollPane = new JScrollPane(logConsole);
        logScrollPane.setPreferredSize(new Dimension(300, 0));
        add(logScrollPane, BorderLayout.EAST);
        add(createButtonDeck(), BorderLayout.SOUTH);

        setSize(SimulationConfig.WINDOW_WIDTH, SimulationConfig.WINDOW_HEIGHT);
        setMinimumSize(new Dimension(1000, 650));
        setLocationRelativeTo(null);

        engine.initialize();
        startFrameTimer();
    }

    private JPanel createButtonDeck() {
        JPanel buttonDeck = new JPanel(new FlowLayout(FlowLayout.CENTER, 15, 10));
        buttonDeck.setBackground(new Color(30, 30, 30));

        JButton addRegularButton = new JButton("➕ Regular Passenger");
        JButton addPriorityButton = new JButton("⭐ Priority Passenger");
        JButton createButton = new JButton("Create Passenger");
        JButton readButton = new JButton("List Passengers");
        JButton updateButton = new JButton("Update Passenger");
        JButton deleteButton = new JButton("Delete Passenger");
        JButton addBusButton = new JButton("Add Bus");
        JButton deleteBusButton = new JButton("Delete Bus");

        styleButton(addRegularButton, new Color(60, 100, 60));
        styleButton(addPriorityButton, new Color(100, 50, 150));
        styleButton(stopBusesButton, new Color(180, 60, 60));
        styleButton(ticketBoothButton, new Color(200, 100, 50));
        styleButton(createButton, new Color(0, 150, 0));
        styleButton(readButton, new Color(0, 100, 200));
        styleButton(updateButton, new Color(200, 150, 0));
        styleButton(deleteButton, new Color(200, 50, 50));
        styleButton(addBusButton, new Color(0, 120, 200));
        styleButton(deleteBusButton, new Color(200, 60, 60));

        addRegularButton.addActionListener(event -> engine.createRandomPassenger(false));
        addPriorityButton.addActionListener(event -> engine.createRandomPassenger(true));
        stopBusesButton.addActionListener(event -> toggleBuses());
        ticketBoothButton.addActionListener(event -> toggleTicketBooth());
        createButton.addActionListener(event -> createPassenger());
        readButton.addActionListener(event -> readPassengers());
        updateButton.addActionListener(event -> updatePassenger());
        deleteButton.addActionListener(event -> deletePassenger());
        addBusButton.addActionListener(event -> addBus());
        deleteBusButton.addActionListener(event -> deleteBus());

        buttonDeck.add(addRegularButton);
        buttonDeck.add(addPriorityButton);
        buttonDeck.add(stopBusesButton);
        buttonDeck.add(ticketBoothButton);
        buttonDeck.add(createButton);
        buttonDeck.add(readButton);
        buttonDeck.add(updateButton);
        buttonDeck.add(deleteButton);
        buttonDeck.add(addBusButton);
        buttonDeck.add(deleteBusButton);
        return buttonDeck;
    }

    private void configureLogConsole() {
        logConsole.setEditable(false);
        logConsole.setBackground(new Color(40, 40, 40));
        logConsole.setForeground(new Color(220, 220, 220));
        logConsole.setFont(new Font("Monospaced", Font.PLAIN, 12));
    }

    private void styleButton(JButton button, Color background) {
        button.setBackground(background);
        button.setForeground(Color.WHITE);
        button.setFont(new Font("Monospaced", Font.BOLD, 12));
        button.setFocusPainted(false);
        button.setBorder(BorderFactory.createLineBorder(Color.WHITE, 2));
    }

    private void startFrameTimer() {
        lastFrameNanos = System.nanoTime();
        frameTimer = new Timer(SimulationConfig.FRAME_DELAY_MS, event -> updateFrame());
        frameTimer.start();
    }

    private void updateFrame() {
        long now = System.nanoTime();
        int elapsedMs = (int) Math.min(
                SimulationConfig.MAX_FRAME_CATCH_UP_MS,
                Math.max(0L, (now - lastFrameNanos) / 1_000_000L)
        );
        lastFrameNanos = now;
        updateAccumulatorMs += elapsedMs;

        while (updateAccumulatorMs >= SimulationConfig.FRAME_DELAY_MS) {
            engine.update(SimulationConfig.FRAME_DELAY_MS);
            updateAccumulatorMs -= SimulationConfig.FRAME_DELAY_MS;
        }
        visualCanvas.repaint();
    }

    private void appendLog(String message) {
        logConsole.append(message + System.lineSeparator());
        int excessLines = logConsole.getLineCount() - (SimulationConfig.LOG_MAX_LINES + 1);
        if (excessLines > 0) {
            try {
                int endOffset = logConsole.getLineEndOffset(excessLines - 1);
                logConsole.replaceRange("", 0, endOffset);
            } catch (BadLocationException exception) {
                logConsole.setText(message + System.lineSeparator());
            }
        }
        logConsole.setCaretPosition(logConsole.getDocument().getLength());
    }

    private void toggleBuses() {
        boolean stopped = engine.toggleBusesStopped();
        stopBusesButton.setText(stopped ? "▶ Resume Buses" : "🛑 Stop Buses");
        stopBusesButton.setBackground(
                stopped ? new Color(200, 150, 50) : new Color(180, 60, 60)
        );
    }

    private void toggleTicketBooth() {
        boolean open = engine.toggleTicketBooth();
        ticketBoothButton.setText(open ? "🚫 Close Ticket Booth" : "✅ Open Ticket Booth");
        ticketBoothButton.setBackground(
                open ? new Color(200, 100, 50) : new Color(60, 180, 80)
        );
    }

    private void createPassenger() {
        String[] passengerTypes = {"Regular", "Priority"};
        int typeChoice = JOptionPane.showOptionDialog(
                this,
                "Choose Passenger Type",
                "Create Passenger",
                JOptionPane.DEFAULT_OPTION,
                JOptionPane.QUESTION_MESSAGE,
                null,
                passengerTypes,
                passengerTypes[0]
        );
        if (typeChoice < 0) {
            return;
        }

        String destination = chooseDestination("Create Passenger", "Choose Destination");
        if (destination != null) {
            engine.createPassengerWithLog(typeChoice == 1, destination);
        }
    }

    private void readPassengers() {
        String[] columns = {"ID", "Destination", "Type", "State", "Assigned Bus"};
        DefaultTableModel model = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };

        for (Person passenger : engine.getWorkingPassengers()) {
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
        JScrollPane scrollPane = new JScrollPane(table);
        scrollPane.setPreferredSize(new Dimension(720, 420));
        JOptionPane.showMessageDialog(
                this,
                scrollPane,
                "Current Passengers",
                JOptionPane.INFORMATION_MESSAGE
        );
    }

    private void updatePassenger() {
        String id = promptForPassengerId("Enter Passenger ID to update:");
        if (id == null) {
            return;
        }
        if (engine.findPassenger(id) == null) {
            showNotFound("Passenger");
            return;
        }

        String destination = chooseDestination(
                "Update Passenger",
                "Choose New Destination for " + id.trim()
        );
        if (destination != null) {
            engine.updatePassengerDestination(id, destination);
        }
    }

    private void deletePassenger() {
        String id = promptForPassengerId("Enter Passenger ID to delete:");
        if (id != null && !engine.removePassenger(id)) {
            showNotFound("Passenger");
        }
    }

    private void addBus() {
        String destination = chooseDestination("Add New Bus", "Choose Bus Destination");
        if (destination == null) {
            return;
        }
        if (engine.addBus(destination) == null) {
            JOptionPane.showMessageDialog(
                    this,
                    "No free bays available!",
                    "Error",
                    JOptionPane.ERROR_MESSAGE
            );
        }
    }

    private void deleteBus() {
        List<Bus> buses = engine.getActiveBuses();
        if (buses.isEmpty()) {
            showNotFound("Bus");
            return;
        }

        String[] busIds = new String[buses.size()];
        for (int index = 0; index < buses.size(); index++) {
            busIds[index] = buses.get(index).busId;
        }
        String selected = (String) JOptionPane.showInputDialog(
                this,
                "Choose a bus to delete. Its passengers will return to the platform.",
                "Delete Bus",
                JOptionPane.QUESTION_MESSAGE,
                null,
                busIds,
                busIds[0]
        );
        if (selected != null) {
            engine.removeBus(selected);
        }
    }

    private String chooseDestination(String title, String message) {
        String[] destinations = {"Davao", "Tagum"};
        int choice = JOptionPane.showOptionDialog(
                this,
                message,
                title,
                JOptionPane.DEFAULT_OPTION,
                JOptionPane.QUESTION_MESSAGE,
                null,
                destinations,
                destinations[0]
        );
        return choice < 0 ? null : destinations[choice];
    }

    private String promptForPassengerId(String message) {
        String id = JOptionPane.showInputDialog(this, message);
        if (id == null || id.trim().isEmpty()) {
            return null;
        }
        return id.trim();
    }

    private void showNotFound(String entity) {
        JOptionPane.showMessageDialog(
                this,
                entity + " not found!",
                "Error",
                JOptionPane.ERROR_MESSAGE
        );
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new TerminalSimulation().setVisible(true));
    }
}
