package cn.lin037.nexus.infrastructure.common.ai.repository.impl;

import cn.lin037.nexus.infrastructure.common.ai.model.po.AiProviderConfig;
import cn.lin037.nexus.infrastructure.common.ai.repository.AiProviderConfigRepository;
import cn.lin037.nexus.infrastructure.common.ai.repository.mapper.AiProviderConfigMapper;
import cn.xbatis.core.sql.executor.chain.QueryChain;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
public class AiProviderConfigRepositoryImpl implements AiProviderConfigRepository {

    private final AiProviderConfigMapper aiProviderConfigMapper;

    public AiProviderConfigRepositoryImpl(AiProviderConfigMapper aiProviderConfigMapper) {
        this.aiProviderConfigMapper = aiProviderConfigMapper;
    }

    @Override
    public void save(AiProviderConfig aiProviderConfig) {
        aiProviderConfigMapper.save(aiProviderConfig);
    }

    @Override
    public void deleteById(Long id) {
        aiProviderConfigMapper.deleteById(id);
    }

    @Override
    public Optional<AiProviderConfig> findById(Long id) {
        if (id == null) return Optional.empty();
        if (id.equals(1L)) {
            AiProviderConfig hardcodedProvider = new AiProviderConfig();
            hardcodedProvider.setApcId(2L);
            hardcodedProvider.setApcName("通义千问");
            hardcodedProvider.setApcOfficialUrl("https://dashscope.aliyun.com/");
            hardcodedProvider.setApcChannel("DASHSCOPE");
            hardcodedProvider.setApcBaseUrl("https://dashscope.aliyuncs.com/api/v1");
            hardcodedProvider.setApcApiKey("sk-7d0409f9de9e4506a0056ad08aa76e74");
            hardcodedProvider.setApcStatus(1);
            hardcodedProvider.setApcCreateTime(LocalDateTime.now());
            hardcodedProvider.setApcUpdateTime(LocalDateTime.now());

            return Optional.of(hardcodedProvider);
        }

        if (id.equals(2L)) {

            AiProviderConfig hardcodedProvider = new AiProviderConfig();
            hardcodedProvider.setApcId(2L);
            hardcodedProvider.setApcName("deepseek");
            hardcodedProvider.setApcOfficialUrl("https://api.deepseek.com");
            hardcodedProvider.setApcChannel("OPENAI");
            hardcodedProvider.setApcBaseUrl("https://api.deepseek.com");
            hardcodedProvider.setApcApiKey("sk-d6cbb97c44ec49e78f3ddf0e1133cede");
            hardcodedProvider.setApcStatus(1);
            hardcodedProvider.setApcCreateTime(LocalDateTime.now());
            hardcodedProvider.setApcUpdateTime(LocalDateTime.now());

            return Optional.of(hardcodedProvider);
        }





        AiProviderConfig model = aiProviderConfigMapper.getById(id);
        return Optional.ofNullable(model);
    }

    @Override
    public List<AiProviderConfig> findAll() {

        // 获取所有，因为服务商不可能超过50个，因此限制50，避免性能问题
        return QueryChain.of(aiProviderConfigMapper)
                .limit(50)
                .list();
    }
}
