package cn.lin037.nexus.infrastructure.common.cache;

import cn.lin037.nexus.infrastructure.common.cache.service.CacheService;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.redisson.Redisson;
import org.redisson.api.RedissonClient;
import org.redisson.config.Config;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.utility.DockerImageName;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.TimeUnit;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * 缓存服务测试类
 * <p>
 * 该测试类使用 Testcontainers 启动 Redis 容器进行集成测试，
 * 验证 CacheService 的各种缓存操作（字符串、哈希、列表、集合等）。
 */
@Testcontainers
@SpringBootTest
// 导入测试专用的 Redis 配置，以覆盖默认的 Redisson 自动配置
@Import(CacheServiceTest.TestRedisConfiguration.class)
class CacheServiceTest {

    /**
     * Redis 容器实例
     * 使用 Redis 7.2.5 镜像并暴露 6379 端口
     */
    @Container
    @SuppressWarnings("resource")
    private static final GenericContainer<?> redisContainer = new GenericContainer<>(DockerImageName.parse("redis:7.2.5-alpine"))
            .withExposedPorts(6379);
    private final String testKey = "test:string";
    private final String testHashKey = "test:hash";
    private final String testListKey = "test:list";
    private final String testSetKey = "test:set";
    @Autowired
    private CacheService cacheService;

    /**
     * 动态注册 Redis 配置属性
     * 用于连接测试容器中的 Redis 服务
     */
    @DynamicPropertySource
    private static void redisProperties(DynamicPropertyRegistry registry) {
        // 只为 Spring Data Redis 配置属性
        registry.add("spring.data.redis.host", redisContainer::getHost);
        registry.add("spring.data.redis.port", () -> redisContainer.getMappedPort(6379));
        // 确保 Spring Data Redis 也不使用密码
        registry.add("spring.data.redis.password", () -> "");
    }

    @AfterEach
    void tearDown() {
        cacheService.delete(Arrays.asList(testKey, testHashKey, testListKey, testSetKey));
    }

    @Test
    void testStringOperations() {
        // Set and Get
        cacheService.set(testKey, "testValue");
        String value = cacheService.get(testKey);
        assertThat(value).isEqualTo("testValue");

        // Set with expiry and check expiry
        cacheService.set(testKey + ":expiry", "expiryValue", 10, TimeUnit.SECONDS);
        long expire = cacheService.getExpire(testKey + ":expiry", TimeUnit.SECONDS);
        assertThat(expire).isBetween(1L, 10L);

        // Set if absent
        boolean success = cacheService.setIfAbsent(testKey, "newValue");
        assertThat(success).isFalse();
        String existingValue = cacheService.get(testKey);
        assertThat(existingValue).isEqualTo("testValue");

        boolean newSuccess = cacheService.setIfAbsent(testKey + ":new", "newValue");
        assertThat(newSuccess).isTrue();

        // Increment/Decrement
        String counterKey = testKey + ":counter";
        cacheService.set(counterKey, "10");
        long incremented = cacheService.increment(counterKey, 5);
        assertThat(incremented).isEqualTo(15L);
        long decremented = cacheService.decrement(counterKey, 3);
        assertThat(decremented).isEqualTo(12L);

        // Exists and Delete
        assertThat(cacheService.exists(testKey)).isTrue();
        cacheService.delete(testKey);
        assertThat(cacheService.exists(testKey)).isFalse();
    }

    @Test
    void testHashOperations() {
        // Hash Set and Get
        cacheService.hashSet(testHashKey, "field1", "value1");
        cacheService.hashSet(testHashKey, "field2", 123);

        String value1 = cacheService.hashGet(testHashKey, "field1");
        assertThat(value1).isEqualTo("value1");

        Integer value2 = cacheService.hashGet(testHashKey, "field2");
        assertThat(value2).isEqualTo(123);

        // Hash Set All and Get All
        cacheService.hashSetAll(testHashKey, Map.of("field3", "value3", "field4", "value4"));
        Map<String, Object> allValues = cacheService.hashGetAll(testHashKey);
        assertThat(allValues).hasSize(4)
                .containsEntry("field1", "value1")
                .containsEntry("field3", "value3");

        // Hash Exists, Size, and Delete
        assertThat(cacheService.hashExists(testHashKey, "field1")).isTrue();
        assertThat(cacheService.hashSize(testHashKey)).isEqualTo(4);
        cacheService.hashDelete(testHashKey, "field1", "field2");
        assertThat(cacheService.hashSize(testHashKey)).isEqualTo(2);
        assertThat(cacheService.hashExists(testHashKey, "field1")).isFalse();
    }

    @Test
    void testListOperations() {
        // Push and Pop
        cacheService.listRightPush(testListKey, "a");
        cacheService.listRightPush(testListKey, "b");
        cacheService.listLeftPush(testListKey, "c"); // List is now [c, a, b]

        assertThat(cacheService.listSize(testListKey)).isEqualTo(3);

        String leftPop = cacheService.listLeftPop(testListKey);
        assertThat(leftPop).isEqualTo("c");

        String rightPop = cacheService.listRightPop(testListKey);
        assertThat(rightPop).isEqualTo("b");

        // Range
        cacheService.listRightPush(testListKey, "x"); // List is now [a, x]
        List<String> range = cacheService.listRange(testListKey, 0, -1);
        assertThat(range).containsExactly("a", "x");
    }

    @Test
    void testSetOperations() {
        // Add and Members
        long addedCount = cacheService.setAdd(testSetKey, Arrays.asList("member1", "member2", "member3", "member1"));
        assertThat(addedCount).isEqualTo(3);

        Set<String> members = cacheService.setMembers(testSetKey);
        assertThat(members).hasSize(3).contains("member1", "member2", "member3");

        // Is Member and Size
        assertThat(cacheService.setIsMember(testSetKey, "member2")).isTrue();
        assertThat(cacheService.setIsMember(testSetKey, "member4")).isFalse();
        assertThat(cacheService.setSize(testSetKey)).isEqualTo(3);

        // Remove
        long removedCount = cacheService.setRemove(testSetKey, Arrays.asList("member2", "member4"));
        assertThat(removedCount).isEqualTo(1);
        assertThat(cacheService.setSize(testSetKey)).isEqualTo(2);
    }

    @Test
    void testKeysOperation() {
        cacheService.set("test:pattern:1", "1");
        cacheService.set("test:pattern:2", "2");
        cacheService.set("test:another:3", "3");

        Set<String> keys = cacheService.keys("test:pattern:*");
        assertThat(keys).hasSize(2).contains("test:pattern:1", "test:pattern:2");

        cacheService.delete(Arrays.asList("test:pattern:1", "test:pattern:2", "test:another:3"));
    }

    /**
     * 测试专用的配置类，用于手动创建 RedissonClient Bean
     */
    @TestConfiguration
    static class TestRedisConfiguration {
        @Bean(destroyMethod = "shutdown")
        public RedissonClient redissonClient() {
            Config config = new Config();
            // 直接使用 Testcontainers 提供的 Redis 地址和端口，不设置密码
            String redisAddress = String.format("redis://%s:%d",
                    redisContainer.getHost(),
                    redisContainer.getMappedPort(6379));
            config.useSingleServer().setAddress(redisAddress);
            return Redisson.create(config);
        }
    }
}