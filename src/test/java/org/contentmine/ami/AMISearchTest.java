package org.contentmine.ami;

import java.io.File;

import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.contentmine.cproject.util.CMineTestFixtures;
import org.junit.Test;

public class AMISearchTest {
	private static final Logger LOG = Logger.getLogger(AMISearchTest.class);
	static {
		LOG.setLevel(Level.DEBUG);
	}

	@Test
	public void testZikaCooccurrence0() {
		File targetDir = new File("target/cooccurrence/zika10");
		CMineTestFixtures.cleanAndCopyDir(AMIFixtures.TEST_ZIKA10_DIR, targetDir);
		String[] args = {targetDir.toString(), "country"}; 
		AMISearch.main(args);
	}
	
	@Test
	public void testZikaCooccurrence() {
		File targetDir = new File("target/cooccurrence/zika10");
		CMineTestFixtures.cleanAndCopyDir(AMIFixtures.TEST_ZIKA10_DIR, targetDir);
		String[] args = {targetDir.toString(), "country", "disease", "funders"}; 
		AMISearch.main(args);
	}
	
}
