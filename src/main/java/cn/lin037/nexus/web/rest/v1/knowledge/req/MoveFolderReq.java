package cn.lin037.nexus.web.rest.v1.knowledge.req;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class MoveFolderReq {
    @NotBlank(message = "目标文件夹ID不能为空")
    private Long newParentFolderId;
}
