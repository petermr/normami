package org.contentmine.ami.tools;

import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.contentmine.ami.tools.AMIOCRTool;
import org.junit.Test;

/** test OCR.
 * 
 * @author pm286
 *
 */
public class AMIGROBIDTest {
	private static final Logger LOG = Logger.getLogger(AMIGROBIDTest.class);
	static {
		LOG.setLevel(Level.DEBUG);
	}

	@Test
	/** 
	 * convert single (good) file
	 */
	public void testGROBID() throws Exception {
		String args = 
				"-t /Users/pm286/workspace/uclforest/dev/bowmann"
//				+ " --input fulltext.pdf"
				+ " --basename tei/"
//				+ " --pqges 9"
				+ " --exe processFullText"
			;
		new AMIGrobidTool().runCommands(args);
	}
}
