package org.contentmine.ami;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.apache.commons.io.FilenameUtils;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.contentmine.ami.plugins.CommandProcessor;
import org.contentmine.ami.plugins.EntityAnalyzer;
import org.contentmine.cproject.files.CProject;
import org.contentmine.norma.NAConstants;
import org.contentmine.norma.Norma;

/** runs operations on CProject , mainly transformations. 
 * Might evolve to a set of default operations as an alternative to commandline
 * 
 * @author pm286
 *
 */
public class AMIProcessor {
	private static final Logger LOG = Logger.getLogger(AMIProcessor.class);
	static {
		LOG.setLevel(Level.DEBUG);
	}
	public static final String SEARCH = "search";

	private CProject cProject;

	private boolean skipConvertPDFs;
	
	private AMIProcessor() {
		
	}
	
	public static AMIProcessor createProcessor(CProject cProject) {
		AMIProcessor integrationProcessor = null;
		if (cProject != null) {
			integrationProcessor = new AMIProcessor();
			integrationProcessor.cProject = cProject;
		}
		return integrationProcessor;
	}
	
	public static AMIProcessor createProcessor(String projectName) {
		
		File userDir = new File(System.getProperty("user.dir"));
		LOG.debug("project name: "+projectName+" "+userDir);
		File projectDir = new File(userDir, projectName);
		if (!projectDir.exists() || !projectDir.isDirectory()) {
			System.err.println("Project does not exist or is not directory:");
			System.err.println("    "+projectDir);
		}
		return createProcessor(projectDir);
	}

	public static AMIProcessor createProcessor(File projectDir) {
		CProject cProject = new CProject(projectDir);

		AMIProcessor integrationProcessor = null;
		if (cProject != null) {
			integrationProcessor = new AMIProcessor();
			integrationProcessor.cProject = cProject;
		}
		return integrationProcessor;
	}
	
	public void defaultAnalyzeCooccurrence(List<String> facets) {
		EntityAnalyzer entityAnalyzer = EntityAnalyzer.createEntityAnalyzer(cProject.getDirectory());
	
		for (String facet : facets) {
			entityAnalyzer.createAndAddOccurrenceAnalyzer(facet).setMaxCount(25);
		}
		
		entityAnalyzer.writeCSVFiles();
		entityAnalyzer.setWriteCSV(true);
		entityAnalyzer.createAllCooccurrences();
	}

	public void runSearches(List<String> facetList) {
		String cmd = "word(frequencies)xpath:@count>20~w.stopwords:pmcstop.txt_stopwords.txt";
		cmd = addSearches(facetList, cmd);
		try {
			
			String argString = /*cProject.getDirectory()+" "+*/cmd;
//			CommandProcessor.main(args);
			CommandProcessor commandProcessor = new CommandProcessor(cProject.getDirectory());
			commandProcessor.processCommands(argString);
			commandProcessor.createDataTables();

		} catch (IOException e) {
			throw new RuntimeException("Cannot run command: "+cmd, e);
		}
	}

	private String addSearches(List<String> facetList, String cmd) {
		for (String facet : facetList) {
			if (facet.equals("gene")) {
				cmd += " gene(human)";
			} else if (facet.equals("species")) {
				cmd += " species(binomial)";
			} else {
				cmd += " "+SEARCH + "("+facet+")";
			}
		}
		return cmd;
	}

	public void makeProject() {
		cProject.makeProject();
	}

	public void convertPDFOutputSVGFilesImageFiles() {
		cProject.convertPDFOutputSVGFilesImageFiles();
	}

	public void convertPDFSVGandWriteHtml() {
		cProject.convertPDFSVGandWriteHtml();
	}

	public void convertJATSXMLandWriteHtml() {
		Norma norma = new Norma();
		String args = "-i fulltext.xml -o scholarly.html --transform nlm2html --project "+cProject.getDirectory();
		norma.run(args);
	}

	public void convertPDFsToProjectAndRunCooccurrence(List<String> facetList) {
		makeProject();
		if (!skipConvertPDFs) {
			convertPDFOutputSVGFilesImageFiles();
		}
		convertPDFSVGandWriteHtml();
		runSearchesAndCooccurrence(facetList);
	}

	public void runSearchesAndCooccurrence(List<String> facetList) {
		runSearches(facetList);
		defaultAnalyzeCooccurrence(facetList);
	}

	public void setSkipConvertPDFs(boolean skip) {
		this.skipConvertPDFs = skip;
	}

	public void setIncludeCTrees(String... treeNames) {
		if (cProject != null) {
			cProject.setIncludeTreeList(Arrays.asList(treeNames));
		}
//		LOG.debug(cTreeList);
	}


	public static void main(String[] args) {
		List<String> argList = Arrays.asList(args);
		if (argList.size() == 0) {
			help();
			return;
		} else {
			String projectName = argList.get(0);
			List<String> facetList = argList.subList(1, argList.size());
			LOG.info("facets "+facetList);
			AMIProcessor amiProcessor = AMIProcessor.createProcessor(projectName);
			if ("abc".length() == 2) amiProcessor.convertJATSXMLandWriteHtml();
			amiProcessor.runSearchesAndCooccurrence(facetList);
		}
	}

	private static void help() {
		System.err.println("amiProcessor <projectDirectory> [dictionary [dictionary]]");
		System.err.println("    projectDirectory can be full name or relative to currentDir");
		System.err.println("\nlist of dictionaries taken from AMI dictionary list:");
		listDictionaries();
		
	}

	private static void listDictionaries() {
		File dictionaryHead = new File(NAConstants.MAIN_AMI_DIR, "plugins/dictionary");
		List<File> files = Arrays.asList(dictionaryHead.listFiles());
		Collections.sort(files);
		int count = 0;
		int perLine = 5;
		System.err.print("\n    ");
		for (File file : files) {
			String filename = file.toString();
			if ("xml".equals(FilenameUtils.getExtension(filename))) {
				String name = FilenameUtils.getBaseName(file.toString());
				System.err.print((name + "                     ").substring(0, 20));
				if (count++ %perLine == perLine - 1) System.err.print("\n    ");
			}
		}
		System.err.println("\nalso:");
		System.err.println("    gene     ");
		System.err.println("    species     ");
	}

}
