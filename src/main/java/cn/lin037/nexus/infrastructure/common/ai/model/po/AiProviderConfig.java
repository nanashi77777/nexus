package cn.lin037.nexus.infrastructure.common.ai.model.po;

import cn.lin037.nexus.infrastructure.common.ai.constant.enums.AiModuleTypeEnum;
import cn.lin037.nexus.infrastructure.common.ai.constant.enums.GeneralStatusEnum;
import cn.lin037.nexus.infrastructure.common.ai.model.handler.AesEncryptTypeHandler;
import cn.xbatis.db.IdAutoType;
import cn.xbatis.db.annotations.LogicDelete;
import cn.xbatis.db.annotations.Table;
import cn.xbatis.db.annotations.TableField;
import cn.xbatis.db.annotations.TableId;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * AI服务商配置模型
 *
 * @author lin037
 */
@Data
@Table("ai_provider_config")
public class AiProviderConfig {

    /**
     * 自增主键
     */
    @TableId(value = IdAutoType.AUTO)
    private Long apcId;

    /**
     * 供应商名称
     */
    private String apcName;

    /**
     * 供应商官网地址
     */
    private String apcOfficialUrl;

    /**
     * 渠道/供应商类型（用于判断最后采用哪个代码依赖来使用该服务商）
     *
     * @see AiModuleTypeEnum
     */
    private String apcChannel;

    /**
     * 基础URL
     */
    private String apcBaseUrl;

    /**
     * API密钥
     */
    @TableField(typeHandler = AesEncryptTypeHandler.class)
    private String apcApiKey;

    /**
     * 状态
     *
     * @see GeneralStatusEnum
     */
    private Integer apcStatus;

    /**
     * 逻辑删除
     */
    @LogicDelete(beforeValue = "false", afterValue = "true")
    private Boolean apcIsDeleted;

    /**
     * 创建时间
     */
    private LocalDateTime apcCreateTime;

    /**
     * 更新时间
     */
    private LocalDateTime apcUpdateTime;
} 