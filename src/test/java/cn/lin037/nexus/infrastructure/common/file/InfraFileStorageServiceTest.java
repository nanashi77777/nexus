package cn.lin037.nexus.infrastructure.common.file;

import cn.lin037.nexus.infrastructure.common.exception.InfrastructureException;
import cn.lin037.nexus.infrastructure.common.file.config.FileStorageProperties;
import cn.lin037.nexus.infrastructure.common.file.enums.AccessLevel;
import cn.lin037.nexus.infrastructure.common.file.exception.FileExceptionCodeEnum;
import cn.lin037.nexus.infrastructure.common.file.model.InfraFileMetadata;
import cn.lin037.nexus.infrastructure.common.file.repository.FileMetadataRepository;
import cn.lin037.nexus.infrastructure.common.file.service.impl.LocalFileStorageService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.io.TempDir;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockMultipartFile;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.util.Optional;
import java.util.Random;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * LocalFileStorageService 的单元测试类。
 * 使用 Mockito 进行依赖模拟，并使用 JUnit 5 的 @TempDir 创建临时目录进行文件操作测试。
 */
@ExtendWith(MockitoExtension.class) // 启用 Mockito 扩展，自动处理 @Mock, @InjectMocks 等注解
public class InfraFileStorageServiceTest {

    private final String OWNER_ID = "user-001";
    private final String GRANTEE_ID = "user-002";
    private final Random random = new Random();
    // @TempDir: JUnit 5 提供的注解，为每个测试方法创建一个临时目录，测试结束后自动清理。
    @TempDir
    Path tempDir;
    // @InjectMocks: 创建一个 LocalFileStorageService 实例，并自动将 @Mock 注解的依赖注入进去。
    @InjectMocks
    private LocalFileStorageService fileStorageService;
    // @Mock: 创建一个 FileMetadataRepository 的模拟对象。所有对该对象的调用都将被拦截。
    @Mock
    private FileMetadataRepository metadataRepository;
    // @Mock: 创建一个 FileStorageProperties 的模拟对象。
    @Mock
    private FileStorageProperties properties;

    /**
     * 在每个测试方法执行前运行的设置方法。
     * 用于配置模拟对象的行为。
     */
    @BeforeEach
    void setUp() {
        // 1. 准备模拟的配置属性
        FileStorageProperties.LocalStorage localStorage = new FileStorageProperties.LocalStorage();
        // 将临时目录设置为文件存储的基础路径，确保测试不会在真实文件系统上留下垃圾文件
        localStorage.setBasePath(tempDir.toString());
        FileStorageProperties.Quota quota = new FileStorageProperties.Quota();
        quota.setEnabled(true); // 启用配额检查
        quota.setFilesPerWeek(10); // 设置每周上传限额为10

        // 2. 定义模拟对象的行为
        // lenient() 用于处理那些并非在所有测试中都会被调用的 mock 设置，避免 UnnecessaryStubbingException。
        // 当调用 properties.getLocal() 时，返回我们准备好的 localStorage 对象。
        lenient().when(properties.getLocal()).thenReturn(localStorage);
        // 当调用 properties.getQuota() 时，返回我们准备好的 quota 对象。
        lenient().when(properties.getQuota()).thenReturn(quota);
    }

    /**
     * 测试文件上传成功场景。
     */
    @Test
    void upload_success() {
        // Arrange: 准备测试数据和模拟行为
        // 1. 创建一个模拟的上传文件
        MockMultipartFile file = new MockMultipartFile("file", "test.txt", "text/plain", "hello world".getBytes());
        // 2. 模拟配额检查：当查询用户本周上传数量时，返回一个未超限的数字 (5 < 10)
        when(metadataRepository.countByOwnerIdentifierAndCreatedAtBetween(eq(OWNER_ID), any(), any())).thenReturn(5L);
        // 3. 模拟元数据保存：当调用 saveBatch 方法时，直接返回传入的 FileMetadata 对象，并模拟ID的生成
        when(metadataRepository.saveOrUpdate(any(InfraFileMetadata.class))).thenAnswer(invocation -> {
            InfraFileMetadata meta = invocation.getArgument(0);
            meta.setFmId(random.nextLong()); // 模拟数据库生成ID
            return meta;
        });

        // Act: 执行被测试的方法
        InfraFileMetadata metadata = fileStorageService.upload(file, OWNER_ID, AccessLevel.PRIVATE);

        // Assert: 验证结果是否符合预期
        assertNotNull(metadata); // 返回的元数据不应为 null
        assertNotNull(metadata.getFmId()); // 元数据ID应该已被设置
        assertEquals(OWNER_ID, metadata.getFmOwnerIdentifier()); // 所有者ID应正确
        assertEquals("test.txt", metadata.getFmOriginalFilename()); // 原始文件名应正确
        // 验证物理文件是否已在临时目录中创建
        assertTrue(Files.exists(tempDir.resolve(metadata.getFmStoragePath())));
        // 验证 metadataRepository.saveBatch 方法是否被确切地调用了1次
        verify(metadataRepository, times(1)).saveOrUpdate(any(InfraFileMetadata.class));
    }

    /**
     * 测试因超出配额导致上传失败的场景。
     */
    @Test
    void upload_quotaExceeded_throwsException() {
        // Arrange: 准备测试数据和模拟行为
        MockMultipartFile file = new MockMultipartFile("file", "test.txt", "text/plain", "hello".getBytes());
        // 模拟配额检查：返回一个已达到限额的数字 (10)
        when(metadataRepository.countByOwnerIdentifierAndCreatedAtBetween(eq(OWNER_ID), any(), any())).thenReturn(10L);

        // Act & Assert: 执行操作并断言会抛出特定异常
        InfrastructureException exception = assertThrows(InfrastructureException.class,
                () -> fileStorageService.upload(file, OWNER_ID, AccessLevel.PRIVATE));
        // 验证抛出的异常的错误码是否为 QUOTA_EXCEEDED
        assertEquals(FileExceptionCodeEnum.QUOTA_EXCEEDED.getCode(), exception.getCode());
    }

