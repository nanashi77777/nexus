package cn.lin037.nexus.infrastructure.common.ai.model.po;

import cn.lin037.nexus.infrastructure.common.ai.constant.enums.GeneralStatusEnum;
import cn.lin037.nexus.infrastructure.common.ai.model.dto.OpenAiParamConfig;
import cn.lin037.nexus.infrastructure.common.ai.model.dto.QwenParamConfig;
import cn.lin037.nexus.infrastructure.common.persistent.handler.JsonbTypeHandler;
import cn.xbatis.db.IdAutoType;
import cn.xbatis.db.annotations.LogicDelete;
import cn.xbatis.db.annotations.Table;
import cn.xbatis.db.annotations.TableField;
import cn.xbatis.db.annotations.TableId;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * AI模型配置模型
 *
 * @author lin037
 */
@Data
@Table("ai_model_config")
public class AiModelConfig {

    /**
     * 自增主键
     */
    @TableId(value = IdAutoType.AUTO)
    private Long amcId;

    /**
     * 关联的供应商ID
     */
    private Long amcProviderId;

    /**
     * 自定义名称（用于显示给前端的modelName，非请求时的modelName）
     */
    private String amcName;

    /**
     * 模型被用于的模块（由后端应用模块自行定义字符串作为自己模块的标识）
     */
    private String amcUsedFor;

    /**
     * 模型特定配置, JSON格式（由后端应用模块自行定义）
     * 例如: {"temperature": 0.7, "maxTokens": 1024, "modelName": Qwen3}
     * @see OpenAiParamConfig
     * @see QwenParamConfig
     */
    @TableField(typeHandler = JsonbTypeHandler.class)
    private String amcConfig;

    /**
     * 状态
     *
     * @see GeneralStatusEnum
     */
    private Integer amcStatus;

    /**
     * 逻辑删除
     */
    @LogicDelete(beforeValue = "false", afterValue = "true")
    private Boolean amcIsDeleted;

    /**
     * 创建时间
     */
    private LocalDateTime amcCreateTime;

    /**
     * 更新时间
     */
    private LocalDateTime amcUpdateTime;

} 