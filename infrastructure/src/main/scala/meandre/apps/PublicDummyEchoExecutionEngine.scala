package meandre.apps

import meandre.kernel.Configuration
import java.util.Date

/**
 * This class is used as a dummy execution engine that just returns whatever is
 * pushed to is STDIN to STDOUT with some verbose added and a few log entries sent
 * to the STDERR
 *
 * @author Xavier Llora
 * @date May 10, 2010 at 3:44:18 PM
 *
 */

object PublicDummyEchoExecutionEngine {

  val in      = System.in
  val console = System.out
  val log     = System.err

  val HEADER = "Meandre %s Dummy Echo Execution Engine\n".format(Configuration.INFRASTRUCTURE_VERSION) +
               "Copyright DITA, NCSA, UofI 2007-2012 (%s)\n".format(new Date())+
               "---------------------------------------------------------------------\n\nPROVIDED REPOSITORY\n\n"

  val FOOTER = "---------------------------------------------------------------------\n\nFINISHED AT %s\n"

  def main(args:Array[String]) = {
    //
    // Signal starting at the log
    //
    log println "[INFO] Started the echo execution process at %s".format(new Date())

    //
    // Print the header
    //
    console println HEADER

    //
    // Dump the repository
    //
    var c = in.read
    while ( c>=0 ) {
      console write c
      c = in.read
    }

    //
    // Print the footer
    //
    console println FOOTER.format(new Date())

    //
    // Signal end at the log
    //
    log println "[INFO] Finished the echo execution process at %s".format(new Date())

  }



}
