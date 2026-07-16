---
tags: [terminal-simulation, obsidian, claudian, codex]
---

# Obsidian and Claudian Setup

## What is already configured

- This repository is an Obsidian vault.
- Claudian 2.0.34 is installed locally for this vault.
- Codex is the selected provider.
- Codex is authenticated through your ChatGPT account.
- Claudian is in read-only mode for safe defense practice.
- The dashboard is [[00 - Defense Dashboard]].

## If Obsidian does not show this vault

1. Open the vault switcher in Obsidian.
2. Select **Open folder as vault**.
3. Choose the `Terminal-Simulation` repository folder.
4. If Obsidian asks whether you trust the vault, review the folder and approve it so Claudian can run.

## Open Claudian

1. Confirm **Settings → Community plugins → Claudian** is enabled.
2. Select the Claudian ribbon icon, or open the command palette with `Ctrl+P` and search for **Claudian**.
3. Confirm the provider shown in the chat is **Codex**.
4. Open [[05 - Claudian Mock Defense]] and copy the strict oral-defense prompt.

## Recommended safety setting

Keep **Codex sandbox mode** set to **Read only** tonight. It can inspect the vault and quiz you without changing Java files. Change it to workspace write only when you intentionally want Claudian to edit notes or code.

## First message to test the connection

```text
Read src/PassengerNode.java and src/PassengerQueue.java. Do not edit anything. Ask me one easy question about how our node-based queue works, then wait for my answer.
```

## If Codex is not detected

In **Settings → Claudian → Codex**, set the CLI path to:

```text
C:\Users\keith\AppData\Local\Programs\CodexCLI\codex.exe
```

The installed CLI version is `codex-cli 0.144.5`.
