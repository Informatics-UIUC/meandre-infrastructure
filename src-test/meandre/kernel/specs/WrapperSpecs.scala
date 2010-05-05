package meandre.kernel.specs

import org.specs.Specification
import meandre.kernel.execution.wrappers.ArbitraryCodeExecutionWrapper

/**
 * Implements a collection of specifications to test the functionality
 * provided by the execution wrapper
 *
 * @author Xavier Llora
 * @date Feb 15, 2010 at 3:44:35 PM
 * 
 */

object WrapperSpecs extends Specification("The context pool specification") {

  "A dummy wrapper " should {

    "be able to run arbitrary commands" in {
      val os = System.getProperty("os.name").toLowerCase
      val cmd = if (os contains "windows" ) {
        List("dir")
      }
      else {
        List("ls")
      }
      val (p,con,err) = new ArbitraryCodeExecutionWrapper(cmd).fireWrapper("")
      con.available must beGreaterThan(0)
      //println(con.available)
      err.available must beEqualTo(0)
    }

  }

}