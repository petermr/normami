package org.contentmine.ami;

import java.io.File;

import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.contentmine.ami.plugins.AMIArgProcessor;
import org.junit.Test;

/** tests AMIArgProcessor and AMIProcessor (the main enrty point)
 * 
 * @author pm286
 *
 */
public class AMIArgProcessorTest {

	private static final Logger LOG = Logger.getLogger(AMIArgProcessorTest.class);
	static {
		LOG.setLevel(Level.DEBUG);
	}

	
	@Test
	public void testVersion() {
		AMIArgProcessor argProcessor = new AMIArgProcessor();
		argProcessor.parseArgs("--version");
	}
	
	// utility method to check first part of resultsElementList


	@Test
	public void testAMIProcessor() {
		File indir = AMIFixtures.TEST_PLOSONE_DIR;
		String cmd = " "+indir.getName();
		cmd += " species";
		LOG.debug(cmd);
		
		AMIProcessor amiProcessor = AMIProcessor.createProcessor(indir.getPath());
		amiProcessor.run(cmd);
		
//		public Process exec(String[] cmdarray,String[] envp, File dir) throws IOException		
	}

}
