package org.contentmine.ami;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.contentmine.cproject.files.CProject;
import org.contentmine.eucl.euclid.Util;
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
		+ " If any of these creates ambiguity, then numeric suffixes are added. "
		+ ""
		+ "By default a logfile of the conversions is created in make_project.json. "
		+ "The name can be cahnged "
)

public class AMIMakeProject extends AbstractAMIProcessor {
	public static final Logger LOG = Logger.getLogger(AMIMakeProject.class);

	static {
		LOG.setLevel(Level.DEBUG);
	}
	
    @Option(names = {"--compress"},
    		arity="0..1",
    		description = "compress and lowercase names. "
    		)
    private int compress = 25;

    @Option(names = {"--logfile"},
    		arity="0..1",
    		description = "logfile name (usually created by default - see particular command)."
    				+ " To omit logfile use argument NONE. Note"
    				+ "that default logfile names are reserved and it is normally a bad idea to use different ones"
    		)
    private String logfile;

	public AMIMakeProject() {
	}
	
    public static void main(String args) throws Exception {
    	new AMIMakeProject().runCommands(args);
    }

    public static void main(String[] args) throws Exception {
    	new AMIMakeProject().runCommands(args);
    }

    protected void parseSpecifics() {
    	argument(Level.INFO, "compress            "+compress);
    }

	protected void runSpecifics() {
        cProject.makeProject(Util.toStringList(rawFileFormats), compress);
        addMakeProjectLogfile();
    }

	private void addMakeProjectLogfile() {
		if (logfile == null) {
        	cProject.getMakeProjectLogfile();
        } else if (NONE.equalsIgnoreCase(logfile.toString())) {
        	LOG.warn("omitting logfile");
        } else {
        	cProject.getMakeProjectLogfile(logfile);
        }
	}
    
    @Override
	protected void validateCTree() {
		if (cTreeDirectory != null) {
			argument(Level.WARN, "must not have --ctree: " + cTreeDirectory+"; IGNORED");
    	}
	}
	
    @Override
    protected void validateRawFormats() {
		if (args.length > 0 && (rawFileFormats == null || rawFileFormats.length == 0)) {
			argument(Level.ERROR, "must give at least one filetype (e.g. html); NO ACTION");
		}
    }

}
