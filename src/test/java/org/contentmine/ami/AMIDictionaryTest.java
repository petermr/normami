package org.contentmine.ami;

import java.io.File;
import java.io.IOException;

import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.contentmine.ami.AMIDictionary.DictionaryFileFormat;
import org.contentmine.eucl.xml.XMLUtil;
import org.contentmine.norma.NAConstants;
import org.junit.Ignore;
import org.junit.Test;

import picocli.CommandLine;

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
	@Ignore // old style
	public void testListSome() {
		String[] args = {"country", "crispr", "disease"};
		AMIDictionary.main(args);
	}
	
	@Test
	@Ignore // old style
	public void testListAll() {
		String[] args = {"LIST"};
		AMIDictionary.main(args);
	}
	
	@Test
	@Ignore // old style
	public void testListFull() {
		String[] args = {"FULL", "country"};
		AMIDictionary.main(args);
	}
	
	@Test
	@Ignore // old style
	public void testListFull2() {
		String[] args = {"FULL", "socialmedia", "noncommunicable"};
		AMIDictionary.main(args);
	}
	
	@Test
	/** argument tester only
	 * outputs help strings to console
	 */
	public void testPicocliEmpty() {
		String[] args = {"--wombx 123"};
		AMIDictionary.main(args);
	}
	
	@Test
	/** argument tester only
	 * 
	 */
	public void testPicocli() {
		String[] args = {
			"-d", "socialmedia",
			"-t", "junk",
			"--wombatx", "129"
			};
		AMIDictionary.main(args);
	}
	
	@Test
	public void testWikipediaTables() throws IOException {
		String dict = "protpredict";
		String[] args = {
			"--input", "https://en.wikipedia.org/wiki/List_of_protein_structure_prediction_software",
			"--informat", "wikitable",
			"--namecol", "Name", 
			"--linkcol", "Name",
			"--urlcol", "Link",
			"--dictionary", dict
			};
		AMIDictionary amiDictionary = new AMIDictionary();
		amiDictionary.runCommands(args);
		XMLUtil.debug(amiDictionary.getSimpleDictionary(), new File(DICTIONARY_DIR, dict+".html"), 1);
		
	}
	
	@Test
	public void testWikipediaTables2() throws IOException {
		String dict = "socialnetwork";
		String[] args = {
			"--input", "https://en.wikipedia.org/wiki/List_of_social_networking_websites",
			"--informat", "wikitable",
			"--namecol", "Name", 
			"--linkcol", "Name",
			"--dictionary", "socialnetwork"};
		AMIDictionary amiDictionary = new AMIDictionary();
		amiDictionary.runCommands(args);
	}
	
	@Test
	public void testWikipediaPage() throws IOException {
		String dict = "proteinStructure";
		String[] args = {
			"--input", "https://en.wikipedia.org/wiki/Protein_structure",
			"--informat", "wikitable",
			"--urlref",
			"--dictionary", dict};
		new AMIDictionary().runCommands(args);
	}

	@Test
	public void testWikipediaNTDs() throws IOException {
		String dict = "ntd";
		String whoCol = ".*WHO.*CDC.*";
		String[] args = {
			"--input", "https://en.wikipedia.org/wiki/Neglected_tropical_diseases",
			"--informat", "wikitable",
			"--namecol", whoCol,
			"--linkcol", whoCol,
//				"--urlcol", whoCol,
			"--base", "http://en.wikipedia.org",
			"--dictionary", dict,
			"--directory", DICTIONARY_DIR.toString()
			};
		new AMIDictionary().runCommands(args);
	}
	
	@Test
	public void testWikipediaNTDPLOS() throws IOException {
		String dict = "ntd1";
		String searchCol = "PLOS.*";
		String[] args = {
			"--input", "https://en.wikipedia.org/wiki/Neglected_tropical_diseases",
			"--informat", "wikitable",
			"--namecol", searchCol,
			"--linkcol", searchCol,
			"--dictionary", dict,
			"--format", "xml,json,html",
			"--directory", DICTIONARY_DIR.toString()
			};
		new AMIDictionary().runCommands(args);
	}
	
	@Test
	public void testCreateFromTerms() {
		String dict = "crystalsystem";
		String[] args = {
			"--terms", "cubic,tetragonal,hexagonal,trigonal,orthorhombic,monoclinic,triclinic",
			"--dictionary", dict,
			"--directory", DICTIONARY_DIR.toString()};
		new AMIDictionary().runCommands(args);
	}
	
	@Test
	public void testReadMammalsCSV() throws IOException {
		File mammalsCSV = new File(NAConstants.PLUGINS_DICTIONARY_DIR, "EDGEMammalsSmall.csv");
		
		String dict = "edgemammals";
		String[] args = {
			"create",
			"--input", mammalsCSV.getAbsolutePath(),
			"--informat", "csv",
			"--termcol", "Species",
			"--namecol", "Common names",
			"--hrefcols", "IUCN Red List link",
			"--datacols", "ED Score,GE Score",
			"--dictionary", dict,
			"--outformats", "xml,html,json",
			"--directory", DICTIONARY_DIR.toString(),
			"--booleanquery"
			};
		new AMIDictionary().runCommands(args);
		
	}

	@Test
	public void testWikipediaConservation() throws IOException {
		String dict = "conservationbio";
		String[] args = {
			"create",
			"--hreftext",  // currently needed to enforce use of names
			"--input", "https://en.wikipedia.org/wiki/Conservation_biology",
			"--informat", "wikipage",
			"--dictionary", dict,
			"--directory", "target/dictionary",
			"--outformats", DictionaryFileFormat.xml.toString(),
			"--log4j", "org.contentmine.ami.lookups.WikipediaDictionary", "INFO",
			"--log4j", "org.contentmine.norma.input.html.HtmlCleaner", "INFO",
			};
		new AMIDictionary().runCommands(args);
	}

	@Test
	public void testCommandMixin() {
		
		MyCommand zip = new MyCommand();
		CommandLine commandLine = new CommandLine(zip);
		ReusableOptions mixin = new ReusableOptions();
		commandLine.addMixin("myMixin", mixin);
		commandLine.parse("-vv", "--wombat", "361");

		// the options defined in ReusableOptions have been added to the zip command
		assert zip.myMixin.verbosityx.length == 3;
		System.err.println("VVV "+zip.myMixin.verbosityx.length);
		System.out.println("WOM "+zip.myMixin.vombatus);

	}

}
