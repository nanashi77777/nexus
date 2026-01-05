package cn.lin037.nexus.infrastructure.common.task.repository.mapper;

import cn.lin037.nexus.infrastructure.common.task.model.AsyncTask;
import cn.xbatis.core.mybatis.mapper.MybatisMapper;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface AsyncTaskMapper extends MybatisMapper<AsyncTask> {
}
