# VertxProxy
Simple HTTP and SSL Vertx Proxy

##Project goals
I want to develop thin, light-weight proxy that is able to proxy SSL and HTTP traffic using [Vert.x](http://vertx.io/).

##Basic features
- Proxying HTTP requests and responses
- Creating tunnels on behalf of connect requests from clients
- Supporting more proxies in a row (proxy chaining)

##Planned features / components 
- Tests

##Possible features (nice to have)
- Traffic analysis and serialization (image, video, files recognition and serialization to disk)
- SSL traffic decoding

##Configuration
```
{
  "network": {
    "port": 8080,
    "host": "0.0.0.0"
  },
  "nextTunnelProxy": {
    "port": 7070,
    "host": "0.0.0.0"
  },
  "nextHttpProxy": {
    "port": 7070,
    "host": "0.0.0.0"
  },
  "idGenerator": "SEQUENCE",
  "customHeaders": {
    "appendToRequest": {
      "Via": "VertxProxy"
    },
    "appendToResponse": {
      "Via": "VertxProxy"
    },
    "removeFromRequest": [
      "X-Forwarded-For"
    ],
    "removeFromResponse": [
      "E-Tag",
      "Cache-Control"
    ],
    "addTransferIdHeader": true,
    "addForwardedForHeaders": true,
    "addForwardedByHeaders": true
  }
}
```
There is a full explanation of each configuration option

|Json key|Default value|Description|
|---|---|---|
|network.port|8080|Port which should be proxied.|
|network.host|0.0.0.0|Host which the proxy should be bound to.|
|nextTunnelProxy|null|Next tunnel (e. g. SSL) proxy to which the traffic should be proxied.|
|nextHttpProxy|null|Next HTTP proxy, similar to nextTunnelProxy but for HTTP requests.|
|idGenerator|RANDOM|Generator which should be used for request ID creation. Possible values are **UUID** (use UUIDs), **RANDOM** (use random strings) of **SEQUENCE** (use sequence starting from 1 and incrementing by 1 for each ID).|
|customHeaders.appendToRequest|empty map|Custom headers that should be appended to each request.|
|customHeaders.appendToResponse|empty map|Similar to customHeaders.appendToRequest but for responses.|
|customHeaders.removeFromRequest|empty list|List of headers that should be stripped off of each request that gets through the proxy.|
|customHeaders.removeFromResponse|empty list|Similar to customHeaders.removeFromRequest but for responses.|
|addTransferIdHeader|false|Appends header **X-Transfer-Id** to each request and response. Header contains proxy generated identifier of a request or tunnel. The same ID is used in log messages.|
|addForwardedForHeaders|false|Appends header **X-Forwarded-For-Ip** and **X-Forwarded-For-Port** to each request and response. Headers contain information about the sender.|
|addForwardedByHeaders|false|Appends header **X-Forwarded-By-Ip** and **X-Forwarded-By-Port** to each request and response. Headers contain information about proxy.|

##Run in IDE
#####Intellij Idea
- Open Run/Debug Configuration window
- Add new Application run configuration
- Set Main class to "io.vertx.core.Launcher"
- Set Program arguments to "run cz.jskrabal.proxy.Proxy -conf src/main/resources/conf/default-proxy.json"
- Set Working directory to the root directory of a project
- Set JRE to Java 8
![Run configuration for IntelliJ Idea](/documentation/readme/RunConfigExampleIdea.png?raw=true"configuration example")

##Author
Jan Škrabal <skrabalja(et)gmail.com>
