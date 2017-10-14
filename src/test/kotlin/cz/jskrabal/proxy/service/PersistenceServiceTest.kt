package cz.jskrabal.proxy.service

import cz.jskrabal.proxy.config.PersistenceConfig
import io.vertx.core.Vertx
import io.vertx.core.json.JsonObject
import io.vertx.ext.unit.TestContext
import io.vertx.ext.unit.junit.VertxUnitRunner
import org.assertj.core.api.Assertions.assertThat
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TemporaryFolder
import org.junit.runner.RunWith
import java.io.File

@RunWith(VertxUnitRunner::class)
class PersistenceServiceTest {

    val tempFolder = TemporaryFolder()
        @Rule get

    private lateinit var vertx: Vertx
    private lateinit var service: PersistenceService
    private lateinit var path: String
    private val testJson = JsonObject().put("foo", "bar")

    @Before
    fun setUp(context: TestContext) {
        vertx = Vertx.vertx()
        path = tempFolder.newFolder().path
        service = PersistenceServiceFactory.create(vertx, PersistenceConfig(path))
    }

    @Test
    fun testSaveConfig(context: TestContext) {
        service.save("test", testJson, context.asyncAssertSuccess {
            assertThat(File("$path/test.json"))
                    .isNotNull()
                    .isFile()
        })
    }

    @Test
    fun testLoadConfig(context: TestContext) {
        service.save("test", testJson, context.asyncAssertSuccess {
            service.load("test", context.asyncAssertSuccess {
                assertThat(it)
                        .isNotNull
                        .isEqualTo(testJson)
            })
        })
    }

    @Test
    fun testDeleteConfig(context: TestContext) {
        service.save("test", testJson, context.asyncAssertSuccess {
            assertThat(File("$path/test.json"))
                    .isNotNull()
                    .isFile()
            service.delete("test", context.asyncAssertSuccess {
                assertThat(File("$path/test.json"))
                        .isNotNull()
                        .doesNotExist()
            })
        })
    }

    @Test
    fun testLoadAllConfig(context: TestContext) {
        service.save("test", testJson, context.asyncAssertSuccess {
            service.all("test", context.asyncAssertSuccess {
                assertThat(it)
                        .hasOnlyOneElementSatisfying {
                            assertThat(it)
                                    .isNotNull
                                    .isEqualTo(testJson)
                        }
            })
        })
    }
}