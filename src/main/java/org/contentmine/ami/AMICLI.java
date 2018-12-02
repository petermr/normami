package org.contentmine.ami;

import org.apache.log4j.Level;
import org.apache.log4j.Logger;

/** simple CLI for ami-* commands.
 * replaces args.xml (may revist that later)
 * 
 * @author pm286
 *
 */
// may make abstract later
public class AMICLI {
	private static final Logger LOG = Logger.getLogger(AMICLI.class);
	static {
		LOG.setLevel(Level.DEBUG);
	}

	public AMICLI() {
		
	}
	
	public AMICLI readCL(String[] args) {
		
		return this;
	}
	

}
