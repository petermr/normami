package org.contentmine.ami.tools;

import java.io.File;
import java.io.IOException;

import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.contentmine.ami.tools.AMIDictionaryTool;
import org.contentmine.ami.tools.AMIDictionaryTool.DictionaryFileFormat;
import org.contentmine.ami.tools.AbstractAMITool;
import org.contentmine.norma.NAConstants;
import org.junit.Test;

import junit.framework.Assert;

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
		String dict = " chem.protpredict";
		String[] args = {
			"create",
			"--input", "https://en.wikipedia.org/wiki/List_of_protein_structure_prediction_software",
			"--informat", "wikitable",
			"--namecol", "Name", 
			"--linkcol", "Name",
			"--urlcol", "Link",
			"--outformats", "xml,json,html",
			"--dictionary", dict
			};
		AbstractAMITool amiDictionary = new AMIDictionaryTool();
		amiDictionary.runCommands(args);
//		XMLUtil.debug(amiDictionary.getSimpleDictionary(), new File(DICTIONARY_DIR, dict+".html"), 1);
		
	}
	
	@Test
	public void testWikipediaTables2() throws IOException {
		String dict = "soc.socialnetwork";
		String[] args = {
			"create",
			"--input", "https://en.wikipedia.org/wiki/List_of_social_networking_websites",
			"--informat", "wikitable",
			"--namecol", "Name", 
			"--linkcol", "Name",
			"--outformats", "xml,json,html",
			"--dictionary", "socialnetwork"};
		AbstractAMITool amiDictionary = new AMIDictionaryTool();
		amiDictionary.runCommands(args);
	}
	
	@Test
	public void testWikipediaPage() throws IOException {
		String dict = "chem.proteinStructure";
		String[] args = {
			"create",
			"--input", "https://en.wikipedia.org/wiki/Protein_structure",
			"--informat", "wikipage",
			"--hreftext",
//			"--urlref",
			"--outformats", "xml,json,html",
			"--dictionary", dict};
		new AMIDictionaryTool().runCommands(args);
	}

	@Test
	// LONG
	public void testWikipediaPageAedesIT() throws IOException {
		String dict = "animal.aedes";
		String[] args = {
			"create",
			"--input", "https://en.wikipedia.org/wiki/Aedes_aegypti",
			"--informat", "wikipage",
			"--hreftext",  // currently needed to enforce use of names
			"--dictionary", dict,
			"--outformats", "xml,json,html",
			"--directory", DICTIONARY_DIR.toString()
			};
		// ami-dictionaries create -i https://en.wikipedia.org/wiki/Aedes_aegypti --informat wikipage --hreftext --dictionary aedes0 --outformats xml --directory ~/ContentMine/dictionary/
		new AMIDictionaryTool().runCommands(args);
	}

	@Test
	public void testWikipediaPageReindeerIT() throws IOException {
		String dict = "animal.reindeer";
		String[] args = {
			"create",
			"--input", "https://en.wikipedia.org/wiki/Category:Reindeer", 
			"--informat", "wikipage",
			"--hreftext",  // currently needed to enforce use of names
			"--outformats", "xml,json,html",
			"--dictionary", dict,
			"--directory", DICTIONARY_DIR.toString()
			};
		new AMIDictionaryTool().runCommands(args);
	}
	@Test
	// LONG
	public void testWikipediaPageMonoterpenesIT() throws IOException {
		String dict = "chem.monoterpenes";
		String[] args = {
			"create",
			"--input", "https://en.wikipedia.org/wiki/Category:Monoterpenes", 
			"--informat", "wikipage",
			"--hreftext",  // currently needed to enforce use of names
			"--outformats", "xml,json,html",
			"--dictionary", dict,
			"--directory", DICTIONARY_DIR.toString()
			};
		new AMIDictionaryTool().runCommands(args);
	}

	@Test
	public void testWikipediaNTDs() throws IOException {
		String dict = "med.ntd";
		String whoCol = ".*WHO.*CDC.*";
		String[] args = {
			"create",
			"--input", "https://en.wikipedia.org/wiki/Neglected_tropical_diseases",
			"--informat", "wikitable",
			"--namecol", whoCol,
			"--linkcol", whoCol,
//				"--urlcol", whoCol,
			"--base", "http://en.wikipedia.org",
			"--outformats", "xml,json,html",
			"--dictionary", dict,
			"--directory", DICTIONARY_DIR.toString()
			};
		new AMIDictionaryTool().runCommands(args);
	}
	
	@Test
	public void testWikipediaNTDPLOS() throws IOException {
		String dict = "med.ntd1";
		String searchCol = "PLOS.*";
		String[] args = {
			"create",
			"--input", "https://en.wikipedia.org/wiki/Neglected_tropical_diseases",
			"--informat", "wikitable",
			"--namecol", searchCol,
			"--linkcol", searchCol,
			"--dictionary", dict,
			"--outformats", "xml,json,html",
			"--directory", DICTIONARY_DIR.toString()
			};
		new AMIDictionaryTool().runCommands(args);
	}
	
	@Test
	public void testWikipediaHumanInsectVectorsIT() throws IOException {
		String dict = "animal.insectvectorshuman";
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
	public void testWikipediaOcimumIT() throws IOException {
		String dict = "plants.ocimumten";
		String[] args = {
			"create",
			"--input", "https://en.wikipedia.org/wiki/Ocimum_tenuiflorum",
			"--informat", "wikipage",
			"--hreftext",  // currently needed to enforce use of names
			"--dictionary", dict,
			"--outformats", "xml,json,html",
			"--directory", DICTIONARY_DIR.toString()
			};
		new AMIDictionaryTool().runCommands(args);
	}
	
	@Test
	public void testCreateFromTerms() {
		String dict = "phys.crystalsystem";
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
		
		String dict = "animal.edgemammals";
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

	@Test
	public void testWikipediaIndianSpice() throws IOException {
		String dict = "plants.spice";
		String searchCol = "Standard English";
		String[] args = {
			"create",
			"--input", "https://en.wikipedia.org/wiki/List_of_Indian_spices",
			"--informat", "wikitable",
			"--namecol", searchCol,
//			"--linkcol", searchCol,
			"--dictionary", dict,
			"--outformats", "xml,json,html",
			"--directory", DICTIONARY_DIR.toString()
			};
		new AMIDictionaryTool().runCommands(args);
	}
	
	@Test
	public void testWikipediaPlantVirus() throws IOException {
		String dict = "plants.virus";
		String[] args = {
			"create",
			"--hreftext",  // currently needed to enforce use of names
			"--input", "https://en.wikipedia.org/wiki/Plant_virus",
			"--informat", "wikipage",
			"--dictionary", dict,
			"--outformats", "html,json,xml",
			"--wikilinks"
			};
		new AMIDictionaryTool().runCommands(args);
	}

	@Test
	public void testCreateVirusesFromTerms() {
		String dict = "plants.viruses";
		String[] args = {
			"create",
			"--terms", "Cucumovirus,Tymovirus,Bromovirus,Potexvirus,Ilarvirus,Nepovirus,Carmovirus,Potyvirus,Potyvirus,"
					+ "Badnavirus,Tymovirus,Tobravirus,Closterovirus,Necrovirus,TNsatV-like satellite,Nepovirus,Nepovirus,"
					+ "Nepovirus,Ilarvirus,Comovirus,Dianthovirus,Carlavirus,Sobemovirus,Caulimovirus,Enamovirus,Cytorhabdovirus,"
					+ "Ophiovirus,Cocadviroid,Aureusvirus,Ilarvirus,Bromovirus,Tungrovirus,Waikavirus,Sobemovirus,Alphacryptovirus,"
					+ "Potyvirus,Potexvirus,Tospovirus,Cavemovirus,Potyvirus,Carlavirus,Mastrevirus,Petuvirus,Potexvirus,Carmovirus,"
					+ "Carmovirus,Nucleorhabdovirus,Ampelovirus",
			"--dictionary", dict,
			"--directory", DICTIONARY_DIR.toString(),
			"--wikilinks wikidata"
};
		new AMIDictionaryTool().runCommands(args);
	}
	
	@Test
	public void testTranslateJSONtoXML() {
		String[] args = {
			"translate",
			"--directory", "src/test/resources/org/contentmine/ami/dictionary",
			"--dictionary", "alliaceae.json", "buxales.json",
			"--outformats", "xml",
		};
		new AMIDictionaryTool().runCommands(args);
	}
	
	@Test
	public void testTranslateJSONtoXMLAbsolute() {
		String[] args = {
			"translate",
//			"--directory", "src/test/resources/org/contentmine/ami/dictionary",
			"--dictionary", "src/test/resources/org/contentmine/ami/dictionary/alliaceae.json", 
			                "src/test/resources/org/contentmine/ami/dictionary/buxales.json",
			"--outformats", "xml",
		};
		new AMIDictionaryTool().runCommands(args);
	}
	
	@Test
	public void testTranslateJSONtoXMLAbsoluteWikidata() {
		String[] args = {
			"translate",
			"--dictionary", "src/test/resources/org/contentmine/ami/dictionary/alliaceae.json", 
			                "src/test/resources/org/contentmine/ami/dictionary/buxales.json",
			"--outformats", "xml",
			"--wikilinks", "wikidata", "wikipedia",
		};
		new AMIDictionaryTool().runCommands(args);
	}
	
	@Test
	public void testWikidataLookup() {
		String dict = "plants.misc";
		String[] args = {
			"create",
			"--terms", "Buxus sempervirens",
			"--dictionary", dict,
			"--directory", DICTIONARY_DIR.toString(),
			"--wikilinks wikidata"
			};
		new AMIDictionaryTool().runCommands(args);
	}

	@Test
	public void testAmbarishBugWikipediaDictionaryCreation() {
		String dict = "plants.misc";
		String[] args = {
			"create",
			"--terms", "Buxus sempervirens",
			"--dictionary", dict,
			"--directory", DICTIONARY_DIR.toString(),
			"--wikilinks wikidata"
			};
		new AMIDictionaryTool().runCommands(args);
	}

	@Test
	public void testListOfRiceVarieties() {
		String args = "create  --input https://en.wikipedia.org/wiki/List_of_rice_varieties --informat wikipage --hreftext --dictionary ricevarieties --outformats xml,json,html";
		new AMIDictionaryTool().runCommands(args);
	}
	
	@Test
	public void testWikipediaPageOcimum() {
		String args = "create"
				+ " --informat wikipage"
				+ " --hreftext"
				+ " --input https://en.wikipedia.org/wiki/Ocimum_tenuiflorum"
				+ " --dictionary otenuiflorum"
				+ " --directory mydictionaries"
				+ " --outformats xml,html";
		new AMIDictionaryTool().runCommands(args);

	}

	
}
