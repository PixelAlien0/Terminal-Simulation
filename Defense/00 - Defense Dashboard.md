---
tags: [terminal-simulation, defense, dashboard]
---

# Terminal Simulation — Defense Dashboard

> [!important] Goal
> Explain how the code works in your own words. Do not memorize every line.

## Start here

1. Read [[01 - One-Minute Project Explanation]].
2. Master [[02 - Queue, Node, and FIFO]].
3. Trace [[03 - Complete Program Flow]].
4. Test yourself with [[04 - Rapid Recall Questions]].
5. Use [[05 - Claudian Mock Defense]] for spoken practice.
6. Review [[06 - Honest Limitations and Improvements]].

## Existing complete references

- [[STUDY_GUIDE|Full defense study guide]]
- [[CODE_WALKTHROUGH|Complete beginner walkthrough]]
- [[CODE_BLOCK_WALKTHROUGH|Code-block walkthrough]]
- [[README|Project overview and run commands]]

## Tonight's focused plan

- [ ] **20 minutes:** Read the one-minute explanation and say it without looking.
- [ ] **40 minutes:** Draw `front -> node -> node -> rear` and explain enqueue/dequeue.
- [ ] **40 minutes:** Trace startup, one timer update, ticket service, and boarding.
- [ ] **30 minutes:** Each member chooses two Java files to explain.
- [ ] **45 minutes:** Run a strict mock defense with follow-up “how” questions.
- [ ] **20 minutes:** Review limitations, then stop adding features.

## Confidence checklist

- [ ] I can define queue, FIFO, node, front, and rear.
- [ ] I can explain why `PassengerQueue` is custom instead of `java.util.Queue`.
- [ ] I can explain what every Java class is responsible for.
- [ ] I can trace a passenger from creation to a bus seat.
- [ ] I can explain why enums are used.
- [ ] I can explain the Swing timer and repaint cycle.
- [ ] I can admit a limitation and suggest a reasonable improvement.

## Run the project

```powershell
New-Item -ItemType Directory -Force out
$sources = (Get-ChildItem src -Filter *.java).FullName
javac -encoding UTF-8 -d out $sources
java -cp out TerminalSimulation
```

Browser version: <https://pixelalien0.github.io/Terminal-Simulation/>
