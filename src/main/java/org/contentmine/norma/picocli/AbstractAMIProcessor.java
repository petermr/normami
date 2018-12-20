package org.contentmine.norma.picocli;

import java.io.File;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.Callable;

import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.contentmine.ami.AMIDictionary.RawFileFormat;
import org.contentmine.cproject.files.CProject;
import org.contentmine.cproject.files.CTree;
import org.contentmine.eucl.euclid.Util;

import picocli.CommandLine;
import picocli.CommandLine.Command;
import picocli.CommandLine.Option;

/**
 * Reusable Commands for picocli CommandLine
 * see Picocli manual
 * 
 * @author pm286
 *
 */
@Command(
	addMethodSubcommands = false,
			//String separator() default "=";
	separator = "=",
			//String[] version() default {};
	mixinStandardHelpOptions = true,
			//boolean helpCommand() default false;
	helpCommand = true,
			//String headerHeading() default "";
	abbreviateSynopsis = true,
			//String[] customSynopsis() default {};
	descriptionHeading = "Description\n===========\n",
			//String[] description() default {};
	parameterListHeading  = "Parameters\n=========\n",
			//String optionListHeading() default "";
	optionListHeading  = "Options\n=======\n",
			//boolean sortOptions() default true;
	sortOptions = true,
			//char requiredOptionMarker() default ' ';
	requiredOptionMarker = '*',
			//Class<? extends IDefaultValueProvider> defaultValueProvider() default NoDefaultProvider.class;
	showDefaultValues = true,
			//String commandListHeading() default "Commands:%n";
	commandListHeading = "Commands:%n=========%n",
			//String footerHeading() default "";
	hidden = false,
			//String resourceBundle() default "";
	usageHelpWidth = 80
	)

public abstract class AbstractAMIProcessor implements Callable<Void> {
	private static final Logger LOG = Logger.getLogger(AbstractAMIProcessor.class);

	protected static final String NONE = "NONE";
	static {
		LOG.setLevel(Level.DEBUG);
	}


    @Option(names = {"--log4j"}, 
    		arity="2",
    		description = "format: <classname> <level>; sets logging level of class, e.g. \n "
    				+ "org.contentmine.ami.lookups.WikipediaDictionary INFO"
    		)
	public String[] log4j;

    @Option(names = {"-p", "--cproject"}, 
		arity = "0..1",
		paramLabel="CProject",
		description = "CProject (directory) to process")
    protected String cProjectDirectory = null;

	@Option(names = {"--rawfiletypes" }, 
		arity = "1..*", 
		split = ",", 
		description = "suffixes of included files (${COMPLETION-CANDIDATES}): "
				+ "can be concatenated with commas ")
	protected RawFileFormat[] rawFileFormats;

    @Option(names = {"-t", "--ctree"}, 
		arity = "0..1",
		paramLabel = "CTree",
		interactive = false,
		descriptionKey = "descriptionKey",
		description = "single CTree (directory) to process")
    protected String cTreeDirectory = null;

	@Option(names = { "-v", "--verbose" }, 
    		description = {
        "Specify multiple -v options to increase verbosity.",
        "For example, `-v -v -v` or `-vvv`"
        + "We map ERROR or WARN -> 0 (i.e. always print), INFO -> 1(-v), DEBUG->2 (-vv)" })
    protected boolean[] verbosity = new boolean[0];
    

	protected CProject cProject;
	protected CTree cTree;
	// needed for testing I think
	protected File cProjectOutputDir;
	protected File cTreeOutputDir;
	
	protected String[] args;
	private Level level;

	public void init() {
	}

	public void runCommands(String cmd) {
		String[] args = cmd == null ? new String[]{} : cmd.split("\\s+");
		runCommands(args);
	}
	
	/** parse commands and pass to CommandLine
	 * calls CommandLine.call(this, args)
	 * 
	 * @param args
	 */
	public void runCommands(String[] args) {
		this.args = args;
		// add help
    	args = args.length == 0 ? new String[] {"--help"} : args;
		CommandLine.call(this, args);
		
    	printGenericHeader();
		parseGenerics();
		
    	printSpecificHeader();
		parseSpecifics();
		if (!Level.WARN.isGreaterOrEqual(level)) {
			System.err.println("processing halted due to argument errors");
		} else {
			runGenerics();
			runSpecifics();
		} 
	}
	
