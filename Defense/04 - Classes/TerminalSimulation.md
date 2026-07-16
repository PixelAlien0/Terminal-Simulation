
# TerminalSimulation

Source: [TerminalSimulation](https://github.com/PixelAlien0/Terminal-Simulation/blob/main/src/TerminalSimulation.java)

`TerminalSimulation` extends `JFrame` and acts as the application shell.

## Responsibilities

- Creates `SimulationEngine` and `TerminalPanel`
- Builds buttons, dialogs, passenger table, and event log
- Starts the Swing `Timer`
- Converts elapsed real time into fixed engine steps
- Calls `terminalPanel.repaint()`
- Routes CRUD button actions to engine methods

## Key methods

| Method | Purpose |
|---|---|
| constructor | Assembles the UI, initializes the engine, starts timer. |
| `createControls` | Creates buttons and action listeners. |
| `updateFrame` | Fixed-step update and repaint. |
| `showPassengers` | Read operation using a non-editable table. |
| CRUD dialog methods | Validate user choices and call engine operations. |
| `main` | Creates the window on the EDT. |

> [!WARNING]
> The timer controls *when* the engine updates; the engine controls *what* the simulation does.
