package cn.lin037.nexus.web.rest.v1.knowledge.req;

import cn.lin037.nexus.application.knowledge.enums.KnowledgeErrorCodeEnum;
import cn.lin037.nexus.common.exception.ApplicationException;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class MaterializeNodeReq {

    private Long folderId;

    private Long knowledgePointId;

    @Max(value = 1, message = "难度不能大于1")
    @Min(value = 0, message = "难度不能小于0")
    private BigDecimal difficulty;

    public void validate() {
        if (folderId == null && knowledgePointId == null) {
            throw new ApplicationException(KnowledgeErrorCodeEnum.MATERIALIZE_NODE_FAILED, "目标文件夹ID与实体知识点ID不能均为空");
        }
    }
} 