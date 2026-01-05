package cn.lin037.nexus.infrastructure.common.ai.model.dto;

import lombok.Data;

import java.time.LocalDateTime;

/**
 * AI 服务商信息的数据传输对象 (DTO)
 * 用于在API中安全地暴露服务商信息，不包含API Key等敏感数据
 *
 * @author lin037
 */
@Data
public class AiProviderInfoDTO {

    /**
     * 主键
     */
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
     * 渠道/供应商类型
     */
    private String apcChannel;

    /**
     * 基础URL
     */
    private String apcBaseUrl;

    /**
     * 状态
     */
    private Integer apcStatus;

    /**
     * 创建时间
     */
    private LocalDateTime apcCreateTime;

    /**
     * 更新时间
     */
    private LocalDateTime apcUpdateTime;
} 