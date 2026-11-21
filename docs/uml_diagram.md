# Nota Hospital Simulation — UML Class Diagram

```mermaid
classDiagram
    class Main {
        -SimulationEngine engine
        -Label tickLabel
        -ListView~String~ waitingList
        -ListView~String~ activeList
        -ListView~String~ doctorList
        -ListView~String~ eventAndDischargedList
        +start(Stage primaryStage) void
        -refreshUI() void
        +main(String[] args)\$ void
    }

    class SimulationEngine {
        -int currentTick
        -BTree~Patient~ patientTree
        -BTree~Doctor~ doctorTree
        -PriorityQueue~Patient~ waitingQueue
        -List~Patient~ scheduledArrivals
        -List~Patient~ activePatients
        -List~Patient~ dischargedPatients
        -List~Doctor~ allDoctors
        +loadDatasets(String patientFile, String doctorFile) void
        +stepTime() void
        +isDoctorOnShift(Doctor doc, int tick) boolean
        +getWaitingPatients() List~Patient~
        +getActivePatients() List~Patient~
        +getActiveDoctors() List~Doctor~
        +lookupPatient(String name, int age, String dob) Patient
        +lookupDoctor(String name, int age, String dob) Doctor
    }

    class DatasetLoader {
        +loadPatients(String fileName)\$ List~Patient~
        +loadDoctors(String fileName)\$ List~Doctor~
    }

    class BTree~T~ {
        -int minimumDegree
        -BTreeNode~T~ root
        +BTree(int minimumDegree)
        +insert(T item) void
        +search(T item) T
        +getDegree() int
    }

    class BTreeNode~T~ {
        ~Object[] keys
        ~BTreeNode~T~[] children
        ~int keyCount
        ~boolean isLeaf
        ~BTreeNode(int minimumDegree, boolean isLeaf)
        ~getKey(int index) T
    }

    class Patient {
        +String name
        +int age
        +String dob
        +int conditionLevel
        +int criticalTimeLimit
        +int arrivalTick
        +int timeWaited
        +boolean isBeingSeen
        +boolean isDischarged
        +getKey() String
        +compareTo(Patient other) int
    }

    class Doctor {
        +String name
        +int age
        +String dob
        +int skillLevel
        +int shiftStart
        +int shiftEnd
        +boolean isBusy
        +int remainingTreatmentTicks
        +String currentPatientKey
        +getKey() String
        +compareTo(Doctor other) int
    }

    Main --> SimulationEngine : drives & queries
    SimulationEngine --> DatasetLoader : loads CSVs via
    SimulationEngine o-- "2" BTree : indexes records
    SimulationEngine o-- "*" Patient : manages triage & state
    SimulationEngine o-- "*" Doctor : schedules shifts
    BTree *-- "*" BTreeNode : contains
    BTree ..> Patient : stores
    BTree ..> Doctor : stores