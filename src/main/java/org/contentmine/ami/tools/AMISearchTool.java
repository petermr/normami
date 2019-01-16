package org.contentmine.ami.tools;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.contentmine.ami.AMIProcessor;
import org.contentmine.cproject.files.CProject;
import org.contentmine.cproject.files.CTree;
import org.contentmine.cproject.files.DebugPrint;
import org.contentmine.cproject.util.CMineGlobber;

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
    	if (processTrees()) { 
    	} else {
			DebugPrint.debugPrint(Level.ERROR, "must give cProject or cTree");
	    }
    }

	public void processTree(CTree cTree) {
		this.cTree = cTree;
		System.out.println("cTree: "+cTree.getName());
		runSearch();
	}

	private void runSearch() {
		AMIProcessor amiProcessor = AMIProcessor.createProcessor(cTree.getDirectory());
		amiProcessor.runSearchesAndCooccurrence(dictionaryList);
	}



}
