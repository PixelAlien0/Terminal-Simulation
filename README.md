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
javac -encoding UTF-8 -d out src/*.java
java -cp out TerminalSimulation
~~~

On PowerShell, the same Java commands work after creating the output directory:

~~~powershell
New-Item -ItemType Directory -Force out
$sources = (Get-ChildItem src -Filter *.java).FullName
javac -encoding UTF-8 -d out $sources
java -cp out TerminalSimulation
~~~

This is a desktop Swing application, so it does not run through GitHub Pages.

## Defense study guide

See [STUDY_GUIDE.md](STUDY_GUIDE.md) for a code-centered walkthrough of the
actual runtime flow, collection changes, state transitions, cleanup rules, and
defense questions.

See [CODE_WALKTHROUGH.md](CODE_WALKTHROUGH.md) for a complete beginner-friendly
explanation of every Java file, class, field group, method, drawing section,
queue operation, runtime trace, and test.

See [CODE_BLOCK_WALKTHROUGH.md](CODE_BLOCK_WALKTHROUGH.md) for the snippet-first
version: each important source block is shown first and then explained line by
line in plain language.

## Project structure

- TerminalSimulation builds the Swing window and handles user input.
- TerminalPanel renders the pixel-art terminal.
- SimulationEngine owns simulation state and transitions.
- PassengerNode and PassengerQueue implement the assigned node-based FIFO queue.
- Person and Bus are the simulation models.
- SimulationConfig contains timing and layout constants.

## Run the regression checks

~~~bash
rm -rf out && mkdir out
javac -encoding UTF-8 -d out src/*.java test/*.java
java -ea -cp out PassengerQueueTest
java -ea -cp out SimulationEngineTest
~~~
