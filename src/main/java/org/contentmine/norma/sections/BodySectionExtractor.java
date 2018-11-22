package org.contentmine.norma.sections;

import org.apache.log4j.Level;
import org.apache.log4j.Logger;

/** special treatment for sections in Body.
 * 
 * @author pm286
 *
 */
public class BodySectionExtractor extends SectionExtractor {
	private static final Logger LOG = Logger.getLogger(BodySectionExtractor.class);
	static {
		LOG.setLevel(Level.DEBUG);
	}
}
