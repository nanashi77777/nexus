package cn.lin037.nexus.infrastructure.common.notification.template.impl;

import cn.lin037.nexus.infrastructure.common.notification.template.TemplateEngine;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * 简单的模板引擎实现
 * 支持 ${variable} 格式的占位符替换
 *
 * @author LinSanQi
 */
@Component("simpleTemplateEngine")
public class SimpleTemplateEngine implements TemplateEngine {

    // 匹配 ${...} 格式的正则表达式
    private static final Pattern PATTERN = Pattern.compile("\\$\\{([^}]+)}");

    @Override
    public String render(String templateContent, Map<String, Object> variables) {
        if (templateContent == null || variables == null || variables.isEmpty()) {
            return templateContent;
        }

        Matcher matcher = PATTERN.matcher(templateContent);
        StringBuilder sb = new StringBuilder();

        while (matcher.find()) {
            String key = matcher.group(1);
            String value = variables.getOrDefault(key, "").toString();
            matcher.appendReplacement(sb, Matcher.quoteReplacement(value));
        }
        matcher.appendTail(sb);

        return sb.toString();
    }
} 