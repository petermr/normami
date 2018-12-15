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
import org.contentmine.norma.picocli.AbstractAMIProcessor;

import picocli.CommandLine;
import picocli.CommandLine.Command;
import picocli.CommandLine.Option;

/** makes a project.
 * 
 * @author pm286
 *
 */
@Command(
		//String name() default "<main class>";
name = "ami-makeproject", 
		//String[] aliases() default {};
aliases = "makeproject",
		//Class<?>[] subcommands() default {};
version = "ami-makeproject 0.1",
		//Class<? extends IVersionProvider> versionProvider() default NoVersionProvider.class;
description = "Processes a directory (CProject) containing files (e.g.*.pdf, *.html, *.xml) to be made into CTrees.%n"
		+ "Assuming a directory foo/ with files%n%n"
		+ "  a.pdf%n"
		+ "  b.pdf%n"
		+ "  c.html%n"
		+ "  d.xml%n"
		+ "%n"
		+ "makeproject -p foo -f pdf,html,xml%n"
		+ "will create:%n"
		+ "foo/%n"
		+ "  a/%n"
		+ "    fulltext.pdf%n"
		+ "  b/%n"
		+ "    fulltext.pdf%n"
		+ "  c/%n"
		+ "    fulltext.html%n"
		+ "  d/%n"
		+ "    fulltext.xml%n"
		+ "%n"
		+ "The directories can contain multiple filetypes%n"
		+ "%n"
		+ "Assuming a directory foo/ with files%n%n"
		+ "  a.pdf%n"
		+ "  b.pdf%n"
		+ "  a.html%n"
		+ "  b.xml%n"
		+ "  c.pdf%n"
		+ "%n"
		+ "makeproject -p foo -f pdf,html,xml%n"
		+ "will create:%n"
		+ "foo/%n"
		+ "  a/%n"
		+ "    fulltext.pdf%n"
		+ "    fulltext.html%n"
		+ "  b/%n"
		+ "    fulltext.pdf%n"
		+ "    fulltext.xml%n"
		+ "  c/%n"
		+ "    fulltext.pdf%n"
		+ "%n"
		+ " raw filename changes occur in CProject.makeProject()"
		+ "Files with uppercase characters, spaces, punctuation, long names, etc. may give problems. By default they %n"
		+ "(a) are lowercased, %n"
		+ "(b) have punctuation set to '_' %n"
		+ "(c) are truncated to --length characters.%n"
		+ " If any of these creates ambiguity, then numeric suffixes are added (NYI). "
)

public class AMIMakeProject extends AbstractAMIProcessor {
	private static final Logger LOG = Logger.getLogger(AMIMakeProject.class);
	static {
		LOG.setLevel(Level.DEBUG);
	}
	

    @Option(names = {"-f", "--filetype"},
		arity = "0..*",
		split=",",
        description = "suffixes of included files: any of html, pdf, xml%n"
        		+ "can be concatenated with commas (e.g. html,pdf "
        		+ "(NO '.' or '*')%n"
        )
    private String[] filetypes;

    @Option(names = {"-c", "--compress"},
    		arity="0..1",
    		defaultValue = "15",
    		description = "compress and lowercase names. "
    		)
    private int compress;
    

    /** used by some non-picocli calls
     * 
     * @param cProject
     */
	public AMIMakeProject(CProject cProject) {
		this.cProject = cProject;
	}
	
	public AMIMakeProject() {
	}
	
    public static void main(String[] args) throws Exception {
    	AMIMakeProject makeProject = new AMIMakeProject();
    	makeProject.runCommands(args);
    }

    public void runCommands(String[] args) {
    	args = args.length == 0 ? new String[] {"--help"} : args;
        CommandLine.call(this, args);
        runMake();
    }

    @Override
    public Void call() throws Exception {
    	super.call();
        return null;
    }
    
	protected boolean parseCProjectAndCTrees() {
		cTree = null;
		if (cProjectDirectory != null) {
			File cProjectDir = new File(cProjectDirectory);
			if (!cProjectDir.exists() || !cProjectDir.isDirectory()) {
				throw new RuntimeException("cProject must be existing directory: "+cProjectDirectory);
			}
			cProject = new CProject(cProjectDir);
    	} else if (cTreeDirectory != null) {
    		System.err.println("must not have --ctree: " + cTreeDirectory);
    		return false;
    	}
    	if (cProject == null) {
    		System.err.println("must give cProject");
    		return false;
    	}
    	if (filetypes == null || filetypes.length == 0) {
    		System.err.println("must give at least one filetype (e.g. html)");
    		return false;
    	}
    	printValues();
        cProject.makeProject(filetypes, compress);
        return true;

	}

	private void printValues() {
		System.out.println("values\n======");
        System.out.println("cproject            " + (cProject == null ? "" : cProject.getDirectory().getAbsolutePath()));
        System.out.println("file types          " + Arrays.asList(filetypes));
        System.out.println("compress            " + compress);
	}

    private void runMake() {
    	LOG.debug("CProject "+cProject);
    }


}
