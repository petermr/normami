package org.contentmine.ami.plugins;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.contentmine.CHESConstants;
import org.contentmine.ami.plugins.OccurrenceAnalyzer.OccurrenceType;
import org.contentmine.cproject.testutil.DataTablesToolAnalyzer;
import org.contentmine.cproject.util.CMineTestFixtures;
import org.contentmine.eucl.euclid.IntMatrix;
import org.contentmine.norma.NAConstants;
import org.junit.Test;

import com.google.common.collect.Multiset.Entry;

import junit.framework.Assert;

//@Ignore("long")
public class CommandProcessorIT {

	private static final Logger LOG = Logger.getLogger(CommandProcessorIT.class);
	static {
		LOG.setLevel(Level.DEBUG);
	}

	private final static String DICTIONARY_RESOURCE = CHESConstants.ORG_CM+"/"+NAConstants.AMI+"/plugins/dictionary";
	
	
	@Test
	public void testCommandLineSearch() throws IOException {
		String project = "zika10";
		File rawDir = new File(NAConstants.TEST_AMI_DIR, project);
		File projectDir = new File("target/tutorial1/"+project);
		CMineTestFixtures.cleanAndCopyDir(rawDir, projectDir);
		String cmd = ""
				// symbol/root
				+ " search(disease)"
				// resource
				+ " search("+DICTIONARY_RESOURCE+"/inn.xml)"
				// file
				+ " search(src/main/resources"+"/"+DICTIONARY_RESOURCE+"/tropicalVirus.xml)"
				// URL - will fail if not on net
//				+ " search(https://raw.githubusercontent.com/ContentMine/dictionaries/master/xml/epidemic.xml)"
	    ;
		String[] args = (projectDir+" "+cmd).split("\\s+");
		CommandProcessor.main(args);
		new DataTablesToolAnalyzer(new File(projectDir, "full.dataTables.html"))
				.assertRowCount(9)
				.assertColumnCount(1+3) // have to count row label columns
				.assertColumnHeadings("[articles, dic:disease, dic:inn, dic:tropicalVirus]")
				.assertCellValue(2, 3, "Zika x 2")
				;
	}

	@Test
	public void testCommandLineShort() throws IOException {
		String project = "zika10";
		File projectDir = new File("target/tutorial/"+project);
		File rawDir = new File(NAConstants.TEST_AMI_DIR, project);
		CMineTestFixtures.cleanAndCopyDir(rawDir, projectDir);
		String cmd = ""
		+ "word(frequencies)xpath:@count>20~w.stopwords:pmcstop.txt_stopwords.txt"
		+ " sequence(dnaprimer)"
		+ " gene(human) "
		+ " search(tropicalVirus)"
	    ;
		CommandProcessor.main((projectDir+" "+cmd).split("\\s+"));
	}

	@Test
	public void testCommandLineShort1() throws IOException {
		String project = "zika10";
		File projectDir = new File("target/tutorial/"+project);
		File rawDir = new File(NAConstants.TEST_AMI_DIR, project);
		CMineTestFixtures.cleanAndCopyDir(rawDir, projectDir);
		String cmd = ""
		// other ways of locating stopwords
		+ "word(frequencies)xpath:@count>20~w.stopwords:"+
		NAConstants.SLASH_AMI_RESOURCE+"/wordutil/pmcstop_https://raw.githubusercontent.com/ContentMine/ami/master/src/main/resources/org/contentmine/ami/wordutil/stopwords.txt"
		+ " sequence(dnaprimer)"
		+ " gene(human) "
		+ " search(tropicalVirus)"
	    ;
		CommandProcessor.main((projectDir+" "+cmd).split("\\s+"));
	}

	@Test
	public void testCommandLineShort2() throws IOException {
		String project = "zika10";
		File projectDir = new File("target/tutorial/"+project);
		File rawDir = new File(NAConstants.TEST_AMI_DIR, project);
		CMineTestFixtures.cleanAndCopyDir(rawDir, projectDir);
		String cmd = ""
		+ " word(20,pmcstop.txt,stopwords.txt)"
		+ " sequence(dnaprimer)"
		+ " gene(human) "
		+ " search(tropicalVirus)"
	    ;
		CommandProcessor.main((projectDir+" "+cmd).split("\\s+"));
	}

	/** preprocessor - may not be good idea.
	 * 
	 * @throws IOException
	 */
	@Test
	public void testCommandLinePreprocessor() throws IOException {
		String project = "zika10";
		File projectDir = new File("target/tutorial/"+project);
		File rawDir = new File(NAConstants.TEST_AMI_DIR, project);
		CMineTestFixtures.cleanAndCopyDir(rawDir, projectDir);
		String cmd = ""
		+ "w_fstop"
		+ " sq_d"
		+ " g_h"
		+ " s_tv"
		+ " s_inn"
		+ " s_nal"
		+ " s_phch"
		
	    ;
		CommandProcessor.main((projectDir+" "+cmd).split("\\s+"));
	}

