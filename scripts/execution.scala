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

//
// Print the header
//
console println HEADER

//
// Dump the repository
//
var c = in.read
while (c >= 0) {
  console write c
  c = in.read
}

//
// Print the footer
//
console println FOOTER.format(new Date())

//
// Signal end at the log
//
log println "[INFO] Finished the echo execution process at %s".format(new Date())
