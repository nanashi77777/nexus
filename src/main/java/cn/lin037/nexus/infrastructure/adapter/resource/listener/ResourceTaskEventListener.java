package cn.lin037.nexus.infrastructure.adapter.resource.listener;

import cn.lin037.nexus.application.resource.event.ResourceParsedEvent;
import cn.lin037.nexus.application.resource.port.ResourceTaskPort;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

/**
 * 资源任务事件监听器
 *
 * @author LinSanQi
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class ResourceTaskEventListener {

    private final ResourceTaskPort resourceTaskPort;

    @EventListener
    public void handleResourceParsedEvent(ResourceParsedEvent event) {
        log.info("接收到资源解析完成事件，资源ID: {}, 分片数量: {}", event.getResourceId(), event.getChunkIds().size());
        
        try {
            resourceTaskPort.submitBatchVectorizeTask(event.getChunkIds(), String.valueOf(event.getCreatedByUserId()));
            log.info("已触发批量向量化任务，资源ID: {}", event.getResourceId());
        } catch (Exception e) {
            log.error("触发批量向量化任务失败，资源ID: {}", event.getResourceId(), e);
        }
    }
}
