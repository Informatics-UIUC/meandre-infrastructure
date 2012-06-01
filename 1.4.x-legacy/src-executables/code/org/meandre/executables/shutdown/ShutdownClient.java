package org.meandre.executables.shutdown;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.OutputStream;
import java.net.InetAddress;
import java.net.Socket;

/**
 * ShutdownClient interacts with an application running a ShutdownControlThread
 * and tells it to shutdown it's target application. It reads the security
 * token that the ShutdownControlThread writes to a file and transmits a 
 * 'stop' order to the ShutdownControlThread over a socket. Typically, this
 * client will run in a different jvm instance than the controlthread.
 * 
 * @author pgroves
 *
 */
public class ShutdownClient {

    /**the same appName used by the listening control thread. 
     * this must be exactly the same in order to find the file with the 
     * security token */
    String _targetAppName;
    
    /**the directory the control thread wrote it's
     * security token file into */
    File _tokenDestinationDir;
    
    /**the port the control thread is listening on */
    int _shutdownListenerPort;
    
    /**
     * create a shutdown client to communicate with a ShutdownControlThread.
     * 
     * @param appName the same appName used by the listening control thread. 
     * this must be exactly the same in order to find the file with the 
     * security token
     * @param tokenDirectory the directory the control thread wrote it's
     * security token file into
     * @param shutdownPort the port the control thread is listening on
     */
    public ShutdownClient(String appName, File tokenDirectory, 
            int shutdownPort) {
        _targetAppName = appName;
        _tokenDestinationDir = tokenDirectory;
        _shutdownListenerPort = shutdownPort;
    }

    /**
     * sends a message over a socket (of 'localhost') telling the listening
     * control thread to shutdown.
     */
    public void shutdown() throws IOException{
        String token;
        try {
            File tokenKeyFile = ShutdownControlThread.getDestinationTokenFile(
                    _tokenDestinationDir, _targetAppName);
            BufferedReader bufferedReader = new BufferedReader(
                    new FileReader(tokenKeyFile));
            token = bufferedReader.readLine().trim();
            bufferedReader.close();
        } catch (IOException e) {
            System.out.println("Security Token File not found! NOT issuing " + 
                    "shutdown command. Reason:");
            e.printStackTrace();
            throw(e);
        }

        try{
            Socket s = new Socket(InetAddress.getLocalHost(), 
                    _shutdownListenerPort);
            OutputStream out = s.getOutputStream();
            out.write((token + "\r\nstop\r\n").getBytes());
            out.flush();
            s.close();
        } catch (IOException e) {
            System.out.println("Security Token found, but could NOT " + 
                    "communicate with App to be shutdown");
            e.printStackTrace();
            throw(e);
        }
    }
    
    

}
