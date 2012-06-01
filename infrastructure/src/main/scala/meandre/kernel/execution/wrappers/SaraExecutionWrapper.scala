package meandre.kernel.execution.wrappers

import java.io.{File, InputStream}
import meandre.kernel.Configuration
import com.mongodb.BasicDBObject
import meandre.kernel.execution.{JobQueue, ExecutionWrapper}
import snare.Snare

/**
 * @author Boris Capitanu
 */

class SaraExecutionWrapper(cnf: Configuration) extends ExecutionWrapper {
  override def fireWrapper(repo: Array[Byte]): (Process, InputStream, InputStream) = {
    fireProcess(
      List(cnf.EXECUTION_JAVA, cnf.EXECUTION_JAVA_ARGS, "-jar", cnf.EXECUTOR_14X, "--port", (cnf.serverPort + 1).toString),
      new File("."),
      repo
      )
  }

  /**
   * Return WebUI meta information about a job
   *
   * @param jobID The job ID
   * @param snare The snare instance to use for communication
   * @return The BasicDBObject describing the metadata
   */
  override def getJobExecutionMeta(jobID:String, snare:Snare): BasicDBObject = {
    val res = new BasicDBObject

    JobQueue(cnf).job(jobID) match {
      case None => res

      case Some(jobData) =>
        val serverID = jobData.getString("server")
        snare.fetchRegisteredPeersInformation match {
          case None => res

          case Some(peersInfo:List[BasicDBObject]) =>
            val serverPeer = peersInfo.filter(p => p.getString("_id").equals(serverID))
            val peer = if (serverPeer.length > 0) serverPeer(0) else null
            peer match {
              case null => res
              case p =>
                val meta = p.get("metadata").asInstanceOf[BasicDBObject]
                val server = meta.get("server").asInstanceOf[BasicDBObject]
                val proto = server.getString("protocol")
                val webuiHost = server.getString("host")
                val webuiPort = server.getInt("port") + 1
                val webuiUrl = "%s://%s:%d" format (proto, webuiHost, webuiPort)
                res.put("webui_host", webuiHost)
                res.put("webui_port", webuiPort)
                res.put("webui_url", webuiUrl)
                res
            }
        }
    }
  }
}
