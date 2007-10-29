package org.meandre.core.repository;

import static org.junit.Assert.*;

import java.util.HashSet;
import java.util.Set;

import org.junit.Test;
import org.meandre.core.store.repository.TagsDescription;

/** This class test the behavior of TagsDescription
 * 
 * @author Xavier Llor&agrave;
 *
 */
public class TagsDescriptionTest {

	/** Counter to track different test examples generated */
	private static int iNumTestExamplesGenerated = 0;

	/** The number of tags to generate */
	private static int iNumTags = 10;
	
	/** Creates a test instance.
	 * 
	 * @return The test instance
	 */
	static TagsDescription createTestInstance() {
		int iInstance = iNumTestExamplesGenerated++;
		
		HashSet<String> hs = new HashSet<String>();
		
		// Populate the test tag set
		for ( int i=0 ; i<iNumTags ; i++ )
			hs.add("tag-"+iInstance+"-"+i);
		
		return new TagsDescription(hs);
	}

	/** Create a test tag set.
	 * 
	 * @return The test tag set
	 */
	private Set<String> createTestTagsSet () {
		HashSet<String> hs = new HashSet<String>();
		
		// Populate the test tag set
		for ( int i=0 ; i<iNumTags ; i++ )
			hs.add("tag-"+i);
		
		return hs;
	}
	
	/**
	 * Test method for {@link org.meandre.core.store.repository.TagsDescription#TagsDescription(java.util.Set)}.
	 */
	@Test
	public void testTagsDescriptionSetOfString() {
		Set<String> set = createTestTagsSet();
		
		TagsDescription td = new TagsDescription(set);
		
		assertEquals(td.getTags(),set);
		assertEquals(td.getTags().size(),iNumTags);
	}

	/**
	 * Test method for {@link org.meandre.core.store.repository.TagsDescription#TagsDescription()}.
	 */
	@Test
	public void testTagsDescription() {
		TagsDescription td = new TagsDescription();
		
		assertEquals(td.getTags().size(),0);
	}

	/**
	 * Test method for {@link org.meandre.core.store.repository.TagsDescription#getTags()}.
	 */
	@Test
	public void testGetTags() {
		Set<String> setOne = createTestTagsSet();
		Set<String> setTwo = createTestTagsSet();
		
		TagsDescription td = new TagsDescription(setOne);
		
		assertEquals(td.getTags().containsAll(setTwo),true);
		assertEquals(setTwo.containsAll(td.getTags()),true);
	}

	/**
	 * Test method for {@link org.meandre.core.store.repository.TagsDescription#toString()}.
	 */
	@Test
	public void testToString() {
		Set<String> set = createTestTagsSet();
		
		TagsDescription td = new TagsDescription(set);
		
		assertEquals(td.toString().split(" ").length,iNumTags);
	}

}
