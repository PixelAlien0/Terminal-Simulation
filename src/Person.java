import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics2D;

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
