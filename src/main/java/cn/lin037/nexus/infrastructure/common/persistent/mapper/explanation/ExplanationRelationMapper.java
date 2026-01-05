package cn.lin037.nexus.infrastructure.common.persistent.mapper.explanation;

import cn.lin037.nexus.infrastructure.common.persistent.entity.explanation.ExplanationRelationEntity;
import cn.xbatis.core.mybatis.mapper.MybatisMapper;
import org.apache.ibatis.annotations.Mapper;

/**
 * 讲解关系Mapper接口
 *
 * @author LinSanQi
 */
@Mapper
public interface ExplanationRelationMapper extends MybatisMapper<ExplanationRelationEntity> {
}