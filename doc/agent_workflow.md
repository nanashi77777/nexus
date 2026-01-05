# 智能体与讲解模块数据流转图 (Agent & Explanation Data Flow)

```mermaid
flowchart TD
    %% =================样式定义=================
    classDef entity fill:#e3f2fd,stroke:#1565c0,stroke-width:2px,rx:5,ry:5,color:#000;
    classDef relation fill:#fff9c4,stroke:#fbc02d,stroke-width:2px,shape:rhombus,color:#000;

    subgraph AgentContext [智能体上下文 Agent Context]
        direction TB
        ChatSession[会话 Session]:::entity
        Memory[记忆 Memory]:::entity
        LearningTask[学习任务 Learning Task]:::entity
    end

    subgraph Interaction [交互流 Interaction Flow]
        direction TB
        ChatMessage[消息 Message]:::entity
        ToolCall[工具调用 Tool Call]:::entity
    end

    subgraph ExplanationModule [讲解模块 Explanation Module]
        direction TB
        ExpDoc[讲解文档 Document]:::entity
        ExpSection[讲解章节 Section]:::entity
        ExpSubsection[讲解小节 Subsection]:::entity
        ExpPoint[讲解点 Point]:::entity
    end

    %% =================关系定义=================
    Rel_Contains{包含}:::relation
    Rel_Triggers{触发}:::relation
    Rel_Stores{存储}:::relation
    Rel_Generates{生成}:::relation
    Rel_Produces{产出}:::relation
    Rel_Has_Sec{包含章节}:::relation
    Rel_Has_Sub{包含小节}:::relation
    Rel_Has_Point{包含知识点}:::relation

    %% =================连线逻辑=================
    
    %% 智能体内部关系
    ChatSession -->|1:N| Rel_Contains --> ChatMessage
    ChatMessage -->|1:N| Rel_Triggers --> ToolCall
    ChatSession -->|1:N| Rel_Stores --> Memory
    ChatSession -->|1:N| Rel_Generates --> LearningTask

    %% 智能体 -> 讲解模块
    LearningTask -->|1:1| Rel_Produces --> ExpDoc

    %% 讲解模块内部关系
    ExpDoc -->|1:N| Rel_Has_Sec --> ExpSection
    ExpSection -->|1:N| Rel_Has_Sub --> ExpSubsection
    ExpDoc -->|1:N| Rel_Has_Point --> ExpPoint

    %% 布局辅助（可选）
    %% linkStyle default stroke-width:2px,fill:none,stroke:gray;
```

## 实体关系说明

### 1. 智能体上下文 (Agent Context)
*   **会话 (Session)**：核心聚合根，管理对话生命周期。
*   **记忆 (Memory)**：存储长期记忆，支持跨会话的知识保持。
*   **学习任务 (Learning Task)**：智能体的长期规划目标，由会话生成。

### 2. 交互流 (Interaction Flow)
*   **消息 (Message)**：记录对话历史。
*   **工具调用 (Tool Call)**：由消息触发，记录具体的工具执行请求。

### 3. 讲解模块 (Explanation Module)
*   **讲解文档 (Document)**：由学习任务产出的最终成果，是结构化的知识输出。
*   **讲解章节 (Section)**：文档的一级结构。
*   **讲解小节 (Subsection)**：章节的子结构，包含具体内容。
*   **讲解点 (Point)**：文档中提取的核心知识点，直接归属于文档，用于构建知识图谱或重点摘要。
