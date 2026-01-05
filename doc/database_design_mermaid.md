# NEXUS Database ER Diagram

This document contains the Mermaid ER diagram code for the NEXUS project database.

```mermaid
erDiagram

    %% ==========================================
    %% 1. 用户与空间模块 (User & Space Module)
    %% ==========================================
    user_accounts {
        bigint ua_id PK "用户ID"
        string ua_username "用户名"
        string ua_email "邮箱"
        string ua_phone "手机号"
        int ua_status "状态"
    }

    learning_spaces {
        bigint ls_id PK "空间ID"
        bigint ls_user_id FK "所属用户ID"
        string ls_name "空间名称"
        string ls_description "描述"
    }

    user_accounts ||--o{ learning_spaces : "owns/creates"

    %% ==========================================
    %% 2. 资源模块 (Resource Module)
    %% ==========================================
    resources {
        bigint rs_id PK "资源ID"
        bigint rs_learning_space_id FK "所属空间ID"
        string rs_title "标题"
        int rs_source_type "来源类型"
        int rs_status "状态"
    }

    resource_chunks {
        bigint rc_id PK "分片ID"
        bigint rc_resource_id FK "所属资源ID"
        string rc_content "分片内容"
        boolean rc_is_vectorized "是否向量化"
    }

    infra_file_metadata {
        bigint fm_id PK "文件ID"
        string fm_original_filename "原始文件名"
        string fm_storage_path "存储路径"
        string fm_mime_type "文件类型"
    }

    learning_spaces ||--o{ resources : "contains"
    resources ||--o{ resource_chunks : "split_into"
    
    %% ==========================================
    %% 3. 知识图谱模块 (Knowledge Graph Module)
    %% ==========================================
    knowledge_folders {
        bigint kf_id PK "文件夹ID"
        bigint kf_parent_id FK "父文件夹ID"
        bigint kf_learning_space_id FK "所属空间ID"
        string kf_name "名称"
    }

    knowledge_points {
        bigint kp_id PK "知识点ID"
        bigint kp_folder_id FK "所属文件夹ID"
        bigint kp_current_version_id FK "当前版本ID"
    }

    knowledge_point_versions {
        bigint kpv_id PK "版本ID"
        bigint kpv_knowledge_point_id FK "所属知识点ID"
        string kpv_title "标题"
        string kpv_definition "定义"
    }

    knowledge_point_relations {
        bigint kpr_id PK "关系ID"
        bigint kpr_source_point_id FK "源知识点ID"
        bigint kpr_target_point_id FK "目标知识点ID"
        string kpr_relation_type "关系类型"
    }

    knowledge_graphs {
        bigint kg_id PK "图谱ID"
        bigint kg_learning_space_id FK "所属空间ID"
        string kg_title "标题"
    }

    graph_nodes {
        bigint gn_id PK "节点ID"
        bigint gn_graph_id FK "所属图谱ID"
        bigint gn_entity_id FK "关联知识点ID"
        boolean gn_is_projection "是否投影"
    }

    graph_edges {
        bigint ge_id PK "边ID"
        bigint ge_graph_id FK "所属图谱ID"
        bigint ge_source_virtual_node_id FK "源节点ID"
        bigint ge_target_virtual_node_id FK "目标节点ID"
    }

    learning_spaces ||--o{ knowledge_folders : "organizes"
    knowledge_folders ||--o{ knowledge_folders : "parent_of"
    knowledge_folders ||--o{ knowledge_points : "contains"
    knowledge_points ||--o{ knowledge_point_versions : "has_history"
    knowledge_points ||--o{ knowledge_point_relations : "source_of"
    knowledge_points ||--o{ knowledge_point_relations : "target_of"
    
    learning_spaces ||--o{ knowledge_graphs : "contains"
    knowledge_graphs ||--o{ graph_nodes : "composed_of"
    knowledge_graphs ||--o{ graph_edges : "composed_of"
    graph_nodes }|..|| knowledge_points : "visualizes"
    graph_edges }|..|| graph_nodes : "connects"

    %% ==========================================
    %% 4. 讲解模块 (Explanation Module)
    %% ==========================================
    explanation_documents {
        bigint ed_id PK "文档ID"
        bigint ed_learning_space_id FK "所属空间ID"
        string ed_title "标题"
        int ed_status "状态"
    }

    explanation_sections {
        bigint es_id PK "章节ID"
        bigint es_explanation_document_id FK "所属文档ID"
        string es_title "标题"
    }

    explanation_subsections {
        bigint ess_id PK "小节ID"
        bigint ess_section_id FK "所属章节ID"
        string ess_title "标题"
    }
    
    explanation_points {
        bigint ep_id PK "讲解点ID"
        bigint ep_explanation_document_id FK "所属文档ID"
        string ep_title "标题"
        string ep_definition "定义"
    }

    explanation_relations {
        bigint er_id PK "讲解关系ID"
        bigint er_explanation_document_id FK "所属文档ID"
        bigint er_source_point_id FK "源讲解点ID"
        bigint er_target_point_id FK "目标讲解点ID"
    }

    learning_spaces ||--o{ explanation_documents : "contains"
    explanation_documents ||--o{ explanation_sections : "structured_by"
    explanation_sections ||--o{ explanation_subsections : "structured_by"
    explanation_documents ||--o{ explanation_points : "defines"
    explanation_documents ||--o{ explanation_relations : "defines"
    explanation_points ||--o{ explanation_relations : "connects"

    %% ==========================================
    %% 5. Agent/聊天模块 (Agent Module)
    %% ==========================================
    agent_chat_sessions {
        bigint acs_id PK "会话ID"
        bigint acs_learning_space_id FK "所属空间ID"
        string acs_title "标题"
        int acs_type "类型"
        int acs_status "状态"
    }

    agent_chat_messages {
        bigint acm_id PK "消息ID"
        bigint acm_session_id FK "所属会话ID"
        string acm_role "角色"
        string acm_content "内容"
        int acm_type "类型"
    }

    agent_tool_calls {
        bigint atc_id PK "工具调用ID"
        bigint atc_message_id FK "所属消息ID"
        string atc_tool_name "工具名"
        string atc_arguments "参数"
        string atc_result "结果"
    }

    agent_memories {
        bigint am_id PK "记忆ID"
        bigint am_learning_space_id FK "所属空间ID"
        bigint am_session_id FK "关联会话ID"
        string am_content "内容"
        int am_level "级别"
    }

    agent_learning_tasks {
        bigint alt_id PK "任务ID"
        bigint alt_session_id FK "所属会话ID"
        string alt_title "标题"
        string alt_objective "目标"
    }

    learning_spaces ||--o{ agent_chat_sessions : "hosts"
    agent_chat_sessions ||--o{ agent_chat_messages : "contains"
    agent_chat_messages ||--o{ agent_tool_calls : "triggers"
    learning_spaces ||--o{ agent_memories : "stores"
    agent_chat_sessions ||--o{ agent_memories : "contextualizes"
    agent_chat_sessions ||--o{ agent_learning_tasks : "generates"

    %% ==========================================
    %% 6. 基础设施模块 (Infrastructure Module)
    %% ==========================================
    ai_provider_config {
        bigint apc_id PK "供应商ID"
        string apc_name "名称"
        string apc_base_url "BaseURL"
    }

    ai_model_config {
        bigint amc_id PK "配置ID"
        bigint amc_provider_id FK "所属供应商ID"
        string amc_name "名称"
        string amc_used_for "用途"
    }

    async_tasks {
        bigint at_id PK "任务ID"
        string at_task_type "类型"
        int at_status "状态"
    }

    ai_provider_config ||--o{ ai_model_config : "provides"
