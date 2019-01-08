package org.contentmine.ami.tool;

import java.io.File;
import java.io.IOException;

import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.contentmine.ami.tools.AMIDictionaryTool;
import org.contentmine.ami.tools.AMIDictionaryTool.DictionaryFileFormat;
import org.contentmine.ami.tools.AbstractAMITool;
import org.contentmine.norma.NAConstants;
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
		AMIDictionaryTool.main(args);
	}
	
	@Test
	public void testListSome() {
		String[] args = {
				"display", 
				"--directory", "src/main/resources/org/contentmine/ami/plugins/dictionary",
				"--dictionary",  "country", "crispr", "disease"
				};
		AMIDictionaryTool.main(args);
	}
	
	@Test
	public void testWikipediaTables() throws IOException {
		String dict = "protpredict";
		String[] args = {
			"create",
			"--input", "https://en.wikipedia.org/wiki/List_of_protein_structure_prediction_software",
			"--informat", "wikitable",
			"--namecol", "Name", 
			"--linkcol", "Name",
			"--urlcol", "Link",
			"--dictionary", dict
			};
		AbstractAMITool amiDictionary = new AMIDictionaryTool();
		amiDictionary.runCommands(args);
//		XMLUtil.debug(amiDictionary.getSimpleDictionary(), new File(DICTIONARY_DIR, dict+".html"), 1);
		
	}
	
	@Test
	public void testWikipediaTables2() throws IOException {
		String dict = "socialnetwork";
		String[] args = {
			"create",
			"--input", "https://en.wikipedia.org/wiki/List_of_social_networking_websites",
			"--informat", "wikitable",
			"--namecol", "Name", 
			"--linkcol", "Name",
			"--dictionary", "socialnetwork"};
		AbstractAMITool amiDictionary = new AMIDictionaryTool();
		amiDictionary.runCommands(args);
	}
	
	@Test
	public void testWikipediaPage() throws IOException {
		String dict = "proteinStructure";
		String[] args = {
			"create",
			"--input", "https://en.wikipedia.org/wiki/Protein_structure",
			"--informat", "wikipage",
//			"--urlref",
			"--dictionary", dict};
		new AMIDictionaryTool().runCommands(args);
	}

	@Test
	public void testWikipediaPageAedes() throws IOException {
		String dict = "aedes";
		String[] args = {
			"create",
			"--input", "https://en.wikipedia.org/wiki/Aedes_aegypti",
			"--informat", "wikipage",
			"--hreftext",  // currently needed to enforce use of names
			"--dictionary", dict,
			"--outformats", "html,xml",
			"--directory", DICTIONARY_DIR.toString()
			};
		// ami-dictionaries create -i https://en.wikipedia.org/wiki/Aedes_aegypti --informat wikipage --hreftext --dictionary aedes0 --outformats xml --directory ~/ContentMine/dictionary/
		new AMIDictionaryTool().runCommands(args);
	}

	@Test
	public void testWikipediaPageReindeer() throws IOException {
		String dict = "reindeer";
		String[] args = {
			"create",
			"--input", "https://en.wikipedia.org/wiki/Category:Reindeer", 
			"--informat", "wikipage",
			"--hreftext",  // currently needed to enforce use of names
			"--outformats", "html",
			"--dictionary", dict,
			"--directory", DICTIONARY_DIR.toString()
			};
		new AMIDictionaryTool().runCommands(args);
	}
	@Test
	public void testWikipediaPageMonoterpenes() throws IOException {
		String dict = "monoterpenes";
		String[] args = {
			"create",
			"--input", "https://en.wikipedia.org/wiki/Category:Monoterpenes", 
			"--informat", "wikipage",
			"--hreftext",  // currently needed to enforce use of names
			"--outformats", "html",
			"--dictionary", dict,
			"--directory", DICTIONARY_DIR.toString()
			};
		new AMIDictionaryTool().runCommands(args);
	}

	@Test
	public void testWikipediaNTDs() throws IOException {
		String dict = "ntd";
		String whoCol = ".*WHO.*CDC.*";
		String[] args = {
			"create",
			"--input", "https://en.wikipedia.org/wiki/Neglected_tropical_diseases",
			"--informat", "wikitable",
			"--namecol", whoCol,
			"--linkcol", whoCol,
//				"--urlcol", whoCol,
			"--base", "http://en.wikipedia.org",
			"--dictionary", dict,
			"--directory", DICTIONARY_DIR.toString()
			};
		new AMIDictionaryTool().runCommands(args);
	}
	
	@Test
	public void testWikipediaNTDPLOS() throws IOException {
		String dict = "ntd1";
		String searchCol = "PLOS.*";
		String[] args = {
			"create",
			"--input", "https://en.wikipedia.org/wiki/Neglected_tropical_diseases",
			"--informat", "wikitable",
			"--namecol", searchCol,
			"--linkcol", searchCol,
			"--dictionary", dict,
			"--format", "xml,json,html",
			"--directory", DICTIONARY_DIR.toString()
			};
		new AMIDictionaryTool().runCommands(args);
	}
	
	@Test
	public void testWikipediaOrthobunyavirus() throws IOException {
		String dict = "insectvectorshuman";
		String[] args = {
			"create",
			"--input", "https://en.wikipedia.org/wiki/Category:Insect_vectors_of_human_pathogens",
			"--informat", "wikicategory",
			"--dictionary", dict,
			"--outformats", "xml,json,html",
			"--directory", DICTIONARY_DIR.toString()
			};
		new AMIDictionaryTool().runCommands(args);
	}
	
	@Test
	public void testCreateFromTerms() {
		String dict = "crystalsystem";
		String[] args = {
			"create",
			"--terms", "cubic,tetragonal,hexagonal,trigonal,orthorhombic,monoclinic,triclinic",
			"--dictionary", dict,
			"--directory", DICTIONARY_DIR.toString()};
		new AMIDictionaryTool().runCommands(args);
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
		new AMIDictionaryTool().runCommands(args);
		
	}

//	@Test
//	public void testWikipediaConservation() throws IOException {
//		String dict = "conservationbio";
//		String[] args = {
//			"create",
//			"--hreftext",  // currently needed to enforce use of names
//			"--input", "https://en.wikipedia.org/wiki/Conservation_biology",
//			"--informat", "wikipage",
//			"--dictionary", dict,
//			"--directory", "target/dictionary",
//			"--outformats", DictionaryFileFormat.xml.toString(),
//			"--log4j", "org.contentmine.ami.lookups.WikipediaDictionary", "INFO",
//			"--log4j", "org.contentmine.norma.input.html.HtmlCleaner", "INFO",
//			};
//		new AMIDictionary().runCommands(args);
//	}
	
	@Test
	public void testWikipediaConservation1() throws IOException {
		String dict = "bio.conservation";
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
		new AMIDictionaryTool().runCommands(args);
	}

}
