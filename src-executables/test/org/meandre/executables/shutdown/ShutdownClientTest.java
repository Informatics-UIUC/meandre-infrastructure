package org.meandre.executables.shutdown;

import java.io.File;
import java.io.IOException;

import junit.framework.TestCase;

/**
 * Test for Shutdownable, ShutdownClient, and ShutdownControlThread
 * 
 * @author pgroves
 *
 */
public class ShutdownClientTest extends TestCase {

    
    String _sInstallDir = "test/output/ShutdownClientTest/";
    String _appName = "MockShutdownableApp";
    int _port = 1611;
    
    File _installDir;
    
    protected void setUp() throws Exception {
        super.setUp();
        
        _sInstallDir = _sInstallDir.replace('/', File.separatorChar);
        _installDir = new File(_sInstallDir);
        _installDir.mkdirs();
    }

    public void testShutdown() throws Exception {
        
        MockShutdownable target = new MockShutdownable();
        ShutdownControlThread sct = new ShutdownControlThread(target, _port,
                _installDir, _appName);
        sct.start();
        
        File tokenFile = ShutdownControlThread.getDestinationTokenFile(
                _installDir, _appName);
        
        assertTrue("!token file exists while running", tokenFile.exists());
        ShutdownClient client = new ShutdownClient(
                _appName, _installDir, _port);
        
        assertFalse("not shutdown before call", target.hasBeenShutdown());
        client.shutdown();
        //give the other thread a few milliseconds to finish
        Thread.sleep(100L);
        assertTrue("is shutdown after call", target.hasBeenShutdown());
        
        //make sure the security token file is cleaned up

        assertFalse("token file exists after shutdown", tokenFile.exists());
    }

    
    /** simple shutdownable that has a state variable that is either 
     * shutdown or not-shutdown.
     */
    private class MockShutdownable implements Shutdownable{
        /**variable set to true when this shutdownable has been shutdown*/
        boolean __hasBeenShutdown = false;
        
        public MockShutdownable(){}
        
        public void stop(){
            __hasBeenShutdown = true;
        }
        
        public boolean hasBeenShutdown(){
            return __hasBeenShutdown;        
        }
        
    }
}
