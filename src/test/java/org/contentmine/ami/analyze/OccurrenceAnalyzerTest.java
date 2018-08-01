package org.contentmine.ami.analyze;

import java.io.File;
import java.io.IOException;
import java.util.List;

import org.apache.commons.io.FileUtils;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.contentmine.ami.AMIFixtures;
import org.contentmine.ami.plugins.CommandProcessor;
import org.contentmine.ami.plugins.EntityAnalyzer;
import org.contentmine.ami.plugins.OccurrenceAnalyzer;
import org.contentmine.ami.plugins.OccurrenceAnalyzer.OccurrenceType;
import org.contentmine.ami.plugins.OccurrenceAnalyzer.SubType;
import org.contentmine.cproject.util.CMineTestFixtures;
import org.contentmine.norma.Norma;
import org.junit.Assert;
import org.junit.Test;

import com.google.common.collect.Multiset.Entry;

public class OccurrenceAnalyzerTest {
	private static final Logger LOG = Logger.getLogger(OccurrenceAnalyzerTest.class);
	static {
		LOG.setLevel(Level.DEBUG);
	}

	@Test
	public void  testMarchantiaEPMC() throws IOException {
		
		boolean runme = true;
		File JUPYTER_DIR = new File("/Users/pm286/workspace/jupyter/demos/");
		File TARGET_JUPYTER_DIR = new File("target/jupyter/demos/");
		String fileroot = "marchantia";
		File rawDir = new File(JUPYTER_DIR, fileroot);
		File projectDir = new File(TARGET_JUPYTER_DIR, fileroot);
		if (runme) {
			CMineTestFixtures.cleanAndCopyDir(rawDir, projectDir);
		}
		String cmd = "word(frequencies)xpath:@count>20~w.stopwords:pmcstop.txt_stopwords.txt"
		+ " species(binomial)"
		+ " gene(human) "
		+ " search(auxin)"
		+ " search(plantDevelopment)"
		+ " search(pectin)"
		+ " search(plantparts)"
		+ " search(synbio)"

	    ;
		if (runme) {
			CommandProcessor.main((projectDir+" "+cmd).split("\\s+"));
		}
		
		EntityAnalyzer entityAnalyzer = EntityAnalyzer.createEntityAnalyzer(projectDir);
		
		entityAnalyzer.createAndAddOccurrenceAnalyzer(OccurrenceType.BINOMIAL).setMaxCount(25);
		entityAnalyzer.createAndAddOccurrenceAnalyzer(OccurrenceType.GENE, SubType.HUMAN).setMaxCount(30);
		entityAnalyzer.createAndAddOccurrenceAnalyzer("auxin");
		entityAnalyzer.writeCSVFiles();
		
		entityAnalyzer.setWriteCSV(true);
		entityAnalyzer.createAllCooccurrences();

	}

	@Test
	public void  testCUCSmall() throws IOException {

		boolean runme = false;
		String fileroot = "cucSmall";
		File rawDir = new File(AMIFixtures.TEST_PLANT_DIR, fileroot);
		File projectDir = new File(AMIFixtures.TARGET_PLANT_DIR, fileroot);
		if (runme) {
			CMineTestFixtures.cleanAndCopyDir(rawDir, projectDir);
			LOG.debug("copied raw");
		}
		if (runme) {
			String args = "-i fulltext.xml -o scholarly.html --transform nlm2html --project "+projectDir;
			new Norma().run(args);
		}
		String cmd = "word(frequencies)xpath:@count>20~w.stopwords:pmcstop.txt_stopwords.txt"
		+ " species(binomial)"
		+ " gene(human) "
		+ " search(auxin)"
		+ " search(plantDevelopment)"
		+ " search(pectin)"
		+ " search(plantparts)"
		+ " search(synbio)"
		

	    ;
		if (runme) {
			CommandProcessor.main((projectDir+" "+cmd).split("\\s+"));
		}
		EntityAnalyzer entityAnalyzer = EntityAnalyzer.createEntityAnalyzer(projectDir);
				
		OccurrenceAnalyzer speciesAnalyzer = entityAnalyzer.createAndAddOccurrenceAnalyzer(OccurrenceType.BINOMIAL)
				.setMaxCount(20);
		File binomialCsvFile = speciesAnalyzer.getCSVFileName();
		FileUtils.deleteQuietly(binomialCsvFile);
		speciesAnalyzer.writeCSV();
		Assert.assertTrue(binomialCsvFile+" exists", binomialCsvFile.exists());

		OccurrenceAnalyzer geneAnalyzer = entityAnalyzer.createAndAddOccurrenceAnalyzer(OccurrenceType.GENE, SubType.HUMAN)
				.setMaxCount(12);
		File geneCsvFile = geneAnalyzer.getCSVFileName();
		FileUtils.deleteQuietly(geneCsvFile);
		geneAnalyzer.writeCSV();
		Assert.assertTrue(geneCsvFile+" exists", geneCsvFile.exists());

		OccurrenceAnalyzer auxinAnalyzer = entityAnalyzer.createAndAddOccurrenceAnalyzer("auxin").setMaxCount(6);
		File auxinCsvFile = auxinAnalyzer.getCSVFileName();
		FileUtils.deleteQuietly(auxinCsvFile);
		auxinAnalyzer.writeCSV();
		Assert.assertTrue(auxinCsvFile+" exists", auxinCsvFile.exists());

		// ====================
		
		
		entityAnalyzer.setWriteCSV(true);
		entityAnalyzer.createAllCooccurrences();
							
	}
	
