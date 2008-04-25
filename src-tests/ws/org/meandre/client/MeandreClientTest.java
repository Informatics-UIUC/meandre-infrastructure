/**
 * 
 */
package org.meandre.client;

import static org.junit.Assert.*;

import org.meandre.client.MeandreClient;

import org.junit.After;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;
import java.io.File;
import java.net.URL;
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.Resource;
import com.hp.hpl.jena.rdf.model.ResourceFactory;

import org.meandre.core.store.repository.*;
import org.meandre.demo.repository.DemoRepositoryGenerator;

/**
 * @author pgroves
 *
 */
public class MeandreClientTest {

    String _serverUrl = "localhost";
    int _serverPort = 1714;

    String _sTestUploadJar = "./test/meandre/component-upload-test.jar";
    
    String _sDemoRepo= "http://" + _serverUrl + ":" + _serverPort +
            "/public/services/demo_repository.nt";
    
    MeandreClient _adminClient = null;
    
    /**
     * @throws java.lang.Exception
     */
    @BeforeClass
    public static void setUpBeforeClass() throws Exception {
    }

    /**
     * @throws java.lang.Exception
     */
    @Before
    public void setUp() throws Exception {
        _adminClient = new MeandreClient(_serverUrl, _serverPort);
        _adminClient.setCredentials("admin", "admin");
        
        //add the demo repository

        boolean ret;
        try{
             ret = _adminClient.addLocation(_sDemoRepo, "Hello Demo");
             
        }catch(TransmissionException e){
            e.printStackTrace();
        }      
        System.out.println("------end setup----------");
        return;
    }

    /**
     * @throws java.lang.Exception
     */
    @After
    public void tearDown() throws Exception {
        System.out.println("------begin teardown----------");
        _adminClient.removeLocation(_sDemoRepo);
        _adminClient.regenerate();
    }

    /**
     * Test method for 
     * {@link org.meandre.client.MeandreClient#retrieveInstallationProperties()}.
     */
    @Test
    public void testRetrieveInstallationProperties() {
        Model mProps = null;
        try{
            mProps = _adminClient.retrieveInstallationProperties();
            
         }catch(TransmissionException e){
             fail("Transmission failure: " + e.toString());
         }
         assertFalse(mProps.isEmpty());
         return;
    }

    /**
     * Test method for {@link org.meandre.client.MeandreClient#retrieveUserRoles()}.
     */
    @Test
    public void testRetrieveUserRoles() {
        Set<String> roles = null;
        try{
           roles = _adminClient.retrieveUserRoles();
           
        }catch(TransmissionException e){
            fail("Transmission failure: " + e.toString());
        }
        //test to see that they all appear to be valid properties
        
        String expectedPrefix = 
                "http://www.meandre.org/accounting/property/action/";
        for(String observedRole: roles){
            assertTrue(observedRole.startsWith(expectedPrefix));
        }
        return;
    }

    /**
     * Test method for {@link org.meandre.client.MeandreClient#retrieveLocations()}.
     */
    @Test
    public void testRetrieveLocations() {
        Set<URL> locs = null;
        URL uExpected = null;
        String sExpected = "http://" + _serverUrl + ":" + _serverPort;
        sExpected += "/public/services/repository.nt";
        try{
            uExpected = new URL(sExpected);
        }catch(Exception e){
            e.printStackTrace();
        }
            
        try{
            locs = _adminClient.retrieveLocations();
        }catch(TransmissionException e){
            fail("Transmission failure: " + e.toString());
        }
        assertTrue("! .../services/repository.nt", locs.contains(uExpected));

    }

    /**
     * Test method for 
     * {@link org.meandre.client.MeandreClient#addLocation(java.lang.String, 
     * java.lang.String)}.
     */
    @Test
    public void testAddLocation() {
        String sNewLoc = "http://" + _serverUrl + ":" + _serverPort;
        sNewLoc += "/public/services/demo_repository.nt";
        
        boolean ret;
        try{
             ret = _adminClient.addLocation(sNewLoc, "Hello Demo");
             assertTrue("!Demo Repo Added.", ret);
             
        }catch(TransmissionException e){
            fail("Transmission failure: " + e.toString());
        }      
        return;
    }

    /**
     * Test method for 
     * {@link org.meandre.client.MeandreClient#removeLocation(java.lang.String)}.
     */
    @Test
    public void testRemoveLocation() {
        String sNewLoc = "http://" + _serverUrl + ":" + _serverPort;
        sNewLoc += "/public/services/demo_repository.nt";
        boolean ret;
        try{
            ret = _adminClient.addLocation(sNewLoc, "Hello Demo");
            assertTrue("!demo repo was added", ret);
            ret = _adminClient.removeLocation(sNewLoc);
            assertTrue("!demo repo was removed", ret);
       }catch(TransmissionException e){
           fail("Transmission failure: " + e.toString());
       }
       return;
    }

