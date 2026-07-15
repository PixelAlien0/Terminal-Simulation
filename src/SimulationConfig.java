final class SimulationConfig {
    static final int FRAME_DELAY_MS = 16;
    static final int MAX_FRAME_CATCH_UP_MS = 250;

    static final int WINDOW_WIDTH = 1380;
    static final int WINDOW_HEIGHT = 720;
    static final int LOG_MAX_LINES = 500;

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
