package org.contentmine.ami.old;

import java.io.File;

import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.contentmine.ami.AMIFixtures;
import org.contentmine.ami.tools.old.AMISearch;
import org.contentmine.cproject.util.CMineTestFixtures;
import org.junit.Test;

/** OLD STYLE - phase out */
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
	
	@Test
	public void testZikaSpecies() {
		File targetDir = new File("target/cooccurrence/zika10");
		CMineTestFixtures.cleanAndCopyDir(AMIFixtures.TEST_ZIKA10_DIR, targetDir);
		String[] args = {targetDir.toString(), "country", "species"}; 
		AMISearch.main(args);
	}
	
	@Test
	public void testZikaDictionary() {
		File targetDir = new File("target/cooccurrence/zika10");
		CMineTestFixtures.cleanAndCopyDir(AMIFixtures.TEST_ZIKA10_DIR, targetDir);
		String[] args = {targetDir.toString(), "country", "/Users/pm286/ContentMine/dictionary/dictionaries/animal/aedes.xml"}; 
		AMISearch.main(args);
	}
	
	@Test
	public void testDictionaryFile() {
		File targetDir = new File("target/tigr2ess/osanctum");
		CMineTestFixtures.cleanAndCopyDir(new File("/Users/pm286/workspace/tigr2ess/osanctum"), targetDir);
		String[] args = {targetDir.toString(), "country", "/Users/pm286/ContentMine/dictionary/dictionaries/bio/plantparts.xml"}; 
		AMISearch.main(args);
	}
	
	@Test
	public void testFile() {
		File foo = new File("foo/");
		File bar = new File(foo, "bar");
		File fb = bar.getParentFile();
		LOG.debug("fb "+fb);
		File q = new File("aa/bb/cc");
		LOG.debug("p "+q.getParent());
	}
	
//	/** add current ami-search-cooccurrence under picocli
//	 * 
//	 */
//	@Test 
//	public void testSearchCooccurrencePicocli() {
//		File targetDir = new File("target/tigr2ess/osanctum");
//		CMineTestFixtures.cleanAndCopyDir(new File("/Users/pm286/workspace/tigr2ess/osanctum"), targetDir);
//		String args = targetDir.toString() +  " country " +  "/Users/pm286/ContentMine/dictionary/dictionaries/bio/plantparts.xml";
//		
//	}

}
