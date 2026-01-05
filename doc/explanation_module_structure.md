# 讲解模块数据结构图 (Explanation Module Structure)

```mermaid
flowchart TD
    %% =================样式定义=================
    classDef entity fill:#e3f2fd,stroke:#1565c0,stroke-width:2px,rx:5,ry:5,color:#000;
    classDef relation fill:#fff9c4,stroke:#fbc02d,stroke-width:2px,shape:rhombus,color:#000;

    subgraph ExplanationModule [讲解模块核心实体]
        direction TB
        ExpDoc[讲解文档 Document]:::entity
        ExpSection[讲解章节 Section]:::entity
        ExpSubsection[讲解小节 Subsection]:::entity
        ExpPoint[讲解点 Point]:::entity
        ExpRelation[讲解关系 Relation]:::entity
    end

    %% =================关系定义=================
    Rel_Contains_Sec{包含章节}:::relation
    Rel_Contains_Sub{包含小节}:::relation
    Rel_Contains_Point{包含知识点}:::relation
    Rel_Contains_Rel{包含关系}:::relation
    Rel_Connects{连接}:::relation

    %% =================连线逻辑=================
    
    %% 文档层级结构
    ExpDoc -->|1:N| Rel_Contains_Sec --> ExpSection
    ExpSection -->|1:N| Rel_Contains_Sub --> ExpSubsection
    
    %% 知识图谱结构（文档内）
    ExpDoc -->|1:N| Rel_Contains_Point --> ExpPoint
    ExpDoc -->|1:N| Rel_Contains_Rel --> ExpRelation
    
    %% 点与关系
    ExpPoint -->|源点 Source| Rel_Connects --> ExpRelation
    ExpRelation -->|目标点 Target| Rel_Connects --> ExpPoint

    %% 布局辅助
    %% linkStyle default stroke-width:2px,fill:none,stroke:gray;
```

## 实体说明

1.  **讲解文档 (ExplanationDocument)**：
    *   `explanation_documents`
    *   讲解内容的顶层容器，对应一次完整的学习产出。
    *   包含文档的元数据（标题、描述、状态）和图谱配置。

2.  **讲解章节 (ExplanationSection)**：
    *   `explanation_sections`
    *   文档的一级目录结构。
    *   包含章节标题、摘要和内容。

3.  **讲解小节 (ExplanationSubsection)**：
    *   `explanation_subsections`
    *   章节下的子结构，是内容的具体承载单元。

4.  **讲解点 (ExplanationPoint)**：
    *   `explanation_points`
    *   从文档内容中提取的核心概念或知识点。
    *   包含定义、解释、公式/代码示例等详细信息。
    *   直接归属于文档，不局限于特定章节，可跨章节引用。

5.  **讲解关系 (ExplanationRelation)**：
    *   `explanation_relations`
    *   定义两个讲解点之间的逻辑关系（如“包含”、“导致”、“相关”）。
    *   用于在文档内部构建小型的知识图谱网络。
