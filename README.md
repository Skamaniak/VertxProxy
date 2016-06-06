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
- Adding custom headers to requests and responses
 
##Possible features (nice to have)
- Traffic analysis and serialization (image, video, files recognition and serialization to disk)
- SSL traffic decoding

##Documentation
TBD

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
