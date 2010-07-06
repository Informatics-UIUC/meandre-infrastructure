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
      //println("----------"+os)
      val cmd = if (os.indexOf("windows")<0 ) List("ls") else List("dir")
      //println(cmd)
      val (p,con,err) = new ArbitraryCodeExecutionWrapper(cmd).fireWrapper("".getBytes)
      con.available must beGreaterThan(0)
      //println(con.available)
      err.available must beEqualTo(0)
    }

  }

}