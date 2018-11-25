package org.contentmine.ami;

import java.io.File;

import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.junit.Assert;
import org.junit.Test;

/** test AMIProcessorPDF
 * 
 * @author pm286
 *
 */
public class AMIProcessorPDFTest {
	private static final Logger LOG = Logger.getLogger(AMIProcessorPDFTest.class);
	static {
		LOG.setLevel(Level.DEBUG);
	}
	
	@Test
	/** reads UCL corpus as PDFs and creates first pass SVG , images and scholarly html
	 * 
	 */
	public void testForestPlots() {
		String[] args = {"/Users/pm286/workspace/uclforest/forestplots"};
		Assert.assertTrue(new File(args[0]).exists());
		AMIProcessorPDF.main(args);
	}

}
