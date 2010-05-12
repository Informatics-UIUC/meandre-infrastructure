import java.io.ByteArrayOutputStream
import java.util.Date
import meandre.kernel.Configuration

val in = System.in
val console = System.out
val log = System.err

val HEADER = "Meandre %s Dummy Echo Execution Engine\n".format(Configuration.INFRASTRUCTURE_VERSION) +
        "Copyright DITA, NCSA, UofI 2007-2010 (%s)\n".format(new Date()) +
        "\n---------------------------------------------------------------------\n\nPROVIDED REPOSITORY\n\n"

val FOOTER = "\n---------------------------------------------------------------------\n\nFINISHED AT %s\n"

//
// Signal starting at the log
//
log println "[INFO] Started the echo execution process at %s".format(new Date())
log.flush

//
// Print the header
//
console write HEADER.getBytes
console.flush

//
// Read the repository
//
val baos = new ByteArrayOutputStream(10000)
var c = in.read
while (c >= 0) {
  baos write c
  c = in.read
}

//
// Dump the repository
//
baos.toByteArray.foreach( b => console write b )
console.flush

//
// Print the footer
//
console write FOOTER.format(new Date()).getBytes
console.flush

//
// Signal end at the log
//
log println "[INFO] Finished the echo execution process at %s".format(new Date())
log.flush
