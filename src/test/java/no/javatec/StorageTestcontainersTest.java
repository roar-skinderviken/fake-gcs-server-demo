package no.javatec;

import com.google.cloud.NoCredentials;
import com.google.cloud.storage.BlobId;
import com.google.cloud.storage.BlobInfo;
import com.google.cloud.storage.Storage;
import com.google.cloud.storage.StorageOptions;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.testcontainers.containers.BindMode;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.utility.DockerImageName;

import java.util.stream.StreamSupport;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

@Testcontainers
class StorageTestcontainersTest {

    private static Storage storage;

    @SuppressWarnings("resource")
    @Container
    private static final GenericContainer<?> STORAGE_CONTAINER =
            new GenericContainer<>(DockerImageName.parse("fsouza/fake-gcs-server:latest"))
                    .withExposedPorts(4443)
                    .withClasspathResourceMapping("data", "/data", BindMode.READ_WRITE)
                    .withCreateContainerCmdModifier(cmd ->
                            cmd.withEntrypoint("/bin/fake-gcs-server", "-data", "/data", "-scheme", "http"));

    @BeforeAll
    static void beforeAll() {
        STORAGE_CONTAINER.start();

        @SuppressWarnings("HttpUrlsUsage")
        var storageHost = String.format("http://%s:%d",
                STORAGE_CONTAINER.getHost(),
                STORAGE_CONTAINER.getFirstMappedPort());

        System.setProperty(STORAGE_HOST_PROPERTY, storageHost);

        storage = StorageOptions.newBuilder()
                .setHost(storageHost)
                .setCredentials(NoCredentials.getInstance())
                .setProjectId("integration-test")
                .build()
                .getService();
    }

    @AfterAll
    static void afterAll() {
        STORAGE_CONTAINER.stop();
    }

    @BeforeEach
    void setUp() {
        deleteAllBlobs();
    }

    @Test
    void verifyTestContainerIsRunning() {
        assertTrue(STORAGE_CONTAINER.isRunning());
    }

    @Test
    void listGivenBucketWithBlobExpectBlob() {
        createTestBlob();

        assertEquals(1, blobCount());
        assertEquals("a-file", getBlob("a-file").getName());
    }

    private static final String STORAGE_HOST_PROPERTY = "storage.host";
    static final String TEST_BUCKET = "test-bucket";

    private void createTestBlob() {
        storage.create(BlobInfo.newBuilder(BlobId.of(TEST_BUCKET, "a-file"))
                        .setContentType("text/plain")
                        .build(),
                "~some-data~".getBytes());
    }

    private long blobCount() {
        return StreamSupport.stream(storage.list(TEST_BUCKET).iterateAll().spliterator(), false).count();
    }

    private BlobInfo getBlob(final String path) {
        return storage.get(BlobId.of(TEST_BUCKET, path));
    }

    private void deleteAllBlobs() {
        storage.list(TEST_BUCKET).iterateAll().forEach(blob -> storage.delete(blob.getBlobId()));
    }
}