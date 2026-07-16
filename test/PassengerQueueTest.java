public final class PassengerQueueTest {
    public static void main(String[] args) {
        runAll();
        System.out.println("PassengerQueueTest: all checks passed");
    }

    static void runAll() {
        queueUsesLinkedNodesAndFifoOrder();
        removalRepairsFrontMiddleAndRearLinks();
        emptyQueueCanBeReused();
        nullPassengerIsRejected();
    }

    private static void queueUsesLinkedNodesAndFifoOrder() {
        PassengerQueue queue = new PassengerQueue();
        Person first = passenger("P1", 1);
        Person second = passenger("P2", 2);
        Person third = passenger("P3", 3);

        queue.enqueue(first);
        queue.enqueue(second);
        queue.enqueue(third);

        PassengerNode front = queue.frontNode();
        check(front.passenger == first, "Front node should contain the first passenger");
        check(front.next.passenger == second, "First node should link to the second node");
        check(front.next.next.passenger == third, "Second node should link to the third node");
        check(front.next.next.next == null, "Rear node should point to null");
        check(queue.size() == 3, "Queue size should be three");
        check(queue.peek() == first, "Peek should return the front passenger");
        check(queue.dequeue() == first, "First passenger should leave first");
        check(queue.dequeue() == second, "Second passenger should leave second");
        check(queue.dequeue() == third, "Third passenger should leave third");
    }

    private static void removalRepairsFrontMiddleAndRearLinks() {
        PassengerQueue queue = new PassengerQueue();
        Person first = passenger("P1", 1);
        Person second = passenger("P2", 2);
        Person third = passenger("P3", 3);
        Person fourth = passenger("P4", 4);

        queue.enqueue(first);
        queue.enqueue(second);
        queue.enqueue(third);
        queue.enqueue(fourth);

        check(queue.remove(second), "Middle passenger should be removable for CRUD cleanup");
        check(!queue.contains(second), "Removed middle passenger remains in the queue");
        check(queue.remove(first), "Front passenger should be removable for CRUD cleanup");
        check(queue.peek() == third, "Front should advance after removing the front passenger");
        check(queue.remove(fourth), "Rear passenger should be removable for CRUD cleanup");
        check(queue.size() == 1, "Only one passenger should remain");
        check(queue.dequeue() == third, "Remaining passenger should still be reachable");
    }

    private static void emptyQueueCanBeReused() {
        PassengerQueue queue = new PassengerQueue();
        Person first = passenger("P1", 1);
        Person second = passenger("P2", 2);

        queue.enqueue(first);
        check(queue.dequeue() == first, "Single passenger should be dequeued");
        check(queue.isEmpty(), "Queue should be empty after its last dequeue");
        check(queue.size() == 0, "Empty queue size should be zero");
        check(queue.peek() == null, "Peek on an empty queue should return null");
        check(queue.dequeue() == null, "Dequeue on an empty queue should return null");

        queue.enqueue(second);
        check(queue.peek() == second, "Queue should work after becoming empty");
        check(queue.frontNode().next == null, "Reused queue should have one rear node");
    }

    private static void nullPassengerIsRejected() {
        PassengerQueue queue = new PassengerQueue();
        boolean rejected = false;
        try {
            queue.enqueue(null);
        } catch (IllegalArgumentException expected) {
            rejected = true;
        }
        check(rejected, "Queue should reject a null passenger");
    }

    private static Person passenger(String id, int order) {
        return new Person(id, "Davao", 0, 0, false, order);
    }

    private static void check(boolean condition, String message) {
        if (!condition) {
            throw new AssertionError(message);
        }
    }
}
