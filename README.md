# CS 201 Project 3 — Nota Hospital Simulation

## 1. How the Simulation Works

### Architecture & Data Structures
Our hospital simulation advances in discrete time steps (`ticks`) triggered manually by a button press in the JavaFX UI. All registered `Patient` and `Doctor` records are indexed in custom **B-Trees** keyed by a composite identifier (`name|age|dob`) so records remain unique even when two patients share the same name and age, while supporting $O(\log_t n)$ lookups.

To determine treatment order, waiting patients are stored in a Java **`PriorityQueue`** ordered by:
1. **Condition Severity (`conditionLevel` descending):** Critical patients (`10`) are always triaged ahead of lower-severity patients (`0`–`9`).
2. **Time Remaining Before Critical (`criticalTimeLimit - timeWaited` ascending):** Among equally severe patients, whoever is closest to deteriorating into a critical state is treated first.
3. **Arrival Order (`arrivalTick` ascending):** First-in, first-out tie-breaking.

### Step-by-Step Tick Lifecycle
Each call to `stepTime()` executes four phases in order:
1. **Treatment Progression & Discharge:** Active doctors decrement `remainingTreatmentTicks`. When a treatment finishes (`0` ticks left), the patient's condition is restored to `0` (healthy), they move to the discharged list, and the doctor becomes available.
2. **Simultaneous Arrivals:** Every patient whose `arrivalTick` matches `currentTick` enters the waiting `PriorityQueue` simultaneously.
3. **Wait Tracking & Critical Escalation:** Each waiting patient's `timeWaited` increments by `1`. If `timeWaited >= criticalTimeLimit`, their `conditionLevel` immediately escalates to `10` (critical) and the priority queue re-heapifies so they jump to the front of triage.
4. **Shift Enforcement & Doctor Assignment:** The engine filters for doctors who are not busy and whose shift (`[shiftStart, shiftEnd)`) covers `currentTick`. Available doctors are paired with the highest-priority waiting patients, with treatment duration computed as $\lceil \text{conditionLevel} / \text{skillLevel} \rceil$ ticks (higher-skill doctors treat patients faster).

### Walkthrough Example (Default CSV Dataset)
- **Tick 0:** `Jane Smith (Cond 9, Limit 2)`, `John Doe (DOB 1996-05-12, Cond 7, Limit 5)`, and `John Doe (DOB 1996-11-03, Cond 4, Limit 8)` are queued. `Dr. Baker (Skill 5)` and `Dr. Cook (Skill 4)` are on shift (`0–12`).
- **Tick 1:** `James Wilson (Cond 8)` and `Maria Garcia (Cond 6)` arrive simultaneously. Triage ranks `Jane Smith (9)` and `James Wilson (8)` at the top. `Dr. Baker` takes `Jane Smith` for $\lceil 9/5 \rceil = 2$ ticks, and `Dr. Cook` takes `James Wilson` for $\lceil 8/4 \rceil = 2$ ticks.
- **Tick 2:** `Dr. Evans (Skill 3, Shift 2–16)` starts his shift and immediately takes `John Doe (1996-05-12, Cond 7)` for $\lceil 7/3 \rceil = 3$ ticks.
- **Tick 3–4:** `Dr. Baker` and `Dr. Cook` discharge `Jane Smith` and `James Wilson` at `Cond 0`. At Tick 4, `Michael Taylor` arrives with `Cond 9` and `Limit 1`, immediately escalating to `Cond 10` (critical) and jumping to the front of the waiting queue.

### Visual Component
The JavaFX interface (`ui.Main`) provides a four-panel live dashboard alongside a top control bar and bottom B-Tree search bar:
- **Top Control Bar:** Displays `Current Tick`, a **Next Tick (+1)** button to advance the simulation step-by-step, and a **Reload CSV Datasets** button to reset and reload `data/patients.csv` and `data/doctors.csv`.
- **Four-Panel Dashboard:** Displays **Waiting Queue (Priority Triage Order)**, **Active Treatments (Being Seen)**, **On-Shift Doctors** (showing shift windows and `[Available]`/`[Busy]` status), and **Tick Event Log & Discharged Patients**.
- **B-Tree Record Lookup Bar:** Queries the underlying `Patient` and `Doctor` B-Trees directly by composite key (`Name`, `Age`, and `DOB`) and validates malformed inputs.

---

## 2. B-Tree Minimum Degree ($t$)
*(TODO — B-Tree Teammate: Document why $t = 3$ was chosen for the B-Tree minimum degree here.)*

---

## 3. Team Member Contributions
> **Team Roster Note:** Our original third team member dropped the course during development. All remaining UI, UML, and integration deliverables were absorbed and completed by Martin Devanathan.

- **Martin Devanathan (`feat/sim-engine`, `feat/ui-docs`):** Designed the initial project architecture, shared data models (`Patient`, `Doctor`), and `Makefile`; implemented `SimulationEngine` (`PriorityQueue` triage, simultaneous arrivals, critical-state escalation, doctor shift enforcement, and skill-based treatment durations); built the JavaFX graphical dashboard and B-Tree search interface (`ui.Main`); created the UML Class Diagram (`docs/uml_diagram.md`); and authored Report Question 1.
- **[B-Tree Teammate Name] (`feat/btree-data`):** Implemented the custom generic `BTree` and `BTreeNode` structures with node splitting and composite-key search, built `DatasetLoader` and the CSV datasets (`data/patients.csv`, `data/doctors.csv`), and authored Report Question 2.
- **Note on Commit History:** Core simulation and UI modules were implemented and tested locally in focused working sessions, then staged and pushed as separate commits by logical component (`feat`, `fix`, `docs`) to keep the repository history clean and reviewable.