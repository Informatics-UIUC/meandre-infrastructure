package meandre.kernel.state

import meandre.kernel.Configuration
import meandre.kernel.Implicits._
import meandre.Tools.safeOp
import com.mongodb.gridfs.GridFS
import java.io.{OutputStream, InputStream}
import collection.jcl.{Buffer, IterableWrapper, CollectionWrapper}
import com.mongodb.{BasicDBObject, Mongo}
import javax.servlet.http.HttpServletResponse
import javax.servlet.http.HttpServletResponse._

/**
 * This class implements a simple abstraction to allow a simple
 * manipulation of context elements.
 *
 * @author Xavier Llora
 * @date Feb 15, 2010 at 11:23:14 PM
 * 
 */

class ContextsPool (val cnf:Configuration) {

  val mongo = new Mongo
  val    db = mongo getDB cnf.MEANDRE_DB_NAME
  val   gfs = new GridFS(db,cnf.MEANDRE_CONTEXT_FS_COLLECTION)
  val colFS = db getCollection cnf.MEANDRE_CONTEXT_FS_COLLECTION+".files"
  val colCK = db getCollection cnf.MEANDRE_CONTEXT_FS_COLLECTION+".chunks"

  //----------------------------------------------------------------------

  /**Returns the number of contexts available.
   *
   * @return The number of context stored
   */
  def size = safeOp { gfs.getFileList.length }

  /**Return the name of the contexts stored in the pool.
   *
   * @return The list of file names
   */
  def listOfFileNames = safeOp {
    var res:List[String] = Nil
    val files = gfs.getFileList
    while ( files.hasNext ) res ::= files.next.get("filename").toString
    res
  }

  /** Save a given content into the context with the given name and mime type.
   *
   * @param fileName The name of the context
   * @param mimeType The mime type of the context
   * @param is The input stream containing the content
   * @return The file name if succeed
   */
  def save(fileName: String, mimeType: String, is: InputStream) = safeOp {
    val nfn = if ( fileName startsWith "/" ) fileName else "/"+fileName
    val file = gfs.createFile(is)
    file.setContentType(mimeType)
    file.save
    file.setFilename("context://localhost/resources/%s%s" format (file.getMD5,nfn))
    file.save
    val (finalMD5, finalFileName) = (file.getMD5,file.getFilename)
    //
    // Clean left overs
    //
    val cnd:BasicDBObject    = ("""{"filename":"%s"}""" format finalFileName)
    val sortF:BasicDBObject  = """{"filename":1,"uploadDate":-1}"""
    val cur = colFS.find(cnd).sort(sortF).skip(1)
    while ( cur.hasNext ) {
      val fileDoc = cur.next
      val id = fileDoc.get("_id")
      val chunksCnd = new BasicDBObject
      chunksCnd.put("files_id",id)
      colCK.remove(chunksCnd)
      colFS.remove(fileDoc)
    }
    (finalMD5, finalFileName)
  }

  /** Remove the given context
   *
   * @param fileName The name of the context
   */
  def remove(fileName: String) = safeOp {
    gfs remove fileName
    fileName
  }

  /**Remove all the contexts in the pool
   *
   */
  def removeAll = safeOp {
    gfs.remove(new BasicDBObject)
    true
  }

  /** Updates the given content into the context with the given name and mime type.
    *
    * @param fileName The name of the context
    * @param mimeType The mime type of the context
    * @param is The input stream containing the content
    * @return The file name if succeed, None otherwise
    */
  def update(fileName:String,mimeType:String,is:InputStream) = remove(fileName) match {
    case  Right(_) => save(fileName,mimeType,is)
    case t@Left(_) => t
  }

  /** Writes a file if found to the provided output stream.
   *
   * @param fileName The file name to retrieve
   * @param os The output stream where to dump the data
   * @return The file name if it succeed
   */
  def write (fileName:String, os:OutputStream) = safeOp {
    val file = gfs findOne fileName
    if (file!=null) { file writeTo os ; fileName }
    else throw new Exception("File "+fileName+" not found")
  }

  /** Writes a file if found to the provided response object.
   *
   * @param fileName The file name to retrieve
   * @param response The response where to dump the data
   * @return The file name if it succeed
   */
  def write (fileName:String, response:HttpServletResponse) = safeOp {
    val file = gfs findOne fileName
    if (file!=null) {
      response.setStatus(SC_OK)
      response.setContentType(file.getContentType)
      response.setContentLength(file.getLength.asInstanceOf[Int])
      file writeTo response.getOutputStream  
      fileName
    }
    else throw new Exception("File "+fileName+" not found")
  }

  /**Return true if the file exists.
   *
   * @param fileName The file name to check
   * @return True if exist
   */
  def containsContext ( fileName:String ) = safeOp {
    !gfs.find(fileName).isEmpty 
  }
}

/**The companion object for the context pool.
  *
 */
object ContextsPool {

  /**Creates a new instance of the context pool.
   *
   * @param cnf The configuration for this context pool
   */
  def apply ( cnf:Configuration ) = new ContextsPool(cnf)
}
