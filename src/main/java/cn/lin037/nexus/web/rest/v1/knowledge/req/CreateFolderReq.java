package cn.lin037.nexus.web.rest.v1.knowledge.req;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import org.hibernate.validator.constraints.Length;

@Data
public class CreateFolderReq {

    @NotNull(message = "学习空间ID不能为空")
    private Long learningSpaceId;

    private Long parentId;

    @NotBlank(message = "文件夹名称不能为空")
    @Length(min = 1, max = 50, message = "文件夹名称长度不能超过50")
    private String name;
} 