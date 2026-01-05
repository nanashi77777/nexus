package cn.lin037.nexus.application.user.service.impl;

import cn.dev33.satoken.stp.StpUtil;
import cn.lin037.nexus.application.user.port.UserAccountRepository;
import cn.lin037.nexus.application.user.service.UserQueryService;
import cn.lin037.nexus.application.user.vo.UserBasicInfoVO;
import cn.lin037.nexus.common.constant.enums.result.impl.CommonResultCodeEnum;
import cn.lin037.nexus.common.exception.ApplicationException;
import cn.lin037.nexus.infrastructure.common.persistent.entity.UserAccountEntity;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

/**
 * 用户查询服务接口实现
 * @author Lin037
 */
@Service
@RequiredArgsConstructor
public class UserQueryServiceImpl implements UserQueryService {
    private final UserAccountRepository userRepository;

    @Override
    public UserBasicInfoVO getUserSelfBasic() {
        long loginIdAsLong = StpUtil.getLoginIdAsLong();
        UserAccountEntity userAccount = userRepository.findById(loginIdAsLong).orElseThrow(
                () -> new ApplicationException(CommonResultCodeEnum.NOT_FOUND, "用户不存在")
        );

        return UserBasicInfoVO.toUserBasicInfoVO(userAccount);
    }

}
