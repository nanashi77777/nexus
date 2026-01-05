package cn.lin037.nexus.application.knowledge.service;

import cn.lin037.nexus.web.rest.v1.knowledge.req.CreateFolderReq;
import cn.lin037.nexus.web.rest.v1.knowledge.vo.KnowledgeFolderVO;

public interface KnowledgeFolderAppService {

    /**
     * 创建知识点文件夹
     *
     * @param req 创建文件夹请求参数
     * @return 创建的文件夹信息
     */
    KnowledgeFolderVO createFolder(CreateFolderReq req);

    /**
     * 重命名知识点文件夹
     *
     * @param folderId 文件夹ID
     * @param newName  新名称
     */
    void renameFolder(Long folderId, String newName);

    /**
     * 移动知识点文件夹
     *
     * @param folderId          要移动的文件夹ID
     * @param newParentFolderId 新的父文件夹ID
     */
    void moveFolder(Long folderId, Long newParentFolderId);

    /**
     * 删除知识点文件夹
     *
     * @param folderId 文件夹ID
     */
    void deleteFolder(Long folderId);

    /**
     * 获取文件夹列表
     *
     * @param learningSpaceId 学习空间ID
     * @param parentId        父文件夹ID
     * @return 文件夹列表
     */
    java.util.List<KnowledgeFolderVO> getFolders(Long learningSpaceId, Long parentId);
}
