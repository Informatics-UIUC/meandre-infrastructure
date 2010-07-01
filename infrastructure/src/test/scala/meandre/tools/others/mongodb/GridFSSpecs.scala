package meandre.tools.others.mongodb

import org.specs.Specification
import meandre.kernel.Configuration
import com.mongodb.gridfs.GridFS
import java.net.URL
import com.mongodb.{BasicDBObject, Mongo}

/**
 * This specs file just targets to get validate the behavior of MongoDB
 * GridFS facility
 *
 * @author Xavier Llora
 * @date Feb 15, 2010 at 9:31:20 PM
 * 
 */

object GridFSSpecs extends Specification {
  //
  // Initialize the connection to the MongoDB test database
  //
  val cnf = Configuration()
  cnf.MEANDRE_DB_NAME = "Meandre_Test"
  val mongo = new Mongo
  val    db = mongo getDB cnf.MEANDRE_DB_NAME
  val   gfs = new GridFS(db)
  val TEST_CONTENT = new URL("http://www.gutenberg.org/files/20417/20417.txt")
  
  "A GridFS store " should {

    "be able to crete files and delete files " in {
      // Count the number of files
      val numFiles = gfs.getFileList.length
      // Create a file
      val nf = gfs.createFile(TEST_CONTENT.openStream,"/test/1.zip")
      nf.setContentType("application/zip")
      nf.save
      gfs.getFileList.length must beEqualTo(numFiles+1)
      // Query for the file
      val files = gfs.find("/test/1.zip")
      files.size must beEqualTo(1)
      // Delete files
      gfs.remove("/test/1.zip")
      gfs.getFileList.length must beEqualTo(numFiles)
    }
    
  }
}