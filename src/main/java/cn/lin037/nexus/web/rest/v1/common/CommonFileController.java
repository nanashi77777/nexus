package cn.lin037.nexus.web.rest.v1.common;

import cn.lin037.nexus.application.common.service.CommonAppService;
import cn.lin037.nexus.common.model.vo.ResultVO;
import cn.lin037.nexus.infrastructure.common.file.enums.AccessLevel;
import cn.lin037.nexus.infrastructure.common.file.model.InfraFileMetadata;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

@RestController
@RequestMapping("/api/v1/common")
public class CommonFileController {
    private final CommonAppService commonAppService;

    public CommonFileController(CommonAppService commonAppService) {
        this.commonAppService = commonAppService;
    }

    /**
     * 上传文件
     *
     * @param file        文件
     * @param accessLevel 访问级别 (PUBLIC, PRIVATE)
     * @return 文件访问URL
     */
    @PostMapping("/upload")
    public ResultVO<String> uploadFile(@RequestParam("file") MultipartFile file,
                                       @RequestParam(value = "accessLevel", defaultValue = "PRIVATE") AccessLevel accessLevel) {
        String fileUrl = commonAppService.uploadFile(file, accessLevel);
        return ResultVO.success(fileUrl);
    }

    /**
     * 获取文件
     *
     * @param fileId   文件ID
     * @param response a
     */
    @GetMapping("/file/{fileId}")
    public void getFile(@PathVariable Long fileId, HttpServletResponse response) throws IOException {
        InfraFileMetadata metadata = (InfraFileMetadata) commonAppService.getFileMetadata(fileId);
        if (metadata == null) {
            response.sendError(HttpServletResponse.SC_NOT_FOUND);
            return;
        }

        response.setContentType(metadata.getFmMimeType());
        response.setHeader("Content-Disposition", "inline; filename=\"" + metadata.getFmOriginalFilename() + "\"");
        response.setContentLengthLong(metadata.getFmFileSize());

        commonAppService.downloadFile(fileId, response.getOutputStream());
    }
}