	protected abstract void parseSpecifics();
	protected abstract void runSpecifics();

	protected boolean parseGenerics() {
		validateCProject();
		validateCTree();
		validateRawFormats();
    	setLogging();
    	printGenericValues();
        return true;
	}

	private void setLogging() {
		if (log4j != null) {
			Map<Class<?>, Level> levelByClass = new HashMap<Class<?>, Level>();
			for (int i = 0; i < log4j.length; ) {
				String className = log4j[i++];
				Class<?> logClass = null;
				try {
					logClass = Class.forName(className);
				} catch (ClassNotFoundException e) {
					LOG.error("Cannot find logger Class: "+logClass);
					continue;
				}
				String levelS = log4j[i++];
				Level level =  Level.toLevel(levelS);
				if (level == null) {
					LOG.error("cannot parse class/level: "+className+":"+levelS);
				} else {
//					LOG.debug(logClass+": "+level);
					levelByClass.put(logClass, level);
					Logger.getLogger(logClass).setLevel(level);
				}
			}
		}
	}

	@Override
    public Void call() throws Exception {
//		LOG.debug("call(); called");
        return null;
    }

//    @Override
    /** subclass this if you want to process CTree and CProject differently
     * 
     */
	protected boolean runGenerics() {
//		if (Level.WARN.isGreaterOrEqual(level)) {
//			LOG.error("errors in parameters, processing aborted");
//			return false;
//		}
        return true;
	}

	protected void validateRawFormats() {
	}

	protected void validateCProject() {
		if (cProjectDirectory != null) {
			File cProjectDir = new File(cProjectDirectory);
			if (!cProjectDir.exists() || !cProjectDir.isDirectory()) {
				throw new RuntimeException("cProject must be existing directory: "+cProjectDirectory);
			}
			cProject = new CProject(cProjectDir);
    	}
	}

	protected void validateCTree() {
		if (cTreeDirectory != null) {
			File cTreeDir = new File(cTreeDirectory);
			if (!cTreeDir.exists() || !cTreeDir.isDirectory()) {
				throw new RuntimeException("cTree must be existing directory: "+cTreeDirectory);
			}
			cTree = new CTree(cTreeDir);
    	}
	}

	private void printGenericValues() {
        System.out.println("cproject            " + (cProject == null ? "" : cProject.getDirectory().getAbsolutePath()));
        System.out.println("ctree               " + (cTree == null ? "" : cTree.getDirectory().getAbsolutePath()));
        System.out.println("file types          " + Util.toStringList(rawFileFormats));
	}
	


	public void setCProject(CProject cProject) {
		this.cProject = cProject;
	}

	public void setCTree(CTree cTree) {
		this.cTree = cTree;
	}

	public CTree getCTree() {
		return cTree;
	}

	public void setCProjectOutputDir(File dir) {
		this.cProjectOutputDir = dir;
	}

	public File getCProjectOutputDir() {
		return cProjectOutputDir;
	}

	public void setCTreeOutputDir(File outputDir) {
		cTreeOutputDir = outputDir;
	}

	public File getCTreeOutputDir() {
		return cTreeOutputDir;
	}

	public CProject getCProject() {
		return cProject;
		
	}

	protected void printGenericHeader() {
		System.out.println();
		System.out.println("Generic values");
		System.out.println("==============");
	}

	protected void printSpecificHeader() {
		System.out.println();
		System.out.println("Specific values");
		System.out.println("===============");
	}

	protected void argument(Level level, String message) {
		combineLevel(level);
		System.out.println(message);
	}

	private void combineLevel(Level level) {
		if (level == null) {
			LOG.warn("null level");
		} else if (this.level== null) {
			this.level = level;
		} else if (level.isGreaterOrEqual(this.level)) {
			this.level = level;
		}
	}
	
	public Level getVerbosity() {
		if (verbosity.length == 0) {
			LOG.error("BUG?? in verbosity");
			return null;
		} else if (verbosity.length == 1) {
			 return verbosity[0] ? Level.INFO : Level.WARN; 
		} else if (verbosity.length == 2) {
			 return Level.DEBUG; 
		} else if (verbosity.length == 3) {
			 return Level.TRACE; 
		}
		return Level.ERROR;
		
	}

}
