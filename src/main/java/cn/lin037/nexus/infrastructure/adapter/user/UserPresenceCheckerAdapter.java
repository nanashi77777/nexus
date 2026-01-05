package cn.lin037.nexus.infrastructure.adapter.user;

import cn.lin037.nexus.application.user.port.UserPresenceCheckerPort;
import cn.lin037.nexus.infrastructure.adapter.user.constant.UserAdapterConstant;
import cn.lin037.nexus.infrastructure.common.cache.service.BloomFilterService;
import cn.lin037.nexus.infrastructure.common.persistent.entity.UserAccountEntity;
import cn.lin037.nexus.infrastructure.common.persistent.mapper.UserAccountMapper;
import cn.xbatis.core.sql.executor.chain.QueryChain;
import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.ArrayList;

import static cn.lin037.nexus.infrastructure.adapter.user.constant.UserAdapterConstant.*;

@Slf4j
@Service
public class UserPresenceCheckerAdapter implements UserPresenceCheckerPort {

    private final BloomFilterService bloomFilterService;
    private final UserAccountMapper userAccountMapper;

    public UserPresenceCheckerAdapter(BloomFilterService bloomFilterService, UserAccountMapper userAccountMapper) {
        this.bloomFilterService = bloomFilterService;
        this.userAccountMapper = userAccountMapper;
    }

    /**
     * 初始化用户PresenceChecker
     */
    @PostConstruct
    private void init() {
        log.info("开始初始化用户PresenceChecker");
        bloomFilterService.tryInit(UserAdapterConstant.BLOOM_FILTER_KEY_OF_USERNAME, 100000L, 0.01);
        bloomFilterService.tryInit(UserAdapterConstant.BLOOM_FILTER_KEY_OF_EMAIL, 100000L, 0.01);
        bloomFilterService.tryInit(UserAdapterConstant.BLOOM_FILTER_KEY_OF_INVITE_CODE, 100000L, 0.01);
        log.info("初始化用户PresenceChecker完成");

        log.info("开始初始化用户PresenceChecker数据");
        ArrayList<String> userNames = new ArrayList<>();
        ArrayList<String> emails = new ArrayList<>();
        ArrayList<String> invitationCodes = new ArrayList<>();
        QueryChain.of(userAccountMapper)
                .select(UserAccountEntity::getUaUsername, UserAccountEntity::getUaEmail, UserAccountEntity::getUaInviteCode)
                .list().forEach(userAccount -> {
                    userNames.add(userAccount.getUaUsername());
                    emails.add(userAccount.getUaEmail());
                    invitationCodes.add(userAccount.getUaInviteCode());
                });

        bloomFilterService.addAll(UserAdapterConstant.BLOOM_FILTER_KEY_OF_USERNAME, userNames);
        bloomFilterService.addAll(UserAdapterConstant.BLOOM_FILTER_KEY_OF_EMAIL, emails);
        bloomFilterService.addAll(UserAdapterConstant.BLOOM_FILTER_KEY_OF_INVITE_CODE, invitationCodes);
        log.info("用户PresenceChecker数据初始化完成");
    }

    @Override
    public boolean existsByUsername(String username) {
        if (!bloomFilterService.contains(BLOOM_FILTER_KEY_OF_USERNAME, username)) {
            return false;
        }
        return QueryChain.of(userAccountMapper)
                .eq(UserAccountEntity::getUaUsername, username)
                .exists();
    }

    @Override
    public boolean existsByEmail(String email) {
        if (!bloomFilterService.contains(BLOOM_FILTER_KEY_OF_EMAIL, email)) {
            return false;
        }
        return QueryChain.of(userAccountMapper)
                .eq(UserAccountEntity::getUaEmail, email)
                .exists();
    }

    @Override
    public boolean existsByInvitationCode(String invitationCode) {
        if (!bloomFilterService.contains(BLOOM_FILTER_KEY_OF_INVITE_CODE, invitationCode)) {
            return false;
        }
        return QueryChain.of(userAccountMapper)
                .eq(UserAccountEntity::getUaInviteCode, invitationCode)
                .exists();
    }
}
