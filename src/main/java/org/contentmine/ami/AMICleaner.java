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

/** cleans some of all of the project.
 * 
 * @author pm286
 *
 */
public class AMICleaner {
	private static final Logger LOG = Logger.getLogger(AMICleaner.class);
	static {
		LOG.setLevel(Level.DEBUG);
	}
	
	public enum Cleaner {
		FULLTEXT_HTML("fulltext.html", "remove fulltext.html (probably not recoverable without redownload)"),
		FULLTEXT_PDF("fulltext.pdf", "remove fulltext.pdf (probably not recoverable without redownload)"),
		FULLTEXT_XML("fulltext.xml", "remove fulltext.xml (probably not recoverable without redownload)"),
		PDFIMAGES("pdfimages/", "remove pdfimages/ directory and contents (created by parsing fulltext.pdf)"),
		RAWIMAGES("rawimages/", "remove rawimages/ directory and contents (probably directly downloaded)"),
		SCHOLARLY_HTML("scholarly.html", "remove scholarly.html (created by parsing)"),
		SVGDIR("svg/", "remove svg/ directory and contents (created by parsing fulltext.pdf)"),
		;
		public String file;
		public String message;
		private Cleaner(String file, String message) {
			this.file = file;
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

	private CProject cProject;
	
	public AMICleaner(CProject cProject) {
		this.cProject = cProject;
	}

	public static void main(String[] args) {
		List<String> argList = new ArrayList<String>(Arrays.asList(args));
		if (argList.size() == 0 || AMIProcessor.HELP.equals(argList.get(0))) {
			if (argList.size() > 0) argList.remove(0);
			AMICleaner.runHelp(argList);
		} else {
			CProject cProject = new CProject(new File(argList.get(0)));
			AMICleaner cleaner = new AMICleaner(cProject);
			argList.remove(0);
			cleaner.clean(argList);
		}
		
	}

	public void clean(List<String> argList) {
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

	public void clean(String arg) {
		cProject.clean(arg);
	}

	public void cleanRegex(String arg) {
		for (Cleaner cleaner : Cleaner.values()) {
			if (cleaner.matches(arg)) {
				cProject.cleanRegex(arg);
				return;
			}
		}
		DebugPrint.debugPrint("failed to delete: "+arg);
	}

	private static void runHelp(List<String> argList) {
		DebugPrint.debugPrintln("ami-clean <project> [args]");
		DebugPrint.debugPrintln("    no args will delete whole project (be careful)");
		for (Cleaner cleaner : Cleaner.values()) {
			cleaner.help();
		}
	}

}
