# Recorded Presentation Script

This is a simple **6–7 minute spoken script**, not a PowerPoint presentation.
Record the running simulation and the GitHub source while reading the script in
your own natural voice. You may divide the numbered parts among group members.

> [!IMPORTANT]
> Practice the ideas instead of memorizing every sentence. It is fine to use
> simpler words as long as the explanation stays accurate.

## Before recording

1. Open the [single Java source](../src/TerminalSimulation.java) on GitHub.
2. Run the desktop application using the [README instructions](../README.md#compile-and-run).
3. Make the simulation window and text large enough to see in the recording.
4. Close unrelated windows and notifications.
5. Test your microphone before recording the full presentation.

## Part 1 — Introduction (about 30 seconds)

**Show:** The running terminal simulation.

**Say:**

> Good day. Our project is a Java Swing bus-terminal simulation. Its main Data
> Structures and Algorithms topic is a queue. We created our own FIFO queue
> using linked nodes instead of using Java's built-in Queue. The program shows
> passengers lining up for tickets, moving to the platform, boarding buses, and
> leaving the terminal. The animation helps us show how queue operations work in
> a real situation.

## Part 2 — Demonstrate the animation (about 1 minute)

**Show:** Let the simulation run. Point to the ticket booth, two passenger
lines, platform, buses, and event log. Add one regular passenger and one priority
passenger.

**Say:**

> On the left is the ticket booth. Passengers first enter either the regular
> line or the priority line. After buying a ticket, they move to the platform.
> Buses arrive at the bays on the right. A passenger boards only when the bus
> destination matches the passenger's destination. The log records important
> actions while the status panel shows the current queue sizes.
>
> These buttons let us add regular and priority passengers. The other controls
> can pause buses, close the ticket booth, and perform create, read, update, and
> delete operations for passengers and buses.

**Optional demonstration:** Close the ticket booth briefly, show that service
pauses, and then open it again. Do not spend time demonstrating every button.

## Part 3 — Explain the file and main classes (about 40 seconds)

**Show:** Open `src/TerminalSimulation.java` on GitHub and use the file outline
or browser search to locate the class names.

**Say:**

> Our group chose one Java source file so it is easier to open and present. The
> program still contains separate classes. `TerminalSimulation` creates the
> window and controls. `SimulationEngine` contains the terminal rules.
> `TerminalPanel` draws the animation. `Person` and `Bus` store the simulation
> objects. `PassengerNode` and `PassengerQueue` implement our assigned data
> structure. Only `TerminalSimulation` is public, which is why Java allows these
> classes to stay in one file.

## Part 4 — Explain the node and FIFO queue (about 1 minute 30 seconds)

**Show:** Search for `class PassengerNode`, then `class PassengerQueue`. Slowly
highlight `front`, `rear`, `enqueue`, and `dequeue`.

**Say:**

> A `PassengerNode` stores one `Person` reference and a `next` reference. The
> `next` field connects one passenger node to the following node.
>
> `PassengerQueue` stores three important fields. `front` points to the first
> node, `rear` points to the last node, and `size` stores the number of nodes.
>
> The `enqueue` method adds a passenger at the rear. It creates a new node. If
> the queue is empty, both front and rear point to that node. Otherwise, the old
> rear points to the new node, and rear moves to the new last node.
>
> The `dequeue` method removes from the front. It saves the current front, moves
> front to the next node, and decreases size. If that was the last node, rear is
> also set to null. Because passengers enter at the rear and leave at the front,
> the queue follows FIFO, or First In, First Out.

**Show this quick drawing if helpful:**

```text
front                              rear
  |                                  |
  v                                  v
[P1 | next] -> [P2 | next] -> [P3 | null]
```

## Part 5 — Connect the queue to passenger creation (about 45 seconds)

**Show:** Search for `createPassenger(boolean priority, String destination)`.

**Say:**

> When a passenger is created, the engine gives the passenger an ID,
> destination, position, type, and arrival order. The method selects the regular
> or priority ticket lane and calls `enqueue`. It also adds the passenger to the
> master passenger list. The queue controls service order, while the master list
> lets the engine update and draw every active passenger.

## Part 6 — Explain how the animation runs (about 1 minute)

**Show:** Search for `main`, `updateFrame`, `update`, and `paintComponent` in
that order. Return briefly to the moving simulation.

**Say:**

> The `main` method starts the Swing window on the Event Dispatch Thread. A Swing
> timer repeatedly calls `updateFrame`. This method measures elapsed time and
> advances the engine in small fixed steps.
>
> `SimulationEngine.update` processes spawning, buses, both ticket lanes, and
> passenger movement. After the data changes, `repaint` asks Swing to call
> `paintComponent`. The panel then draws the background, buses, passengers,
> ticket booth, schedule, and status panel using their latest positions.
>
> In simple terms, the engine changes the data first, and the panel draws the
> result. Repeating this process creates the animation.

## Part 7 — Explain boarding and bus movement (about 50 seconds)

**Show:** A bus loading passengers. In the source, search for `loadBus` and
`enum BusState`.

**Say:**

> During loading, the engine looks for passengers whose destination matches the
> bus. Priority passengers are considered first. Regular passengers selected
> for that bus enter its custom boarding queue. At each boarding interval, the
> front passenger is dequeued and assigned an available seat.
>
> A bus moves through the states arriving, loading, waiting for departure,
> departing, and departed. When it leaves the screen, the engine removes it and
> cleans up its passenger references. The fixed seat array prevents the bus from
> exceeding its capacity.

## Part 8 — Conclusion (about 30 seconds)

**Show:** Return to the full running simulation.

**Say:**

> To conclude, our project demonstrates a node-based FIFO queue through a Java
> Swing terminal animation. The linked queue controls passenger order, the
> simulation engine applies the rules, and the panel visualizes the result. We
> also tested the queue operations, link repair, empty-queue reuse, bus capacity,
> cleanup, and long simulation runs. Thank you for watching our presentation.

## Suggested division for group members

| Member | Parts |
|---|---|
| Member 1 | Introduction and animation demonstration |
| Member 2 | Classes, node, and FIFO queue |
| Member 3 | Passenger creation and animation loop |
| Member 4 | Bus boarding and conclusion |

If your group has fewer members, combine adjacent parts. If it has more members,
split Part 4 between the node explanation and queue operations.

## Recording checklist

- [ ] The simulation is visible and moving.
- [ ] The mouse points to the feature being discussed.
- [ ] The source is zoomed in enough to read.
- [ ] `PassengerNode`, `PassengerQueue`, `enqueue`, and `dequeue` are shown.
- [ ] The narration explains FIFO in simple words.
- [ ] Every member speaks clearly and slowly.
- [ ] The final recording has understandable audio.
- [ ] The group watches the complete video once before submitting it.

## Short version if the recording must be under 4 minutes

Keep Parts 1, 2, 4, 6, and 8. In Part 2, demonstrate only one regular passenger
and one priority passenger. In Part 4, explain only the normal enqueue and
dequeue cases plus the final-node `rear = null` rule.
