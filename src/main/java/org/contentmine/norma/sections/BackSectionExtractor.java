package org.contentmine.norma.sections;

import org.apache.log4j.Level;
import org.apache.log4j.Logger;

/** special treatment for sections in Body.
 * 
 * @author pm286
 *
 */
public class BackSectionExtractor extends SectionExtractor {
	private static final Logger LOG = Logger.getLogger(BackSectionExtractor.class);
	static {
		LOG.setLevel(Level.DEBUG);
	}
}
