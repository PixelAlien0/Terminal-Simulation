
# Startup Trace

```text
TerminalSimulation.main
  → SwingUtilities.invokeLater
  → new TerminalSimulation
  → configure log and JFrame
  → new SimulationEngine(this::appendLog)
  → new TerminalPanel(engine)
  → create controls and layout
  → engine.initialize()
  → create 22 initial passengers
  → start Swing Timer at 16 ms
  → setVisible(true)
```

## Evidence

- `main` schedules window creation with `SwingUtilities.invokeLater`.
- The constructor creates the engine before the panel so the panel can receive it.
- `engine.initialize()` loops 22 times and calls `createRandomPassenger`.
- The timer calls `updateFrame` using `SimulationConfig.FRAME_DELAY_MS`.

<details>
<summary><strong>Why use `SwingUtilities.invokeLater`?</strong></summary>

Swing expects interface creation and updates to occur on its Event Dispatch Thread.

</details>

Source: [TerminalSimulation](https://github.com/PixelAlien0/Terminal-Simulation/blob/main/src/TerminalSimulation.java) · [TerminalSimulation](https://github.com/PixelAlien0/Terminal-Simulation/blob/main/src/TerminalSimulation.java) · [TerminalSimulation](https://github.com/PixelAlien0/Terminal-Simulation/blob/main/src/TerminalSimulation.java)
