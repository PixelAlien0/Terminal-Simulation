const startButton = document.querySelector("#start-button");
const fullscreenButton = document.querySelector("#fullscreen-button");
const statusText = document.querySelector("#status");
const displayShell = document.querySelector("#display-shell");
const displayParent = document.querySelector("#java-display");
const placeholder = document.querySelector("#display-placeholder");

let started = false;

function wait(milliseconds) {
  return new Promise((resolve) => window.setTimeout(resolve, milliseconds));
}

async function waitForSwingWindow(timeoutMilliseconds) {
  const deadline = Date.now() + timeoutMilliseconds;

  while (Date.now() < deadline) {
    const display = document.querySelector("#cheerpjDisplay");
    if (display && !display.classList.contains("cheerpjLoading")) {
      return;
    }
    await wait(250);
  }

  throw new Error("The Swing window did not appear before the timeout.");
}

async function startSimulation() {
  if (started) {
    return;
  }

  started = true;
  startButton.disabled = true;
  startButton.textContent = "Starting…";
  statusText.className = "status loading";
  statusText.textContent = "Loading the browser Java runtime…";

  try {
    await cheerpjInit({ version: 8, status: "splash" });
    placeholder.hidden = true;
    cheerpjCreateDisplay(1400, 780, displayParent);

    const jarUrl = new URL("terminal-simulation.jar", window.location.href);
    const cheerpjJarPath = `/app${jarUrl.pathname}`;

    statusText.textContent = "Starting the Java main class…";
    const javaProcess = cheerpjRunMain(
      "TerminalSimulation",
      cheerpjJarPath
    );

    statusText.textContent = "Waiting for the Swing window…";
    const javaStoppedEarly = javaProcess.then(
      () => Promise.reject(new Error("Java stopped before opening the window.")),
      (error) => Promise.reject(error)
    );
    await Promise.race([
      waitForSwingWindow(120000),
      javaStoppedEarly
    ]);

    startButton.textContent = "Simulation running";
    fullscreenButton.disabled = false;
    statusText.className = "status success";
    statusText.textContent = "The Java Swing simulation is running below.";
  } catch (error) {
    console.error(error);
    started = false;
    startButton.disabled = false;
    startButton.textContent = "Try again";
    statusText.className = "status error";
    statusText.textContent =
      "The simulation could not start. Check the internet connection, then try again.";
  }
}

async function toggleFullscreen() {
  try {
    if (document.fullscreenElement) {
      await document.exitFullscreen();
    } else {
      await displayShell.requestFullscreen();
    }
  } catch (error) {
    console.error(error);
    statusText.className = "status error";
    statusText.textContent = "Full-screen mode is not available in this browser.";
  }
}

function updateFullscreenLabel() {
  fullscreenButton.textContent = document.fullscreenElement
    ? "Exit full screen"
    : "Full screen";
}

startButton.addEventListener("click", startSimulation);
fullscreenButton.addEventListener("click", toggleFullscreen);
document.addEventListener("fullscreenchange", updateFullscreenLabel);
