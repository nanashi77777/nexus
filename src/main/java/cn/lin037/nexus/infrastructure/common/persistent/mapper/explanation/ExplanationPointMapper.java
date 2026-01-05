package cn.lin037.nexus.infrastructure.common.persistent.mapper.explanation;

import cn.lin037.nexus.infrastructure.common.persistent.entity.explanation.ExplanationPointEntity;
import cn.xbatis.core.mybatis.mapper.MybatisMapper;
import org.apache.ibatis.annotations.Mapper;

/**
 * 讲解知识点Mapper接口
 *
 * @author LinSanQi
 */
@Mapper
public interface ExplanationPointMapper extends MybatisMapper<ExplanationPointEntity> {
}