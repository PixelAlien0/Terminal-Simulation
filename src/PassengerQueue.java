final class PassengerQueue {
    private PassengerNode front;
    private PassengerNode rear;
    private int size;

    void enqueue(Person passenger) {
        if (passenger == null) {
            throw new IllegalArgumentException("Passenger cannot be null");
        }

        PassengerNode newNode = new PassengerNode(passenger);
        if (rear == null) {
            front = newNode;
            rear = newNode;
        } else {
            rear.next = newNode;
            rear = newNode;
        }
        size++;
    }

    Person dequeue() {
        if (front == null) {
            return null;
        }

        PassengerNode removedNode = front;
        front = front.next;
        removedNode.next = null;
        size--;

        if (front == null) {
            rear = null;
        }
        return removedNode.passenger;
    }

    Person peek() {
        return front == null ? null : front.passenger;
    }

    boolean remove(Person passenger) {
        PassengerNode previous = null;
        PassengerNode current = front;

        while (current != null) {
            if (current.passenger == passenger) {
                if (previous == null) {
                    front = current.next;
                } else {
                    previous.next = current.next;
                }
                if (current == rear) {
                    rear = previous;
                }
                current.next = null;
                size--;
                return true;
            }
            previous = current;
            current = current.next;
        }
        return false;
    }

    boolean contains(Person passenger) {
        PassengerNode current = front;
        while (current != null) {
            if (current.passenger == passenger) {
                return true;
            }
            current = current.next;
        }
        return false;
    }

    boolean isEmpty() {
        return front == null;
    }

    int size() {
        return size;
    }

    PassengerNode frontNode() {
        return front;
    }
}
