package org.contentmine.ami.tools;

import java.io.File;

import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.contentmine.ami.AMIFixtures;
import org.contentmine.cproject.util.CMineTestFixtures;
import org.junit.Test;

public class AMIWordsToolTest {
	private static final Logger LOG = Logger.getLogger(AMIWordsToolTest.class);
	static {
		LOG.setLevel(Level.DEBUG);
	}

	@Test
	public void testZikaCooccurrence0() {
		File targetDir = new File("target/cooccurrence/zika10");
		CMineTestFixtures.cleanAndCopyDir(AMIFixtures.TEST_ZIKA10_DIR, targetDir);
		/** need HTML */
		String args = 
				"-p /Users/pm286/workspace/cmdev/normami/target/cooccurrence/zika10/"
			;
		new AMITransformTool().runCommands(args);
		args = 
				"-p /Users/pm286/workspace/cmdev/normami/target/cooccurrence/zika10/"
				+ " --stopwords w.stopwords:pmcstop.txt stopwords.txt"
			;
		new AMIWordsTool().runCommands(args);
	}
	
	@Test
	public void testZikaCooccurrence() {
		File targetDir = new File("target/cooccurrence/zika10");
		CMineTestFixtures.cleanAndCopyDir(AMIFixtures.TEST_ZIKA10_DIR, targetDir);
		String args = 
				"-p /Users/pm286/workspace/cmdev/normami/target/cooccurrence/zika10"
				+ " --dictionary country disease funders"
			;
		new AMIWordsTool().runCommands(args);
	}

	
}
