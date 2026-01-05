package cn.lin037.nexus.web.rest.v1.isolation;

import cn.lin037.nexus.application.isolation.service.LearningSpaceAppService;
import cn.lin037.nexus.common.model.vo.ResultVO;
import cn.lin037.nexus.web.rest.v1.isolation.req.CreateLearningSpaceReq;
import cn.lin037.nexus.web.rest.v1.isolation.req.UpdateLearningSpaceReq;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;

/**
 * 学习空间管理接口
 *
 * @author Lin037
 */
@RestController
@RequestMapping("/api/v1/isolation/learning-space")
public class LearningSpaceController {

    private final LearningSpaceAppService learningSpaceAppService;

    public LearningSpaceController(LearningSpaceAppService learningSpaceAppService) {
        this.learningSpaceAppService = learningSpaceAppService;
    }

    /**
     * 创建学习空间
     *
     * @param request 创建学习空间请求
     * @return 学习空间ID
     */
    @PostMapping
    public ResultVO<Long> createLearningSpace(
            @Valid @RequestBody CreateLearningSpaceReq request) {
        Long learningSpaceId = learningSpaceAppService.createLearningSpace(request);
        return ResultVO.success(learningSpaceId);
    }

    /**
     * 更新学习空间
     *
     * @param id      学习空间ID
     * @param request 更新学习空间请求
     */
    @PutMapping("/{id}")
    public ResultVO<Void> updateLearningSpace(
            @PathVariable Long id,
            @Valid @RequestBody UpdateLearningSpaceReq request) {
        learningSpaceAppService.updateLearningSpace(id, request);
        return ResultVO.success();
    }

    /**
     * 删除学习空间
     *
     * @param id 学习空间ID
     */
    @DeleteMapping("/{id}")
    public ResultVO<Void> deleteLearningSpace(@PathVariable Long id) {
        learningSpaceAppService.deleteLearningSpace(id);
        return ResultVO.success();
    }
}

