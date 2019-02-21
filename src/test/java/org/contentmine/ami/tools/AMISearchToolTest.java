package org.contentmine.ami.tools;

import java.io.File;

import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.contentmine.ami.AMIFixtures;
import org.contentmine.cproject.util.CMineTestFixtures;
import org.junit.Test;

public class AMISearchToolTest {
	private static final Logger LOG = Logger.getLogger(AMISearchToolTest.class);
	static {
		LOG.setLevel(Level.DEBUG);
	}

	@Test
	public void testZikaCooccurrence0() {
		File targetDir = new File("target/cooccurrence/zika10a");
		CMineTestFixtures.cleanAndCopyDir(AMIFixtures.TEST_ZIKA10A_DIR, targetDir);
		String args = 
//				"-p /* /Users/pm286/workspace/cmdev/normami/target/cooccurrence/zika10a/"
				"-p "+targetDir
				+ " --dictionaryTop /Users/pm286/ContentMine/dictionary/dictionaries"
				+ " --dictionary "
				+ ""
				+ "country bio.auxin"
				+ " --ignorePlugins word"
			;
		new AMISearchTool().runCommands(args);
	}
	
	@Test
	public void testZikaCooccurrence() {
		File targetDir = new File("target/cooccurrence/zika10");
		CMineTestFixtures.cleanAndCopyDir(AMIFixtures.TEST_ZIKA10_DIR, targetDir);
		String args = 
				"-p /Users/pm286/workspace/cmdev/normami/target/cooccurrence/zika10"
				+ " --dictionary species gene country disease funders "
			;
		new AMISearchTool().runCommands(args);
	}

	@Test
	public void testAMISearchNew() {
		File targetDir = new File("target/cooccurrence/ocimum");
		CMineTestFixtures.cleanAndCopyDir(new File("/Users/pm286/workspace/tigr2ess/osanctum200"), targetDir);
		String args = /*ami-search-new*/""
				+ " -p "+targetDir 
				+ " --ignorePlugins word"
//				+ " --dictionaryTop /Users/pm286/workspace/tigr2ess/dictionaries/"
+ " --dictionary" + /*" country plantparts" +*/ " /Users/pm286/workspace/tigr2ess/dictionaries/examples/monoterpenes"
//+ " --dictionary" + /*" country plantparts" +*/ " /Users/pm286/workspace/tigr2ess/dictionaries/monoterpenes"
				;
		new AMISearchTool().runCommands(args);
	}
	
	@Test
	/** serious performance problems on PMC3390897 - */
	public void testAMISearchNewSpecies() {
		File targetDir = new File("target/cooccurrence/species");
		CMineTestFixtures.cleanAndCopyDir(new File("/Users/pm286/workspace/tigr2ess/scratch/centaurea"), targetDir);
		String args = /*ami-search-new*/""
				+ " -p "+targetDir 
				+ " --ignorePlugins word"
				+ " --dictionary species "
				;
		new AMISearchTool().runCommands(args);
	}

	@Test
	public void testAMISearchBug() {
		File targetDir = new File("target/cooccurrence/ocimum");
		CMineTestFixtures.cleanAndCopyDir(new File("/Users/pm286/workspace/tigr2ess/osanctum200"), targetDir);
		String args = /*ami-search-new*/""
				+ " -p "+targetDir 
				+ " --ignorePlugins word"//				
				+ " --dictionary /Users/pm286/workspace/tigr2ess/dictionaries/examples/monoterpenes country " 
				;
		new AMISearchTool().runCommands(args);
	}
	

}
