package meandre.kernel.state

import meandre.kernel.Implicits._
import meandre.kernel.Configuration
import java.io.{LineNumberReader, InputStreamReader, InputStream}
import com.mongodb.{BasicDBList, BasicDBObject, Mongo}
import PersistentTextBuffer._
import net.liftweb.json.JsonParser
import net.liftweb.json.JsonAST.{JString, JInt, JField, JObject}

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

  val mongo = cnf.mongo
  val db = mongo getDB cnf.MEANDRE_DB_NAME
  val collection = db getCollection cnf.MEANDRE_TEXT_BUFFER_PREFIX+"."+id

  val sortCnd:BasicDBObject = """{"%s":1}""" format KEY
  var tid = 0

  collection ensureIndex sortCnd



  /**Given a text entry, it stores it in the the temporary persistent buffer.
   *
   * @param text The text to append to the buffer
   * @return The object itself to facilitate chaining
   */
  def append(text:String):PersistentTextBuffer = {
    val txt = text replace("\"","\\\"")
    val obj:BasicDBObject=
      """
        {
          "%s" : %d,
          "%s" : %d,
          "%s" : "%s"
        }""" format (KEY, tid, TS, System.currentTimeMillis, TEXT, txt)
    collection.insert(obj)
    tid += 1
    this
  }


  /**Given an input stream, it stores it in the the temporary persistent buffer.
   *
   * @param is The input stream to store
   * @return The object itself to facilitate chaining
   */
  def append(is:InputStream):PersistentTextBuffer = {
    val ir = new LineNumberReader(new InputStreamReader(is),10000)
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
    val cur = collection.find.sort(sortCnd)
    while ( cur.hasNext )
      sb append(cur.next().get(TEXT)) append("\n")

    sb.toString
  }

  def toString(since:Int) = {
    val sb = new StringBuffer
    val cnd:BasicDBObject = """{"%s":{"$gte":%d}}""" format (KEY, since)
    val cur = collection.find(cnd).sort(sortCnd)
    while ( cur.hasNext )
      sb append(cur.next().get(TEXT)) append ("\n")

    sb.toString
  }

  def get(since:Int):BasicDBList = {
    val cnd:BasicDBObject = """{"%s":{"$gte":%d}}""" format (KEY, since)
    val cur = collection.find(cnd).sort(sortCnd)
    val res = new BasicDBList
    while ( cur.hasNext )
      res add cur.next()

    res
  }

  /**Destroys the temporary persistent buffer contents so far.
   *
   */
  def destroy = collection.drop


  /**Returns the number of appends in the collections. This is an approximate number
   * due to the aggregation of appends by timestamp.
   *
   * @return The appoximate number of appends done
   */
  def approximateNumberOfAppends = collection.find.count
}


/**
 *  The companion object for the persistent text buffer class acts as a
 * simple persistent text buffer which allow appending text and then
 * retrieving it in a single swift.
 *
 * @author Xavier Llora
 * @date May 11, 2010 at 12:23:10 PM
 *
 */

object PersistentTextBuffer {
  protected val KEY  = "_id"
  protected val TEXT = "t"
  protected val TS = "ts"

  def apply (cnf:Configuration,id:String) = new PersistentTextBuffer(cnf,id)

  /**
   * Parses a serialized version of the text dump (annotated with id and timestamp) and returns
   * only the entries after a particular point specified in the parameter 'since'
   *
   * @param str The serialized text dump
   * @param since The guard
   * @return The list of entries satisfying the condition
   */
  def parseAndGetSince(str:String,since:Int):BasicDBList = {
    val res = new BasicDBList
    val json = JsonParser.parse(str)
    for {
      JObject(te) <- json
      JField(KEY, JInt(id)) <- te
      JField(TS, JInt(ts)) <- te
      JField(TEXT, JString(t)) <- te
      if id >= since
    } {
      val obj = new BasicDBObject
      obj.put(KEY, id)
      obj.put(TS, ts)
      obj.put(TEXT, t)
      res add obj
    }
    res
  }

  def getText(str:String, since:Int):String = {
    val sb = new StringBuilder
    val data = parseAndGetSince(str, since)
    (0 until data.size()).foreach(i => sb.append(data.get(i).asInstanceOf[BasicDBObject] getString(TEXT)).append("\n"))
    sb.toString
  }
}
