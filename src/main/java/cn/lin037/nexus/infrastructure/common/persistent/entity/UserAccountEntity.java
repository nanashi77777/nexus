package cn.lin037.nexus.infrastructure.common.persistent.entity;

import cn.xbatis.db.IdAutoType;
import cn.xbatis.db.annotations.Ignore;
import cn.xbatis.db.annotations.LogicDelete;
import cn.xbatis.db.annotations.Table;
import cn.xbatis.db.annotations.TableId;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 用户基础表实体，存储用户的核心账户信息
 *
 * @author GitHub Copilot
 */
@Data
@Table(value = "user_accounts")
public class UserAccountEntity implements Serializable {
    @Serial
    @Ignore
    private static final long serialVersionUID = 1L;

    /**
     * 用户ID，使用雪花算法生成
     */
    @TableId(value = IdAutoType.NONE)
    private Long uaId;

    /**
     * 用户名，3-16个字符，仅允许字母、数字、下划线和连字符
     */
    private String uaUsername;

    /**
     * 电子邮箱地址
     */
    private String uaEmail;

    /**
     * 用户手机号
     */
    private String uaPhone;

    /**
     * 加密后的用户密码
     */
    private String uaPassword;

    /**
     * 用户状态 (1=ACTIVE正常, 2=BANNED禁用, 0=PENDING待验证)
     */
    private Integer uaStatus;

    /**
     * 用户专属邀请码
     */
    private String uaInviteCode;

    /**
     * 邀请人的用户ID
     */
    private Long uaInviterId;

    /**
     * 创建时间
     */
    private LocalDateTime uaCreatedAt;

    /**
     * 最后修改时间
     */
    private LocalDateTime uaUpdatedAt;

    /**
     * 逻辑删除时间，为NULL表示未删除
     */
    @LogicDelete(beforeValue = "", afterValue = "{NOW}")
    private LocalDateTime uaDeletedAt;
}
