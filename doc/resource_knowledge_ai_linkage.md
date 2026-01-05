# 资源与知识图谱 AI 联动关系图 (Entity-Action View)

```mermaid
graph TD
    %% 定义样式
    classDef entity fill:#e3f2fd,stroke:#1565c0,stroke-width:2px,shape:rect;
    classDef action fill:#fff9c4,stroke:#fbc02d,stroke-width:2px,shape:diamond;
    classDef external fill:#f5f5f5,stroke:#616161,stroke-width:2px,stroke-dasharray: 5 5,shape:rect;

    subgraph 资源模块
        direction TB
        Input[外部输入源]
        Upload{上传/解析}
        Resource[资源表 Resource]
        Slice{分片处理}
        Chunk[分片表 Chunk]
        Vectorize{向量化}
        Vector[向量表 Vector]

        Input --> Upload --> Resource
        Resource --> Slice --> Chunk
        Chunk --> Vectorize --> Vector
    end

    subgraph AI处理
        direction TB
        Analyze{AI 深度分析}
        ExtractConcept{提取概念}
        ExtractRelation{提取关系}

        Chunk -.-> Analyze
        Analyze --> ExtractConcept
        Analyze --> ExtractRelation
    end

    subgraph 知识点模块
        direction TB
        ProjectionNode[投影节点表]
        ProjectionEdge[投影关系表]
        
        MaterializeNode{实体化节点}
        MaterializeEdge{实体化关系}
        
        KnowledgePoint[知识点表]
        KnowledgeRelation[知识关系表]

        %% 投影生成
        ExtractConcept --> ProjectionNode
        ExtractRelation --> ProjectionEdge

        %% 实体化流程
        ProjectionNode --> MaterializeNode --> KnowledgePoint
        ProjectionEdge --> MaterializeEdge --> KnowledgeRelation
        
        %% 关联
        KnowledgePoint <--> KnowledgeRelation
    end

    %% 跨模块引用
    KnowledgePoint -.->|引用| Chunk
    Vector -.->|关联| Chunk

    %% 样式应用
    class Input external;
    class Resource,Chunk,Vector,ProjectionNode,ProjectionEdge,KnowledgePoint,KnowledgeRelation entity;
    class Upload,Slice,Vectorize,Analyze,ExtractConcept,ExtractRelation,MaterializeNode,MaterializeEdge action;

    %% 连接样式
    linkStyle default stroke-width:2px,fill:none,stroke:gray;
```

## 图例说明

*   **矩形 [ ]**：代表数据库中的**表实体 (Entity)**，用于存储数据。
*   **菱形 { }**：代表系统或 AI 执行的**动作 (Action)**，用于处理数据。
*   **虚线矩形 [ ]**：代表外部输入或非系统内的实体。

## 流程详解

1.  **资源处理流**：
    *   外部输入经过 **{上传/解析}** 动作，存入 **[资源表]**。
    *   资源经过 **{分片处理}** 动作，生成 **[分片表]** 数据。
    *   分片经过 **{向量化}** 动作，生成 **[向量表]** 数据。

2.  **知识提取流**：
    *   AI 对分片进行 **{AI 深度分析}**。
    *   通过 **{提取概念}** 动作，生成 **[投影节点表]** 数据。
    *   通过 **{提取关系}** 动作，生成 **[投影关系表]** 数据。

3.  **知识构建流**：
    *   投影节点经过 **{实体化节点}** 动作（通常包含用户确认），转变为正式的 **[知识点表]** 数据。
    *   投影关系经过 **{实体化关系}** 动作，转变为正式的 **[知识关系表]** 数据。
