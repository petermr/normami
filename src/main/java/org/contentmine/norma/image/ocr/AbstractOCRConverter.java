package org.contentmine.norma.image.ocr;

import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.contentmine.norma.util.CommandRunner;

public abstract class AbstractOCRConverter extends CommandRunner {
	private static final Logger LOG = Logger.getLogger(AbstractOCRConverter.class);
	static {
		LOG.setLevel(Level.DEBUG);
	}

}
