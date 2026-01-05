package cn.lin037.nexus.web.rest.v1.explanation;

import cn.lin037.nexus.application.explanation.service.ExplanationSectionAppService;
import cn.lin037.nexus.application.explanation.service.ExplanationSubsectionAppService;
import cn.lin037.nexus.common.model.vo.ResultVO;
import cn.lin037.nexus.web.rest.v1.explanation.req.*;
import cn.lin037.nexus.web.rest.v1.explanation.vo.ExplanationSectionVO;
import cn.lin037.nexus.web.rest.v1.explanation.vo.ExplanationSubsectionVO;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 章节和小节控制器
 *
 * @author LinSanQi
 */
@RestController
@RequestMapping("/api/v1/explanation/sections")
public class ExplanationSectionController {

    private final ExplanationSectionAppService explanationSectionAppService;
    private final ExplanationSubsectionAppService explanationSubsectionAppService;

    public ExplanationSectionController(ExplanationSectionAppService explanationSectionAppService,
                                        ExplanationSubsectionAppService explanationSubsectionAppService) {
        this.explanationSectionAppService = explanationSectionAppService;
        this.explanationSubsectionAppService = explanationSubsectionAppService;
    }

    /**
     * 新增章节
     *
     * @param req 创建章节请求
     * @return 新创建的章节
     */
    @PostMapping
    public ResultVO<ExplanationSectionVO> createSection(@Valid @RequestBody CreateExplanationSectionReq req) {
        ExplanationSectionVO section = explanationSectionAppService.createSection(req);
        return ResultVO.success(section);
    }

    /**
     * 修改章节信息
     *
     * @param sectionId 章节ID
     * @param req       修改请求
     * @return 修改后的章节
     */
    @PutMapping("/{sectionId}")
    public ResultVO<ExplanationSectionVO> updateSection(@PathVariable Long sectionId, @Valid @RequestBody UpdateExplanationSectionReq req) {
        ExplanationSectionVO section = explanationSectionAppService.updateSection(sectionId, req);
        return ResultVO.success(section);
    }

    /**
     * 删除章节
     *
     * @param sectionId 章节ID
     * @return 删除成功
     */
    @DeleteMapping("/{sectionId}")
    public ResultVO<Void> deleteSection(@PathVariable Long sectionId) {
        explanationSectionAppService.deleteSection(sectionId);
        return ResultVO.success();
    }

    /**
     * 在章节下新增小节
     *
     * @param sectionId 章节ID
     * @param req       创建小节请求
     * @return 新创建的小节
     */
    @PostMapping("/{sectionId}/subsections")
    public ResultVO<ExplanationSubsectionVO> createSubsection(@PathVariable Long sectionId, @Valid @RequestBody CreateExplanationSubsectionReq req) {
        ExplanationSubsectionVO subsection = explanationSubsectionAppService.createSubsection(sectionId, req);
        return ResultVO.success(subsection);
    }

    /**
     * 修改小节信息
     *
     * @param subsectionId 小节ID
     * @param req          修改请求
     * @return 修改后的小节
     */
    @PutMapping("/subsections/{subsectionId}")
    public ResultVO<ExplanationSubsectionVO> updateSubsection(@PathVariable Long subsectionId, @Valid @RequestBody UpdateExplanationSubsectionReq req) {
        ExplanationSubsectionVO subsection = explanationSubsectionAppService.updateSubsection(subsectionId, req);
        return ResultVO.success(subsection);
    }

    /**
     * 删除小节
     *
     * @param subsectionId 小节ID
     * @return 删除成功
     */
    @DeleteMapping("/subsections/{subsectionId}")
    public ResultVO<Void> deleteSubsection(@PathVariable Long subsectionId) {
        explanationSubsectionAppService.deleteSubsection(subsectionId);
        return ResultVO.success();
    }

    /**
     * 调整章节和小节的位置关系
     *
     * @param req 位置调整请求
     * @return 调整成功
     */
    @PutMapping("/adjust-position")
    public ResultVO<Void> adjustPositions(@Valid @RequestBody AdjustSectionPositionReq req) {
        explanationSectionAppService.adjustPositions(req);
        return ResultVO.success();
    }

    /**
     * 根据文档ID获取章节列表
     *
     * @param explanationDocumentId 文档ID
     * @return 章节列表
     */
    @GetMapping("/document/{explanationDocumentId}")
    public ResultVO<List<ExplanationSectionVO>> listSectionsByDocument(@PathVariable Long explanationDocumentId) {
        List<ExplanationSectionVO> sections = explanationSectionAppService.listSectionsByDocument(explanationDocumentId);
        return ResultVO.success(sections);
    }
}
