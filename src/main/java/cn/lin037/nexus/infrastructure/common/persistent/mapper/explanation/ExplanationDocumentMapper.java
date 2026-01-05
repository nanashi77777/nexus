package cn.lin037.nexus.infrastructure.common.persistent.mapper.explanation;

import cn.lin037.nexus.infrastructure.common.persistent.entity.explanation.ExplanationDocumentEntity;
import cn.xbatis.core.mybatis.mapper.MybatisMapper;
import org.apache.ibatis.annotations.Mapper;

/**
 * 讲解文档Mapper接口
 *
 * @author LinSanQi
 */
@Mapper
public interface ExplanationDocumentMapper extends MybatisMapper<ExplanationDocumentEntity> {
}