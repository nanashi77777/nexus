package cn.lin037.nexus.web.rest.v1.knowledge;

import cn.lin037.nexus.application.knowledge.service.KnowledgeFolderAppService;
import cn.lin037.nexus.application.knowledge.service.KnowledgeFolderAppService;
import cn.lin037.nexus.common.model.vo.ResultVO;
import cn.lin037.nexus.web.rest.v1.knowledge.req.CreateFolderReq;
import cn.lin037.nexus.web.rest.v1.knowledge.req.MoveFolderReq;
import cn.lin037.nexus.web.rest.v1.knowledge.req.RenameFolderReq;
import cn.lin037.nexus.web.rest.v1.knowledge.vo.KnowledgeFolderVO;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;

import java.util.List;


@RestController
@RequestMapping("/api/v1/knowledge")
public class KnowledgeFolderController {

    private final KnowledgeFolderAppService knowledgeFolderAppService;

    public KnowledgeFolderController(KnowledgeFolderAppService knowledgeFolderAppService) {
        this.knowledgeFolderAppService = knowledgeFolderAppService;
    }

    /**
     * 创建文件夹
     * COMPLETE
     *
     * @param req 创建文件夹请求
     * @return 新文件夹
     */
    @PostMapping("/folders")
    public ResultVO<KnowledgeFolderVO> createFolder(@Valid @RequestBody CreateFolderReq req) {
        KnowledgeFolderVO folder = knowledgeFolderAppService.createFolder(req);
        return ResultVO.success(folder);
    }

    /**
     * 重命名文件夹
     * COMPLETE
     *
     * @param folderId 文件夹ID
     * @param req      重命名文件夹请求
     * @return 重命名后的文件夹
     */
    @PutMapping("/folders/{folderId}/name")
    public ResultVO<Void> renameFolder(@PathVariable Long folderId, @Valid @RequestBody RenameFolderReq req) {
        knowledgeFolderAppService.renameFolder(folderId, req.getNewName());
        return ResultVO.success();
    }

    /**
     * 移动文件夹
     * COMPLETE
     *
     * @param folderId 文件夹ID
     * @param req      移动文件夹请求
     * @return 移动后的文件夹
     */
    @PutMapping("/folders/{folderId}/move")
    public ResultVO<Void> moveFolder(@PathVariable Long folderId, @Valid @RequestBody MoveFolderReq req) {
        knowledgeFolderAppService.moveFolder(folderId, req.getNewParentFolderId());
        return ResultVO.success();
    }

    /**
     * 删除文件夹
     * TODO: 删除文件夹下的所有文件和子文件夹
     *
     * @param folderId 文件夹ID
     * @return 删除成功
     */
    @DeleteMapping("/folders/{folderId}")
    public ResultVO<Void> deleteFolder(@PathVariable Long folderId) {
        knowledgeFolderAppService.deleteFolder(folderId);
        return ResultVO.success();
    }

    /**
     * 获取文件夹列表
     *
     * @param learningSpaceId 学习空间ID
     * @param parentId        父文件夹ID（可选，默认为null即根目录）
     * @return 文件夹列表
     */
    @GetMapping("/folders")
    public ResultVO<List<KnowledgeFolderVO>> getFolders(
            @RequestParam Long learningSpaceId,
            @RequestParam(required = false) Long parentId) {
        List<KnowledgeFolderVO> folders = knowledgeFolderAppService.getFolders(learningSpaceId, parentId);
        return ResultVO.success(folders);
    }
}
