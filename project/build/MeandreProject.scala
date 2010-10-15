import sbt._
import Process._

class MeandreProject(info: ProjectInfo) extends ParentProject(info)
{
	//
	// Subprojects definitions
	//
	lazy val infrastructure = project("infrastructure", "Infrastructure", new InfrastructureProject(_))

	//
	// Infrastructure project information
	//
	class InfrastructureProject(info: ProjectInfo) extends DefaultProject(info) {
		//
		// Infrastructure build and testing dependencies
		// 
	
		// Crochet
		lazy val crochet = "crochet" % "crochet" % "0.1.5" from "http://github.com/downloads/xllora/Crochet/crochet-0.1.5.jar"
		lazy val crochetAux = "crochet" % "crochet-3rdparty" % "0.1.X" from "http://github.com/downloads/xllora/Crochet/crochet-3dparty-libraries-0.1.X.jar"
	
		// Snare
		lazy val snare = "snare" % "snare" % "0.4.1" from "http://github.com/downloads/xllora/Snare/snare-0.4.1.jar"
	
		// Mongo
		lazy val mongo = "mongo" % "mongo" % "2.2" from "http://github.com/downloads/mongodb/mongo-java-driver/mongo-2.2.jar"
	
		// Commons
		lazy val comUpload = "commons-fileupload" % "commons-fileupload" % "1.2"
		lazy val comIO = "commons-io" % "commons-io" % "1.3.2"

		// Jena	
		lazy val jena = "com.hp.hpl.jena" % "jena" % "2.6.2"
	
		// Joda Time
		lazy val joda = "joda-time" % "joda-time" % "1.6"
	
		// Testing facilities
		lazy val scalatest = "org.scalatest" % "scalatest" % "1.0"
		lazy val specs = "specs" % "specs" % "1.6.1" from "http://specs.googlecode.com/files/specs-1.6.1.jar"
		lazy val scalacheck = "org.scala-tools.testing" % "scalacheck_2.7.7" % "1.6"

    override lazy val mainClass = Some("meandre.apps.DefaultServerApp")
    // The run task
//    lazy val runServer = task {
//      //log.info("Preparing to run the server...")
//      List("sh","scala","scripts/server.scala")!;
//      None
//    } dependsOn(compile) describedAs("Runs one server")
	}
}


