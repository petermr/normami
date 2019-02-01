package org.contentmine.ami.tools;

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
		String cmd = "word(frequencies)xpath:@count>20~w.stopwords:pmcstop.txt_stopwords.txt";
		String cmd1 = cmd;
		for (String facet : dictionaryList) {
			if (facet.equals("gene")) {
				cmd1 += " gene(human)";
			} else if (facet.equals("species")) {
				cmd1 += " species(binomial)";
			} else {
				cmd1 += " "+AMIProcessor.SEARCH + "("+facet+")";
			}
		}
		cmd = cmd1;
		try {
			
			CommandProcessor commandProcessor = new CommandProcessor(cProject.getDirectory());
			List<String> cmdList = Arrays.asList(cmd.trim().split("\\s+"));
			for (String cmd0 : cmdList) {
				System.out.println("cmd> "+cmd0);
			}
			commandProcessor.parseCommands(cmdList);
			List<AMIPluginOption> pluginOptions = commandProcessor.getPluginOptions();
			for (AMIPluginOption option : pluginOptions) {
				LOG.debug("plug>"+option);
			}
			commandProcessor.runCommands();
/**
			commandProcessor.runNormaIfNecessary();
			for (AMIPluginOption pluginOption : commandProcessor.pluginOptions) {
				System.out.println("running: "+pluginOption);
				try {
					pluginOption.run();
				} catch (Exception e) {
					CommandProcessor.LOG.error("cannot run command: "+pluginOption +"; " + e.getMessage());
					continue;
				}
				System.out.println("filter: "+pluginOption);
				pluginOption.runFilterResultsXMLOptions();
				System.out.println("summary: "+pluginOption);
				pluginOption.runSummaryAndCountOptions(); 
			}
			CommandProcessor.LOG.trace(commandProcessor.pluginOptions);	 */
			
			commandProcessor.createDataTables();
		
		} catch (IOException e) {
			throw new RuntimeException("Cannot run command: "+cmd, e);
		}
		
		amiProcessor.defaultAnalyzeCooccurrence(dictionaryList);
	}



}
