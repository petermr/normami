package org.contentmine.ami.tools;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;

import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.contentmine.ami.plugins.CommandProcessor;
import org.contentmine.ami.plugins.word.WordPluginOption;
import org.contentmine.cproject.files.CProject;
import org.contentmine.cproject.files.OptionFlag;

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
	private static final String OPTIONS_JOIN = "~";
	private static final String XPATH_JOIN = ":";
	private static final String STOPWORD_JOIN = "_";
	private static final String W_STOPWORDS = "w.stopwords:";

	private static final Logger LOG = Logger.getLogger(AMIWordsTool.class);
	static {
		LOG.setLevel(Level.DEBUG);
	}

	/** I think only frequencies works!
	 * 
	 * @author pm286
	 *
	 */
	public enum WordTarget {
		frequencies,
//		wordFrequencies,
		wordLengths,
//		search,
//		wordSearch
		;
		}
		
    @Option(names = {"--filter"},
    		arity = "0..1",
            description = "run filter")
    private Boolean filter;

    @Option(names = {"--filterfinal"},
    		arity = "0..1",
            description = "run filter final (reduce)")
    private Boolean filterFinal;

    @Option(names = {"--filtersummary"},
    		arity = "0..1",
            description = "run filter summary")
    private Boolean filterSummary;

    @Option(names = {"--mincount"},
    		arity = "1",
            description = "minimum count for wrds for acceptance in frequencies")
    private Integer minCount = 20;

    @Option(names = {"--targets"},
    		arity = "1..*",
            description = "frequencies and other word targets (frequencies, wordFrequencies, wordLengths, search, wordSearch); "
            		+ "201902 only frequencies implemented")
    private WordTarget[] targets = new WordTarget[]{WordTarget.frequencies};

    @Option(names = {"--stopworddir"},
    		arity = "1",
            description = "Stop word directory (only one allowed) not yet working")
    private List<File> stopwordDir;
	
    @Option(names = {"--stopwords"},
    		arity = "1..*",
            description = "Stop word files for w.stopwords: (ex: pmcstop.txt stopwords.txt)")
    private String[] stopwordFileList = {"pmcstop.txt", "stopwords.txt"};

	private String wordCmd;
	

    /**
    @Option(names = {"--word"},
    		arity = "1..*",
            description = "options to analyze 'word' analysis; includes 'frequencies'")
    private List<String> wordList = Arrays.asList(new String[]{"frequencies"});

    word([frequencies])[{xpath:@count>20}, {w.stopwords:pmcstop.txt stopwords.txt}]
     */
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
		System.out.println("minCount             " + minCount);
		System.out.println();
	}

    @Override
    protected void runSpecifics() {
		String wordTargets = makeWordTargets();
    	String wordOptions = makeWordOptions();
		wordCmd = wordTargets + wordOptions;
    	LOG.debug("WORD>>> "+wordCmd);
    	processTrees();
    }

	private String makeWordTargets() {
		String wordString = null;
		if (targets != null && targets.length > 0) {
			wordString = "word(";
			for (WordTarget target : targets) {
				wordString += target + " ";			
			}
			wordString = wordString.trim() + ")";
		}
		return wordString;
	}

	private String makeWordOptions() {
		return makeXPathOptions() + OPTIONS_JOIN + makeStopwordOptions();
	}

	private String makeXPathOptions() {
		String xPathString = null;
		if (minCount != null) {
			xPathString = "" + "xpath" + XPATH_JOIN + "@count" + ">" + minCount + "";
		}
		return xPathString;		
	}

	private String makeStopwordOptions() {
		String stopwordOptions = null;
		// this is awful
		if (stopwordFileList != null && stopwordFileList.length > 0) {
			stopwordOptions = W_STOPWORDS;
			for (int i = 0; i < stopwordFileList.length; i++) {
				if (i > 0) {
					stopwordOptions += STOPWORD_JOIN;
				}
				stopwordOptions += stopwordFileList[i];
			}
		}
		return stopwordOptions;
	}

	@Override
	public boolean processTrees() {
		System.out.println("cProject: "+cProject.getName());
//		runWordsNew();
		runWords();
		return true;
	}

	public void processTree() {
		System.out.println("cTree: "+cTree.getName());
//		runWordsNew();
		runWords();
//		WordArgProcessor argProcessor = new WordArgProcessor();
//		argProcessor.setCTree(cTree);
//		argProcessor.runExtractWords((ArgumentOption) null);
	}
	
	public void runWordsNew() {
		
		try {
			
			CommandProcessor commandProcessor = new CommandProcessor(cProject.getDirectory());
//			private void createPluginOptionNew(String commandTag, List<String> subOptions, List<OptionFlag> optionFlags) {
			List<String> subOptions = Arrays.asList(new String[]{"fff"});
			List<OptionFlag> optionFlags = Arrays.asList(new OptionFlag[]{new OptionFlag("foo", "value")});
			commandProcessor.parseCommandsNew(WordPluginOption.TAG, subOptions, optionFlags);
			// this runs commands and filters results
			LOG.debug("running command: "+wordCmd);
			commandProcessor.runCommands(this);
			commandProcessor.createDataTables();
		} catch (IOException e) {
			throw new RuntimeException("Cannot run command: "+wordCmd, e);
		}
	}
		
	public void runWords() {
		
		try {
			
			CommandProcessor commandProcessor = new CommandProcessor(cProject.getDirectory());
			List<String> cmdList = Arrays.asList(wordCmd.trim().split("\\s+"));
			commandProcessor.parseCommands(cmdList);
			// this runs commands and filters results
			LOG.debug("running command: "+wordCmd);
			commandProcessor.runLegacyPluginOptions(this);
			// runs runExtractWords
			// runs outputWords
			//
			// then
			//running method: runFilter
			// xpath goes into this
			// DefaultArgProcessor  - filterCTree file(**/word/frequencies/results.xml)xpath(//result[@count>70])
			// DefaultArgProcessor  - running method: outputFilter
			// DefaultArgProcessor  - outputFile PMC3113902/word.frequencies.snippets.xml
			// DefaultArgProcessor  - running method: outputMethod
			
//			String cmd = " --project /Users/pm286/workspace/cmdev/normami/target/cooccurrence/zika10
//			    -i scholarly.html --w.words frequencies --w.stopwords pmcstop.txt stopwords.txt";
//			new WordArgProcessor(cmd).runAndOutput();
	/**
			commandProcessor.runNormaIfNecessary();
			for (AMIPluginOption pluginOption : commandProcessor.pluginOptions) {
				System.out.println("running: "+pluginOption);
				try {
					pluginOption.run();
//	protected void run() {
//		StringBuilder commandString = createCoreCommandStringBuilder();
//		commandString.append(" --w.words "+optionString);
//		String sw = getOptionFlagString("w.stopwords", " ");
//		commandString.append(sw);
//		LOG.debug("WORD "+commandString);
//		System.out.print("WS: "+projectDir+"  ");
 // --project /Users/pm286/workspace/cmdev/normami/target/cooccurrence/zika10 -i scholarly.html --w.words frequencies --w.stopwords pmcstop.txt stopwords.txt
WS: /
//		new WordArgProcessor(commandString.toString()).runAndOutput();
//	}
					
				} catch (Exception e) {
					CommandProcessor.LOG.error("cannot run command: "+pluginOption +"; " + e.getMessage());
					continue;
				}
				System.out.println("filter: "+pluginOption);
				pluginOption.runFilterResultsXMLOptions();
				System.out.println("summary: "+pluginOption);
				pluginOption.runSummaryAndCountOptions(); 
			}
			LOG.trace(commandProcessor.pluginOptions);	 
	*/
			commandProcessor.createDataTables();
		} catch (IOException e) {
			throw new RuntimeException("Cannot run command: "+wordCmd, e);
		}
	}
}