	@Test
	//@Ignore
	// runs defaults
	public void testCommandLineShortEmpty() throws IOException {
		String project = "zika10";
		File projectDir = new File("target/tutorial/"+project);
		File rawDir = new File(NAConstants.TEST_AMI_DIR, project);
		CMineTestFixtures.cleanAndCopyDir(rawDir, projectDir);
		CommandProcessor.main(new String[]{projectDir.toString()});
	}

	@Test
//	@Ignore // LONG
	public void testCommandLine() throws IOException {
		String project = "zika10";
		File projectDir = new File("target/tutorial/"+project);
		File rawDir = new File(NAConstants.TEST_AMI_DIR, project);
		CMineTestFixtures.cleanAndCopyDir(rawDir, projectDir);
		String cmd = ""
		+ "word(frequencies)xpath:@count>20~w.stopwords:pmcstop.txt_stopwords.txt"
		+ " sequence(dnaprimer)"
		+ " gene(human) "
		+ " word(search)w.search:"+DICTIONARY_RESOURCE+"/tropicalVirus.xml"
	    ;
		CommandProcessor.main((projectDir+" "+cmd).split("\\s+"));
	}

	@Test
	// FIXME TEST
	public void  testHindawiSampleMini() throws IOException {
		File rawDir = new File(NAConstants.TEST_AMI_DIR, "hindawiepmc");
		File projectDir = new File("target/tutorial/hindawi/samplemini");
		CMineTestFixtures.cleanAndCopyDir(rawDir, projectDir);
		String cmd = ""
		+ " word(search)w.search:"+DICTIONARY_RESOURCE+"/disease.xml"
	    ;
		CommandProcessor.main((projectDir+" "+cmd).split("\\s+"));
	}
		
	@Test
	/** ca 100 files
	 * 
	 * works
	 * 
	 * @throws IOException
	 */
	// FIXME TEST
	public void  testHindawiEPMC() throws IOException {
		
		File rawDir = new File(NAConstants.TEST_AMI_DIR, "hindawiepmc");
		File projectDir = new File("target/tutorial/hindawi/epmc");
		CMineTestFixtures.cleanAndCopyDir(rawDir, projectDir);
		String cmd = "word(frequencies)xpath:@count>20~w.stopwords:pmcstop.txt_stopwords.txt"
		+ " sequence(dnaprimer)"
		+ " species(binomial)"
		+ " gene(human) "
		+ " word(search)w.search:"+DICTIONARY_RESOURCE+"/disease.xml"
		+ " word(search)w.search:"+DICTIONARY_RESOURCE+"/phytochemicals2.xml"
		+ " word(search)w.search:"+DICTIONARY_RESOURCE+"/inn.xml"
		+ " search(tropicalVirus)"

	    ;
		CommandProcessor.main((projectDir+" "+cmd).split("\\s+"));
			
	}
	
	@Test
	public void  testMarchantiaEPMCOld() throws IOException {
		File JUPYTER_DIR = new File("/Users/pm286/workspace/jupyter/demos/");
		LOG.debug(JUPYTER_DIR);
		File TARGET_JUPYTER_DIR = new File("target/jupyter/demos/");
		Assert.assertTrue("jupyter exists", JUPYTER_DIR.exists());
		String fileroot = "marchantia";
		File rawDir = new File(JUPYTER_DIR, fileroot);
		Assert.assertTrue("marchantia exists", rawDir.exists());
		File projectDir = new File(TARGET_JUPYTER_DIR, fileroot);
		CMineTestFixtures.cleanAndCopyDir(rawDir, projectDir);
		File auxin = new File(NAConstants.LOCAL_DICTIONARIES, "auxin.xml");
		File pectin = new File(NAConstants.LOCAL_DICTIONARIES, "pectin.xml");
		File plantDev = new File(NAConstants.LOCAL_DICTIONARIES, "plantDevelopment.xml");
		Assert.assertTrue("auxin dict", auxin.exists());
		Assert.assertTrue("pectin dict", pectin.exists());
		Assert.assertTrue("plant dict", plantDev.exists());
		String WORDSEARCH = " word(search)w.search:";
		String cmd = "word(frequencies)xpath:@count>20~w.stopwords:pmcstop.txt_stopwords.txt"
		+ " sequence(dnaprimer)"
		+ " species(binomial)"
		+ " gene(human) "
		+ WORDSEARCH + DICTIONARY_RESOURCE+"/disease.xml"
		+ WORDSEARCH + DICTIONARY_RESOURCE+"/phytochemicals2.xml"
		+ WORDSEARCH + DICTIONARY_RESOURCE+"/inn.xml"
		+ WORDSEARCH + auxin
		+ WORDSEARCH + plantDev
		+ WORDSEARCH + pectin
		+ " search(tropicalVirus)"

	    ;
		CommandProcessor.main((projectDir+" "+cmd).split("\\s+"));
			
	}
	
