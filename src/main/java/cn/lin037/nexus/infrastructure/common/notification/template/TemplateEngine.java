package cn.lin037.nexus.infrastructure.common.notification.template;

import java.util.Map;

/**
 * 模板引擎接口
 * 负责将变量填充到模板内容中
 *
 * @author LinSanQi
 */
public interface TemplateEngine {

    /**
     * 渲染模板
     *
     * @param templateContent 模板原始内容
     * @param variables       变量Map
     * @return 渲染后的内容
     */
    String render(String templateContent, Map<String, Object> variables);
} 