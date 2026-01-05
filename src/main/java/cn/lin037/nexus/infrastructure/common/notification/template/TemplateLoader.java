package cn.lin037.nexus.infrastructure.common.notification.template;

import java.util.Optional;

/**
 * 模板加载器接口
 * 负责根据模板ID加载模板内容
 *
 * @author LinSanQi
 */
public interface TemplateLoader {

    /**
     * 加载模板内容
     *
     * @param templateId 模板ID
     * @return 模板内容的Optional封装
     */
    Optional<String> load(String templateId);
} 