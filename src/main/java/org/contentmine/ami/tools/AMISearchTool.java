package org.contentmine.ami.tools;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.contentmine.ami.AMIProcessor;
import org.contentmine.ami.plugins.CommandProcessor;
import org.contentmine.ami.plugins.search.SearchPluginOption;
import org.contentmine.cproject.files.CProject;
import org.contentmine.cproject.files.DebugPrint;
import org.contentmine.norma.NAConstants;

import picocli.CommandLine.Option;
/**
 * 
 * @author pm286
 *
 */
public class AMISearchTool extends AbstractAMITool {
	private static final Logger LOG = Logger.getLogger(AMISearchTool.class);
	static {
		LOG.setLevel(Level.DEBUG);
	}

    @Option(names = {"--dictionary"},
    		arity = "1..*",
            description = "symbolic names of dictionaries (likely to be obsoleted). Good values are (country, disease, funders)")
    private List<String> dictionaryList;

    @Option(names = {"--dictionarySuffix"},
    		arity = "1",
    		defaultValue = "xml",
            description = "suffix for search dictionary")
    private List<String> dictionarySuffix;

    @Option(names = {"--dictionaryTop"},
    		arity = "1",
            description = " local dictionary home directory")
    private List<String> dictionaryTopList;

    @Option(names = {"--ignorePlugins"},
    		arity = "1..*",
            description = " list of plugins to skip (mainly for debugging)")
    private List<String> ignorePluginList = new ArrayList<>();

    @Option(names = {"--wikidataBiblio"},
    		arity = "0",
            description = " lookup wikidata biblographic object")
    private Boolean wikidataBiblio = false;


    private File dictionaryFile;

	private InputStream dictionaryInputStream;
    /** used by some non-picocli calls
     * obsolete it
     * @param cProject
     */
	public AMISearchTool(CProject cProject) {
		this.cProject = cProject;
	}
	
	public AMISearchTool() {
	}
	
    public static void main(String[] args) throws Exception {
    	new AMISearchTool().runCommands(args);
    }


    @Override
	protected void parseSpecifics() {
		System.out.println("dictionaryList       " + dictionaryList);
		System.out.println("dictionaryTop        " + dictionaryTopList);
		System.out.println("dictionarySuffix     " + dictionarySuffix);
		System.out.println("ignorePlugins        " + ignorePluginList);
		System.out.println();
	}

    @Override
    protected void runSpecifics() {
    	if (cProject == null) {
    		DebugPrint.errorPrintln(Level.ERROR, "requires cProject");
    	} else if (projectExists(cProject)) {
    		processProject();
    	}
    }

	private boolean projectExists(CProject cProject) {
		return cProject == null || cProject.getDirectory() == null ? false : cProject.getDirectory().isDirectory();
	}

	public void processProject() {
		System.out.println("cProject: "+cProject.getName());
		runSearch();
	}

	private void runSearch() {
		AMIProcessor amiProcessor = AMIProcessor.createProcessorFromDir(cProject.getDirectory());
		String cmd = buildCommandFromBuiltinsAndFacets();
		/** this uses SearchArgProcessor.runSearch()
		 * this should be called directly.
		 */
		runLegacyCommandProcessor(cmd);
		amiProcessor.defaultAnalyzeCooccurrence(dictionaryList);
	}

	private void runLegacyCommandProcessor(String cmd) {
//		System.out.println("SEARCH running legacy processors");
		try {
			
			CommandProcessor commandProcessor = new CommandProcessor(cProject.getDirectory());
			List<String> cmdList = Arrays.asList(cmd.trim().split("\\s+"));
//			for (String cmd0 : cmdList) {
//				System.out.println("cmd> "+cmd0);
//			}
			commandProcessor.parseCommands(cmdList);
			commandProcessor.runNormaIfNecessary();
			commandProcessor.runJsonBibliography();
			commandProcessor.runLegacyPluginOptions(this);
			commandProcessor.createDataTables(wikidataBiblio);
		
		} catch (IOException e) {
			throw new RuntimeException("Cannot run command: "+cmd, e);
		}
	}

	private String buildCommandFromBuiltinsAndFacets() {
		String cmd = "word(frequencies)xpath:@count>20~w.stopwords:pmcstop.txt_stopwords.txt";
		String cmd1 = cmd;
		if (dictionaryList != null) {
			for (String facet : dictionaryList) {
				if (facet.equals("gene")) {
					cmd1 += " gene(human)";
				} else if (facet.equals("species")) {
					cmd1 += " species(binomial)";
				} else {
					checkDictionaryExists(facet);
					cmd1 += " "+AMIProcessor.SEARCH + "("+facet+")";
				}
			}
		}
		return cmd1;
	}

	private void checkDictionaryExists(String facet) {
		/** builtin? */
		if (false) {
		} else if (getLocalDictionaryInputStream(facet) != null) {
		} else if (getBuiltinDictionaryInputStream(facet) != null) {
		} else {
//			System.err.println("cannot find dictionary: "+facet);
		}
	}

	private InputStream getBuiltinDictionaryInputStream(String dictionary) {
		String resource = SearchPluginOption.createSearchDictionaryResourceString(dictionary);
		dictionaryInputStream = this.getClass().getResourceAsStream(resource);
		if (dictionaryInputStream == null) {
			File builtinFile = new File(NAConstants.PLUGINS_DICTIONARY_DIR, dictionary+".xml");
			try {
				dictionaryInputStream = new FileInputStream(builtinFile);
			} catch (FileNotFoundException e) {
				// cannot find file
			}
		}
		if (dictionaryInputStream == null) {
			LOG.trace("cannot find builtin dictionary: " + dictionary);
		}
		return dictionaryInputStream;
	}

	private InputStream getLocalDictionaryInputStream(String facet) {
		if (dictionaryTopList != null) {
			for (String dictTop : dictionaryTopList) {
				dictionaryFile = new File(dictTop, facet+"."+dictionarySuffix.get(0));
				if (dictionaryFile.exists()) {
					LOG.debug("exists: "+dictionaryFile);
					try {
						dictionaryInputStream = new FileInputStream(dictionaryFile);
					} catch (FileNotFoundException e) {
						// 
					}
					break;
				} else {
					LOG.trace("cannot find: "+dictionaryFile);
				}
			}
		}
		return dictionaryInputStream;
	}

	public List<String> getIgnorePluginList() {
		return ignorePluginList;
	}

}
