# Sui Move Plugin v2.0 - Architecture Diagrams

## Component Interaction Diagram

```mermaid
graph TB
    subgraph "IntelliJ Platform"
        IDE[IntelliJ IDEA]
        PSI[PSI Infrastructure]
        EP[Extension Points]
    end
    
    subgraph "Core Layer"
        Parser[Move Parser]
        Lexer[Move Lexer]
        AST[AST/PSI Tree]
        Parser --> AST
        Lexer --> Parser
    end
    
    subgraph "Type System"
        TIE[Type Inference Engine]
        TC[Type Cache]
        TR[Type Resolver]
        TIE --> TC
        TR --> TIE
    end
    
    subgraph "Code Intelligence"
        CC[Completion Contributor]
        REF[Reference Provider]
        INSP[Inspections]
        CC --> TIE
        REF --> TR
        INSP --> TIE
    end
    
    subgraph "Sui Integration"
        SF[Sui Framework]
        CLI[Sui CLI Bridge]
        NET[Network Service]
        SF --> TIE
        CLI --> NET
    end
    
    subgraph "Developer Tools"
        DBG[Debugger]
        TEST[Test Runner]
        REFACT[Refactoring]
        DBG --> CLI
        TEST --> CLI
    end
    
    IDE --> EP
    EP --> Parser
    AST --> TIE
    AST --> CC
    AST --> REF
```

## Type Resolution Flow

```mermaid
sequenceDiagram
    participant User
    participant Editor
    participant PSI
    participant TypeEngine
    participant Cache
    participant Framework
    
    User->>Editor: Types code
    Editor->>PSI: Parse text
    PSI->>TypeEngine: Request type
    TypeEngine->>Cache: Check cache
    
    alt Cache hit
        Cache-->>TypeEngine: Return cached type
    else Cache miss
        TypeEngine->>PSI: Analyze context
        TypeEngine->>Framework: Resolve imports
        Framework-->>TypeEngine: Type info
        TypeEngine->>Cache: Store result
    end
    
    TypeEngine-->>Editor: Type information
    Editor-->>User: Show hints/completion
```

## Code Completion Architecture

```mermaid
graph LR
    subgraph "Completion Request"
        CUR[Cursor Position]
        CTX[Context Analysis]
        CUR --> CTX
    end
    
    subgraph "Providers"
        KW[Keyword Provider]
        TYPE[Type Provider]
        FUNC[Function Provider]
        IMP[Import Provider]
    end
    
    subgraph "Processing"
        FILT[Filter by Context]
        RANK[Rank by Relevance]
        PRES[Presentation]
    end
    
    CTX --> KW
    CTX --> TYPE
    CTX --> FUNC
    CTX --> IMP
    
    KW --> FILT
    TYPE --> FILT
    FUNC --> FILT
    IMP --> FILT
    
    FILT --> RANK
    RANK --> PRES
    PRES --> UI[Completion Popup]
```

## Service Layer Architecture

```mermaid
classDiagram
    class PluginServices {
        <<interface>>
    }
    
    class ApplicationServices {
        +MoveTypeService
        +SuiFrameworkService
        +MoveLanguageService
    }
    
    class ProjectServices {
        +MoveProjectService
        +MoveIndexService
        +MoveBuildService
    }
    
    class MoveTypeService {
        +inferType(element)
        +resolveGeneric(type, context)
        +getTypeHierarchy(type)
    }
    
    class SuiFrameworkService {
        +getModule(name)
        +getDocumentation(element)
        +getFrameworkTypes()
    }
    
    class MoveProjectService {
        +getRoot()
        +getDependencies()
        +getConfiguration()
    }
    
    PluginServices <|-- ApplicationServices
    PluginServices <|-- ProjectServices
    ApplicationServices *-- MoveTypeService
    ApplicationServices *-- SuiFrameworkService
    ProjectServices *-- MoveProjectService
```

## Build & Deploy Pipeline

```mermaid
graph TD
    subgraph "Build Phase"
        SRC[Source Files]
        PARSE[Parse & Validate]
        DEPS[Resolve Dependencies]
        COMP[Compile]
        SRC --> PARSE
        PARSE --> DEPS
        DEPS --> COMP
    end
    
    subgraph "Test Phase"
        UNITS[Unit Tests]
        INTEG[Integration Tests]
        COV[Coverage Report]
        COMP --> UNITS
        UNITS --> INTEG
        INTEG --> COV
    end
    
    subgraph "Deploy Phase"
        PKG[Package]
        GAS[Gas Estimation]
        NET[Network Selection]
        TX[Transaction]
        COV --> PKG
        PKG --> GAS
        GAS --> NET
        NET --> TX
    end
    
    TX --> RESULT[Deployment Result]
```

## Memory Management Strategy

```mermaid
graph TB
    subgraph "Memory Layers"
        HOT[Hot Cache - 10MB]
        WARM[Warm Cache - 50MB]
        COLD[Cold Storage - 200MB]
        DISK[Disk Cache - Unlimited]
    end
    
    subgraph "Data Types"
        TYPES[Type Information]
        AST_C[AST Cache]
        IDX[Indices]
        DOC[Documentation]
    end
    
    TYPES --> HOT
    AST_C --> WARM
    IDX --> COLD
    DOC --> DISK
    
    HOT -->|LRU Eviction| WARM
    WARM -->|Age Out| COLD
    COLD -->|Persist| DISK
```

## Plugin Lifecycle

```mermaid
stateDiagram-v2
    [*] --> Loading
    Loading --> Initializing
    
    Initializing --> RegisteringServices
    RegisteringServices --> LoadingFramework
    LoadingFramework --> Ready
    
    Ready --> Active
    Active --> Processing : User Action
    Processing --> Active : Complete
    
    Active --> Indexing : File Change
    Indexing --> Active : Complete
    
    Active --> Disposing : Shutdown
    Disposing --> [*]
    
    note right of Ready : Plugin fully loaded
    note right of Active : Normal operation
    note right of Processing : Handling requests
```

## Error Handling Flow

```mermaid
flowchart TD
    A[Operation] --> B{Error?}
    B -->|No| C[Success]
    B -->|Yes| D{Error Type}
    
    D -->|Parse Error| E[Syntax Highlighting]
    D -->|Type Error| F[Type Annotation]
    D -->|Network Error| G[Retry Logic]
    D -->|Unknown| H[Log & Report]
    
    E --> I[Quick Fix]
    F --> I
    G --> J{Retry Count}
    J -->|< 3| A
    J -->|>= 3| K[User Notification]
    
    I --> L[Apply Fix]
    K --> M[Fallback Mode]
    H --> M
```

## Performance Monitoring

```mermaid
graph LR
    subgraph "Metrics Collection"
        CPU[CPU Usage]
        MEM[Memory Usage]
        RESP[Response Time]
        THR[Throughput]
    end
    
    subgraph "Analysis"
        AGG[Aggregation]
        THRESH[Threshold Check]
        TREND[Trend Analysis]
    end
    
    subgraph "Actions"
        OPT[Optimization]
        CACHE[Cache Tuning]
        ALERT[User Alert]
    end
    
    CPU --> AGG
    MEM --> AGG
    RESP --> AGG
    THR --> AGG
    
    AGG --> THRESH
    AGG --> TREND
    
    THRESH -->|Exceeded| ALERT
    TREND -->|Degrading| OPT
    TREND -->|Memory Issue| CACHE
```

---

These diagrams provide a visual representation of the v2.0 architecture, showing how components interact and data flows through the system.
