package cn.lin037.nexus.application.resource.event;

import lombok.Getter;
import org.springframework.context.ApplicationEvent;

import java.util.List;

/**
 * 资源解析完成事件
 *
 * @author LinSanQi
 */
@Getter
public class ResourceParsedEvent extends ApplicationEvent {

    private final Long resourceId;
    private final List<Long> chunkIds;
    private final Long createdByUserId;

    public ResourceParsedEvent(Object source, Long resourceId, List<Long> chunkIds, Long createdByUserId) {
        super(source);
        this.resourceId = resourceId;
        this.chunkIds = chunkIds;
        this.createdByUserId = createdByUserId;
    }
}
