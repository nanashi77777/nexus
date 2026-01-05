package cn.lin037.nexus.web.rest.v1.user;

import cn.lin037.nexus.application.user.service.UserQueryService;
import cn.lin037.nexus.application.user.vo.UserBasicInfoVO;
import cn.lin037.nexus.common.model.vo.ResultVO;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 用户信息查询接口
 *
 * @author Lin
 **/
@RestController
@RequestMapping("/api/v1/users/query")
@RequiredArgsConstructor
public class UserQueryController {

    private final UserQueryService userQueryService;

    /**
     * 获取用户基础信息
     *
     * @return 用户基础信息
     */
    @GetMapping("/basic")
    public ResultVO<UserBasicInfoVO> getUserBasicInfoById() {
        return ResultVO.success(userQueryService.getUserSelfBasic());
    }

}
