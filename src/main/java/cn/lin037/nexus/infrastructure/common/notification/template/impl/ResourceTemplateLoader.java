package cn.lin037.nexus.infrastructure.common.notification.template.impl;

import cn.lin037.nexus.infrastructure.common.exception.InfrastructureException;
import cn.lin037.nexus.infrastructure.common.file.exception.FileExceptionCodeEnum;
import cn.lin037.nexus.infrastructure.common.notification.template.TemplateLoader;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Component;
import org.springframework.util.FileCopyUtils;

import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * 从类路径加载模板的实现
 * <p>
 * 该加载器会自动查找指定ID的模板文件，支持多种扩展名。
 * 如果找到多个同名但不同扩展名的文件，会抛出异常。
 *
 * @author LinSanQi
 */
@Slf4j
@Component("resourceTemplateLoader")
public class ResourceTemplateLoader implements TemplateLoader {

    private static final String BASE_PATH = "/templates/notifications/";
    private static final List<String> SUPPORTED_EXTENSIONS = List.of(".html", ".text", ".md");

    @Override
    public Optional<String> load(String templateId) {
        List<String> foundLocations = new ArrayList<>();
        for (String extension : SUPPORTED_EXTENSIONS) {
            String location = BASE_PATH + templateId + extension;
            ClassPathResource resource = new ClassPathResource(location);
            if (resource.exists()) {
                foundLocations.add(location);
            }
        }

        if (foundLocations.isEmpty()) {
            log.warn("模板文件未找到: templateId={}, 搜索路径前缀={}", templateId, BASE_PATH);
            return Optional.empty();
        }

        if (foundLocations.size() > 1) {
            log.error("找到多个同名模板文件: templateId={}, 找到的文件: {}", templateId, foundLocations);
            throw new InfrastructureException(
                    FileExceptionCodeEnum.AMBIGUOUS_TEMPLATE_FILE,
                    "Template ID '" + templateId + "' is ambiguous. Found files: " + foundLocations
            );
        }

        String locationToLoad = foundLocations.getFirst();
        try {
            ClassPathResource resource = new ClassPathResource(locationToLoad);
            try (Reader reader = new InputStreamReader(resource.getInputStream(), StandardCharsets.UTF_8)) {
                return Optional.of(FileCopyUtils.copyToString(reader));
            }
        } catch (IOException e) {
            log.error("加载模板文件失败: {}", locationToLoad, e);
            return Optional.empty();
        }
    }
}