package cn.lin037.nexus.infrastructure.common.persistent.mapper;

import cn.lin037.nexus.infrastructure.common.persistent.entity.UserAccountEntity;
import cn.xbatis.core.mybatis.mapper.MybatisMapper;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface UserAccountMapper extends MybatisMapper<UserAccountEntity> {
}
