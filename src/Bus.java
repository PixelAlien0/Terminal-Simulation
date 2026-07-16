import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.Point;
import java.util.Random;

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
