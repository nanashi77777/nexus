package cn.lin037.nexus.application.common.service;

import cn.lin037.nexus.infrastructure.common.file.enums.AccessLevel;
import org.springframework.web.multipart.MultipartFile;

import java.io.OutputStream;

public interface CommonAppService {
    /**
     * 上传文件
     *
     * @param file        文件
     * @param accessLevel 访问级别
     * @return 文件访问URL
     */
    String uploadFile(MultipartFile file, AccessLevel accessLevel);

    /**
     * 下载文件
     *
     * @param fileId       文件ID
     * @param outputStream 输出流
     */
    void downloadFile(Long fileId, OutputStream outputStream);

    /**
     * 获取文件元数据，用于在控制器中设置响应头
     *
     * @param fileId 文件ID
     * @return 文件元数据
     */
    Object getFileMetadata(Long fileId);
}