    /**
     * Test method for {@link org.meandre.client.MeandreClient#retrieveRepository()}.
     */
    @Test
    public void testRetrieveRepository() {
        QueryableRepository repo = null;
        try{
            repo = _adminClient.retrieveRepository();
        }catch (TransmissionException e){
            fail("Transmission failure: " + e.toString());
        }
        //TODO: test what's in the repository?
    }

    /**
     * Test method for {@link org.meandre.client.MeandreClient#regenerate()}.
     */
    @Test
    public void testRegenerate() {
        try{
            boolean ret = _adminClient.regenerate();
            assertTrue("!regeneration success", ret);
        }catch(TransmissionException e){
            fail("Transmission failure: " + e.toString());
        }
        return;
    }

    /**
     * Test method for {@link org.meandre.client.MeandreClient#retrieveComponentUrls()}.
     */
    @Test
    public void testRetrieveComponentUrls() {
        Set<URL> compUrls = null;
        try{
            compUrls = _adminClient.retrieveComponentUrls();
        }catch (TransmissionException e){
            fail("Transmission failure: " + e.toString());
        }
        try{
            URL expected1 = new URL("http://test.org/component/push_string");
            URL expected2 = new URL("http://test.org/component/print-object");
            assertTrue("!contains 1", compUrls.contains(expected1));
            assertTrue("!contains 2", compUrls.contains(expected2));

        }catch(Exception e){
            e.printStackTrace();
        }
        return;
    }

    /**
     * Test method for {@link org.meandre.client.MeandreClient#retrieveFlowUrls()}.
     * 
     * FIXME: requires testAddLocation to be run first
     */
    @Test
    public void testRetrieveFlowUrls() {
        Set<URL> flowUrls = null;
        String sUrl = "http://test.org/flow/test-hello-world-with-python-and-lisp/";
        try{
            flowUrls = _adminClient.retrieveFlowUrls();
        }catch (TransmissionException e){
            fail("Transmission failure: " + e.toString());
        }
        try{
            URL expected = 
                new URL(sUrl);
            assertTrue("!has hello world", flowUrls.contains(expected));
        }catch(Exception e){
            e.printStackTrace();
        }
        return;
    }

    /**
     * Test method for {@link org.meandre.client.MeandreClient#retrieveAllTags()}.
     */
    @Test
    public void testRetrieveAllTags() {
        Set<String> tags = null;
        try{
            tags = _adminClient.retrieveAllTags();
        }catch(TransmissionException e){
            fail("Transmission failure: " + e.toString());
        }
        
        assertTrue("!has string", tags.contains("string"));
        assertTrue("!has print", tags.contains("print"));        
        assertTrue("!has concatenate", tags.contains("concatenate"));
    }

    /**
     * Test method for {@link org.meandre.client.MeandreClient#retrieveComponentTags()}.
     */
    @Test
    public void testRetrieveComponentTags() {
        Set<String> tags = null;
        try{
            tags = _adminClient.retrieveComponentTags();
        }catch(TransmissionException e){
            fail("Transmission failure: " + e.toString());
        }
        assertTrue("!has string", tags.contains("string"));
        assertTrue("!has print", tags.contains("print"));        
        assertTrue("!has concatenate", tags.contains("concatenate"));       
        return;
    }

    /**
     * Test method for {@link org.meandre.client.MeandreClient#retrieveFlowTags()}.
     */
    @Test
    public void testRetrieveFlowTags() {
        Set<String> tags = null;
        try{
            tags = _adminClient.retrieveComponentTags();
        }catch(TransmissionException e){
            fail("Transmission failure: " + e.toString());
        }     
        assertTrue("!has string", tags.contains("string"));
        assertTrue("!has print", tags.contains("print"));        
        assertTrue("!has concatenate", tags.contains("concatenate"));
        return;
    }

    /**
     * Test method for 
     * {@link org.meandre.client.MeandreClient#retrieveComponentsByTag(
     * java.lang.String)}.
     */
    @Test
    public void testRetrieveComponentsByTag() {
        //test retrieving "PrintObject" component with tag "print"
        String tag = "print";
        URL expectedCompUrl = null;
        try{
            expectedCompUrl = new URL("http://test.org/component/print-object");
        }catch(Exception e){
            fail("url prob");
        }
        Set<URL> compUrls = null;
        try{
            compUrls = _adminClient.retrieveComponentsByTag(tag);
        }catch(TransmissionException e){
            fail("Transmission failure: " + e.toString());
        }     
        assertEquals(1, compUrls.size());
        assertTrue(compUrls.contains(expectedCompUrl));
        
        //test doing a retrieve when there are no components for a tag
        tag = "lkjdklwkeknc";
        try{
            compUrls = _adminClient.retrieveComponentsByTag(tag);
        }catch(TransmissionException e){
            fail("Transmission failure: " + e.toString());
        }
        assertEquals(0, compUrls.size());
    }

