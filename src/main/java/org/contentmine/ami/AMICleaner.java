package org.contentmine.ami;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.contentmine.cproject.files.CProject;
import org.contentmine.cproject.files.CTree;
import org.contentmine.cproject.files.DebugPrint;
import org.contentmine.eucl.euclid.Util;
import org.contentmine.norma.picocli.AbstractAMIProcessor;

import picocli.CommandLine;
import picocli.CommandLine.Command;
import picocli.CommandLine.Option;

/** cleans some of all of the project.
 * 
 * @author pm286
 *
 */
@Command(
		//String name() default "<main class>";
name = "ami-clean", 
		//String[] aliases() default {};
aliases = "clean",
		//Class<?>[] subcommands() default {};
version = "ami-clean 0.1",
		//Class<? extends IVersionProvider> versionProvider() default NoVersionProvider.class;
description = "cleans specific files of directories in project"
)

public class AMICleaner extends AbstractAMIProcessor {
	private static final Logger LOG = Logger.getLogger(AMICleaner.class);
	static {
		LOG.setLevel(Level.DEBUG);
	}
	
	public enum Cleaner {
		FULLTEXT_HTML("fulltext.html", "f", "remove fulltext.html (probably not recoverable without redownload)"),
		FULLTEXT_PDF("fulltext.pdf", "f", "remove fulltext.pdf (probably not recoverable without redownload)"),
		FULLTEXT_XML("fulltext.xml", "f", "remove fulltext.xml (probably not recoverable without redownload)"),
		PDFIMAGES("pdfimages", "d", "remove pdfimages/ directory and contents (created by parsing fulltext.pdf)"),
		RAWIMAGES("rawimages", "d", "remove rawimages/ directory and contents (probably directly downloaded)"),
		SCHOLARLY_HTML("scholarly.html", "f", "remove scholarly.html (created by parsing)"),
		SVGDIR("svg", "d", "remove svg/ directory and contents (created by parsing fulltext.pdf)"),
		;
		public String file;
		public String type;
		public String message;
		
		private Cleaner(String file, String type, String message) {
			this.file = file;
			this.type = type;
			this.message = message;
		}
		
		public void help() {
			DebugPrint.debugPrint(file + ": " + message);
		}

		public void clean(CTree cTree, String arg) {
		}

		public boolean matches(String arg) {
			return this.file.equals(arg);
		}
	}

    @Option(names = {"--file"},
		arity = "0..*",
        description = "files to delete by name")
    private String[] files;

    @Option(names = {"--fileglob"},
		arity = "0..*",
        description = "files to delete by glob")
    private String[] fileGlobs;

    @Option(names = {"--dir"},
		arity = "0..*",
        description = "directories to delete by name")
    private String[] dirs;

    @Option(names = {"--dirglob"},
		arity = "0..*",
        description = "directories to delete by glob")
    private String[] dirGlobs;

    /** used by some non-picocli calls
     * obsolete it
     * @param cProject
     */
	public AMICleaner(CProject cProject) {
		this.cProject = cProject;
	}
	
	public AMICleaner() {
	}
	
    public static void main(String[] args) throws Exception {
    	AMICleaner amiCleaner = new AMICleaner();
    	amiCleaner.runCommands(args);
    }

    @Override
	protected void parseSpecifics() {
    	System.out.println("files         "+Util.toStringList(files));
    	System.out.println("fileGlobs     "+Util.toStringList(fileGlobs));
    	System.out.println("dirs          "+Util.toStringList(dirs));
    	System.out.println("dirGlobs      "+Util.toStringList(dirGlobs));
	}

    @Override
    protected void runSpecifics() {
        runClean();
    }

    private void runClean() {
    	if (files != null) cleanFiles(Arrays.asList(files));
    	if (dirs != null) cleanFiles(Arrays.asList(dirs));
    }

	public void cleanFiles(List<String> argList) {
		for (String arg : argList) {
			cleanReserved(arg);
		}
	}

	public void cleanReserved(String arg) {
		for (Cleaner cleaner : Cleaner.values()) {
			if (cleaner.matches(arg)) {
				cProject.clean(arg);
				return;
			}
		}
		DebugPrint.debugPrint("failed to delete: "+arg);
	}

	public void clean(String filename) {
		cProject.clean(filename);
	}

}