	@Test
	public void  testCUCEPMC() throws IOException {
		String fileroot = "cuc";
		boolean copyToTarget = false;
		File inputDir = AMIFixtures.TEST_PLANT_DIR;
		File rawDir = new File(inputDir, fileroot);
		File outputDir =  AMIFixtures.TARGET_PLANT_DIR;
		File projectDir = new File(outputDir, fileroot);
		if (copyToTarget) {
			CMineTestFixtures.cleanAndCopyDir(rawDir, projectDir);
			LOG.debug("copied raw");
		}

		EntityAnalyzer entityAnalyzer = EntityAnalyzer.createEntityAnalyzer(projectDir);
		entityAnalyzer.setWriteCSV(true);
		entityAnalyzer.setForceRun(false);
		entityAnalyzer.analyzePlantCoocurrences();
	}
	
	@Test
	public void  testObesity() throws IOException {
		String fileroot = "obesity";
		boolean copyToTarget = true;
		boolean forceRun = true;
		File inputDir = new File(new File(AMIFixtures.TEST_PROJECTS_DIR, fileroot), fileroot); // nested
		File outputDir =  new File(AMIFixtures.TARGET_PROJECTS_DIR, fileroot);
		if (copyToTarget) {
			CMineTestFixtures.cleanAndCopyDir(inputDir, outputDir);
			LOG.debug("copied raw");
		}

		EntityAnalyzer entityAnalyzer = EntityAnalyzer.createEntityAnalyzer(outputDir);
		entityAnalyzer.setWriteCSV(true);
		entityAnalyzer.setForceRun(forceRun);
		entityAnalyzer.analyzeObesityCoocurrences();
	}
	@Test
	public void  testObesityLarge() throws IOException {
		String fileroot = "obesityLarge";
		boolean copyToTarget = true;
		boolean forceRun = true;
		File inputDir = new File(new File(AMIFixtures.TEST_PROJECTS_DIR, "obesity"), fileroot); // nested
		File outputDir =  new File(AMIFixtures.TARGET_PROJECTS_DIR, fileroot);
		if (copyToTarget) {
			CMineTestFixtures.cleanAndCopyDir(inputDir, outputDir);
			LOG.debug("copied raw");
		}

		EntityAnalyzer entityAnalyzer = EntityAnalyzer.createEntityAnalyzer(outputDir);
		entityAnalyzer.setWriteCSV(true);
		entityAnalyzer.setForceRun(forceRun);
		entityAnalyzer.analyzeObesityCoocurrences();
	}
	@Test
	public void  testZika() throws IOException {
		String fileroot = "zika2018";
		boolean copyToTarget = false;
		File inputDir = new File(AMIFixtures.TEST_PROJECTS_DIR, fileroot);
		File outputDir =  new File(AMIFixtures.TARGET_PROJECTS_DIR, fileroot);
		if (copyToTarget) {
			CMineTestFixtures.cleanAndCopyDir(inputDir, outputDir);
			LOG.debug("copied raw");
		}

		EntityAnalyzer entityAnalyzer = EntityAnalyzer.createEntityAnalyzer(outputDir);
		entityAnalyzer.setWriteCSV(true);
		entityAnalyzer.setForceRun(false);
		entityAnalyzer.analyzeMosquitoCoocurrences();
	}
	@Test
	public void  testZikaSmall() throws IOException {
		String fileroot = "zikaSmall";
		boolean copyToTarget = true;
		File inputDir = new File(AMIFixtures.TEST_PROJECTS_DIR, fileroot);
		File outputDir =  new File(AMIFixtures.TARGET_PROJECTS_DIR, fileroot);
		if (copyToTarget) {
			CMineTestFixtures.cleanAndCopyDir(inputDir, outputDir);
			LOG.debug("copied raw");
		}

		EntityAnalyzer entityAnalyzer = EntityAnalyzer.createEntityAnalyzer(outputDir);
		entityAnalyzer.setWriteCSV(true);
		entityAnalyzer.setForceRun(true);
		entityAnalyzer.analyzeMosquitoCoocurrences();
	}
}
