package org.meandre.client;

import static org.junit.Assert.fail;

import java.io.File;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.meandre.core.security.SecurityManager;
import org.meandre.core.store.security.SecurityManagerProctor;
import org.meandre.core.utils.NetworkTools;
import org.meandre.webservices.MeandreServer;
import org.meandre.webservices.logger.WSLoggerFactory;


/**
 * Tests cases for MeandreAdminClient. Starts up a MeandreServer and creates a
 * client that talks to it. Then calls all the remote calls the client
 * can make, one per test case. Delegates the actual test procedures
 * to SecurityManagerProctor, which contains all the test cases necessary
 * for an instance of the SecurityManager interface (which is all
 * MeandreAdminClient is).
 *
 * @author pgroves
 */
public class MeandreAdminClientTest {
    
    
    private static String _serverUrl = "localhost";
    private static int _serverPort = 1704;
    
    //made crossplatform in setupBeforeClass
    private static String _workingDir = "./test/output/MeandreAdminClientTest";


    
    private static MeandreServer _server = null;
    private static SecurityManager _secManager;
    
    @BeforeClass
    public static void setUpBeforeClass() throws Exception {
        log("setupBeforeClass begin");
        _serverUrl = NetworkTools.getLocalHostName();
        
        _workingDir = _workingDir.replace('/', File.separatorChar);
        
        File fWorkDir = new File(_workingDir);
        if(!fWorkDir.exists()){
            fWorkDir.mkdirs();
        }

        _server = new MeandreServer(_serverPort, _workingDir);

        _secManager = makeAdminClient();
        
        _server.start(false);
        log("setupBeforeClass end");
    }

    /** creates and returns a new meandreAdminClient/SecurityManager
     * for the admin user of the server we're testing against.
     */
    private static MeandreAdminClient makeAdminClient(){
        MeandreAdminClient client = new MeandreAdminClient(_serverUrl, 
                _serverPort);
        client.setCredentials("admin", "admin");
        return client;
    }

    @AfterClass
    public static void tearDownAfterClass() throws Exception {
        log("tearDownAfterClass begin");
         _secManager = null;
        _server.stop();
        MeandreServer.uninstall(new File(_workingDir));
        _server = null;
        log("tearDownAfterClass end");        
    }

    @Before
    public void setUp() throws Exception {
        log("setup begin");
        SecurityManagerProctor.removeAllButAdmin(_secManager);
        log("setup end");
    }

    @After
    public void tearDown() throws Exception {
        log("tearDown begin");
        SecurityManagerProctor.removeAllButAdmin(_secManager);
        log("tearDown end");
    }

    
    @Test
    public void testInitialState(){
        SecurityManagerProctor.testInitialState(_secManager);
    }
    
    @Test
    public void testCreateUser() {
        SecurityManagerProctor.testCreateUser(_secManager);
    }

    @Test
    public void testRemoveUser() {
        SecurityManagerProctor.testRemoveUser(_secManager);
    }

    @Test
    public void testGetUsers() {
        SecurityManagerProctor.testGetUsers(_secManager);
    }

    @Test
    public void testGetUser() {
        SecurityManagerProctor.testGetUser(_secManager);
    }

    @Test
    public void testHasRoleGranted() {
        SecurityManagerProctor.testHasGrantedRole(_secManager);
    }

    @Test
    public void testGetRolesOfUser() {
        SecurityManagerProctor.testGetRolesOfUser(_secManager);
    }

    @Test
    public void testGrantRole() {
        SecurityManagerProctor.testGrantRole(_secManager);
    }

    @Test
    public void testRevokeRole() {
        SecurityManagerProctor.testRevokeRole(_secManager);
    }

    @Test
    public void testGrantRoles() {
        SecurityManagerProctor.testGrantRoles(_secManager);
    }

    @Test
    public void testRevokeAllRoles() {
        SecurityManagerProctor.testRevokeAllRoles(_secManager);
    }

    @Test
    public void testRevokeRoles() {
        SecurityManagerProctor.testRevokeRoles(_secManager);
    }

    @Test
    public void testPersistence() {
        SecurityManagerProctor.testPersistencePart1(_secManager);
        //restart meandre server
        try{
            _server.stop();
            _server = null;
            _server = new MeandreServer(_serverPort, _workingDir);
            _secManager =  makeAdminClient();
            _server.start(false);
        }catch(Exception e){
            fail("failed to restart server properly");
        }
        SecurityManagerProctor.testPersistencePart2(_secManager);
    }
    
    @Test
    public void testGetValidRoles(){
    	SecurityManagerProctor.testGetValidRoles(_secManager);
    }
    
/*    @Test
    public void testUpdateUser() {
        fail("Not yet implemented");
    }
  */  
    private static void log(String msg){
        WSLoggerFactory.getWSLogger().info("MeandreAdminClientTest." + msg);
    }

}