    /**
     * Test method for 
     * {@link org.meandre.client.MeandreClient#retrieveFlowsByTag(java.lang.String)}.
     */
    @Test
    public void testRetrieveFlowsByTag() {
        
        //test retrieving "hello world" by "demo"
        String tag = "string";
        URL expectedFlowUrl = null;
        try{
            String sUrl = "http://test.org/flow/";
            sUrl += "test-hello-world-with-python-and-lisp/";
            expectedFlowUrl = new URL(sUrl);
        }catch(Exception e){
            fail("url prob");
        }
        Set<URL> flowUrls = null;
        try{
            flowUrls = _adminClient.retrieveFlowsByTag(tag);
        }catch(TransmissionException e){
            fail("Transmission failure: " + e.toString());
        }     
        assertEquals(1, flowUrls.size());
        assertTrue(flowUrls.contains(expectedFlowUrl));   
    }

    /**
     * Test method for 
     * {@link org.meandre.client.MeandreClient#retrieveComponentDescriptor(
     * java.lang.String)}.
     */
    @Test
    public void testRetrieveComponentDescriptor() {
        String sUrl = "http://test.org/component/print-object";
        ExecutableComponentDescription comp = null;
        try{
            comp = _adminClient.retrieveComponentDescriptor(sUrl);
        }catch(TransmissionException e){
            fail("Transmission failure: " + e.toString());
        }
        assertEquals(sUrl, comp.getExecutableComponentAsString());
    }

    /**
     * Test method for 
     * {@link org.meandre.client.MeandreClient#retrieveFlowDescriptor(java.lang.String)}.
     */
    @Test
    public void testRetrieveFlowDescriptor() {
        String sUrl = "http://test.org/flow/test-hello-world-with-python-and-lisp/";
        FlowDescription comp = null;
        try{
            comp = _adminClient.retrieveFlowDescriptor(sUrl);
        }catch(TransmissionException e){
            fail("Transmission failure: " + e.toString());
        }
        assertEquals(sUrl, comp.getFlowComponentAsString());
    }

    /**
     * Test method for 
     * {@link org.meandre.client.MeandreClient#retrieveComponentUrlsByQuery(
     * java.lang.String)}.
     */
    /*@Test
    public void testRetrieveComponentUrlsByQuery() {
        fail("Not yet implemented");
    }*/

    /**
     * Test method for 
     * {@link org.meandre.client.MeandreClient#retrieveFlowUrlsByQuery(
     * java.lang.String)}.
     */
    /*@Test
    public void testRetrieveFlowUrlsByQuery() {
        fail("Not yet implemented");
    }*/

    /**
     * Test method for 
     * {@link org.meandre.client.MeandreClient#upload(byte[][], 
     * boolean, boolean, com.hp.hpl.jena.rdf.model.Model)}.
     */
    @Test
    public void testUploadComponent() {
        Set<File> jars = new HashSet<File>();
        File jarFile = new File(_sTestUploadJar);
        jars.add(jarFile);
        //assertTrue("FileExists", jarFile.exists());
        System.out.println(jarFile.toString());
        Model modRepo = DemoRepositoryGenerator.getTestHelloWorldRepository();
        RepositoryImpl repo = new RepositoryImpl(modRepo);
        for(Resource comp: 
                repo.getAvailableExecutableComponents()){
            System.out.println(comp.toString());
        }
        String sCompUrl = "http://test.org/component/print-object";
        Resource rCompUrl = ResourceFactory.createResource(sCompUrl);
        ExecutableComponentDescription comp = 
                repo.getExecutableComponentDescription(rCompUrl);
        try{
            boolean ret;
            //ret = _adminClient.removeResource(sCompUrl);
            ret = _adminClient.uploadComponent(comp, jars, true);
            assertTrue("!upload success", ret);
        }catch(Exception e){
            e.printStackTrace();
            fail("could not complete upload");
        }
        return;
    }

