package org.contentmine.ami.tools;

import java.io.File;
import java.util.List;

import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.contentmine.ami.plugins.word.WordArgProcessor;
import org.contentmine.cproject.args.ArgumentOption;
import org.contentmine.cproject.files.CProject;
import org.contentmine.cproject.files.CTree;

import picocli.CommandLine.Command;
import picocli.CommandLine.Option;

/**
running: word([frequencies])[{xpath:@count>20}, {w.stopwords:pmcstop.txt stopwords.txt}]
WS: /Users/pm286/workspace/cmdev/normami/target/cooccurrence/zika10  
DefaultArgProcessor  - running method: runExtractWords
DefaultArgProcessor  - running method: outputWords
[...7 snips]
DefaultArgProcessor  - running method: runExtractWords
DefaultArgProcessor  - running method: outputWords

filter: word([frequencies])[{xpath:@count>20}, {w.stopwords:pmcstop.txt stopwords.txt}]
DefaultArgProcessor  - running method: runFilter
DefaultArgProcessor  - running method: outputFilter
DefaultArgProcessor  - running method: outputMethod
[...7 snips]
DefaultArgProcessor  - running method: runFilter
DefaultArgProcessor  - running method: outputFilter
DefaultArgProcessor  - running method: outputMethod

DefaultArgProcessor  - running method: finalFilter

summary: word([frequencies])[{xpath:@count>20}, {w.stopwords:pmcstop.txt stopwords.txt}]
DefaultArgProcessor  - running method: runSummaryFile
DefaultArgProcessor  - running method: runDFFile
[...7snipped]
DefaultArgProcessor  - running method: runSummaryFile
DefaultArgProcessor  - running method: runDFFile

DefaultArgProcessor  - running method: finalSummaryFile
DefaultArgProcessor  - running method: finalDFFile
*/

/** analyses bitmaps
 * 
 * @author pm286
 *
 */
@Command(
		//String name() default "<main class>";
name = "ami-words", 
		//String[] aliases() default {};
aliases = "words",
		//Class<?>[] subcommands() default {};
version = "ami-words 0.1",
		//Class<? extends IVersionProvider> versionProvider() default NoVersionProvider.class;
description = "Analyze word frequencies"
)

public class AMIWordsTool extends AbstractAMITool {
	private static final Logger LOG = Logger.getLogger(AMIWordsTool.class);
	static {
		LOG.setLevel(Level.DEBUG);
	}
		
    @Option(names = {"--stopwords"},
    		arity = "1..*",
            description = "Stop word files (ex: w.stopwords:pmcstop.txt stopwords.txt)")
    private List<File> stopwordFileList;
	
    @Option(names = {"--stopworddir"},
    		arity = "1",
            description = "Stop word directory (only one allowed)")
    private List<File> stopwordDir;
	
    @Option(names = {"--frequencies"},
    		arity = "0..1",
            description = "run frequencies")
    private Boolean frequencies;

    @Option(names = {"--filter"},
    		arity = "0..1",
            description = "run filter")
    private Boolean filter;

    @Option(names = {"--filterFinal"},
    		arity = "0..1",
            description = "run filter final (reduce)")
    private Boolean filterFinal;

    @Option(names = {"--filtersummary"},
    		arity = "0..1",
            description = "run filter summary")
    private Boolean filterSummary;

    /** used by some non-picocli calls
     * obsolete it
     * @param cProject
     */
	public AMIWordsTool(CProject cProject) {
		this.cProject = cProject;
	}
	
	public AMIWordsTool() {
	}
	
    public static void main(String[] args) throws Exception {
    	new AMIWordsTool().runCommands(args);
    }

    @Override
	protected void parseSpecifics() {
		System.out.println("stopword files       " + stopwordFileList);
		System.out.println();
	}

    @Override
    protected void runSpecifics() {
    	if (processTrees()) { 
	    }
    }

	public void processTree() {
		System.out.println("cTree: "+cTree.getName());
		WordArgProcessor argProcessor = new WordArgProcessor();
		argProcessor.setCTree(cTree);
		argProcessor.runExtractWords((ArgumentOption) null);
	}
	
}
