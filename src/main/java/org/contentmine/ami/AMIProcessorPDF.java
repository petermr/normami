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
	
	public AMIProcessorPDF() {
	}
	
    public static void main(String[] args) throws Exception {
    	AMIProcessorPDF amiProcessorPDF = new AMIProcessorPDF();
    	amiProcessorPDF.runCommands(args);
    }

	@Override
	protected void parseSpecifics() {
		// no local variables
	}

	@Override
	protected void runSpecifics() {
        runPDF();
	}


    private void runPDF() {
    	if (cProject != null) {
    		runPDF(cProject.getDirectory());
    	}
    }

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
