package cn.lin037.nexus.web.rest.v1.knowledge.req;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.util.Map;

/**
 * 导入投影节点请求
 *
 * @author LinSanQi
 */
@Data
public class ImportProjectionNodeReq {

    @NotNull(message = "所属图谱ID不能为空")
    private Long graphId;

    @Valid
    @Size(max = 100, message = "最多导入100个节点")
    @NotEmpty(message = "至少需要导入一个节点")
    private Map<Long, NodeStyleConfigReq> nodes;
}