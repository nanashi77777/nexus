package cn.lin037.nexus.application.common.port;

import cn.lin037.nexus.infrastructure.common.file.enums.AccessLevel;
import cn.lin037.nexus.infrastructure.common.file.model.InfraFileMetadata;
import org.springframework.web.multipart.MultipartFile;

import java.io.OutputStream;

/**
 * 文件操作端口
 *
 * @author GitHub Copilot
 */
public interface FilePort {
    /**
     * 上传文件
     *
     * @param file            文件
     * @param ownerIdentifier 文件所有者标识
     * @param accessLevel     访问级别
     * @return 文件元数据
     */
    InfraFileMetadata upload(MultipartFile file, String ownerIdentifier, AccessLevel accessLevel);

    /**
     * 下载文件
     *
     * @param fileId             文件ID
     * @param accessorIdentifier 访问者标识
     * @param outputStream       输出流
     * @return 是否成功
     */
    boolean download(Long fileId, String accessorIdentifier, OutputStream outputStream);

    /**
     * 获取文件元数据
     *
     * @param fileId 文件ID
     * @return 文件元数据
     */
    InfraFileMetadata getMetadata(Long fileId);
}

