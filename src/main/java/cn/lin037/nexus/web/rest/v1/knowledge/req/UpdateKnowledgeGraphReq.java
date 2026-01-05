package cn.lin037.nexus.web.rest.v1.knowledge.req;

import lombok.Data;
import org.hibernate.validator.constraints.Length;

/**
 * 更新知识图谱请求
 *
 * @author LinSanQi
 */
@Data
public class UpdateKnowledgeGraphReq {

    /**
     * 图谱标题
     */
    @Length(min = 1, max = 255, message = "标题长度不能超过255个字符")
    private String title;

    /**
     * 图谱描述
     */
    @Length(min = 1, max = 500, message = "图谱描述长度不能超过500个字符")
    private String description;

    /**
     * 缩略图URL
     */
    @Length(min = 1, max = 500, message = "缩略图URL长度不能超过255个字符")
    private String thumbnailUrl;

    /**
     * 图谱配置数据 (暂时不启用)
     */
//    private String graphConfigData;
}

