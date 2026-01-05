package cn.lin037.nexus.application.user.vo;

import cn.lin037.nexus.infrastructure.common.persistent.entity.UserAccountEntity;
import lombok.Data;

@Data
public class UserBasicInfoVO {
    private Long userId;
    private String username;
    private String inviteCode;
    private String email;
    private String phone;
    private Integer status;

    public static UserBasicInfoVO toUserBasicInfoVO(UserAccountEntity entity) {
        if (entity == null) {
            return null;
        }

        UserBasicInfoVO vo = new UserBasicInfoVO();

        if (entity.getUaId() != null) {
            vo.setUserId(entity.getUaId());
        }
        if (entity.getUaUsername() != null) {
            vo.setUsername(entity.getUaUsername());
        }
        if (entity.getUaEmail() != null) {
            vo.setEmail(entity.getUaEmail());
        }
        if (entity.getUaPhone() != null) {
            vo.setPhone(entity.getUaPhone());
        }
        if (entity.getUaStatus() != null) {
            vo.setStatus(entity.getUaStatus());
        }
        if (entity.getUaInviteCode() != null) {
            vo.setInviteCode(entity.getUaInviteCode());
        }

        return vo;
    }
}
