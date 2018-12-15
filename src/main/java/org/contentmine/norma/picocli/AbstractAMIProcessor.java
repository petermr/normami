package org.contentmine.norma.picocli;

import java.io.File;
import java.util.concurrent.Callable;

import org.contentmine.cproject.files.CProject;
import org.contentmine.cproject.files.CTree;

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

public class AbstractAMIProcessor implements Callable<Void> {

	@Option(names = { "-v", "--verbose" }, 
    		description = {
        "Specify multiple -v options to increase verbosity.",
        "For example, `-v -v -v` or `-vvv`" })
    protected boolean[] verbosity = new boolean[0];
    
    @Option(names = {"-t", "--ctree"}, 
    		arity = "0..1",
    		paramLabel = "paramLabel",
//    		defaultValue = "defaultCTree",
    		interactive = false,
    		descriptionKey = "descriptionKey",
    		description = "single CTree (directory) to process")
    protected String cTreeDirectory = null;

    @Option(names = {"-p", "--cproject"}, 
    		arity = "0..1",
    		description = "CProject (directory) to process")
    protected String cProjectDirectory = null;

	protected CProject cProject;
	protected CTree cTree;
	protected File cProjectOutputDir;
	protected File cTreeOutputDir;
	
	protected String[] args;

	/** parse commands and pass to CommandLine
	 * calls CommandLine.call(this, args)
	 * 
	 * @param args
	 */
	public void runCommands(String[] args) {
    	args = args.length == 0 ? new String[] {"--help"} : args;
		CommandLine.call(this, args);
	}

    @Override
    public Void call() throws Exception {
    	parseCProjectAndCTrees();
        return null;
    }

//    @Override
    /** subclass this if you want tp process CTree and CProject differently
     * 
     */
	protected boolean parseCProjectAndCTrees() {
		if (cProjectDirectory != null) {
    		cProject = new CProject(new File(cProjectDirectory));
    	} else if (cTreeDirectory != null) {
    		cTree = new CTree(new File(cTreeDirectory));
    	} else if (cProject == null && cTree == null) {
//    		System.err.println("must give cTree or cProject");
//    		return false;
    	}
    	System.out.println("Values\n======");
        System.out.println("cProject            " + (cProject == null ? "null" : cProject.getDirectory().getAbsolutePath()));
        System.out.println("cTree               " + (cTree == null    ? "null" : cTree.getDirectory().getAbsolutePath()));
        return true;
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

//	/** maybe use Option */
//    @Parameters(index = "0", 
//		description = "the CProject",
//		arity="1",
//		paramLabel="cProject directory (relative or absolute)",
//		hideParamSyntax=false,
//		type=String.class,
////    		converter=MyConverter.class, // doesn't yet exist
////		split="#",
//		hidden=false,
////		defaultValue="-99",
////		showDefaultValue=Help.Visibility.ALWAYS,
////    		completionCandidates=NoCompletionCandidates.class,
//		interactive=true
////		descriptionKey="bar"
//    		
//	)
//    private Integer intArg;

}
