package cn.lin037.nexus.application.common.service.impl;

import cn.dev33.satoken.stp.StpUtil;
import cn.lin037.nexus.application.common.port.FilePort;
import cn.lin037.nexus.application.common.service.CommonAppService;
import cn.lin037.nexus.infrastructure.common.file.enums.AccessLevel;
import cn.lin037.nexus.infrastructure.common.file.model.InfraFileMetadata;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.io.OutputStream;

@Service
public class CommonAppServiceImpl implements CommonAppService {

    private final FilePort filePort;

    public CommonAppServiceImpl(FilePort filePort) {
        this.filePort = filePort;
    }

    @Override
    public String uploadFile(MultipartFile file, AccessLevel accessLevel) {
        String userId = StpUtil.getLoginIdAsString();
        InfraFileMetadata metadata = filePort.upload(file, userId, accessLevel);

        // 构建并返回文件的可访问URL
        return ServletUriComponentsBuilder.fromCurrentContextPath()
                .path("/api/v1/common/file/")
                .path(String.valueOf(metadata.getFmId()))
                .toUriString();
    }

    @Override
    public void downloadFile(Long fileId, OutputStream outputStream) {
        String accessorId = StpUtil.getLoginIdAsString();
        filePort.download(fileId, accessorId, outputStream);
    }

    @Override
    public InfraFileMetadata getFileMetadata(Long fileId) {
        return filePort.getMetadata(fileId);
    }
}