    /**
     * 测试文件所有者成功下载自己的私有文件。
     */
    @Test
    void download_asOwner_success() throws IOException {
        // Arrange
        // 1. 创建一个虚拟的物理文件及其元数据
        InfraFileMetadata metadata = createFakeFile("private.txt");
        // 2. 模拟数据库查询：当按ID查找时，返回该元数据
        when(metadataRepository.findById(metadata.getFmId())).thenReturn(Optional.of(metadata));

        // Act
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        // 执行下载，下载者是文件所有者
        boolean result = fileStorageService.download(metadata.getFmId(), OWNER_ID, baos);

        // Assert
        assertTrue(result); // 验证下载操作返回 true
        assertEquals("fake content", baos.toString()); // 验证下载的内容是否正确
    }

    /**
     * 测试非授权用户下载私有文件被拒绝的场景。
     */
    @Test
    void download_accessDenied_throwsException() throws IOException {
        // Arrange
        InfraFileMetadata metadata = createFakeFile("private.txt");
        when(metadataRepository.findById(metadata.getFmId())).thenReturn(Optional.of(metadata));

        // Act & Assert
        // GRANTEE_ID (非所有者，也未被授权) 尝试下载
        InfrastructureException exception = assertThrows(InfrastructureException.class,
                () -> fileStorageService.download(metadata.getFmId(), GRANTEE_ID, new ByteArrayOutputStream()));
        // 验证是否抛出 ACCESS_DENIED 异常
        assertEquals(FileExceptionCodeEnum.ACCESS_DENIED.getCode(), exception.getCode());
    }

    /**
     * 测试文件所有者成功删除文件的场景。
     */
    @Test
    void delete_asOwner_success() throws IOException {
        // Arrange
        InfraFileMetadata metadata = createFakeFile("to-delete.txt");
        Path filePath = tempDir.resolve(metadata.getFmStoragePath());
        assertTrue(Files.exists(filePath)); // 确认物理文件存在
        when(metadataRepository.findById(metadata.getFmId())).thenReturn(Optional.of(metadata));

        // Act
        // 所有者执行删除操作
        fileStorageService.delete(metadata.getFmId(), OWNER_ID);

        // Assert
        assertFalse(Files.exists(filePath)); // 验证物理文件已被删除
        verify(metadataRepository, times(1)).delete(metadata); // 验证元数据删除方法被调用
    }

    /**
     * 测试授权和取消授权的完整流程。
     */
    @Test
    void grantAndRevokeAccess_success() throws IOException {
        // Arrange
        InfraFileMetadata metadata = createFakeFile("share.txt");
        when(metadataRepository.findById(metadata.getFmId())).thenReturn(Optional.of(metadata));

        // --- 授权测试 ---
        // Act: 所有者授权给 GRANTEE_ID
        fileStorageService.grantAccess(metadata.getFmId(), OWNER_ID, GRANTEE_ID);
        // Assert: 验证 GRANTEE_ID 已在授权列表中
        assertTrue(metadata.getFmGrantees().contains(GRANTEE_ID));
        // 验证 saveBatch 方法被调用以持久化授权信息
        verify(metadataRepository, times(1)).saveOrUpdate(metadata);

        // --- 验证授权效果 ---
        // Act & Assert: 被授权者现在应该可以成功下载，不抛出异常
        assertDoesNotThrow(() -> fileStorageService.download(metadata.getFmId(), GRANTEE_ID, new ByteArrayOutputStream()));

        // --- 取消授权测试 ---
        // Act: 所有者取消对 GRANTEE_ID 的授权
        fileStorageService.revokeAccess(metadata.getFmId(), OWNER_ID, GRANTEE_ID);
        // Assert: 验证 GRANTEE_ID 已从授权列表中移除
        assertFalse(metadata.getFmGrantees().contains(GRANTEE_ID));
        // 验证 saveBatch 方法再次被调用以持久化更改（总共2次）
        verify(metadataRepository, times(2)).saveOrUpdate(metadata);

        // --- 验证取消授权效果 ---
        // Act & Assert: 被授权者现在下载应该失败，并抛出 ACCESS_DENIED 异常
        assertThrows(InfrastructureException.class, () -> fileStorageService.download(metadata.getFmId(), GRANTEE_ID, new ByteArrayOutputStream()));
    }

    /**
     * 辅助方法，用于在临时目录中创建一个物理文件，并返回其对应的元数据对象。
     * 这使得测试设置更加简洁和可重用。
     *
     * @param filename 原始文件名
     * @return 创建好的文件元数据
     */
    private InfraFileMetadata createFakeFile(String filename) throws IOException {
        String content = "fake content";
        // 创建一个假的相对路径，模拟真实的文件存储结构
        Path relativePath = Path.of("user-001", "2023-01", UUID.randomUUID() + ".txt");
        Path fullPath = tempDir.resolve(relativePath);
        Files.createDirectories(fullPath.getParent());
        Files.writeString(fullPath, content);

        InfraFileMetadata metadata = new InfraFileMetadata(
                relativePath.toString().replace('\\', '/'),
                "user-001",
                AccessLevel.PRIVATE.getCode(),
                filename,
                (long) content.length(),
                "text/plain",
                "local"
        );
        // 为元数据设置一个随机ID和创建时间，以模拟从数据库中读取的真实对象
        metadata.setFmId(random.nextLong(Long.MAX_VALUE));
        metadata.setFmCreatedAt(LocalDateTime.now());
        return metadata;
    }
}
