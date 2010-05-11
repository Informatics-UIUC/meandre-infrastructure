package meandre.kernel.state

import meandre.kernel.Implicits._
import meandre.kernel.Configuration
import com.mongodb.{BasicDBObject, Mongo}
import java.io.{LineNumberReader, InputStreamReader, InputStream}

/**
 * The persistent text buffer class acts as a simple persistent text
 * buffer which allow appending text and then retrieving it in a single
 * swift.
 *
 * @author Xavier Llora
 * @date May 11, 2010 at 12:06:25 PM
 * 
 */

class PersistentTextBuffer(cnf:Configuration,id:String) {

  protected val KEY = "t"

  val mongo = new Mongo
  val db = mongo getDB cnf.MEANDRE_DB_NAME
  val collection = db getCollection cnf.MEANDRE_TEXT_BUFFER_PREFIX+"."+id

  val sortCnd:BasicDBObject = """{"_id":1}"""


  /**Given a text entry, it stores it in the the temporary persistent buffer.
   *
   * @param text The text to append to the buffer
   * @return The object itself to facilitate concatenations
   */
  def append(text:String):PersistentTextBuffer = {
    val obj = new BasicDBObject
    obj.put(KEY,text)
    collection.save(obj)
    this
  }


  /**Given an input stream, it stores it in the the temporary persistent buffer.
   *
   * @param text The text to append to the buffer
   * @return The object itself to facilitate concatenations
   */
  def append(is:InputStream):PersistentTextBuffer = {
    val ir = new LineNumberReader(new InputStreamReader(is))
    var line = ir.readLine
    while ( line!=null ) {
      append(line)
      line = ir.readLine
    }
    this
  }

  /**Returns the current text stored in the temporary persistent buffer.
   *
   * @return The buffered text
   */
  override def toString = {
    val sb = new StringBuffer
    var cur = collection.find.sort(sortCnd)
    while ( cur.hasNext ) sb append cur.next.get(KEY).toString+"\n"
    sb.toString
  }

  /**Destroys the temporary persistent buffer contents so far.
   *
   */
  def destroy = collection.drop


  /**Returns the number of appends in the collections.
   *
   * @return The number of appends done
   */
  def numberOfAppends = collection.find.count
}


/**
 *  The companion objec for the persistent text buffer class acts as a
 * simple persistent text buffer which allow appending text and then
 * retrieving it in a single swift.
 *
 * @author Xavier Llora
 * @date May 11, 2010 at 12:23:10 PM
 *
 */

object PersistentTextBuffer {

  def apply (cnf:Configuration,id:String) = new PersistentTextBuffer(cnf,id)

}