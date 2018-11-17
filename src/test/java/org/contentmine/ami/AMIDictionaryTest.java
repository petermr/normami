package org.contentmine.ami;

import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.junit.Test;

/** tests AMIDictinary
 * 
 * @author pm286
 *
 */
public class AMIDictionaryTest {
	private static final Logger LOG = Logger.getLogger(AMIDictionaryTest.class);
	static {
		LOG.setLevel(Level.DEBUG);
	}

	@Test
	public void testHelp() {
		String[] args = {};
		AMIDictionary.main(args);
	}
	
	@Test
	public void testListSome() {
		String[] args = {"country", "crispr", "disease"};
		AMIDictionary.main(args);
	}
	
	@Test
	public void testListAll() {
		String[] args = {"LIST"};
		AMIDictionary.main(args);
	}
	
	@Test
	public void testListFull() {
		String[] args = {"FULL", "country"};
		AMIDictionary.main(args);
	}
	
	@Test
	public void testListFull2() {
		String[] args = {"FULL", "socialmedia", "noncommunicable"};
		AMIDictionary.main(args);
	}
	

}
