package cn.lin037.nexus.application.user.port;

import cn.lin037.nexus.infrastructure.common.persistent.entity.UserAccountEntity;
import cn.lin037.nexus.infrastructure.common.persistent.enums.UserStatusEnum;
import com.sun.istack.NotNull;
import db.sql.api.Getter;

import java.util.List;
import java.util.Optional;
import java.util.function.Consumer;

/**
 * 用户仓储接口
 * 负责与用户账户相关的持久化操作
 *
 * @author LinSanQi
 */
public interface UserAccountRepository {

    // ====== 查询操作 ======

    /**
     * 根据邀请码查询用户ID
     *
     * @param inviteCode 邀请码
     * @return 用户ID的Optional封装
     */
    Optional<Long> findUserIdByInviteCode(String inviteCode);
    /**
     * 根据用户名查找用户实体
     *
     * @param username 用户名
     * @param getters  获取字段列表
     * @return 用户实体的Optional封装
     */
    Optional<UserAccountEntity> findByUsername(String username, List<Getter<UserAccountEntity>> getters);

    /**
     * 根据邮箱查找用户实体
     *
     * @param email   邮箱
     * @param getters 获取字段列表
     * @return 用户实体的Optional封装
     */
    Optional<UserAccountEntity> findByEmail(String email, List<Getter<UserAccountEntity>> getters);

    /**
     * 根据账户信息（用户名或邮箱）查找用户实体
     *
     * @param account 账户信息
     * @param getters 获取字段列表
     * @return 用户实体的Optional封装
     */
    Optional<UserAccountEntity> findByAccount(String account, List<Getter<UserAccountEntity>> getters);

    /**
     * 根据用户ID查找用户实体
     *
     * @param userId  用户ID
     * @param getters 获取字段列表
     * @return 用户实体
     */
    Optional<UserAccountEntity> findById(Long userId, List<Getter<UserAccountEntity>> getters);

    /**
     * 根据用户ID查找用户基本信息
     * @param userId 用户ID
     * @return 用户实体
     */
    Optional<UserAccountEntity> findById(Long userId);



    // ====== 状态/删除操作 ======

    /**
     * 更新用户状态
     * 操作类，防止改动错误的信息，因此都需要对ID设置NotNull注解
     * @param userId 用户ID
     * @param status 新状态
     * @return 是否更新成功
     */
    Boolean updateStatus(@NotNull Long userId, UserStatusEnum status);

    /**
     * 删除用户
     *
     * @param userId 用户ID
     * @return 是否删除成功
     */
    Boolean deleteUser(Long userId);

    // ====== 写操作 ======

    /**
     * 生成唯一邀请码
     *
     * @return 唯一邀请码字符串
     */
    String generateUniqueInviteCode();

    /**
     * 保存新用户
     *
     * @param userAccount 用户实体
     */
    void save(UserAccountEntity userAccount);

    /**
     * 更新用户信息
     *
     * @param updater 用户更新函数
     */
    void updateById(@NotNull Long userId, Consumer<UserAccountEntity> updater);
}
