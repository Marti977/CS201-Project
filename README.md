# CS 201 Project 3 — Nota Hospital Simulation

## 1. How the Simulation Works

### Architecture & Data Structures
Our hospital simulation advances in discrete time steps (`ticks`) triggered manually by a button press in the JavaFX UI[cite: 1, 2]. All registered `Patient` and `Doctor` records are indexed in custom **B-Trees** keyed by a composite identifier (`name|age|dob`) so records remain unique even when two patients share the same name and age, while supporting $O(\log_t n)$ lookups[cite: 1].

To determine treatment order, waiting patients are stored in a Java **`PriorityQueue`** ordered by:
1. **Condition Severity (`conditionLevel` descending):** Critical patients (`10`) are always triaged ahead of lower-severity patients (`0`–`9`)[cite: 1].
2. **Time Remaining Before Critical (`criticalTimeLimit - timeWaited` ascending):** Among equally severe patients, whoever is closest to deteriorating into a critical state is treated first[cite: 1].
3. **Arrival Order (`arrivalTick` ascending):** First-in, first-out tie-breaking[cite: 1].

### Step-by-Step Tick Lifecycle
Each call to `stepTime()` executes four phases in order:
1. **Treatment Progression & Discharge:** Active doctors decrement `remainingTreatmentTicks`. When a treatment finishes (`0` ticks left), the patient's condition is restored to `0` (healthy), they move to the discharged list, and the doctor becomes available[cite: 1].
2. **Simultaneous Arrivals:** Every patient whose `arrivalTick` matches `currentTick` enters the waiting `PriorityQueue` simultaneously[cite: 1].
3. **Wait Tracking & Critical Escalation:** Each waiting patient's `timeWaited` increments by `1`. If `timeWaited >= criticalTimeLimit`, their `conditionLevel` immediately escalates to `10` (critical) and the priority queue re-heapifies so they jump to the front of triage[cite: 1].
4. **Shift Enforcement & Doctor Assignment:** The engine filters for doctors who are not busy and whose shift (`[shiftStart, shiftEnd)`) covers `currentTick`[cite: 1]. Available doctors are paired with the highest-priority waiting patients, with treatment duration computed as $\lceil \text{conditionLevel} / \text{skillLevel} \rceil$ ticks (higher-skill doctors treat patients faster)[cite: 1].

### Walkthrough Example
* **Tick 0:** `John Doe (DOB 1996-05-12, Cond 7, Limit 3)` and `John Doe (DOB 1996-11-03, Cond 4, Limit 5)` arrive simultaneously. `Dr. House (Skill 5)` and `Dr. Cuddy (Skill 3)` are on shift (`0–8`).
* **Tick 1:** Both waiting patients increment `timeWaited` to `1`. `Jane Smith (Cond 9, Limit 2)` and `Alex Rivera (Cond 6, Limit 4)` arrive simultaneously. Triage ranks `Jane (9)` first and `John Doe 1996-05-12 (7)` second. `Dr. House` takes `Jane` for $\lceil 9/5 \rceil = 2$ ticks, and `Dr. Cuddy` takes `John Doe` for $\lceil 7/3 \rceil = 3$ ticks.
* **Tick 3:** `Dr. House` finishes treating `Jane Smith`, discharging her at `Cond 0`, and picks up the highest-priority waiting patient on the next assignment pass.

### Visual Component
*(TODO — UI Member: Describe the JavaFX panels for waiting/active patients, tick advancement button, and B-Tree search bar here.)*

---

## 2. B-Tree Minimum Degree ($t$)
*(TODO — B-Tree Member: Document the chosen value of $t$ and why here.)*

---

## 3. Team Member Contributions
* **Marti Devanathan (`feat/sim-engine`):** Designed the initial project architecture, shared data models (`Patient`, `Doctor`), and `Makefile`; implemented `SimulationEngine` with `PriorityQueue` triage, simultaneous arrivals, critical-state (`10`) escalation timers, doctor shift enforcement, and skill-based treatment durations; wrote Report Question 1.
* **[B-Tree Teammate Name] (`feat/btree-data`):** *(Add contribution summary here)*
* **[UI Teammate Name] (`feat/ui-docs`):** *(Add contribution summary here)*