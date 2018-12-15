package org.contentmine.ami;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.contentmine.cproject.files.CProject;
import org.contentmine.cproject.files.DebugPrint;
import org.contentmine.norma.picocli.AbstractAMIProcessor;

import picocli.CommandLine;
import picocli.CommandLine.Command;

	@Command(
			//String name() default "<main class>";
	name = "ami-pdf", 
			//String[] aliases() default {};
	aliases = "pdf",
			//Class<?>[] subcommands() default {};
	version = "ami-pdf 0.1",
			//Class<? extends IVersionProvider> versionProvider() default NoVersionProvider.class;
	description = "Convert PDFs to SVG/Images"
	)


public class AMIProcessorPDF extends AbstractAMIProcessor {
	private static final Logger LOG = Logger.getLogger(AMIProcessorPDF.class);
	static {
		LOG.setLevel(Level.DEBUG);
	}
	
    /** used by some non-picocli calls
     * 
     * @param cProject
     */
	public AMIProcessorPDF(CProject cProject) {
		this.cProject = cProject;
	}
	
	public AMIProcessorPDF() {
	}
	
    public static void main(String[] args) throws Exception {
    	// debug
    	args = args.length == 0 ? new String[] {"--help"} : args;
    	AMIProcessorPDF amiProcessorPDF = new AMIProcessorPDF();
    	amiProcessorPDF.runCommands(args);
    }

    @Override
    public void runCommands(String[] args) {
    	super.runCommands(args);
        runPDF();
    }

    @Override
    public Void call() throws Exception {
    	super.call();
        return null;
    }

    private void runPDF() {
    	if (cProject != null) {
    		runPDF(cProject.getDirectory());
    	}
    }

//	public static void main1(String[] args) {
//		List<String> argList = new ArrayList<String>(Arrays.asList(args));
//		if (argList.size() == 0 || AMIProcessor.HELP.equals(argList.get(0))) {
//			if (argList.size() > 0) argList.remove(0);
//			AMIProcessor.runHelp(argList);
//		} else {
//			String projectName = argList.get(0);
//			File projectDir = new File(projectName);
//			argList.remove(0);
//			runPDF(projectDir, argList);
//		}
//	}
    public static void runPDF(CProject cProject) {
    	runPDF(cProject == null ? null : cProject.getDirectory());
    }

	private static void runPDF(File projectDir) {
		if (projectDir != null) {
			AMIProcessor amiProcessor = AMIProcessor.createProcessor(projectDir);
			amiProcessor.setDebugLevel(Level.DEBUG);
			amiProcessor.convertPDFsToProject();
		}
	}

}
