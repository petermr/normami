package org.contentmine.ami.plugins.search;

import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.contentmine.ami.plugins.AbstractSearchArgProcessor;
import org.contentmine.cproject.args.AbstractTool;

/** 
 * Processes commandline arguments.
 * 
 * @author pm286
 */
public class SearchArgProcessor extends AbstractSearchArgProcessor {
	
	// Dummy at present
	
	public static final Logger LOG = Logger.getLogger(SearchArgProcessor.class);
	static {
		LOG.setLevel(Level.DEBUG);
	}
	
	
	public SearchArgProcessor() {
		super();
	}

	public SearchArgProcessor(String args) {
		this();
		parseArgs(args);
	}

	public SearchArgProcessor(String[] args) {
		this();
		parseArgs(args);
	}

	// =============== METHODS ==============

	public SearchArgProcessor(AbstractTool abstractTool) {
		super(abstractTool);
	}



}
