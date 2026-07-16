---
tags: [terminal-simulation, runtime, flow]
---

# Complete Program Flow

## 1. Startup

```text
TerminalSimulation.main
  -> SwingUtilities.invokeLater
  -> new TerminalSimulation
  -> new SimulationEngine
  -> new TerminalPanel
  -> create controls and layout
  -> engine.initialize
  -> create 22 starting passengers
  -> start Swing Timer
  -> show JFrame
```

`SwingUtilities.invokeLater` schedules Swing window creation on the Event Dispatch Thread, the thread Swing uses for user-interface work.

## 2. One frame

```text
Swing Timer fires
  -> TerminalSimulation.updateFrame
  -> calculate elapsed time
  -> SimulationEngine.update
  -> update passengers, queues, and buses
  -> TerminalPanel.repaint
  -> paintComponent draws current state
```

The engine changes data. The panel reads that data and draws it. This separation is important.

## 3. Passenger journey

```text
created
  -> enqueued in regular or priority ticket queue
  -> served at ticket booth
  -> moves to platform
  -> waits for matching destination
  -> boards matching bus
  -> receives a seat
  -> leaves the active terminal when the bus departs
```

The passenger's `PassengerState` enum records which stage is currently valid.

## 4. Bus journey

```text
bus created in a free bay
  -> arrives
  -> accepts matching passengers
  -> loading countdown decreases
  -> departs
  -> passengers and bay are cleaned up
```

The bus's `BusState` enum prevents random state text and makes transitions readable.

## 5. Responsibility map

| File | Main responsibility |
|---|---|
| `TerminalSimulation.java` | Window, controls, dialogs, log, Swing timer |
| `TerminalPanel.java` | Pixel-art drawing |
| `SimulationEngine.java` | Simulation rules and changing collections |
| `PassengerQueue.java` | Custom node-based FIFO operations |
| `PassengerNode.java` | Passenger data plus `next` link |
| `Person.java` | Passenger model, position, state, drawing |
| `Bus.java` | Bus model, seats, timing, drawing |
| `PassengerState.java` | Allowed passenger states |
| `BusState.java` | Allowed bus states |
| `SimulationConfig.java` | Shared timing, capacity, and layout constants |

## Separation sentence for defense

> `SimulationEngine` decides what happens; `TerminalPanel` draws what happened; `TerminalSimulation` connects the engine to the Swing interface.
