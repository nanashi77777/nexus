package cn.lin037.nexus.infrastructure.adapter.user;

import cn.hutool.core.util.RandomUtil;
import cn.lin037.nexus.application.user.port.UserAccountRepository;
import cn.lin037.nexus.common.constant.enums.result.impl.CommonResultCodeEnum;
import cn.lin037.nexus.common.exception.ApplicationException;
import cn.lin037.nexus.infrastructure.adapter.user.constant.UserAdapterConstant;
import cn.lin037.nexus.infrastructure.adapter.utils.RepositoryUtils;
import cn.lin037.nexus.infrastructure.common.cache.service.BloomFilterService;
import cn.lin037.nexus.infrastructure.common.id.HutoolSnowflakeIdGenerator;
import cn.lin037.nexus.infrastructure.common.persistent.entity.UserAccountEntity;
import cn.lin037.nexus.infrastructure.common.persistent.enums.UserStatusEnum;
import cn.lin037.nexus.infrastructure.common.persistent.mapper.UserAccountMapper;
import cn.xbatis.core.sql.executor.chain.QueryChain;
import cn.xbatis.core.sql.executor.chain.UpdateChain;
import com.sun.istack.NotNull;
import db.sql.api.Getter;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.function.Consumer;

@Service
public class UserAccountRepositoryImpl implements UserAccountRepository {

    private final BloomFilterService bloomFilterService;
    private final UserAccountMapper userAccountMapper;

    public UserAccountRepositoryImpl(BloomFilterService bloomFilterService, UserAccountMapper userAccountMapper) {
        this.bloomFilterService = bloomFilterService;
        this.userAccountMapper = userAccountMapper;
    }

    @Override
    public Optional<Long> findUserIdByInviteCode(String inviteCode) {
        UserAccountEntity userAccount = QueryChain.of(userAccountMapper)
                .select(UserAccountEntity::getUaId)
                .eq(UserAccountEntity::getUaInviteCode, inviteCode)
                .limit(1)
                .get();
        if (userAccount == null) {
            return Optional.empty();
        }
        return Optional.ofNullable(userAccount.getUaId());
    }
    @Override
    public Optional<UserAccountEntity> findByUsername(String username, List<Getter<UserAccountEntity>> getters) {

        UserAccountEntity userAccount = RepositoryUtils.getQueryChainWithFields(userAccountMapper, getters)
                .eq(UserAccountEntity::getUaUsername, username)
                .limit(1)
                .get();
        return Optional.ofNullable(userAccount);
    }

    @Override
    public Optional<UserAccountEntity> findByEmail(String email, List<Getter<UserAccountEntity>> getters) {

        UserAccountEntity userAccount = RepositoryUtils.getQueryChainWithFields(userAccountMapper, getters)
                .eq(UserAccountEntity::getUaEmail, email)
                .limit(1)
                .get();
        return Optional.ofNullable(userAccount);
    }

    @Override
    public Optional<UserAccountEntity> findByAccount(String account, List<Getter<UserAccountEntity>> getters) {
        QueryChain<UserAccountEntity> queryChain = RepositoryUtils.getQueryChainWithFields(userAccountMapper, getters)
                .eq(UserAccountEntity::getUaUsername, account)
                .or().eq(UserAccountEntity::getUaEmail, account)
                .or().eq(UserAccountEntity::getUaPhone, account);
        if (account != null && isValidLongId(account)) {
            queryChain.or().eq(UserAccountEntity::getUaId,Long.parseLong(account));
        }


        UserAccountEntity userAccount = queryChain.limit(1).get();
        return Optional.ofNullable(userAccount);
    }


    @Override
    public Optional<UserAccountEntity> findById(Long userId, List<Getter<UserAccountEntity>> getters) {
        UserAccountEntity userAccount = RepositoryUtils.getQueryChainWithFields(userAccountMapper, getters)
                .eq(UserAccountEntity::getUaId, userId)
                .limit(1)
                .get();
        return Optional.ofNullable(userAccount);
    }

    @Override
    public Optional<UserAccountEntity> findById(Long userId) {
        UserAccountEntity userAccount = QueryChain.of(userAccountMapper)
                .eq(UserAccountEntity::getUaId, userId)
                .limit(1)
                .get();
        return Optional.ofNullable(userAccount);
    }



    @Override
    public Boolean updateStatus(@NotNull Long userId, UserStatusEnum status) {
        int rows = UpdateChain.of(userAccountMapper)
                .set(UserAccountEntity::getUaStatus, status.getCode())
                .eq(UserAccountEntity::getUaId, userId)
                .execute();
        return rows > 0;
    }

    @Override
    public Boolean deleteUser(Long userId) {
        int rows = userAccountMapper.deleteById(userId);
        return rows > 0;
    }

    @Override
    public synchronized String generateUniqueInviteCode() {
        String inviteCode = RandomUtil.randomString(6).toUpperCase();
        int maxStep = 100;
        while (maxStep > 0) {
            if (!bloomFilterService.contains(UserAdapterConstant.BLOOM_FILTER_KEY_OF_INVITE_CODE, inviteCode)) {
                bloomFilterService.add(UserAdapterConstant.BLOOM_FILTER_KEY_OF_INVITE_CODE, inviteCode);
                return inviteCode;
            }
            maxStep--;
        }
        throw new ApplicationException(CommonResultCodeEnum.FUTURE_ERROR, "邀请码在达到最大重试次数后依旧生成失败，请联系管理员检查系统的邀请码配额数量是否已满。");
    }

    @Override
    public void save(@NotNull UserAccountEntity userAccount) {
        if (userAccount.getUaId() == null) {
            userAccount.setUaId(HutoolSnowflakeIdGenerator.generateLongId());
        }
        int save = userAccountMapper.save(userAccount);
        if (save > 0) {
            addToBloomFiltersIfPresent(userAccount);
        }
    }

    @Override
    public void updateById(@NotNull Long userId, Consumer<UserAccountEntity> updater) {
        UserAccountEntity userAccount = new UserAccountEntity();
        updater.accept(userAccount);
        userAccount.setUaId(userId);
        int update = userAccountMapper.update(userAccount);
        if (update > 0) {
            addToBloomFiltersIfPresent(userAccount);
        }
    }

    private void addToBloomFiltersIfPresent(UserAccountEntity user) {
        if (user.getUaUsername() != null && !user.getUaUsername().isBlank()) {
            bloomFilterService.add(UserAdapterConstant.BLOOM_FILTER_KEY_OF_USERNAME, user.getUaUsername());
        }
        if (user.getUaEmail() != null && !user.getUaEmail().isBlank()) {
            bloomFilterService.add(UserAdapterConstant.BLOOM_FILTER_KEY_OF_EMAIL, user.getUaEmail());
        }
        if (user.getUaInviteCode() != null && !user.getUaInviteCode().isBlank()) {
            bloomFilterService.add(UserAdapterConstant.BLOOM_FILTER_KEY_OF_INVITE_CODE, user.getUaInviteCode());
        }
    }
    public boolean isValidLongId(String idString) {
        if (idString == null || idString.trim().isEmpty()) {
            return false;
        }
        try {
            Long.parseLong(idString);
            return true;
        } catch (NumberFormatException e) {
            return false;
        }
    }
}
