package cn.lin037.nexus.web.rest.v1.knowledge.req;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class RenameFolderReq {

    @NotBlank(message = "名称不能为空")
    private String newName;
}
