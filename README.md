# Terminal Simulation

A Java Swing desktop simulation of a bus terminal. It animates passengers moving
through ticket queues and platform areas, gives priority passengers preferential
boarding, and manages buses across four terminal bays.

## Features

- Custom node-based FIFO queue implementation
- Regular and priority passenger queues
- Ticket-booth controls
- Animated passenger movement
- Bus arrival, loading, countdown, and departure states
- Passenger and bus CRUD controls
- Davao and Tagum destinations
- On-screen event log and departure board

## Requirements

- JDK 8 or newer

## Compile and run

From the repository root:

~~~bash
mkdir out
javac -encoding UTF-8 -d out src/TerminalSimulation.java
java -cp out TerminalSimulation
~~~

All project classes are in `src/TerminalSimulation.java`. Java allows this
because only `TerminalSimulation` is public; the supporting classes are
package-private classes in the same file.

On PowerShell, the same Java commands work after creating the output directory:

~~~powershell
New-Item -ItemType Directory -Force out
javac -encoding UTF-8 -d out src\TerminalSimulation.java
java -cp out TerminalSimulation
~~~

## Browser version

The same compiled Swing application can also run in a modern browser through
[CheerpJ](https://cheerpj.com/). Open the published version here:

**[Run Terminal Simulation in the browser](https://pixelalien0.github.io/Terminal-Simulation/)**

Select **Start simulation** and wait while the browser downloads the Java
runtime. The first launch can take longer. Internet access is required.

The browser page is only a launcher. The simulation, queue, nodes, models, and
drawing code remain Java. The normal desktop commands above are still the most
reliable option for the project defense.

GitHub Pages publishes the browser launcher from the `docs` directory. Its
`terminal-simulation.jar` is compiled for Java 8 compatibility with CheerpJ.
After changing the Java source, rebuild that JAR on PowerShell and commit it:

~~~powershell
.\scripts\build-browser.ps1
~~~

## Defense study guide

Start with the **[GitHub Defense Study Guide](Defense/README.md)** for the
recommended study order, queue traces, program-flow diagrams, class guides,
practice questions, and test evidence. It works directly on GitHub and does not
require Obsidian.

See [STUDY_GUIDE.md](STUDY_GUIDE.md) for a code-centered walkthrough of the
actual runtime flow, collection changes, state transitions, cleanup rules, and
defense questions.

See [CODE_WALKTHROUGH.md](CODE_WALKTHROUGH.md) for a complete beginner-friendly
explanation of every class, field group, method, drawing section,
queue operation, runtime trace, and test.

See [CODE_BLOCK_WALKTHROUGH.md](CODE_BLOCK_WALKTHROUGH.md) for the snippet-first
version: each important source block is shown first and then explained line by
line in plain language.

## Project structure

The program uses one source file, `src/TerminalSimulation.java`, divided into
clearly labeled class sections:

- TerminalSimulation builds the Swing window and handles user input.
- TerminalPanel renders the pixel-art terminal.
- SimulationEngine owns simulation state and transitions.
- PassengerNode and PassengerQueue implement the assigned node-based FIFO queue.
- Person and Bus are the simulation models.
- SimulationConfig contains timing and layout constants.

## Run the regression checks

~~~bash
rm -rf out && mkdir out
javac -encoding UTF-8 -d out src/TerminalSimulation.java test/*.java
java -ea -cp out PassengerQueueTest
java -ea -cp out SimulationEngineTest
~~~
