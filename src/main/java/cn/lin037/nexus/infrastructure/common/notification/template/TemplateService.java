package cn.lin037.nexus.infrastructure.common.notification.template;

import cn.lin037.nexus.infrastructure.common.exception.InfrastructureException;
import cn.lin037.nexus.infrastructure.common.notification.exception.NotificationExceptionCodeEnum;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 模板服务，负责模板的加载、缓存和渲染
 *
 * @author LinSanQi
 */
@Service
public class TemplateService {

    private final TemplateLoader templateLoader;
    private final TemplateEngine templateEngine;
    private final Map<String, String> templateCache = new ConcurrentHashMap<>();

    public TemplateService(@Qualifier("resourceTemplateLoader") TemplateLoader templateLoader,
                           @Qualifier("simpleTemplateEngine") TemplateEngine templateEngine) {
        this.templateLoader = templateLoader;
        this.templateEngine = templateEngine;
    }

    /**
     * 根据模板ID和变量渲染内容
     *
     * @param templateId 模板ID
     * @param variables  变量
     * @return 渲染后的内容
     */
    public String renderTemplate(String templateId, Map<String, Object> variables) {
        String templateContent = getTemplateContent(templateId);
        try {
            return templateEngine.render(templateContent, variables);
        } catch (Exception e) {
            throw new InfrastructureException(NotificationExceptionCodeEnum.TEMPLATE_RENDERING_ERROR, e);
        }
    }

    /**
     * 获取模板内容，优先从缓存读取
     *
     * @param templateId 模板ID
     * @return 模板内容
     */
    private String getTemplateContent(String templateId) {
        return templateCache.computeIfAbsent(templateId, id ->
                templateLoader.load(id)
                        .orElseThrow(() -> new InfrastructureException(NotificationExceptionCodeEnum.TEMPLATE_NOT_FOUND, "Template ID: " + id))
        );
    }

    /**
     * 清理所有模板缓存
     */
    public void clearCache() {
        templateCache.clear();
    }

    /**
     * 清理指定模板的缓存
     *
     * @param templateId 模板ID
     */
    public void clearCache(String templateId) {
        templateCache.remove(templateId);
    }
} 