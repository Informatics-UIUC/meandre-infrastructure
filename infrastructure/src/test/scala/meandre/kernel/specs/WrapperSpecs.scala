package meandre.kernel.specs

import org.specs.Specification
import meandre.kernel.execution.wrappers.ArbitraryCodeExecutionWrapper
import io.Source
import java.net.URL
import java.io.File

/**
 * Implements a collection of specifications to test the functionality
 * provided by the execution wrapper
 *
 * @author Xavier Llora
 * @date Feb 15, 2010 at 3:44:35 PM
 * 
 */


object WrapperSpecs extends Specification("The context pool specification") {
  //
  // Added definitions for OS test, command strings, and a inputStream printer (BAcs)
  //
  val beVerbose:Boolean = false
  val howLongToWait = if(beVerbose) 1000 else 2000
  val isWindows:Boolean = { if(System.getProperty("os.name").toLowerCase.indexOf("windows") == -1 )  false else true }
  val outWinCommand = { "cmd /C dir".split(" ").toList : List[String] }
  val errWinCommand = { "java -version".split(" ").toList: List[String] }
  val outLinCommand = { "ls".split(" ").toList : List[String] }
  val errLinCommand = { "java -version".split(" ").toList:List[String] }
  //
  // getStringFrom( Nearly-Anything )  :)
  //
  def getStringFrom( source:Object ) = (
    source  match {
        case source: URL =>
          Source.fromURL(source).getLines.mkString
        case source: File =>
          Source.fromFile(source).getLines.mkString
        case source: java.io.InputStream =>
          Source.fromInputStream(source).getLines.mkString
        case source: Array[Byte] =>
          Source.fromBytes(source).getLines.mkString
        case source: Array[Char] =>
          Source.fromChars(source).getLines.mkString
        case _ =>
          Source.fromString(source.toString).getLines.mkString
    }
  )

  "A dummy wrapper " should {

    "be able to run arbitrary commands::testing standard output" in {
      val cmd = if (isWindows ) outWinCommand else outLinCommand
      val (p,con,err) = new ArbitraryCodeExecutionWrapper(cmd).fireWrapper("".getBytes)
      //
      // Need to make sure the our process
      Thread.sleep(howLongToWait)

      con.available must beGreaterThan(0)
      // Show what we got on SDTOUT  ...
        if(beVerbose)println( "con.available == "+con.available +"\n"+ getStringFrom(con) )

      err.available must beEqualTo(0)
      //Show what we got on SDTERR
        if(beVerbose)println( "err.available == "+err.available +"\n"+ getStringFrom(err) )

    }
    "be able to run arbitrary commands::testing standard error" in {
      val cmd = if (isWindows) errWinCommand else errLinCommand
      val (p,con,err) = new ArbitraryCodeExecutionWrapper(cmd).fireWrapper("".getBytes)

      Thread.sleep(howLongToWait)

      con.available must beEqualTo(0)
      // Print what we got on SDTOUT  ...
      if(beVerbose)   println( "con.available == "+con.available +"\n"+ getStringFrom(con) )

      err.available must beGreaterThan(0)
      //Print what we got on SDTERR
      if(beVerbose)  println( "err.available == "+err.available +"\n"+ getStringFrom(err) )

    //      Just playing with getFromString()
    //
    //      println("getStringFrom using InputStream \n" + getStingFrom(err) )
    //
    //      println("getStringFrom using URL: \n" +
    //              getStingFrom( new URL("http://repository.seasr.org/Fedora/ZoteroUploader/service.config"))
    //      )
    //      println("getStringFrom using File: \n" +
    //        getStingFrom( new File(System.getProperty("user.dir")+"\\"+"dir.cmd"))
    //      )
    //
    }

  }
}

