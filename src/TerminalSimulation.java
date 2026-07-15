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
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.event.ActionListener;
import java.util.List;

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
