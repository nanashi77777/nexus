package cn.lin037.nexus.application.user.service;

import cn.lin037.nexus.application.user.vo.UserBasicInfoVO;
import org.springframework.stereotype.Service;

/**
 * 用户查询服务接口
 * @author Lin037
 */
@Service
public interface UserQueryService {

    /**
     * 查询当前用户的基本信息
     * @return 用户基本信息
     */
    UserBasicInfoVO getUserSelfBasic();
}
