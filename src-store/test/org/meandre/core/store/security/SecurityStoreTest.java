package org.meandre.core.store.security;

import static org.junit.Assert.*;

import java.io.File;
import java.io.StringWriter;
import java.net.InetAddress;
import java.net.UnknownHostException;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import org.meandre.webservices.MeandreServer;
import org.meandre.core.security.SecurityManager;
import org.meandre.core.store.security.SecurityStore;

import com.hp.hpl.jena.rdf.model.Model;

public class SecurityStoreTest {
    
    
    @SuppressWarnings("unused")
	private static String _serverUrl = "localhost";
    private static int _serverPort = 1704;
    
    private static String _workingDir = 
        "." + File.separator + "test" + File.separator + "output";


    
    private static MeandreServer _server = null;
    private static SecurityManager _secManager;
    
    @BeforeClass
    public static void setUpBeforeClass() throws Exception {
        log("setupBeforeClass begin");
        try {
            _serverUrl = InetAddress.getLocalHost().getCanonicalHostName();
        } catch (UnknownHostException e) {
        }
        
        File fWorkDir = new File(_workingDir);
        if(!fWorkDir.exists()){
            fWorkDir.mkdirs();
        }

        _server = new MeandreServer(_serverPort, _workingDir);
        _secManager = _server.getStore().getSecurityStore();
        _server.start(false);
        log("setupBeforeClass end");
    }

    @AfterClass
    public static void tearDownAfterClass() throws Exception {
        log("tearDownAfterClass begin");
         _secManager = null;
        _server.stop();
        _server = null;
        File fWorkDir = new File(_workingDir);
        try{
            fWorkDir.delete();
        }catch(Exception e){
            log(e.toString());
        }
        
        log("tearDownAfterClass end");        
    }

    @Before
    public void setUp() throws Exception {
        log("setup begin");
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
        SecurityStore ss = (SecurityStore)_secManager;
        Model mod = ss.getModel();
        StringWriter sw = new StringWriter();
        mod.write(sw);
        log("testCreateUser: original model");
        log(sw.toString());
        SecurityManagerProctor.testCreateUser(_secManager);
        mod = ss.getModel();
        sw = new StringWriter();
        mod.write(sw);
        log("testCreateUser: after model");
        log(sw.toString());
        
    }

    @Test
    public void testRemoveUser() {
        SecurityManagerProctor.testRemoveUser(_secManager);
    }

 /*   @Test
    public void testGetUsersNickNames() {
        SecurityManagerProctor.testGetUsersNickNames(_secManager);
    }
    */

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
            _secManager = _server.getStore().getSecurityStore();
            _server.start(false);
        }catch(Exception e){
            fail("failed to restart server properly");
        }
        SecurityManagerProctor.testPersistencePart2(_secManager);
    }
    
/*    @Test
    public void testUpdateUser() {
        fail("Not yet implemented");
    }
  */  
    private static void log(String msg){
        System.out.println("SecurityStoreTest." + msg);
    }

}
