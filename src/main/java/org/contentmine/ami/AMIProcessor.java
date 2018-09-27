package org.contentmine.ami;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.apache.commons.io.FilenameUtils;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.contentmine.ami.plugins.CommandProcessor;
import org.contentmine.ami.plugins.EntityAnalyzer;
import org.contentmine.cproject.files.CProject;
import org.contentmine.cproject.files.CTree;
import org.contentmine.cproject.util.CMineGlobber;
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
	public static final String HELP = "help";

	private CProject cProject;
	private boolean skipConvertPDFs;
	
	private AMIProcessor() {
		
	}
	
	private static AMIProcessor createProcessor(CProject cProject) {
		AMIProcessor amiProcessor = null;
		if (cProject != null) {
			amiProcessor = new AMIProcessor();
			amiProcessor.cProject = cProject;
		}
		return amiProcessor;
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

		AMIProcessor amiProcessor = null;
		if (cProject != null) {
			amiProcessor = new AMIProcessor();
			amiProcessor.cProject = cProject;
		}
		return amiProcessor;
	}
	
	public void defaultAnalyzeCooccurrence(List<String> facets) {
		EntityAnalyzer entityAnalyzer = EntityAnalyzer.createEntityAnalyzer(cProject.getDirectory());
	
		for (String facet : facets) {
			entityAnalyzer.createAndAddOccurrenceAnalyzer(facet).setMaxCount(25);
		}
		
		entityAnalyzer.writeCSVFiles();
		entityAnalyzer.setWriteCSV(true);
		entityAnalyzer.setWriteSVG(true);
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
		if (cProject.hasScholarlyHTML(0.6)) {
			LOG.debug("ScholarlyHTML exists, skipping makeProject");
			return;
		}
		cProject.makeProject(CTree.PDF);
		cProject.makeProject(CTree.XML);
		cProject.makeProject(CTree.HTML);
// flush old CProject as CTreeList needs to be reset		
		cProject = new CProject(cProject.getDirectory());
	}

	public void convertPDFOutputSVGFilesImageFiles() {
		cProject.setCTreelist(null);
		cProject.convertPDFOutputSVGFilesImageFiles();
		return;
	}

	public void convertPSVGandWriteHtml() {
		cProject.convertPSVGandWriteHtml();
	}

	public void convertJATSXMLandWriteHtml() {
		Norma norma = new Norma();
		String args = "-i fulltext.xml -o scholarly.html --transform nlm2html --project "+cProject.getDirectory();
		norma.run(args);
	}

	public void convertHTMLsToProjectAndRunCooccurrence(List<String> facetList) {
		makeProject(/*CTree.HTML*/);
		convertRawHTMLToScholarly();
		runSearchesAndCooccurrence(facetList);
	}

	private void convertRawHTMLToScholarly() {
		Norma norma = new Norma();
		String args = " -i fulltext.html -o scholarly.html --html jsoup --project "+cProject.getDirectory();
		norma.run(args);
	}

	public void convertPDFsToProjectAndRunCooccurrence(List<String> facetList) {
		makeProject();
		if (!skipConvertPDFs) {
			convertPDFOutputSVGFilesImageFiles();
		}
		convertPSVGandWriteHtml();
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
		LOG.debug("CP "+cProject);
	}


	public static void main(String[] args) {
		List<String> argList = new ArrayList<String>(Arrays.asList(args));
		if (argList.size() == 0 || HELP.equals(argList.get(0))) {
			if (argList.size() > 0) argList.remove(0);
			help(argList);
		} else {
			String projectName = argList.get(0);
			argList.remove(0);
			if (argList.size() == 0) {
				System.err.println("No default action for project: "+projectName+" (yet)");
			} else {
				AMIProcessor amiProcessor = AMIProcessor.createProcessor(projectName);
				// FIX THIS
				if ("abc".length() == 2) amiProcessor.convertJATSXMLandWriteHtml();
				amiProcessor.runSearchesAndCooccurrence(argList);
			}
		}
	}

	public static void help(List<String> argList) {
		System.err.println("amiProcessor <projectDirectory> [dictionary [dictionary]]");
		System.err.println("    projectDirectory can be full name or relative to currentDir");
		if (argList.size() == 0) {
			System.err.println("\nlist of dictionaries taken from AMI dictionary list:");
		}
		SimpleDictionaries simpleDictionaries = new SimpleDictionaries();
		simpleDictionaries.listDictionaries(argList);
	}

	public void run(String cmd) {
		try {
			CommandProcessor.main((cProject.getDirectory()+" "+cmd).split("\\s+"));
		} catch (IOException e) {
			throw new RuntimeException("Cannot run command: "+cmd, e);
		}
	}

	/** creates directory ffrom first argument; if not exists, return null
	 * 
	 * @param argList
	 * @return
	 */
	public static File createProjectDirAndTrimArgs(List<String> argList) {
		File projectDir = null;
		if (argList != null && argList.size() > 0) {
			String pathname = argList.get(0);
			argList.remove(0);
			projectDir = new File(pathname);
			if (!projectDir.exists() || !projectDir.isDirectory()) {
				projectDir = new File(System.getProperty("user.dir"), pathname);
			}
			if (!projectDir.exists() || !projectDir.isDirectory()) {
				LOG.error("cannot find directory: "+projectDir);
				projectDir = null;
			}
		}
		return projectDir;
	}
	


}
