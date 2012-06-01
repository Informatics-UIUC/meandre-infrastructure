//
// A basic script to launch a server
//

import meandre.kernel.Configuration
import meandre.webservices.api.{MeandreServer}
import scala.concurrent.ops.spawn

val SERVER_PORT = Integer parseInt args(0)
val LOCAL_HOST_NAME = java.net.InetAddress.getLocalHost.getCanonicalHostName

val (cnf1, cnf2, cnf3) = (
        Configuration("http", LOCAL_HOST_NAME, SERVER_PORT, LOCAL_HOST_NAME, 27017, None),
        Configuration("http", LOCAL_HOST_NAME, SERVER_PORT + 1000, LOCAL_HOST_NAME, 27017, None),
        Configuration("http", LOCAL_HOST_NAME, SERVER_PORT + 2000, LOCAL_HOST_NAME, 27017, None)
    )

cnf1.EXECUTION_SCALA = "scala"
cnf1.EXECUTION_SCRIPT = "scripts/execution_%s.scala"
cnf1.EXECUTOR_14X = "lib/executor-1.4.11.jar"
cnf1.EXECUTION_JAVA_ARGS = "-Xmx1024m"
cnf1.EXECUTION_CLASSPATH = "infrastructure/target/scala_2.7.7/infrastructure_2.7.7-2.0.jar"

cnf2.EXECUTION_SCALA = cnf1.EXECUTION_SCALA
cnf2.EXECUTION_SCRIPT = cnf1.EXECUTION_SCRIPT
cnf2.EXECUTOR_14X = cnf1.EXECUTOR_14X
cnf2.EXECUTION_JAVA_ARGS = cnf1.EXECUTION_JAVA_ARGS
cnf2.EXECUTION_CLASSPATH = cnf1.EXECUTION_CLASSPATH

cnf3.EXECUTION_SCALA = cnf1.EXECUTION_SCALA
cnf3.EXECUTION_SCRIPT = cnf1.EXECUTION_SCRIPT
cnf3.EXECUTOR_14X = cnf1.EXECUTOR_14X
cnf3.EXECUTION_JAVA_ARGS = cnf1.EXECUTION_JAVA_ARGS
cnf3.EXECUTION_CLASSPATH = cnf1.EXECUTION_CLASSPATH


val server  = MeandreServer(cnf1, "/", "infrastructure/src/main/resources/styling/", "docs")
//val server2 = MeandreServer(cnf2, "/", "infrastructure/src/main/resources/styling/", "docs")
//val server3 = MeandreServer(cnf3, "/", "infrastructure/src/main/resources/styling/", "docs")

//spawn { server2.start  }
//spawn { server3.start  }
//server.start

server.go

//server.join
//server2.join
//server3.join
