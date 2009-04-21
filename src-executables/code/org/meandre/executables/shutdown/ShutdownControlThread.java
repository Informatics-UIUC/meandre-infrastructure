package org.meandre.executables.shutdown;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.LineNumberReader;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Date;
import java.util.Random;

/**
 * A thread that listens on a socket for a shutdown command, and calls
 * 'stop()' on some Shutdownable object. The shutdown command received must
 * contain a random string that this control thread writes to a 
 * security key file when it starts up. This is to ensure only a local process with
 * privileges to read the installation files of this app can shutdown this app.
 *
 * <p> To invoke the shutdown process, a message must be sent over a
 * socket where the first line is the string found in the 
 * security token file, and the second line is the string 'stop'. 
 *
 * <p>The 'stop' keyword can also be replaced by 'status', in which case
 * the string 'ok' will be written to the socket as a response. Will only
 * respond 'ok' if the ShutdownControlThread is running and the proper 
 * security key is used.
 *
 * <p>When the 'stop' command is received, it performs the shutdown and 
 * also deletes the security token file. An instance of ShutdownControlThread
 * cannot be reused after it receives a valid 'stop' command.
 *
 *
 * @author Amit Kumar
 * @author Peter Groves 
 */

public class ShutdownControlThread extends Thread{

    /**
     * the base filename to use as the destination for the security key. 
     * a file with this suffix will be created in whatever directory this 
     * control thread is given in the constructor. */
    public static String SECURITY_FILE_BASENAME = "shutdown-security-key.txt";

    /** the object that will be 'stopped' when the shutdown command is
     * received.
     */
    private Shutdownable _target;

    /**
     * the random string that we must receive along with a command to
     * verify the caller has access to our files (and therefore has
     * permission to shutdown the target.
     */
    private String _securityToken;

    /** the file that this instance of ShutdownControlThread wrote it's security 
     * token in. */
    private File _tokenFile;

    /** the socket we will listen for 'shutdown' commands on*/
    private ServerSocket _socket;
    
    /**the instance variable that the thread checks to see if it should keep
     * running.*/
    private boolean _keepRunning;

    /**
     * creates a new thread (that isn't yet running). the security token for
     * this ShutdownControlThread can be found in the file returned by
     * getDestinationTokenFile() called with the same parameters as this
     * constructor.
     *
     * @param target the Shutdownable object that will be stopped when
     * the key is received over the socket.
     *
     * @param securityTokenDir the directory that the file containing the 
     * security key will be written to.
     *
     * @param targetName an identifier for the object being shutdown. this
     * will be the prefix of the filename that the security token is written to.
     * @throws IOException 
     */
    public ShutdownControlThread(Shutdownable target, int listeningPort,
            File securityTokenDir, String targetName) throws IOException{
        this.setDaemon(true);
        this.setName(targetName + "-ShutdownMonitor");
        
        _target = target;
        _securityToken = generateSecurityToken();
        _tokenFile = getDestinationTokenFile(securityTokenDir, targetName);

        this.writeSecurityTokenFile();

        InetAddress localhost = InetAddress.getLocalHost();
        _socket = new ServerSocket(listeningPort, 1, localhost);
        _keepRunning = true;
    }

    /** creates a random string to identify our process*/
    private String generateSecurityToken() {
        Random rand = new Random(new Date().getTime());
        double x1 = rand.nextDouble() * 100000;
        double x2 = rand.nextDouble() * 100000;
        String tok = Double.toString(x1 * x2);
        return tok;
    }

    /**
     * constructs the same file location that a ShutdownControlThread instance
     * will write the security token to if constructed with the same
     * securityTokenDir and targetName.
     *
     * @param securityTokenDir the directory that the file containing the 
     * security key will be written to.
     *
     * @param targetName an identifier for the object being shutdown. this
     * will be the prefix of the filename that the security token is written to.
     */
    public static File getDestinationTokenFile(File securityTokenDir, 
            String targetName){

        String baseName = targetName + "-" + SECURITY_FILE_BASENAME;
        File fl = new File(securityTokenDir, baseName);
        return fl;     
   }

   /**
    * writes this instance's security token to it's tokenFile
 * @throws IOException 
    */
   private void writeSecurityTokenFile() throws IOException{
        BufferedWriter bufferedWriter = new BufferedWriter(
                new FileWriter(_tokenFile));
        bufferedWriter.write(_securityToken);
        bufferedWriter.flush();
        bufferedWriter.close();
   }

    /** deletes the file written by writeSecurityTokenFile()
     */
    private void deleteSecurityTokenFile(){
        _tokenFile.delete();
    }

    /**
     * listens for messages on the socket and shutsdown the target (and itself) 
     * if a valid shutdown command is received.
     */
    public void run(){

        while (_keepRunning) {
            Socket socket = null;
            try {
                socket = _socket.accept();
                LineNumberReader lin = new LineNumberReader(
                new InputStreamReader(socket.getInputStream()));
                String key = lin.readLine();
                if (!_securityToken.equals(key))
                    continue;

                String cmd = lin.readLine();
				
                if ("stop".equals(cmd)) {
                    this.shutdown();
                    System.exit(0);
                } else if ("status".equals(cmd)) {

                    socket.getOutputStream().write("OK\r\n".getBytes());
                    socket.getOutputStream().flush();
                }
            } catch (Exception e) {
                System.err.println(e.toString());
            } finally {
                if (socket != null) {
                    try {
                        socket.close();
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            socket = null;
            }
        }
    }

    /** instructs the thread to stop running on it's next iteration, if
     * it hasn't stopped already. once stopped, the thread cannot be
     * restarted.
     * @throws IOException 
     */
    public void shutdown() throws IOException{
        _keepRunning = false;
        this.deleteSecurityTokenFile();
        _target.stop();
        System.out.println("ShutdownControlThread: target stopped.");
    }

}
