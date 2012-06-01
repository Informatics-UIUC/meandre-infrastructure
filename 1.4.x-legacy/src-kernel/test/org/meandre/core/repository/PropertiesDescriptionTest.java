package org.meandre.core.repository;

import static org.junit.Assert.assertEquals;

import java.util.HashSet;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.Set;

import org.junit.Test;

/** This test class test the interface and behavior of class PropertiesDescription
 * 
 * @author Xavier Llor&agrave;
 *
 */
public class PropertiesDescriptionTest {

	/** The number of properties to use on the tests */
	private static int iPropToAdd = 10;
	
	/**
	 * Test method for {@link org.meandre.core.store.repository.PropertiesDescription#PropertiesDescription()}.
	 */
	@Test
	public void testPropertiesDescription() {
		
		PropertiesDescription pd = new PropertiesDescription();

		assertEquals(0,pd.getKeys().size());
		assertEquals(0,pd.getValues().size());
	}

	/**
	 * Test method for {@link org.meandre.core.store.repository.PropertiesDescription#PropertiesDescription(java.util.Hashtable)}.
	 */
	@Test
	public void testPropertiesDescriptionHashtableOfStringString() {
		Hashtable<String,String> hs = new Hashtable<String,String>();
		
		PropertiesDescription pd = new PropertiesDescription(hs);
		
		assertEquals(hs.keySet(),pd.getKeys());
		assertEquals(hs.values(),pd.getValues());
		
	}

	/**
	 * Test method for {@link org.meandre.core.store.repository.PropertiesDescription#getKeys()}.
	 */
	@Test
	public void testGetKeys() {
		PropertiesDescription pd = new PropertiesDescription();
		HashSet<String> hs = new HashSet<String>();
		
		// Add a bunch of properties
		for ( int i=0 ; i<iPropToAdd ; i++ ) {
			pd.add("key-"+i, "value-"+i);
			hs.add("key-"+i);
		}
		
		// Check the stored keys
		Iterator<String> csvi = pd.getKeys().iterator();
		while ( csvi.hasNext() ) {
			boolean bContains = hs.contains(csvi.next());
			assertEquals(bContains,true);
		}
	}

	/**
	 * Test method for {@link org.meandre.core.store.repository.PropertiesDescription#getValues()}.
	 */
	@Test
	public void testGetValues() {
		PropertiesDescription pd = new PropertiesDescription();
		HashSet<String> hs = new HashSet<String>();
		
		// Add a bunch of properties
		for ( int i=0 ; i<iPropToAdd ; i++ ) {
			pd.add("key-"+i, "value-"+i);
			hs.add("value-"+i);
		}
		
		
		// Check the stored values
		Iterator<String> csvi = pd.getValues().iterator();
		while ( csvi.hasNext() ) {
			boolean bContains = hs.contains(csvi.next());
			assertEquals(bContains,true);
		}
		
	}

	/**
	 * Test method for {@link org.meandre.core.store.repository.PropertiesDescription#getValue(java.lang.String)}.
	 */
	@Test
	public void testGetValue() {
		
		PropertiesDescription pd = new PropertiesDescription();
		
		// Add a bunch of properties
		for ( int i=0 ; i<iPropToAdd ; i++ )
			pd.add("key-"+i, "value-"+i);
		
		// Check the stored values
		for ( int i=0 ; i<iPropToAdd ; i++ )
			assertEquals(pd.getValue("key-"+i), "value-"+i);
		
	}

	/**
	 * Test method for {@link org.meandre.core.store.repository.PropertiesDescription#add(java.lang.String, java.lang.String)}.
	 */
	@Test
	public void testAdd() {
		
		PropertiesDescription pd = new PropertiesDescription();

		Set<String> ks = pd.getKeys();
		
		int iSize = ks.size();

		// Add a bunch of properties
		for ( int i=0 ; i<iPropToAdd ; i++ )
			pd.add("key-"+i, "value-"+i);
		
		assertEquals(iSize+iPropToAdd,pd.getKeys().size());
	}

	/**
	 * Test method for {@link org.meandre.core.store.repository.PropertiesDescription#remove(java.lang.String)}.
	 */
	@Test
	public void testRemove() {
		
		PropertiesDescription pd = new PropertiesDescription();

		// Add a bunch of properties
		for ( int i=0 ; i<iPropToAdd ; i++ )
			pd.add("key-"+i, "value-"+i);
		
		int iSize = pd.getKeys().size();

		// Remove and test
		String [] saKeys = new String[pd.getKeys().size()];
		pd.getKeys().toArray(saKeys);
		for ( String s:saKeys ) {
			pd.remove(s);
			assertEquals(iSize-1,pd.getKeys().size());
			iSize--;
		}
		
		// Remove something it is not there
		pd.remove("no-key");
		assertEquals(0,pd.getKeys().size());
	}

}
