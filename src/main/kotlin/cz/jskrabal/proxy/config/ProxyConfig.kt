package cz.jskrabal.proxy.config

import org.hibernate.validator.constraints.NotEmpty
import org.hibernate.validator.constraints.Range
import javax.validation.constraints.NotNull


data class ProxyConfig(@NotNull val network: NetworkConfig = NetworkConfig(),
                       val nextHttpProxy: NetworkConfig?,
                       val nextTunnelProxy: NetworkConfig?,
                       @NotNull val idGenerator: IdGeneratorType = IdGeneratorType.RANDOM,
                       @NotNull val customHeaders: CustomHeadersConfig = CustomHeadersConfig(),
                       @NotNull val stream: StreamConfig = StreamConfig())

data class CustomHeadersConfig(val appendToRequest: Map<String, String> = emptyMap(),
                               val appendToResponse: Map<String, String> = emptyMap(),
                               val removeFromRequest: List<String> = emptyList(),
                               val removeFromResponse: List<String> = emptyList(),
                               val addTransferIdHeader: Boolean = false,
                               val addForwardedForHeaders: Boolean = false,
                               val addForwardedByHeaders: Boolean = false
)

data class NetworkConfig(@NotEmpty val host: String = "0.0.0.0",
                         @Range(min = 0, max = 65536) val port: Int = 8080)

data class StreamConfig(val upstream: UpstreamConfig = UpstreamConfig(),
                        val downstream: DownstreamConfig = DownstreamConfig())

data class UpstreamConfig(val debugLogging: Boolean = false,
                          val idleTimeoutMillis: Int = 30000)

data class DownstreamConfig(val debugLogging: Boolean = false,
                            val idleTimeoutMillis: Int = 30000,
                            val connectionTimeoutMillis: Int = 60000,
                            val httpRequestTimeoutMillis: Int = 0)

