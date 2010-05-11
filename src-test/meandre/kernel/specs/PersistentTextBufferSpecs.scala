package meandre.kernel.specs

import org.specs._
import meandre.kernel.state.PersistentTextBuffer
import meandre.kernel.Configuration
import java.io.ByteArrayInputStream

/**
 * A simple specs to get to test the persistent text buffer.
 *
 * @author Xavier Llora
 * @date May 11, 2010 at 1:26:02 PM
 * 
 */

object PersistentTextBufferSpecs extends Specification("The persisten text buffer specifications") {

  val cnf = Configuration()
  cnf.MEANDRE_DB_NAME = "Meandre_Test"

  "A persitent text buffer" should {

    "Store, retrieve, and destroy temporary text" in {
      val ptb = PersistentTextBuffer(cnf,"ptb")
      (0 to 9).foreach(i=>ptb.append("Line %d" format i))
      ptb.numberOfAppends mustBe equalTo(10)
      (0 to 9).toList.zip(ptb.toString.split("\n").toList).foreach(
        (l) => l._2 mustBe equalTo("Line %d" format l._1)
      )
      ptb.destroy
      ptb.numberOfAppends mustBe equalTo(0)
    }
  }


  "Store, retrieve, and destroy temporary text from an InputStream" in {
    val bais = new ByteArrayInputStream("Line 1\nLine 2\nLine 3\n".getBytes)
    val ptb = PersistentTextBuffer(cnf,"ptb")
    ptb append bais
    ptb.numberOfAppends mustBe equalTo(3)
    (1 to 3).toList.zip(ptb.toString.split("\n").toList).foreach(
      (l) => l._2 mustBe equalTo("Line %d" format l._1)
    )
    ptb.destroy
    ptb.numberOfAppends mustBe equalTo(0)
  }

}