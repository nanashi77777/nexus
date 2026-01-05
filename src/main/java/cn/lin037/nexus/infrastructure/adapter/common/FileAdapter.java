package cn.lin037.nexus.infrastructure.adapter.common;

import cn.lin037.nexus.application.common.port.FilePort;
import cn.lin037.nexus.infrastructure.common.file.enums.AccessLevel;
import cn.lin037.nexus.infrastructure.common.file.model.InfraFileMetadata;
import cn.lin037.nexus.infrastructure.common.file.service.FileStorageService;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import java.io.OutputStream;

/**
 * 文件操作适配器
 *
 * @author GitHub Copilot
 */
@Component
public class FileAdapter implements FilePort {

    private final FileStorageService fileStorageService;

    public FileAdapter(FileStorageService fileStorageService) {
        this.fileStorageService = fileStorageService;
    }

    @Override
    public InfraFileMetadata upload(MultipartFile file, String ownerIdentifier, AccessLevel accessLevel) {
        return fileStorageService.upload(file, ownerIdentifier, accessLevel);
    }

    @Override
    public boolean download(Long fileId, String accessorIdentifier, OutputStream outputStream) {
        return fileStorageService.download(fileId, accessorIdentifier, outputStream);
    }

    @Override
    public InfraFileMetadata getMetadata(Long fileId) {
        return fileStorageService.getMetadata(fileId);
    }
}

