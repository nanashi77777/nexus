package cn.lin037.nexus.web.rest.v1.knowledge.req;

import cn.lin037.nexus.infrastructure.common.persistent.entity.dto.NodeStyleConfig;
import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class NodeStyleConfigReq {

    private Boolean collapsed = false;

    @Size(max = 255, message = "填充颜色值长度不能超过255个字符")
    private String fill;

    @DecimalMin(value = "0.0", message = "填充透明度最小为0.0")
    @DecimalMax(value = "1.0", message = "填充透明度最大为1.0")
    private Double fillOpacity;

    private Double x = 0.0;

    private Double y = 0.0;

    private Double z = 0.0;

    /**
     * 将NodeStyleConfigVO转换为NodeStyleConfig
     *
     * @return NodeStyleConfig对象
     */
    public NodeStyleConfig convertToNodeStyleConfig() {
        NodeStyleConfig config = new NodeStyleConfig();
        config.setCollapsed(collapsed);
        config.setFill(fill);
        config.setFillOpacity(fillOpacity);
        config.setX(x);
        config.setY(y);
        config.setZ(z);
        return config;
    }
}