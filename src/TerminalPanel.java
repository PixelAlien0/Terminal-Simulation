import javax.swing.JPanel;
import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Random;

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

            for (Bus bus : engine.getActiveBuses()) {
                bus.draw(canvas);
            }

            List<Person> passengers = new ArrayList<Person>(engine.getWorkingPassengers());
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

        Person priorityClient = engine.getPriorityTicketClient();
        Person regularClient = engine.getRegularTicketClient();
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

        for (Bus bus : engine.getActiveBuses()) {
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
        canvas.drawString("Prio Queue: " + engine.getPriorityTicketQueueSize(), 20, 48);
        canvas.drawString("Reg Queue: " + engine.getRegularTicketQueueSize(), 20, 66);
        canvas.drawString("Lounge Queue: " + engine.getPlatformQueueSize(), 20, 84);
        canvas.drawString(
                "Booth: " + (engine.isTicketBoothOpen() ? "OPEN" : "CLOSED"),
                20,
                102
        );
    }
}
