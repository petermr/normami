package org.contentmine.ami;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.contentmine.cproject.files.CProject;
import org.contentmine.cproject.files.DebugPrint;

public class AMIProcessorPDF {
	private static final Logger LOG = Logger.getLogger(AMIProcessorPDF.class);
	static {
		LOG.setLevel(Level.DEBUG);
	}
	
	public static void main(String[] args) {
		List<String> argList = new ArrayList<String>(Arrays.asList(args));
		if (argList.size() == 0 || AMIProcessor.HELP.equals(argList.get(0))) {
			if (argList.size() > 0) argList.remove(0);
			AMIProcessor.runHelp(argList);
		} else {
			String projectName = argList.get(0);
			File projectDir = new File(projectName);
			argList.remove(0);
			runPDF(projectDir, argList);
		}
	}

	public static void runPDF(CProject cProject) {
		runPDF(cProject.getDirectory(), new ArrayList<String>());
	}

	private static void runPDF(File projectDir, List<String> facetList) {
		AMIProcessor amiProcessor = AMIProcessor.createProcessor(projectDir);
		amiProcessor.setDebugLevel(Level.DEBUG);
		amiProcessor.convertPDFsToProject/*AndRunCooccurrence*/(/*facetList*/);
	}

}