    /**
     * Test method for 
     * {@link org.meandre.client.MeandreClient#uploadFlow(FlowDescription, boolean)}.
     */
    @Test
    public void testUploadFlow() {
        Model modTestRepo = DemoRepositoryGenerator.getTestHelloWorldRepository();
        QueryableRepository repo = new RepositoryImpl(modTestRepo);
        Set<FlowDescription> flows = repo.getAvailableFlowDecriptions();
        Iterator<FlowDescription> iter = flows.iterator();
        FlowDescription flow = iter.next();
        String sResourceUrl = flow.getFlowComponentAsString();
        System.out.println(sResourceUrl);
        boolean retStat = false;
        Set<URL> flowsAfter = null;
        try{
            retStat = _adminClient.uploadFlow(flow, true);
            flowsAfter = _adminClient.retrieveFlowUrls();
        }catch(Exception e){
            fail("TransmissionFailure: " + e.toString());
        }
        System.out.println("Retrieved Flows:");
        for(URL flowUrl: flowsAfter){
            System.out.println(flowUrl.toString());
        }
        assertTrue("!upload return code", retStat);
        try{
        assertTrue("!Contains resource URL", 
                   flowsAfter.contains(new URL(sResourceUrl)));
        }catch(Exception e){
            e.printStackTrace();
        }
    }

    /**
     * Test method for 
     * {@link org.meandre.client.MeandreClient#remove(java.lang.String)}.
     */
    @Test
    public void testRemove() {
        Model modTestRepo = DemoRepositoryGenerator.getTestHelloWorldRepository();
        QueryableRepository repo = new RepositoryImpl(modTestRepo);
        Set<FlowDescription> flows = repo.getAvailableFlowDecriptions();
        Iterator<FlowDescription> iter = flows.iterator();
        FlowDescription flow = iter.next();
        String sResourceUrl = flow.getFlowComponentAsString();
        System.out.println(sResourceUrl);
        boolean retStat = false;
        Set<URL> flowsAfter = null;
        try{
            Set<URL> flowsBefore = _adminClient.retrieveFlowUrls();
            retStat = _adminClient.uploadFlow(flow, true);
            assertTrue(retStat);
            Set<URL> flowsMid = _adminClient.retrieveFlowUrls();
            retStat =  _adminClient.removeResource(sResourceUrl);
            assertTrue(retStat);
            flowsAfter = _adminClient.retrieveFlowUrls();
        }catch(Exception e){
            fail("TransmissionFailure: " + e.toString());
        }
        try{
            assertFalse("Contains resource URL", 
                    flowsAfter.contains(new URL(sResourceUrl)));
        }catch(Exception e){
            e.printStackTrace();
        }        

    }

    /**
     * Test method for 
     * {@link org.meandre.client.MeandreClient#publish(java.lang.String)}.
     */
    @Test
    public void testPublish() {
        String sUrl = "http://test.org/flow/test-hello-world-with-python-and-lisp/";
        try{
            boolean ret = _adminClient.publish(sUrl);
            assertTrue("!publish success", ret);
        }catch(Exception e){
            e.printStackTrace();
            fail(e.toString());
        }
        return;
        
    }

    /**
     * Test method for 
     * {@link org.meandre.client.MeandreClient#unpublish(java.lang.String)}.
     */
    @Test
    public void testUnpublish() {
        String sUrl = "http://test.org/flow/test-hello-world-with-python-and-lisp/";
        boolean ret;
        try{
            //ret = _adminClient.publish(sUrl);
            //assertTrue("!publish success", ret);
            ret = _adminClient.unpublish(sUrl);
            assertTrue("!unpublish success", ret);
        }catch(Exception e){
            e.printStackTrace();
            fail(e.toString());
        }
        return;       
    }

    /**
     * Test method for 
     * {@link org.meandre.client.MeandreClient#runFlow(java.lang.String)}.
     */
    /*@Test
    public void testRunFlow() {
        fail("Not yet implemented");
    }*/

    /**
     * Test method for {@link org.meandre.client.MeandreClient#retrieveRunningFlows()}.
     */
    /*@Test
    public void testRetrieveRunningFlows() {
        fail("Not yet implemented");
    }*/

    /**
     * Test method for 
     * {@link org.meandre.client.MeandreClient#retrievePublicRepository()}.
     */
    @Test
    public void testRetrievePublicRepository() {
        QueryableRepository repo = null;
        try{
            repo = _adminClient.retrievePublicRepository();
        }catch (TransmissionException e){
            fail("Transmission failure: " + e.toString());
        }
        return;
    }

    /**
     * Test method for {@link org.meandre.client.MeandreClient#retrieveDemoRepository()}.
     */
    @Test
    public void testRetrieveDemoRepository() {
        QueryableRepository repo = null;
        try{
            repo = _adminClient.retrieveDemoRepository();
        }catch (TransmissionException e){
            fail("Transmission failure: " + e.toString());
        }
        return;
    }

    /**
     * Test method for {@link org.meandre.client.MeandreClient#abortFlow()}.
     */
    /*@Test
    public void testAbortFlow() {
        fail("Not yet implemented");
    }*/

    /**
     * Test method for 
     * {@link org.meandre.client.MeandreClient#retrieveRunningFlowStatisitics()}.
     */
    /*@Test
    public void testRetrieveRunningFlowStatisitics() {
        fail("Not yet implemented");
    }*/

}
