package cn.lin037.nexus.infrastructure.common.persistent.mapper.explanation;

import cn.lin037.nexus.infrastructure.common.persistent.entity.explanation.ExplanationSubsectionEntity;
import cn.xbatis.core.mybatis.mapper.MybatisMapper;
import org.apache.ibatis.annotations.Mapper;

/**
 * 讲解小节Mapper接口
 *
 * @author LinSanQi
 */
@Mapper
public interface ExplanationSubsectionMapper extends MybatisMapper<ExplanationSubsectionEntity> {
}