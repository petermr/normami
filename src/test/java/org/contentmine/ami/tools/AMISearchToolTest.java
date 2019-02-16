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
				+ " --dictionary country bio/auxin"
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

	
}
