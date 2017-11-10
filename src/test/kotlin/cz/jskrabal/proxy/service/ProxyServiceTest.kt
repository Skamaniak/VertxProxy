package cz.jskrabal.proxy.service

import com.fasterxml.jackson.module.kotlin.registerKotlinModule
import com.nhaarman.mockito_kotlin.any
import com.nhaarman.mockito_kotlin.doAnswer
import com.nhaarman.mockito_kotlin.mock
import com.nhaarman.mockito_kotlin.verify
import cz.jskrabal.proxy.config.ProxyConfig
import cz.jskrabal.proxy.model.Proxy
import io.vertx.core.AsyncResult
import io.vertx.core.Future
import io.vertx.core.Handler
import io.vertx.core.Vertx
import io.vertx.core.json.Json
import io.vertx.core.json.JsonObject
import io.vertx.ext.unit.TestContext
import io.vertx.ext.unit.junit.VertxUnitRunner
import org.assertj.core.api.SoftAssertions
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Matchers.anyString

@RunWith(VertxUnitRunner::class)
class ProxyServiceTest {

    private lateinit var vertx: Vertx
    private lateinit var service: ProxyService
    private lateinit var persistence: PersistenceService
    private val testJson = Proxy(id = "test").toJson()


    @Before
    fun setUp(context: TestContext) {
        Json.mapper.registerKotlinModule()
        vertx = Vertx.vertx()
        persistence = mock {
            on { save(anyString(), any(), any()) } doAnswer {
                respondWith(it.arguments[2], Future.succeededFuture<Void>())
            }
            on { load(anyString(), any()) } doAnswer {
                respondWith(it.arguments[1], Future.succeededFuture<JsonObject>(testJson))
            }
            on { delete(anyString(), any()) } doAnswer {
                respondWith(it.arguments[1], Future.succeededFuture<Void>())
            }
        }

        service = ProxyServiceImpl(vertx, ProxyConfig(), persistenceService = persistence)
    }

    private fun <T> respondWith(handlerArg: Any, future: Future<T>): PersistenceService {
        val handler = handlerArg as Handler<AsyncResult<T>>
        handler.handle(future)
        return persistence
    }

    @Test
    fun testDeployProxy(context: TestContext) {
        service.deployProxy(testJson, context.asyncAssertSuccess {
            verify(persistence).save(anyString(), any(), any())
        })
    }

    @Test
    fun testGetProxy(context: TestContext) {
        service.deployProxy(testJson, context.asyncAssertSuccess {
            service.getProxy("test", context.asyncAssertSuccess { result ->
                SoftAssertions.assertSoftly {
                    it.assertThat(result.getString("id")).isEqualTo("test")
                    it.assertThat(result.getString("deploymentId")).isNotEmpty
                }
            })
        })
    }

    @Test
    fun testUndeployProxy(context: TestContext) {
        service.deployProxy(testJson, context.asyncAssertSuccess {
            service.undeployProxy("test", context.asyncAssertSuccess {

            })
        })
    }

}