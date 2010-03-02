package meandre.webservices.api

import org.mortbay.jetty.Server
import org.mortbay.thread.BoundedThreadPool
import java.io.File
import meandre.kernel.Configuration
import org.mortbay.jetty.servlet.{ServletHolder, DefaultServlet, Context}
import meandre.webservices.realm.MongoDBRealm
import org.mortbay.jetty.security.{HashUserRealm, SecurityHandler, ConstraintMapping, Constraint}

/**
 * The Meandre server class
 *
 * @author Xavier Llora
 * @date Mar 2, 2010 at 10:52:40 AM
 *
 */
class MeandreServer(val cnf:Configuration, val port: Int, val prefix: String, val staticFolder:String, val resourceFolder:String, val idle: Int, val maxThreads: Int, val minThreads: Int) {
  //
  // The main server
  //
  protected val server = new Server(port)

  //
  // The bounded thread pool
  //
  protected val tp = new BoundedThreadPool
  tp.setMinThreads(minThreads)
  tp.setMaxThreads(maxThreads)
  tp.setMaxIdleTimeMs(idle)
  server.setThreadPool(tp)

  //
  // Static folder serving servlet
  //
  protected val staticContext = new Context(server,"/static",Context.NO_SESSIONS)
  protected val staticFileServlet = new DefaultServlet
  staticContext setResourceBase staticFolder
  staticContext.addServlet(new ServletHolder(staticFileServlet), "/*")

  //
  // Public resources serveing servlet
  //
  protected val resourceContext = new Context(server,"/public/resources",Context.NO_SESSIONS)
  protected val resourceFileServlet = new DefaultServlet
  resourceContext setResourceBase resourceFolder
  resourceContext.addServlet(new ServletHolder(resourceFileServlet), "/*")

  //
  // Install the WebUI proxy
  //
  // TODO Needs to be migrated from the old 1.4 server

  //
  // The basic context for authenticated services
  //
  protected val contextWS  = new Context(server,"/",Context.SESSIONS)
  protected val constraint = new Constraint()
  constraint.setName(Constraint.__BASIC_AUTH)
  constraint.setRoles(MongoDBRealm.AVAILABLE_ROLES)
  constraint.setAuthenticate(true)

  protected val cm = new ConstraintMapping();
  cm.setConstraint(constraint);
  cm.setPathSpec(prefix+"/services/*");
  protected val sJettyHome = System.getProperty("jetty.home") match {
    case null => new File(".").getCanonicalFile
    case s    => s
  }
  protected val sh = new SecurityHandler
  sh.setUserRealm(new MongoDBRealm(cnf,"Meandre Flow Execution Engine"))
  sh.setConstraintMappings(Array(cm))

  //
  // Set the security handler
  //
  contextWS.addHandler(sh);

  //
  // Add the public services
  //
  contextWS.addServlet(new ServletHolder(new MeandreInfrastructurePublicAPI(cnf)), prefix+"/public/services/*")

  //
  // Add the services that require registration
  //
  contextWS.addServlet(new ServletHolder(new MeandreInfrastructurePrivateAPI(cnf)), prefix+"/services/*")

  //
  // Add the basic welcome page
  //
  contextWS.addServlet(new ServletHolder(new MeandreInfrastructureRootAPI(cnf)), prefix+"/*")

  //
  // Add the basic default error handler
  //
  contextWS.setErrorHandler(new MeandreDefaultErrorHandler)

  //
  // TODO Add the Snare registration and activation code here
  //

  //
  // Basic methods to manage the server
  //
  def start = server.start
  def  stop = server.stop
  def  join = server.join
  def    go = { server.start; server.join }
}

/**
 * The Meandre server companion object
 *
 * @author Xavier Llora
 * @date Mar 2, 2010 at 10:52:40 AM
 *
 */
object MeandreServer {

  /**Maximum jetty thread idle time */
  val MAXIMUM_JETTY_THREAD_IDLE_TIME = 300000;

  /**Maximum number of jetty theads */
  val MAXIMUM_NUMBER_OF_JETTY_THREADS = 256;

  /**Minimum number of jetty threads */
  val MINIMUM_NUMBER_OF_JETTY_THREADS = 6;

  /** The default static folder */
  val STATIC_FOLDER = new File(".").getCanonicalFile+File.separator+"static"

  /** The default public folder */
  val PUBLIC_RESOURCE_FOLDER = new File(".").getCanonicalFile+File.separator+"public"

  /** A default configuration */
  val DEFAULT_CONFIGURATION = Configuration()

  /** The default service configuration */
  val DEFAULT_PREFIX = ""


  def apply() = new MeandreServer(DEFAULT_CONFIGURATION, 1714, DEFAULT_PREFIX, STATIC_FOLDER, PUBLIC_RESOURCE_FOLDER, MAXIMUM_JETTY_THREAD_IDLE_TIME, MAXIMUM_NUMBER_OF_JETTY_THREADS, MINIMUM_NUMBER_OF_JETTY_THREADS)

  def apply(cnf:Configuration) = new MeandreServer(cnf, 1714, DEFAULT_PREFIX, STATIC_FOLDER, PUBLIC_RESOURCE_FOLDER, MAXIMUM_JETTY_THREAD_IDLE_TIME, MAXIMUM_NUMBER_OF_JETTY_THREADS, MINIMUM_NUMBER_OF_JETTY_THREADS)

  def apply(cnf:Configuration, port: Int) = new MeandreServer(cnf, port, DEFAULT_PREFIX, STATIC_FOLDER, PUBLIC_RESOURCE_FOLDER, MAXIMUM_JETTY_THREAD_IDLE_TIME, MAXIMUM_NUMBER_OF_JETTY_THREADS, MINIMUM_NUMBER_OF_JETTY_THREADS)

  def apply(cnf:Configuration, port: Int, prefix: String) = new MeandreServer(cnf, port, prefix, STATIC_FOLDER, PUBLIC_RESOURCE_FOLDER, MAXIMUM_JETTY_THREAD_IDLE_TIME, MAXIMUM_NUMBER_OF_JETTY_THREADS, MINIMUM_NUMBER_OF_JETTY_THREADS)

  def apply(cnf:Configuration, port: Int, prefix: String, staticFolder:String, resourceFolder:String) = new MeandreServer(cnf, port, prefix, staticFolder, resourceFolder, MAXIMUM_JETTY_THREAD_IDLE_TIME, MAXIMUM_NUMBER_OF_JETTY_THREADS, MINIMUM_NUMBER_OF_JETTY_THREADS)

  def apply(cnf:Configuration, port: Int, prefix: String, staticFolder:String, resourceFolder:String, idle: Int, maxThreads: Int, minThreads: Int) = new MeandreServer(cnf, port, prefix, staticFolder, resourceFolder, idle, maxThreads, minThreads)
}