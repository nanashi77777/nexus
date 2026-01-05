package cn.lin037.nexus.infrastructure.common.task;

import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ActiveProfiles;
import org.testcontainers.junit.jupiter.Testcontainers;

/**
 * 异步任务模块集成测试
 * <p>
 * 该测试使用 Testcontainers 启动一个 PostgreSQL 数据库实例，
 * 对 AsyncTaskManager 的核心功能进行端到端验证。
 */
@Testcontainers
@SpringBootTest
//@Import(AsyncTaskModuleTest.TestTaskConfiguration.class)
// 通过此注解，我们告知Spring Test在执行完此类中的所有测试后，应关闭并重建应用上下文。
// 这会触发ExecutorService的销毁方法（shutdown），确保所有后台任务在数据库容器关闭前结束。
// 这是解决测试生命周期与后台任务生命周期冲突的关键。
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_CLASS)
@ActiveProfiles("task")
public class AsyncTaskModuleTest {
    /**
     * 定义 Testcontainers PostgreSQL 容器。
     * 使用与项目匹配的 PostgreSQL 13.3 镜像。
     * 为了清晰和安全，我们为测试容器显式指定了数据库名称、用户名和密码。
     * 数据库的表结构将通过 @BeforeEach 中的 JdbcTemplate 直接创建，而不是通过外部脚本。
     *//*
    @Container
    @SuppressWarnings("resource")
    private static final PostgreSQLContainer<?> postgresqlContainer = new PostgreSQLContainer<>("postgres:13.3")
            .withDatabaseName("nexus-test-db")
            .withUsername("test-user")
            .withPassword("test-password");
    @Autowired
    private AsyncTaskManager asyncTaskManager;
    @Autowired
    private AsyncTaskRepository asyncTaskRepository;
    @Autowired
    private JdbcTemplate jdbcTemplate;

    *//**
     * 动态配置数据源属性。
     * <p>
     * 在 Spring 上下文启动前，将应用的 JDBC URL、用户名和密码
     * 指向由 Testcontainers 启动的临时数据库实例。
     *
     * @param registry 动态属性注册表
     *//*
    @DynamicPropertySource
    static void postgresqlProperties(DynamicPropertyRegistry registry) {
        String jdbcUrl = postgresqlContainer.getJdbcUrl() + "?currentSchema=public&stringtype=unspecified";
        registry.add("spring.datasource.url", () -> jdbcUrl);
        registry.add("spring.datasource.username", postgresqlContainer::getUsername);
        registry.add("spring.datasource.password", postgresqlContainer::getPassword);
        registry.add("spring.datasource.driver-class-name", () -> "org.postgresql.Driver");

        // 在这里，容器已启动，但Spring上下文尚未创建。
        // 这是执行一次性数据库初始化的完美时机。
        try (Connection conn = DriverManager.getConnection(
                jdbcUrl,
                postgresqlContainer.getUsername(),
                postgresqlContainer.getPassword());
             Statement stmt = conn.createStatement()) {

            String schemaSql = """
                    CREATE TABLE public.async_tasks
                    (
                        at_id                BIGSERIAL PRIMARY KEY,
                        at_task_type         VARCHAR(255)             NOT NULL,
                        at_status            INT                      NOT NULL,
                        at_parameters_json   JSONB,
                        at_result_json       JSONB,
                        at_error_message     TEXT,
                        at_owner_identifier  VARCHAR(255),
                        at_created_at        TIMESTAMP NOT NULL DEFAULT NOW(),
                        at_started_at        TIMESTAMP,
                        at_finished_at       TIMESTAMP
                    );
                    """;
            stmt.execute(schemaSql);
        } catch (SQLException e) {
            throw new RuntimeException("Failed to initialize database schema for tests", e);
        }
    }

    *//**
     * 在每个测试用例执行前，仅清空表数据，确保测试隔离性。
     * 表结构已在 @DynamicPropertySource 中被创建。
     *//*
    @BeforeEach
    void cleanupData() {
        // TRUNCATE 比 DROP+CREATE 更高效，适合在测试之间清理数据
        jdbcTemplate.execute("TRUNCATE TABLE public.async_tasks RESTART IDENTITY");
    }

    *//**
     * 测试任务成功执行的场景。
     * 提交一个任务，并使用 Awaitility 等待其完成，
     * 最后验证数据库中的任务状态为 COMPLETED，并且结果已正确写入。
     *//*
    @Test
    void testSubmitAndSuccessfulExecution() {
        // 提交一个保证成功的任务
        Long taskId = asyncTaskManager.submit("SUCCESS_TASK", Map.fromCode("param", "value"), "test-user");
        assertThat(taskId).isNotNull();

        // 使用 Awaitility 等待任务状态变为 COMPLETED，最长等待10秒
        await().atMost(10, SECONDS).untilAsserted(() -> {
            AsyncTask task = asyncTaskRepository.findById(taskId).orElseThrow();
            assertThat(task.getAtStatus()).isEqualTo(TaskStatusEnum.COMPLETED.getCode());
            // 确保与实际的JSON序列化库输出（可能包含空格）匹配
            assertThat(task.getAtResultJson()).isEqualTo("{\"result\": \"SUCCESS_RESULT\"}");
        });
    }

    *//**
     * 测试任务执行失败的场景。
     * 提交一个注定会抛出异常的任务，等待其完成，
     * 最后验证数据库中的任务状态为 FAILED，并且错误信息已被记录。
     *//*
    @Test
    void testSubmitAndFailedExecution() {
        // 提交一个保证失败的任务
        Long taskId = asyncTaskManager.submit("FAIL_TASK", Map.fromCode(), "test-user");

        // 等待任务状态变为 FAILED
        await().atMost(10, SECONDS).untilAsserted(() -> {
            AsyncTask task = asyncTaskRepository.findById(taskId).orElseThrow();
            assertThat(task.getAtStatus()).isEqualTo(TaskStatusEnum.FAILED.getCode());
            assertThat(task.getAtErrorMessage()).contains("Task deliberately failed");
        });
    }

    *//**
     * 测试取消一个处于 WAITING 状态的任务。
     * 提交任务后，立即对其进行取消操作，
     * 验证其状态被直接设置为 CANCELLED。
     *//*
    @Test
    void testCancelWaitingTask() {
        // 提交一个长耗时任务，但在它开始执行前就取消它
        Long taskId = asyncTaskManager.submit("LONG_RUNNING_TASK", Map.fromCode(), "test-user");
        asyncTaskManager.cancel(taskId, "test-admin");

        // 验证任务状态立即变为 CANCELLED
        AsyncTask task = asyncTaskRepository.findById(taskId).orElseThrow();
        assertThat(task.getAtStatus()).isEqualTo(TaskStatusEnum.CANCELLED.getCode());
    }

    *//**
     * 测试取消一个处于 RUNNING 状态的任务。
     * 提交一个长耗时任务，等待其进入 RUNNING 状态，
     * 然后发起取消请求，并验证任务最终状态为 CANCELLED。
     *//*
    @Test
    void testCancelRunningTaskIsSuccessful() {
        // 提交一个长耗时任务
        Long taskId = asyncTaskManager.submit("LONG_RUNNING_TASK", Map.fromCode(), "test-user");

        // 使用Awaitility等待，直到任务状态变为RUNNING
        await().atMost(5, SECONDS).until(() -> {
            AsyncTask task = asyncTaskRepository.findById(taskId).orElseThrow();
            return task.getAtStatus().equals(TaskStatusEnum.RUNNING.getCode());
        });

        // 对运行中的任务发起取消请求
        asyncTaskManager.cancel(taskId, "test-admin");

        // 断言：等待任务最终状态变为 CANCELLED
        await().atMost(5, SECONDS).untilAsserted(() -> {
            AsyncTask task = asyncTaskRepository.findById(taskId).orElseThrow();
            assertThat(task.getAtStatus()).isEqualTo(TaskStatusEnum.CANCELLED.getCode());
            assertThat(task.getAtErrorMessage()).contains("任务执行期间被取消");
        });
    }

    *//**
     * 测试专用的 Spring 配置类。
     * <p>
     * 在这里我们定义了几个测试用的 TaskExecutor Bean，
     * 以便在测试用例中模拟不同类型的任务。
     *//*
    @TestConfiguration
    static class TestTaskConfiguration {

        *//**
         * 模拟一个总是成功执行的任务。
         * 返回一个Map，以确保结果可以被正确序列化为有效的JSON。
     *//*
        @Bean
        public TaskExecutor<Map<String, Object>, Map<String, String>> successTaskExecutor() {
            return new TaskExecutor<>() {
                @Override
                public String getTaskType() {
                    return "SUCCESS_TASK";
                }

                @Override
                @SuppressWarnings("unchecked")
                public Class<Map<String, Object>> getParametersType() {
                    return (Class<Map<String, Object>>) (Class<?>) Map.class;
                }

                @Override
                public Map<String, String> execute(Map<String, Object> parameters, TaskContext context) {
                    return Map.fromCode("result", "SUCCESS_RESULT");
                }
            };
        }

        *//**
         * 模拟一个总是执行失败的任务。
     *//*
        @Bean
        public TaskExecutor<Map<String, Object>, Void> failTaskExecutor() {
            return new TaskExecutor<>() {
                @Override
                public String getTaskType() {
                    return "FAIL_TASK";
                }

                @SuppressWarnings("unchecked")
                @Override
                public Class<Map<String, Object>> getParametersType() {
                    return (Class<Map<String, Object>>) (Class<?>) Map.class;
                }

                @Override
                public Void execute(Map<String, Object> parameters, TaskContext context) {
                    throw new IllegalStateException("Task deliberately failed");
                }
            };
        }

        *//**
         * 模拟一个长时间运行的任务，用于测试取消逻辑。
     *//*
        @Bean
        public TaskExecutor<Map<String, Object>, Void> testLongRunningTaskExecutor() {
            return new TaskExecutor<>() {
                @Override
                public String getTaskType() {
                    return "LONG_RUNNING_TASK";
                }

                @SuppressWarnings("unchecked")
                @Override
                public Class<Map<String, Object>> getParametersType() {
                    return (Class<Map<String, Object>>) (Class<?>) Map.class;
                }


                @Override
                public Void execute(Map<String, Object> parameters, TaskContext context) throws InterruptedException {
                    // 模拟一个可被中断的长时间运行任务。
                    // 它会周期性地检查取消标志。
                    for (int i = 0; i < 300; i++) { // 最多运行30秒
                        if (context.isCancellationRequested()) {
                            // 当任务被外部请求取消时，它应抛出异常或干净地退出。
                            // 抛出异常可以被调度器捕获，从而触发正确的失败/取消处理流程。
                            throw new InterruptedException("Task was cancelled by request.");
                        }
                        Thread.sleep(100);
                    }
                    return null;
                }
            };
        }
    }*/
}


