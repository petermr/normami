package org.contentmine.norma;

import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.contentmine.cproject.args.DefaultArgProcessor;

public class Norma {

	private static final Logger LOG = Logger.getLogger(Norma.class);
	static {
		LOG.setLevel(Level.DEBUG);
	}
	
	public final static String NORMA_OUTPUT_RESOURCE = NAConstants.NORMA_RESOURCE + "/" +"output";
	private DefaultArgProcessor argProcessor;

	public static void main(String[] args) {
		Norma norma = new Norma();
		norma.run(args);
	}

	public void run(String[] args) {
		argProcessor = new NormaArgProcessor(args);
		argProcessor.runAndOutput();
	}

	public void run(String args) {
		args = args == null ? null : args.trim();
		argProcessor = new NormaArgProcessor(args.split("\\s+"));
		argProcessor.runAndOutput();
	}

	public DefaultArgProcessor getArgProcessor() {
		return argProcessor;
	}
}
