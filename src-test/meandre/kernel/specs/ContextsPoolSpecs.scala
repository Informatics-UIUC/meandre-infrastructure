package meandre.kernel.specs

import org.specs.Specification
import meandre.kernel.Configuration
import meandre.kernel.state.ContextsPool
import java.net.URL
import java.io.ByteArrayOutputStream

/**
 * Implements a collection of specifications to test the functionality
 * provided by the Store class and companion object
 *
 * @author Xavier Llora
 * @date Feb 15, 2010 at 3:44:35 PM
 * 
 */

object ContextsPoolSpecs extends Specification {
  val cnf = Configuration()
  cnf.MEANDRE_DB_NAME = "Meandre_Test"
  val cp = new ContextsPool(cnf)
  val TEST_CONTENT = new URL("http://www.gutenberg.org/files/20417/20417.txt")

  "A context pool " should {

    "Be able to save, retrieve, and delete contexts" in {

      // Count the number of files
      var tmp = cp.size
      tmp.isRight must beTrue
      val numFiles:Int = cp.size.right.get
      // Create a file
      val nf = cp.save("/test/1.zip","application/zip",TEST_CONTENT.openStream)
      nf.isRight must beTrue
      tmp = cp.size
      tmp.isRight must beTrue
      tmp.right.get must beEqualTo(numFiles+1)
      // Query for the file
      val baos = new ByteArrayOutputStream
      val file = cp.write("/test/1.zip",baos)
      file.isRight must beTrue
      baos.size must beGreaterThan(0)
      // Delete files
      cp.remove("/test/1.zip").isRight must beTrue

    }
    
  }

}