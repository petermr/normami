package org.contentmine.ami.tools;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;

import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.contentmine.ami.AMIProcessor;
import org.contentmine.ami.plugins.AMIPluginOption;
import org.contentmine.ami.plugins.CommandProcessor;
import org.contentmine.cproject.files.CProject;
import org.contentmine.cproject.files.DebugPrint;

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
    		arity = "1*",
            description = " local dictionary home directory")
    private List<String> dictionaryTopList;


    private File dictionaryFile;
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
		System.out.println();
	}

    @Override
    protected void runSpecifics() {
    	if (cProject == null) {
    		DebugPrint.errorPrintln(Level.ERROR, "requires cProject");
    	} else {
    		processProject();
    	}
    }

	public void processProject() {
		System.out.println("cProject: "+cProject.getName());
		runSearch();
	}

	private void runSearch() {
		AMIProcessor amiProcessor = AMIProcessor.createProcessorFromDir(cProject.getDirectory());
		String cmd = buildCommandFromBuiltinsAndFacets();
		runLegacyCommandProcessor(cmd);
		amiProcessor.defaultAnalyzeCooccurrence(dictionaryList);
	}

	private void runLegacyCommandProcessor(String cmd) {
		try {
			
			CommandProcessor commandProcessor = new CommandProcessor(cProject.getDirectory());
			List<String> cmdList = Arrays.asList(cmd.trim().split("\\s+"));
//			for (String cmd0 : cmdList) {
//				System.out.println("cmd> "+cmd0);
//			}
			commandProcessor.parseCommands(cmdList);
			commandProcessor.runNormaIfNecessary();
			commandProcessor.runJsonBibliography();
			commandProcessor.runPluginOptions();
			commandProcessor.createDataTables();
		
		} catch (IOException e) {
			throw new RuntimeException("Cannot run command: "+cmd, e);
		}
	}

	private String buildCommandFromBuiltinsAndFacets() {
		String cmd = "word(frequencies)xpath:@count>20~w.stopwords:pmcstop.txt_stopwords.txt";
		String cmd1 = cmd;
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
		return cmd1;
	}

	private void checkDictionaryExists(String facet) {
		/** builtin? */
		if (false) {
		} else if (checkLocal(facet)) {
		} else if (checkBuiltin(facet)) {
		} else {
			LOG.error("cannot find dictionary: "+facet);
//			throw new RuntimeException("cannot find dictionary: "+facet);
		}
	}

	private boolean checkBuiltin(String facet) {
		LOG.debug("check builtin dictionary NYI");
		return false;
	}

	private boolean checkLocal(String facet) {
		boolean check = false;
		if (dictionaryTopList != null) {
			for (String dictTop : dictionaryTopList) {
				dictionaryFile = new File(dictTop, facet+"."+dictionarySuffix.get(0));
				if (dictionaryFile.exists()) {
					LOG.debug("exists: "+dictionaryFile);
					check = true;
					break;
				} else {
					LOG.debug("cannot find: "+dictionaryFile);
				}
			}
		}
		LOG.debug("check local dictionary NYI");
		
		return check;
	}



}
