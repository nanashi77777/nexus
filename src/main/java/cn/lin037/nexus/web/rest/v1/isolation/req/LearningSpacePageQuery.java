package cn.lin037.nexus.web.rest.v1.isolation.req;

import cn.lin037.nexus.common.model.query.BasePageQuery;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 学习空间分页查询参数
 * 继承自基础分页查询类，未来可按需扩展学习空间特有的筛选条件
 *
 * @author LinSanQi
 */
@Data
@EqualsAndHashCode(callSuper = true)
@Schema(description = "学习空间分页查询参数")
public class LearningSpacePageQuery extends BasePageQuery {

}