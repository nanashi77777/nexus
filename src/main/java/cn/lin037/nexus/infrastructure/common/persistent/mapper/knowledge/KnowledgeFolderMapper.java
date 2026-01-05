package cn.lin037.nexus.infrastructure.common.persistent.mapper.knowledge;

import cn.lin037.nexus.infrastructure.common.persistent.entity.knowledge.KnowledgeFolderEntity;
import cn.xbatis.core.mybatis.mapper.MybatisMapper;
import org.apache.ibatis.annotations.Mapper;

/**
 * 知识点文件夹Mapper
 *
 * @author LinSanQi
 */
@Mapper
public interface KnowledgeFolderMapper extends MybatisMapper<KnowledgeFolderEntity> {
} 