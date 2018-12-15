package org.contentmine.ami;

import java.io.File;
import java.io.IOException;

import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.contentmine.eucl.xml.XMLUtil;
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
	private static final File TARGET = new File("target");
	public static final File DICTIONARY_DIR = new File(TARGET, "dictionary");
	

	@Test
	public void testHelp() {
		String[] args = {"--help"};
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
	
	@Test
	public void testPicocli() {
		String[] args = {"-d", "socialmedia", "noncommunicable", "-t", "junk", "grot"};
		AMIDictionary.main(args);
	}
	
	@Test
	public void testWikipediaTables() throws IOException {
		String dict = "protpredict";
		String[] args = {
				"--wikipedia", "https://en.wikipedia.org/wiki/List_of_protein_structure_prediction_software",
				"--namecol", "Name", 
				"--linkcol", "Link",
				"--dictionary", dict};
		AMIDictionary amiDictionary = new AMIDictionary();
		amiDictionary.runCommands(args);
		XMLUtil.debug(amiDictionary.getSimpleDictionary(), new File(DICTIONARY_DIR, dict+".html"), 1);
		
	}
	
	@Test
	public void testWikipediaTables2() throws IOException {
		String dict = "socialnetwork";
		String[] args = {
				"-w", "https://en.wikipedia.org/wiki/List_of_social_networking_websites",
				"-n", "Name", 
				"-l", "Name",
				"-d", "socialnetwork"};
		AMIDictionary amiDictionary = new AMIDictionary();
		amiDictionary.createAndSaveDictionary(dict, args);
	}
	
	@Test
	public void testWikipediaPage() throws IOException {
		String dict = "proteinStructure";
		String[] args = {
				"--wikipedia", "https://en.wikipedia.org/wiki/Protein_structure",
				"--urlref",
				"--dictionary", dict};
		AMIDictionary amiDictionary = new AMIDictionary();
		amiDictionary.createAndSaveDictionary(dict, args);
	}

	@Test
	public void testWikipediaNTDs() throws IOException {
		String dict = "ntd";
		String whoCol = ".*WHO.*CDC.*";
		String[] args = {
				"--wikipedia", "https://en.wikipedia.org/wiki/Neglected_tropical_diseases",
				"--namecol", whoCol,
				"--linkcol", whoCol,
//				"--urlcol", whoCol,
				"--dictionary", dict,
				"--directory", DICTIONARY_DIR.toString()
				};
		AMIDictionary amiDictionary = new AMIDictionary();
		amiDictionary.createAndSaveDictionary(dict, args);
	}
	
	@Test
	public void testWikipediaNTDPLOS() throws IOException {
		String dict = "ntd1";
		String searchCol = "PLOS.*";
		String[] args = {
				"--wikipedia", "https://en.wikipedia.org/wiki/Neglected_tropical_diseases",
				"--namecol", searchCol,
				"--linkcol", searchCol,
				"--dictionary", dict,
				"--format", "xml,json,html",
				"--directory", DICTIONARY_DIR.toString()
				};
		AMIDictionary amiDictionary = new AMIDictionary();
		amiDictionary.createAndSaveDictionary(dict, args);
	}
	
	@Test
	public void testTerms() {
		String dict = "crystalsystem";
		String[] args = {
			"--terms", "cubic,tetragonal,hexagonal,trigonal,orthorhombic,monoclinic,triclinic",
			"--dictionary", dict,
			"--directory", DICTIONARY_DIR.toString()};
		AMIDictionary amiDictionary = new AMIDictionary();
		amiDictionary.createAndSaveDictionary(dict, args);
	}
	

}
