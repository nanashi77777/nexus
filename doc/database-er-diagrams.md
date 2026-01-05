# NEXUS 项目数据库 ER 图设计文档

本文档展示 NEXUS 项目的完整数据库设计，按功能模块分别呈现。

## 📋 目录

1. [用户模块](#1-用户模块)
2. [学习空间模块](#2-学习空间模块)
3. [Agent智能对话模块](#3-agent智能对话模块)
4. [知识点管理模块](#4-知识点管理模块)
5. [知识图谱模块](#5-知识图谱模块)
6. [资源管理模块](#6-资源管理模块)
7. [讲解文档模块](#7-讲解文档模块)
8. [AI配置模块](#8-ai配置模块)
9. [异步任务模块](#9-异步任务模块)
10. [基础设施模块](#10-基础设施模块)

---

## 1. 用户模块

**核心功能**: 用户账户管理、认证授权、邀请机制

```mermaid
erDiagram
    user_accounts {
        bigint ua_id PK "用户ID-雪花算法"
        varchar ua_username UK "用户名"
        varchar ua_email UK "邮箱"
        varchar ua_phone UK "手机号"
        varchar ua_password "加密密码"
        smallint ua_status "状态1正常2禁用0待验证"
        varchar ua_invite_code UK "邀请码"
        bigint ua_inviter_id FK "邀请人ID"
        timestamp ua_created_at
        timestamp ua_updated_at
        timestamp ua_deleted_at "逻辑删除"
    }
    
    user_accounts ||--o{ user_accounts : "invites"
```

---

## 2. 学习空间模块

**核心功能**: 多租户数据隔离核心，每个用户可创建多个独立学习空间

```mermaid
erDiagram
    user_accounts ||--o{ learning_spaces : "owns"
    
    learning_spaces {
        bigint ls_id PK "空间ID-雪花算法"
        bigint ls_user_id FK "所属用户"
        varchar ls_name "空间名称"
        varchar ls_description "空间描述"
        text ls_space_prompt "AI全局Prompt"
        varchar ls_cover_image_url "封面图"
        timestamp ls_created_at
        timestamp ls_updated_at
        timestamp ls_deleted_at
    }
```

---

## 3. Agent智能对话模块

**核心功能**: 智能对话、工具调用、记忆管理、学习规划

```mermaid
erDiagram
    user_accounts ||--o{ agent_chat_sessions : "creates"
    learning_spaces ||--o{ agent_chat_sessions : "contains"
    agent_chat_sessions ||--o{ agent_chat_messages : "has"
    agent_chat_sessions ||--o{ agent_tool_calls : "triggers"
    agent_chat_sessions ||--o{ agent_memories : "stores"
    agent_chat_sessions ||--o{ agent_learning_tasks : "generates"
    agent_chat_messages ||--o{ agent_tool_calls : "invokes"
    
    agent_chat_sessions {
        bigint acs_id PK "会话ID"
        bigint acs_user_id FK
        bigint acs_learning_space_id FK
        varchar acs_title "会话标题"
        smallint acs_type "1普通2学习"
        smallint acs_status "状态"
        smallint acs_belongs_to "所属模块"
        smallint acs_is_auto_call_tool "自动调用工具"
        timestamp acs_created_at
        timestamp acs_updated_at
        timestamp acs_deleted_at
    }
    
    agent_chat_messages {
        bigint acm_id PK "消息ID"
        bigint acm_session_id FK
        bigint acm_user_id FK
        bigint acm_learning_space_id FK
        varchar acm_role "USER/ASSISTANT/SYSTEM"
        text acm_content "消息内容"
        int acm_type "0普通1工具请求2工具响应3工具列表"
        text acm_correlation_content
        int acm_tokens
        timestamp acm_created_at
        timestamp acm_updated_at
        timestamp acm_deleted_at
    }
    
    agent_tool_calls {
        bigint atc_id PK "工具调用ID"
        bigint atc_message_id FK
        bigint atc_session_id FK
        bigint atc_user_id FK
        bigint atc_learning_space_id FK
        varchar atc_tool_name "工具名称"
        jsonb atc_arguments "参数JSON"
        text atc_result "结果"
        smallint atc_status "0待授权2已授权4完成5错误"
        text atc_error_message
        timestamp atc_created_at
        timestamp atc_updated_at
        timestamp atc_deleted_at
    }
    
    agent_memories {
        bigint am_id PK "记忆ID"
        bigint am_user_id FK
        bigint am_learning_space_id FK
        bigint am_session_id FK "NULL表示全局"
        smallint am_level "0不启用1会话级2全局"
        varchar am_title
        text am_content
        smallint am_importance_score "1-10评分"
        jsonb am_tags "标签JSON"
        varchar am_source "来源"
        int am_access_count "访问次数"
        timestamp am_last_accessed_at
        timestamp am_created_at
        timestamp am_updated_at
        timestamp am_deleted_at
    }
    
    agent_learning_tasks {
        bigint alt_id PK "学习任务ID"
        bigint alt_user_id FK
        bigint alt_learning_space_id FK
        bigint alt_session_id FK
        varchar alt_title "任务标题"
        text alt_objective "学习目标"
        smallint alt_difficulty_level "1初级2中级3高级4专家"
        bool alt_is_completed
        timestamp alt_created_at
        timestamp alt_updated_at
        timestamp alt_deleted_at
    }
```

---

## 4. 知识点管理模块

**核心功能**: 知识点组织、版本控制、关系网络

```mermaid
erDiagram
    learning_spaces ||--o{ knowledge_folders : "contains"
    knowledge_folders ||--o{ knowledge_folders : "nested"
    knowledge_folders ||--o{ knowledge_points : "organizes"
    knowledge_points ||--o{ knowledge_point_versions : "has_versions"
    knowledge_points ||--|| knowledge_point_versions : "current_version"
    knowledge_points ||--o{ knowledge_point_relations : "source"
    knowledge_points ||--o{ knowledge_point_relations : "target"
    
    knowledge_folders {
        bigint kf_id PK "文件夹ID"
        bigint kf_learning_space_id FK
        bigint kf_created_by_user_id FK
        bigint kf_parent_id FK "父文件夹"
        varchar kf_name
        smallint kf_level "0-5层级"
        timestamp kf_created_at
        timestamp kf_updated_at
        timestamp kf_deleted_at
    }
    
    knowledge_points {
        bigint kp_id PK "知识点ID"
        bigint kp_learning_space_id FK
        bigint kp_created_by_user_id FK
        bigint kp_folder_id FK
        bigint kp_current_version_id FK "当前版本"
        timestamp kp_created_at
        timestamp kp_updated_at
        timestamp kp_deleted_at
    }
    
    knowledge_point_versions {
        bigint kpv_id PK "版本ID"
        bigint kpv_knowledge_point_id FK
        bigint kpv_created_by_user_id FK
        varchar kpv_title "标题"
        text kpv_definition "定义"
        text kpv_explanation "讲解"
        text kpv_formula_or_code "公式代码"
        text kpv_example "示例"
        numeric kpv_difficulty "难度系数"
        timestamp kpv_created_at
        timestamp kpv_deleted_at
    }
    
    knowledge_point_relations {
        bigint kpr_id PK "关系ID"
        bigint kpr_learning_space_id FK
        bigint kpr_created_by_user_id FK
        bigint kpr_source_point_id FK "源知识点"
        bigint kpr_target_point_id FK "目标知识点"
        varchar kpr_relation_type "关系类型"
        text kpr_description
        timestamp kpr_created_at
        timestamp kpr_updated_at
        timestamp kpr_deleted_at
    }
```

---

## 5. 知识图谱模块

**核心功能**: 可视化知识关系，支持虚体和投影体

```mermaid
erDiagram
    learning_spaces ||--o{ knowledge_graphs : "contains"
    knowledge_graphs ||--o{ graph_nodes : "has_nodes"
    knowledge_graphs ||--o{ graph_edges : "has_edges"
    knowledge_points ||--o{ graph_nodes : "projects_to"
    knowledge_point_relations ||--o{ graph_edges : "projects_to"
    graph_nodes ||--o{ graph_edges : "source"
    graph_nodes ||--o{ graph_edges : "target"
    
    knowledge_graphs {
        bigint kg_id PK "图谱ID"
        bigint kg_learning_space_id FK
        bigint kg_created_by_user_id FK
        varchar kg_title
        text kg_description
        varchar kg_thumbnail_url "缩略图"
        jsonb kg_graph_config_data "图谱配置JSON"
        timestamp kg_created_at
        timestamp kg_updated_at
        timestamp kg_deleted_at
    }
    
    graph_nodes {
        bigint gn_id PK "节点ID"
        bigint gn_learning_space_id FK
        bigint gn_created_by_user_id FK
        bigint gn_graph_id FK
        bool gn_is_projection "是否投影体"
        bigint gn_entity_id FK "关联知识点实体"
        varchar gn_title
        text gn_definition
        text gn_explanation
        text gn_formula_or_code
        text gn_example
        jsonb gn_style_config "样式配置JSON"
        timestamp gn_created_at
        timestamp gn_updated_at
        timestamp gn_deleted_at
    }
    
    graph_edges {
        bigint ge_id PK "边ID"
        bigint ge_learning_space_id FK
        bigint ge_created_by_user_id FK
        bigint ge_graph_id FK
        bigint ge_source_virtual_node_id FK "源虚体节点"
        bigint ge_target_virtual_node_id FK "目标虚体节点"
        bigint ge_source_projection_node_id FK "源投影节点"
        bigint ge_target_projection_node_id FK "目标投影节点"
        bool ge_is_projection
        bigint ge_relation_entity_relation_id FK "关联关系实体"
        varchar ge_relation_type
        text ge_description
        jsonb ge_style_config "样式配置JSON"
        timestamp ge_created_at
        timestamp ge_updated_at
        timestamp ge_deleted_at
    }
```

---

## 6. 资源管理模块

**核心功能**: 资源上传、解析、分片、向量化

```mermaid
erDiagram
    learning_spaces ||--o{ resources : "contains"
    resources ||--o{ resource_chunks : "splits_into"
    
    resources {
        bigint rs_id PK "资源ID"
        bigint rs_created_by_user_id FK
        bigint rs_learning_space_id FK
        varchar rs_title
        varchar rs_description
        smallint rs_source_type "0上传1链接2AI生成3手动"
        varchar rs_source_uri "来源URI"
        varchar rs_prompt "AI提示词"
        smallint rs_status "0待解析1解析中2完成3失败"
        text rs_parse_error_message
        timestamp rs_created_at
        timestamp rs_updated_at
        timestamp rs_deleted_at
    }
    
    resource_chunks {
        bigint rc_id PK "分片ID"
        bigint rc_resource_id FK
        bigint rc_learning_space_id FK
        bigint rc_created_by_user_id FK
        text rc_content "分片内容"
        int rc_page_index "页码索引"
        int rc_chunk_index "分片索引"
        int rc_token_count "Token数"
        varchar rc_vector_id "向量ID"
        int rc_vector_dimension "向量维度"
        bool rc_is_vectorized "是否已向量化"
        timestamp rc_created_at
        timestamp rc_updated_at
        timestamp rc_deleted_at
    }
```

---

## 7. 讲解文档模块

**核心功能**: 结构化文档、知识点关联、图谱集成

```mermaid
erDiagram
    learning_spaces ||--o{ explanation_documents : "contains"
    explanation_documents ||--o{ explanation_sections : "has_sections"
    explanation_documents ||--o{ explanation_points : "has_points"
    explanation_documents ||--o{ explanation_relations : "has_relations"
    explanation_sections ||--o{ explanation_subsections : "has_subsections"
    explanation_points ||--o{ explanation_relations : "source"
    explanation_points ||--o{ explanation_relations : "target"
    
    explanation_documents {
        bigint ed_id PK "文档ID"
        bigint ed_learning_space_id FK
        bigint ed_created_by_user_id FK
        varchar ed_title
        text ed_description
        smallint ed_status "0草稿1AI生成中2正常"
        jsonb ed_section_order "章节顺序JSON"
        jsonb ed_graph_config "图谱配置JSON"
        timestamp ed_created_at
        timestamp ed_updated_at
        timestamp ed_deleted_at
    }
    
    explanation_sections {
        bigint es_id PK "章节ID"
        varchar es_title
        text es_summary "摘要"
        text es_content
        bigint es_created_by_user_id FK
        bigint es_explanation_document_id FK
        jsonb es_subsection_order "小节顺序JSON"
        timestamp es_created_at
        timestamp es_updated_at
        timestamp es_deleted_at
    }
    
    explanation_subsections {
        bigint ess_id PK "小节ID"
        varchar ess_title
        text ess_summary
        text ess_content
        bigint ess_created_by_user_id FK
        bigint ess_explanation_document_id FK
        bigint ess_section_id FK
        int ess_order "顺序"
        timestamp ess_created_at
        timestamp ess_updated_at
        timestamp ess_deleted_at
    }
    
    explanation_points {
        bigint ep_id PK "知识点ID"
        bigint ep_explanation_document_id FK
        bigint ep_created_by_user_id FK
        varchar ep_title
        text ep_definition
        text ep_explanation
        text ep_formula_or_code
        text ep_example
        jsonb ep_style_config "样式配置JSON"
        timestamp ep_created_at
        timestamp ep_updated_at
        timestamp ep_deleted_at
    }
    
    explanation_relations {
        bigint er_id PK "关系ID"
        bigint er_explanation_document_id FK
        bigint er_created_by_user_id FK
        bigint er_source_point_id FK
        bigint er_target_point_id FK
        varchar er_relation_type
        text er_description
        jsonb er_style_config "样式配置JSON"
        timestamp er_created_at
        timestamp er_updated_at
        timestamp er_deleted_at
    }
```

---

## 8. AI配置模块

**核心功能**: AI服务商和模型配置管理

```mermaid
erDiagram
    ai_provider_config ||--o{ ai_model_config : "provides"
    
    ai_provider_config {
        bigint apc_id PK "供应商ID"
        varchar apc_name "供应商名称"
        varchar apc_official_url "官网地址"
        varchar apc_channel "渠道类型"
        varchar apc_base_url "基础URL"
        text apc_api_key "API密钥-加密"
        int apc_status "1启用2禁用3删除"
        timestamp apc_create_time
        timestamp apc_update_time
        bool apc_is_deleted
    }
    
    ai_model_config {
        bigint amc_id PK "模型ID"
        bigint amc_provider_id FK
        varchar amc_name "模型名称"
        varchar amc_used_for "用途模块"
        jsonb amc_config "模型配置JSON"
        int amc_status "1启用2禁用3删除"
        timestamp amc_create_time
        timestamp amc_update_time
        bool amc_is_deleted
    }
```

---

## 9. 异步任务模块

**核心功能**: 后台任务调度、执行、监控

```mermaid
erDiagram
    async_tasks {
        bigint at_id PK "任务ID"
        varchar at_task_type "任务类型"
        int at_status "状态"
        jsonb at_parameters_json "参数JSON"
        jsonb at_result_json "结果JSON"
        text at_error_message "错误信息"
        varchar at_owner_identifier "所有者标识"
        timestamp at_created_at "创建时间"
        timestamp at_started_at "开始时间"
        timestamp at_finished_at "完成时间"
        varchar at_user_friendly_message "用户友好消息"
        varchar at_audit_details "审计详情"
    }
```

---

## 10. 基础设施模块

**核心功能**: 文件存储、向量存储

```mermaid
erDiagram
    infra_file_metadata {
        bigint fm_id PK "文件ID"
        varchar fm_storage_path "存储路径"
        varchar fm_owner_identifier "所有者标识"
        smallint fm_access_level "访问级别0私有1公开"
        varchar fm_original_filename "原始文件名"
        bigint fm_file_size "文件大小字节"
        varchar fm_mime_type "MIME类型"
        varchar fm_provider "存储提供商"
        timestamp fm_created_at
        text_array fm_grantees "授权用户列表"
    }
    
    vector_store_1024 {
        uuid embedding_id PK "嵌入ID"
        vector embedding "1024维向量"
        text text "文本内容"
        json metadata "元数据"
    }
    
    vector_store_1536 {
        uuid embedding_id PK "嵌入ID"
        vector embedding "1536维向量"
        text text "文本内容"
        json metadata "元数据"
    }
    
    vector_store_2048 {
        uuid embedding_id PK "嵌入ID"
        vector embedding "2048维向量"
        text text "文本内容"
        json metadata "元数据"
    }
```

---

## 🔑 关键设计特性

### 1. 数据隔离
- **学习空间**: 核心隔离单元，所有业务数据关联到学习空间
- **多租户**: 同一用户可创建多个独立学习空间

### 2. ID生成策略
- **雪花算法**: 所有业务表主键使用雪花算法生成全局唯一ID
- **自增ID**: 基础设施表（如文件元数据、AI配置）使用自增ID

### 3. 逻辑删除
- **deleted_at**: 所有业务表包含逻辑删除字段
- **数据追溯**: 保留历史数据用于审计和恢复

### 4. 版本控制
- **知识点版本**: 支持知识点内容的演进追踪
- **当前版本指针**: 通过 current_version_id 指向当前使用版本

### 5. 灵活配置
- **JSONB字段**: 大量使用JSONB存储灵活配置（样式、参数、元数据）
- **扩展性**: 便于功能扩展而不修改表结构

### 6. 性能优化
- **索引策略**: 针对高频查询字段创建B-Tree索引
- **GIN索引**: 用于JSONB字段和全文搜索
- **向量索引**: IVFFlat索引优化向量相似度搜索

### 7. 数据完整性
- **外键约束**: 严格的外键约束保证数据一致性
- **级联删除**: 合理的级联策略自动清理关联数据
- **CHECK约束**: 业务规则约束（如邮箱和手机号至少一个）

---

## 📊 统计信息

- **总表数**: 30+
- **核心业务模块**: 7个
- **基础设施模块**: 3个
- **关系数量**: 50+
- **索引数量**: 100+

---

## 🎯 使用建议

1. **开发新功能时**: 先确定数据归属的学习空间
2. **查询优化**: 利用已有索引，避免全表扫描
3. **数据删除**: 使用逻辑删除，保留审计轨迹
4. **扩展字段**: 优先使用JSONB而非频繁修改表结构
5. **向量存储**: 根据嵌入模型维度选择合适的向量表

---

**文档版本**: v1.0  
**最后更新**: 2025-12-20  
**维护者**: NEXUS团队
