import java.awt.Color;
import java.awt.Graphics2D;

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
        } else if ("TrashCan".equals(type)) {
            graphics.setColor(new Color(200, 200, 200));
            graphics.fillRoundRect(x, y, 22, 35, 5, 5);
            graphics.setColor(new Color(150, 150, 150));
            graphics.fillRect(x + 4, y + 5, 2, 25);
            graphics.fillRect(x + 10, y + 5, 2, 25);
            graphics.fillRect(x + 16, y + 5, 2, 25);
            graphics.setColor(Color.BLACK);
            graphics.drawRoundRect(x, y, 22, 35, 5, 5);
            graphics.setColor(new Color(180, 180, 180));
            graphics.fillOval(x - 2, y - 5, 26, 10);
            graphics.setColor(Color.BLACK);
            graphics.drawOval(x - 2, y - 5, 26, 10);
        }
    }
}