	@Test
	public void  testMarchantiaEPMC() throws IOException {
		
		boolean runme = false;
		File JUPYTER_DIR = new File("/Users/pm286/workspace/jupyter/demos/");
		File TARGET_JUPYTER_DIR = new File("target/jupyter/demos/");
		String fileroot = "marchantia";
		File rawDir = new File(JUPYTER_DIR, fileroot);
		File projectDir = new File(TARGET_JUPYTER_DIR, fileroot);
		if (runme) {
			CMineTestFixtures.cleanAndCopyDir(rawDir, projectDir);
		}
		String cmd = "word(frequencies)xpath:@count>20~w.stopwords:pmcstop.txt_stopwords.txt"
//		+ " sequence(dnaprimer)"
		+ " species(binomial)"
		+ " gene(human) "
//		+ " search(phytochemicals2)"
		+ " search(auxin)"
		+ " search(plantDevelopment)"
		+ " search(pectin)"
		+ " search(plantparts)"
		+ " search(synbio)"

	    ;
		if (runme) {
			CommandProcessor.main((projectDir+" "+cmd).split("\\s+"));
		}
		// generates files
		OccurrenceAnalyzer speciesAnalyzer = new OccurrenceAnalyzer();
		speciesAnalyzer.setProjectDir(projectDir)
			.setSearch(OccurrenceType.SPECIES)
			.setResultsRegex(".*/binomial/results\\.xml")
			.setCode("species")
			.setMaxCount(25);
		List<Entry<String>> binomialsByImportance = speciesAnalyzer.getEntriesSortedByImportance();
		LOG.debug(binomialsByImportance);
		
		OccurrenceAnalyzer geneAnalyzer = new OccurrenceAnalyzer();
		geneAnalyzer.setProjectDir(projectDir)
			.setSearch(OccurrenceType.STRING)
			.setResultsRegex(".*/gene/human/results\\.xml")
			.setCode("gene")
			.setMaxCount(30);
		List<Entry<String>> genesByImportance = geneAnalyzer.getEntriesSortedByImportance();
		LOG.debug(genesByImportance);

		OccurrenceAnalyzer auxinAnalyzer = new OccurrenceAnalyzer();
		auxinAnalyzer.setProjectDir(projectDir)
			.setSearch(OccurrenceType.STRING)
			.setResultsRegex(".*/auxin/results\\.xml")
			.setCode("auxin")
			.setMaxCount(25);
		List<Entry<String>> auxinsByImportance = auxinAnalyzer.getEntriesSortedByImportance();
		LOG.debug("AUX"+auxinsByImportance);

		CooccurrenceAnalyzer speciesGeneCoo = new CooccurrenceAnalyzer().setRowAnalyzer(speciesAnalyzer).setColAnalyzer(geneAnalyzer);
		IntMatrix coocurrenceMatrix = speciesGeneCoo.analyze();
		LOG.debug("CO SPG "+coocurrenceMatrix);
							
		CooccurrenceAnalyzer geneGeneCoo = new CooccurrenceAnalyzer().setRowAnalyzer(geneAnalyzer).setColAnalyzer(geneAnalyzer);
		coocurrenceMatrix = geneGeneCoo.analyze();
		LOG.debug("CO GG"+coocurrenceMatrix);
							
		CooccurrenceAnalyzer speciesSpeciesCoo = new CooccurrenceAnalyzer().setRowAnalyzer(speciesAnalyzer).setColAnalyzer(speciesAnalyzer);
		coocurrenceMatrix = speciesSpeciesCoo.analyze();
		LOG.debug("CO SPSP"+coocurrenceMatrix);
							
		CooccurrenceAnalyzer speciesAuxinCoo = new CooccurrenceAnalyzer().setRowAnalyzer(speciesAnalyzer).setColAnalyzer(auxinAnalyzer);
		coocurrenceMatrix = speciesAuxinCoo.analyze();
		LOG.debug("CO SPA"+coocurrenceMatrix);
							
		CooccurrenceAnalyzer geneAuxinCoo = new CooccurrenceAnalyzer().setRowAnalyzer(geneAnalyzer).setColAnalyzer(auxinAnalyzer);
		coocurrenceMatrix = geneAuxinCoo.analyze();
		LOG.debug("CO GA"+coocurrenceMatrix);
							
		CooccurrenceAnalyzer auxinAuxinCoo = new CooccurrenceAnalyzer().setRowAnalyzer(auxinAnalyzer).setColAnalyzer(auxinAnalyzer);
		coocurrenceMatrix = auxinAuxinCoo.analyze();
		LOG.debug("CO AA"+coocurrenceMatrix);
							
	}

}