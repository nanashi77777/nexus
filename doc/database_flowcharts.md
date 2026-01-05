# NEXUS 数据库设计 - 模块化实体关系流程图

本文档采用分模块视图展示数据库实体间的关系，特别强调了 **Agent (智能体)** 在系统中的核心连接作用。

> **图例说明**：
> - 🟦 **蓝色矩形**：实体 (Entity)
> - 🟡 **黄色菱形**：关系 (Relationship)

## 0. 全局统辖与基础架构视图
展示用户账户体系、多租户学习空间以及空间对核心业务模块的统辖关系。

```mermaid
flowchart TD
    %% =================样式定义=================
    classDef entity fill:#e3f2fd,stroke:#1565c0,stroke-width:2px,rx:5,ry:5,color:#000;
    classDef relation fill:#fff9c4,stroke:#fbc02d,stroke-width:2px,shape:rhombus,color:#000;

    UserAccount[用户账户]:::entity
    LearningSpace[学习空间]:::entity
    
    Rel_Invite{邀请}:::relation
    Rel_Owns{拥有/创建}:::relation
    Rel_Governs{统辖/包含}:::relation

    %% 被统辖的业务模块
    subgraph Modules [核心业务模块]
        direction TB
        Agent[Agent会话]:::entity
        Knowledge[知识体系]:::entity
        Graph[知识图谱]:::entity
        Resource[资源文件]:::entity
        Doc[讲解文档]:::entity
    end

    %% 用户与空间关系
    UserAccount -->|1:N| Rel_Invite --> UserAccount
    UserAccount -->|1:N| Rel_Owns --> LearningSpace

    %% 空间与模块关系
    LearningSpace -->|1:N| Rel_Governs
    
    Rel_Governs --> Agent
    Rel_Governs --> Knowledge
    Rel_Governs --> Graph
    Rel_Governs --> Resource
    Rel_Governs --> Doc
```

## 1. 智能体核心交互视图 (Agent Core)
展示智能体对话、记忆与工具调用的内部机制。

```mermaid
flowchart TD
    %% =================样式定义=================
    classDef entity fill:#e3f2fd,stroke:#1565c0,stroke-width:2px,rx:5,ry:5,color:#000;
    classDef relation fill:#fff9c4,stroke:#fbc02d,stroke-width:2px,shape:rhombus,color:#000;

    subgraph AgentContext [智能体上下文]
        direction TB
        ChatSession[会话]:::entity
        Memory[记忆]:::entity
        LearningTask[学习任务]:::entity
    end

    subgraph Interaction [交互流]
        direction TB
        ChatMessage[消息]:::entity
        ToolCall[工具调用]:::entity
    end

    Rel_Contains{包含}:::relation
    Rel_Triggers{触发}:::relation
    Rel_Stores{存储}:::relation
    Rel_Generates{生成}:::relation

    %% 关系连线
    ChatSession -->|1:N| Rel_Contains --> ChatMessage
    ChatMessage -->|1:N| Rel_Triggers --> ToolCall
    ChatSession -->|1:N| Rel_Stores --> Memory
    ChatSession -->|1:N| Rel_Generates --> LearningTask
```

## 2. 智能体能力与外部关联视图 (核心)
**核心视图**：展示智能体如何通过工具调用连接资源、知识图谱与内容生成模块。

```mermaid
flowchart TD
    %% =================样式定义=================
    classDef entity fill:#e3f2fd,stroke:#1565c0,stroke-width:2px,rx:5,ry:5,color:#000;
    classDef relation fill:#fff9c4,stroke:#fbc02d,stroke-width:2px,shape:rhombus,color:#000;

    subgraph Agent [智能体]
        ChatMessage[消息 / 工具调用]:::entity
    end

    subgraph KnowledgeBase [外部知识库]
        ResourceChunk[资源分片 RAG]:::entity
        KnowledgeGraph[知识图谱]:::entity
    end

    subgraph Output [内容产出]
        ExpDoc[讲解文档]:::entity
    end

    Rel_Retrieves{RAG检索}:::relation
    Rel_Queries{图谱查询}:::relation
    Rel_Generates{生成文档}:::relation

    %% 关系连线
    ChatMessage =.=>|向量相似度搜索| Rel_Retrieves =.=> ResourceChunk
    ChatMessage =.=>|结构化数据查询| Rel_Queries =.=> KnowledgeGraph
    ChatMessage =.=>|规划与生成| Rel_Generates =.=> ExpDoc
```

## 3. 知识体系视图 (知识点与图谱)
展示抽象知识点与其具体化（版本）及可视化（图谱节点）之间的映射关系。

```mermaid
flowchart TD
    %% =================样式定义=================
    classDef entity fill:#e3f2fd,stroke:#1565c0,stroke-width:2px,rx:5,ry:5,color:#000;
    classDef relation fill:#fff9c4,stroke:#fbc02d,stroke-width:2px,shape:rhombus,color:#000;

    subgraph AbstractKnowledge [抽象知识层]
        KPoint[知识点]:::entity
        PointVer[知识点版本]:::entity
        PointRel[知识点关系]:::entity
    end

    subgraph VisualGraph [可视化图谱层]
        GNode[图谱节点]:::entity
        GEdge[图谱边]:::entity
    end

    Rel_HasVer{拥有版本}:::relation
    Rel_Links{关联}:::relation
    Rel_Projects{投影}:::relation
    Rel_Connects{连接}:::relation

    %% 抽象层关系
    KPoint -->|1:N| Rel_HasVer --> PointVer
    KPoint -->|源| Rel_Links --> PointRel -->|目标| KPoint

    %% 投影关系 (核心机制)
    KPoint -.->|实体投影| Rel_Projects -.-> GNode
    PointRel -.->|关系投影| Rel_Projects -.-> GEdge

    %% 可视化层关系
    GNode -->|源| Rel_Connects --> GEdge -->|目标| GNode
```

## 4. 资源与内容生产视图
展示原始资源的解析处理、向量化以及最终教学文档的结构。

```mermaid
flowchart TD
    %% =================样式定义=================
    classDef entity fill:#e3f2fd,stroke:#1565c0,stroke-width:2px,rx:5,ry:5,color:#000;
    classDef relation fill:#fff9c4,stroke:#fbc02d,stroke-width:2px,shape:rhombus,color:#000;

    subgraph InputResource [输入资源]
        Resource[原始资源]:::entity
        ResChunk[资源分片]:::entity
        Vector[向量索引]:::entity
    end

    subgraph OutputContent [输出文档]
        ExpDoc[讲解文档]:::entity
        Section[章节]:::entity
        SubSection[小节]:::entity
    end

    Rel_Splits{拆分}:::relation
    Rel_Vectorizes{向量化}:::relation
    Rel_Composed{组成}:::relation
    Rel_Subdivides{细分}:::relation

    %% 资源处理流
    Resource -->|1:N| Rel_Splits --> ResChunk
    ResChunk -->|1:1| Rel_Vectorizes --> Vector

    %% 文档结构流
    ExpDoc -->|1:N| Rel_Composed --> Section
    Section -->|1:N| Rel_Subdivides --> SubSection
