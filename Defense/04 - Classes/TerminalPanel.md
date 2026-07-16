
# TerminalPanel

Source: [TerminalSimulation](https://github.com/PixelAlien0/Terminal-Simulation/blob/main/src/TerminalSimulation.java)

`TerminalPanel` extends `JPanel` and renders the engine's current state.

## `paintComponent` order

1. Call `super.paintComponent(graphics)`.
2. Create a separate `Graphics2D` context.
3. Draw background, waiting areas, booth, and schedule.
4. Draw buses.
5. Copy and sort passengers by `y` for visual depth.
6. Draw passengers except those already seated.
7. Draw the status panel.
8. Dispose the copied graphics context.

> [!IMPORTANT]
> **Separation**
> Drawing reads `engine.buses()` and `engine.passengers()`. It does not decide ticket order, boarding eligibility, or state transitions.

<details>
<summary><strong>Why copy the passenger list before sorting?</strong></summary>

Sorting the copy changes only drawing order and does not reorder the engine's master collection.

</details>
