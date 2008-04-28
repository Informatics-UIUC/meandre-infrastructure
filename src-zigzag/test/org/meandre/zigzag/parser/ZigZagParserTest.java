package org.meandre.zigzag.parser;

import static org.junit.Assert.fail;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FilenameFilter;

import org.junit.Test;
import org.meandre.webservices.MeandreServer;
import org.meandre.zigzag.semantic.FlowGenerator;

/** This class is intended to run the test for the ZigZag parser
 * based on the test file on data/test/zigzag directory.
 * 
 * @author Xavier Llor&agrave;
 *
 */
public class ZigZagParserTest {
	
	/** Test for the ZigZag parser.
	 * 
	 * @throws Exception Something went wrong
	 * 
	 */
	@Test
	public void testZigZagParserObject () throws Exception {
		MeandreServer ms = new MeandreServer();
		ms.start(false);
		
		Thread.sleep(10000);
		
		for ( File file:listZigZagTestFiles()) {
			 try {
				 FileInputStream fis = new FileInputStream(file);
				 ZigZag parser = new ZigZag(fis);    
				 parser.sFileName = file.getAbsolutePath();
				 parser.fg = new FlowGenerator();
				 parser.start(); 
		    }
		    catch ( ParseException pe ) {
		    	fail(pe.toString());
		    } catch (FileNotFoundException e) {
		    	fail(e.toString());
			} 
		}
		
		ms.stop();
	}

	/** List the ZigZag test files.
	 * 
	 * @return The array of test files
	 */
	public File[] listZigZagTestFiles () {
		File file = new File("./data/test/zigzag/");
		File [] fa = file.listFiles(new FilenameFilter() {
			public boolean accept ( File file, String sName ) {
				if ( sName.endsWith(".zz"))
					return true;
				else
					return false;
			}
		});
		
		return fa;
	}
	
	/** Test for the ZigZag parser and MAU generator.
	 * 
	 * @throws Exception Something went wrong
	 * 
	 */
	@Test
	public void testZigZagParserObjectAndMauGenerator () throws Exception {
		MeandreServer ms = new MeandreServer();
		ms.start(false);
		
		Thread.sleep(10000);
		
		for ( File file:listZigZagTestFiles()) {
			 try {
				 FileInputStream fis = new FileInputStream(file);
				 ZigZag parser = new ZigZag(fis);    
				 parser.sFileName = file.getAbsolutePath();
				 parser.fg = new FlowGenerator();
				 parser.start(); 
				 parser.fg.generateMAU(file.getAbsolutePath().replaceAll(".zz$", ".mau"));
		    }
		    catch ( ParseException pe ) {
		    	fail(pe.toString());
		    } catch (FileNotFoundException e) {
		    	fail(e.toString());
			} 
		}
		
		ms.stop();
	}

}
