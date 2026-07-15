import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.util.HashSet;
import java.util.Random;
import java.util.Set;
import java.util.function.BooleanSupplier;

public final class SimulationEngineTest {
    public static void main(String[] args) {
        deletingPassengerClearsSeatAndWorkingList();
        deletingBusReturnsPassengerToPlatform();
        updatingAssignedPassengerRequeuesForNewDestination();
        identifiersAreTrimmedAndCaseInsensitive();
        simulationMaintainsInvariantsOverTime();
        terminalPanelPaintsHeadlessly();
        System.out.println("SimulationEngineTest: all checks passed");
    }

    private static void deletingPassengerClearsSeatAndWorkingList() {
        SimulationEngine engine = newEngine();
        Person passenger = engine.createPassenger(false, "Davao");
        Bus bus = loadingBus(engine, "Davao", 1);
        waitUntilSeated(engine, passenger);

        check(isPassengerOnBus(bus, passenger), "Passenger should occupy a bus seat");
        check(engine.removePassenger(passenger.id), "Passenger deletion should succeed");
        check(engine.findPassenger(passenger.id) == null, "Deleted passenger remains in working list");
        check(!isPassengerOnBus(bus, passenger), "Deleted passenger remains in a bus seat");
        checkEngineState(engine);
    }

    private static void deletingBusReturnsPassengerToPlatform() {
        SimulationEngine engine = newEngine();
        Person passenger = engine.createPassenger(true, "Davao");
        Bus bus = loadingBus(engine, "Davao", 1);
        waitUntilSeated(engine, passenger);

        check(engine.removeBus(bus.busId), "Bus deletion should succeed");
        check(passenger.assignedBus == null, "Passenger still references the deleted bus");
        check(
                passenger.state == PassengerState.WALKING_TO_PLATFORM,
                "Passenger was not sent back to the platform"
        );
        check(
                engine.platformPassengers().contains(passenger),
                "Passenger is missing from the platform queue"
        );
        checkEngineState(engine);
    }

    private static void updatingAssignedPassengerRequeuesForNewDestination() {
        SimulationEngine engine = newEngine();
        Person passenger = engine.createPassenger(true, "Davao");
        Bus bus = loadingBus(engine, "Davao", 1);
        waitUntilSeated(engine, passenger);

        check(
                engine.updatePassengerDestination(passenger.id, "Tagum"),
                "Destination update should succeed"
        );
        check("Tagum".equals(passenger.destination), "Destination was not updated");
        check(passenger.assignedBus == null, "Updated passenger remains assigned to old bus");
        check(!isPassengerOnBus(bus, passenger), "Updated passenger remains in old bus seat");
        check(
                engine.platformPassengers().contains(passenger),
                "Updated passenger was not requeued"
        );
        checkEngineState(engine);
    }

    private static void identifiersAreTrimmedAndCaseInsensitive() {
        SimulationEngine engine = newEngine();
        Person passenger = engine.createPassenger(false, "Tagum");
        check(
                engine.findPassenger("  " + passenger.id.toLowerCase() + "  ") == passenger,
                "Passenger ID lookup should ignore surrounding spaces and case"
        );
    }

    private static void simulationMaintainsInvariantsOverTime() {
        SimulationEngine engine = newEngine();
        engine.initialize();
        for (int tick = 0; tick < 7_500; tick++) {
            engine.update(SimulationConfig.FRAME_DELAY_MS);
            if (tick % 100 == 0) {
                checkEngineState(engine);
                for (Bus bus : engine.buses()) {
                    check(
                            bus.getPassengerCount() <= bus.capacity,
                            "Bus exceeded its passenger capacity"
                    );
                }
            }
        }
    }

    private static void terminalPanelPaintsHeadlessly() {
        SimulationEngine engine = newEngine();
        engine.initialize();
        TerminalPanel panel = new TerminalPanel(engine);
        panel.setSize(1050, 620);
        BufferedImage image = new BufferedImage(
                1050,
                620,
                BufferedImage.TYPE_INT_ARGB
        );
        Graphics2D graphics = image.createGraphics();
        try {
            panel.paint(graphics);
        } finally {
            graphics.dispose();
        }
    }

    private static SimulationEngine newEngine() {
        return new SimulationEngine(new Random(7), message -> {
        });
    }

    private static Bus loadingBus(SimulationEngine engine, String destination, int bay) {
        Bus bus = engine.addBusAtBay(destination, bay);
        check(bus != null, "Test bus could not be created");
        bus.x = 620;
        bus.state = BusState.LOADING;
        return bus;
    }

    private static void waitUntilSeated(SimulationEngine engine, Person passenger) {
        advanceUntil(
                engine,
                () -> passenger.state == PassengerState.SEATED_IN_BUS,
                1_500,
                "Passenger did not reach a bus seat"
        );
    }

    private static void advanceUntil(
            SimulationEngine engine,
            BooleanSupplier condition,
            int maximumTicks,
            String failureMessage) {
        for (int tick = 0; tick < maximumTicks && !condition.getAsBoolean(); tick++) {
            engine.update(SimulationConfig.FRAME_DELAY_MS);
        }
        check(condition.getAsBoolean(), failureMessage);
    }

    private static void check(boolean condition, String message) {
        if (!condition) {
            throw new AssertionError(message);
        }
    }

    private static boolean isPassengerOnBus(Bus bus, Person passenger) {
        if (bus.boardingLine.contains(passenger)) {
            return true;
        }
        for (Person seatedPassenger : bus.seats) {
            if (seatedPassenger == passenger) {
                return true;
            }
        }
        return false;
    }

    private static void checkEngineState(SimulationEngine engine) {
        Set<Person> known = new HashSet<Person>(engine.passengers());
        for (Bus bus : engine.buses()) {
            for (Person passenger : bus.boardingLine) {
                check(known.contains(passenger), "Unknown passenger in boarding line");
                check(passenger.assignedBus == bus, "Incorrect boarding-line assignment");
            }
            for (Person passenger : bus.seats) {
                if (passenger != null) {
                    check(known.contains(passenger), "Unknown passenger in bus seat");
                    check(passenger.assignedBus == bus, "Incorrect seat assignment");
                }
            }
        }
    }
}
