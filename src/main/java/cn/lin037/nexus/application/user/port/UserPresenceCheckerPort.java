package cn.lin037.nexus.application.user.port;

/**
 * 用户布隆过滤器服务接口
 * 提供基于布隆过滤器的快速存在性检查功能
 *
 * @author LinSanQi
 */
public interface UserPresenceCheckerPort {

    /**
     * 检查用户名是否已存在
     *
     * @param username 用户名
     * @return 如果存在返回true
     */
    boolean existsByUsername(String username);

    /**
     * 检查邮箱是否已存在
     *
     * @param email 邮箱
     * @return 如果存在返回true
     */
    boolean existsByEmail(String email);

    /**
     * 检查邀请码是否已存在
     *
     * @param invitationCode 邀请码
     * @return 如果存在返回true
     */
    boolean existsByInvitationCode(String invitationCode);
}