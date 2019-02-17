package org.contentmine.ami.plugins;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.contentmine.ami.tools.AMISearchTool;
import org.contentmine.ami.tools.AbstractAMITool;
import org.contentmine.cproject.files.CProject;
import org.contentmine.cproject.files.CTree;
import org.contentmine.cproject.files.OptionFlag;
import org.contentmine.cproject.files.ResourceLocation;
import org.contentmine.cproject.metadata.AbstractMetadata;
import org.contentmine.cproject.util.DataTablesTool;
import org.contentmine.eucl.xml.XMLUtil;
import org.contentmine.norma.NAConstants;
import org.contentmine.norma.Norma;
import org.contentmine.norma.biblio.json.EPMCConverter;

import nu.xom.Element;

/** processes commandline , higher level functions
 * 
 * @author pm286
 *
 */
public class CommandProcessor {


	public static final Logger LOG = Logger.getLogger(CommandProcessor.class);
	static {
		LOG.setLevel(Level.DEBUG);
	}
	
	public final static String SYMBOLS = NAConstants.PLUGINS_RESOURCE+"/symbols.xml";
	private static final String EXPAND = "expand";
	private static final String ABBREVIATION = "abbreviation";
	private static final String ARTICLES = "articles";
	private static final String BIBLIOGRAPHY = "bibliography";

	private List<AMIPluginOption> pluginOptions;
	public File projectDir = null;
	private List<String> cmdList = new ArrayList<String>();

	private Map<String, String> symbolMap;
	private String helpString;
	private Map<String, AbstractMetadata> metadataByCTreename;
	private AbstractAMITool abstractAMITool;
	
	private CommandProcessor() {
		init();
	}

	private void init() {
		readSymbols();
	}

	public CommandProcessor(File projectDir) {
		this();
		setProjectDir(projectDir);
	}

	private void readSymbols() {
		InputStream is = new ResourceLocation().getInputStreamHeuristically(SYMBOLS);
		if (is == null) {
			throw new RuntimeException("cannot find symbols: "+SYMBOLS);
		}
		Element symbolsElement = XMLUtil.parseQuietlyToDocument(is).getRootElement();
		symbolMap = new HashMap<String, String>();
		helpString = "";
		for (int i = 0; i < symbolsElement.getChildElements().size(); i++) {
			Element childElement = symbolsElement.getChildElements().get(i);
			String abbrev = childElement.getAttributeValue(ABBREVIATION);
			String replace = childElement.getAttributeValue(EXPAND);
			helpString += (abbrev+" => "+replace+"; ");
			if (i % 4 == 0) helpString += "\n";
			symbolMap.put(abbrev, replace);
		}
//		LOG.debug("symbols:\n"+helpString);
	}

	private void setProjectDir(File projectDir) {
		this.projectDir = projectDir;
	}

	
	public void processCommands(String commandString) {
		if (commandString == null) {
			throw new RuntimeException("Null command");
		}
		processCommands(Arrays.asList(commandString.trim().split("\\s+")));
	}	

	public void processCommands(List<String> cmds) {
		parseCommands(cmds);
		runCommands(null);
	}

	
	public void parseCommands(List<String> cmds0) {
		getOrCreatePluginOptions();
		if (cmds0.size() == 0) {
			throw new RuntimeException("No commands given");
		}
		List<String> cmds = preprocess(cmds0);
		
		for (String cmd : cmds) {
			createPluginOption(cmd);
		}
	}

	private void getOrCreatePluginOptions() {
		if (pluginOptions == null) {
			pluginOptions = new ArrayList<AMIPluginOption>();
		}
	}
	
	public void parseCommandsNew(String commandTag, List<String> subOptions, List<OptionFlag> optionFlags) {
		createPluginOptionNew(/*String */commandTag, /*List<String>*/ subOptions, /*List<OptionFlag>*/ optionFlags);
	}
	
	private void createPluginOptionNew(String commandTag, List<String> subOptions, List<OptionFlag> optionFlags) {
		getOrCreatePluginOptions();
		String cmd = commandTag;
		LOG.trace("creating pluginOption: "+cmd);
		AMIPluginOption pluginOption = AMIPluginOption.createPluginOption(commandTag, subOptions, optionFlags);
		if (pluginOption == null) {
			LOG.error("skipping unknown command: "+cmd);
		} else {
			LOG.trace(pluginOption);
			pluginOption.setProject(projectDir);
			pluginOptions.add(pluginOption);
		}
	}
	
	private void createPluginOption(String cmd) {
		LOG.trace("creating pluginOption: "+cmd);
		AMIPluginOption pluginOption = AMIPluginOption.createPluginOption(cmd);
		if (pluginOption == null) {
			LOG.error("skipping unknown command: "+cmd);
		} else {
			LOG.trace(pluginOption);
			pluginOption.setProject(projectDir);
			pluginOptions.add(pluginOption);
		}
	}
	
	private List<String> preprocess(List<String> cmds0) {
		List<String> cmds = new ArrayList<String>();
		for (String cmd0 : cmds0) {
			String cmd = symbolMap.get(cmd0);
			if (cmd == null) {
				cmds.add(cmd0); 
			} else {
				cmds.addAll(Arrays.asList(cmd.split("\\s+")));
			}
		}
		return cmds;
	}

	public void runNormaIfNecessary() {
		if (!new CProject(projectDir).hasScholarlyHTML(0.1)) {
			String args = "-i fulltext.xml -o scholarly.html --transform nlm2html --project "+projectDir;
			LOG.debug("running NORMA "+args);
			new Norma().run(args);
		}
	}

	public void addCommand(String cmd) {
		cmdList.add(cmd);
	}

	public void setDefaultCommands(String cmds) {
		setDefaultCommands(Arrays.asList(cmds.split("\\s+")));
	}

	public void setDefaultCommands(List<String> cmds) {
		List<String> commands = new ArrayList<String>();
		boolean start = true;
		for (String cmd : cmds) {
			String command = lookup(cmd);
			if (command == null) {
				LOG.warn("abbreviation ignored: "+cmd);
				continue;
			}
			commands.add(command);
		}
		this.processCommands(commands);
	}

	private String lookup(String cmd) {
		return null;
	}
	
	public void createDataTables() throws IOException {
		System.out.println("create data tables");
		if (projectDir == null) {
			throw new RuntimeException("projectDir must be set");
		}
		DataTablesTool dataTablesTool = DataTablesTool.createBiblioEnabledTable();
		dataTablesTool.setProjectDir(projectDir);
		dataTablesTool.setMetadataByTreename(metadataByCTreename);
		
		ResultsAnalysisImpl resultsAnalysis = new ResultsAnalysisImpl(dataTablesTool);
		resultsAnalysis.addDefaultSnippets(this.projectDir);
		resultsAnalysis.setRemoteLink0(EPMCConverter.HTTP_EUROPEPMC_ORG_ARTICLES);
		resultsAnalysis.setRemoteLink1("");
		resultsAnalysis.setLocalLink0("");
		resultsAnalysis.setLocalLink1(ResultsAnalysisImpl.SCHOLARLY_HTML);
		resultsAnalysis.setRowHeadingName("EPMCID");

		dataTablesTool.createTableComponents(resultsAnalysis);
	}

	public static void main(String[] args) throws IOException {
		CommandProcessor commandProcessor = new CommandProcessor();
		if (args.length == 0) {
			help();
		} else {
			// first arg is projectDir
			commandProcessor.setProjectDir(new File(args[0]));
			List<String> commands = getDefaultCommands();
			if (args.length > 1) {
				commands = new ArrayList<String>(Arrays.asList(args));
				// remove projectDir
				commands.remove(0);
			}
			commandProcessor.processCommands(commands);
			commandProcessor.createDataTables();
		}
	}

	/** other commands
	 * 	<symbol abbreviation="g_h" expand="gene(human)"/>
	<symbol abbreviation="sp_b" expand="species(binomial)"/>
	<symbol abbreviation="sp_g" expand="species(genus)"/>
	<symbol abbreviation="sq_p" expand="sequence(dnaprimer)"/>
	<symbol abbreviation="sq_d" expand="sequence(dna)"/>
	<symbol abbreviation="w_f" expand="word(frequencies)"/>
	<symbol abbreviation="w_fstop" expand="word(frequencies)xpath:@count>20~w.stopwords:pmcstop.txt_stopwords.txt"/>
	<symbol abbreviation="sm_d" expand="summary(datatables) --filter snippets"/>
	<symbol abbreviation="s_inn" expand="search(inn)"/>
	<symbol abbreviation="s_tv" expand="search(tropicalVirus)"/>
	<symbol abbreviation="s_nal" expand="search(nal)"/>
	<symbol abbreviation="s_phch" expand="search(phytochemicals1)"/>
	<symbol abbreviation="s_optr" expand="search(opentrials)"/>

	 * @return
	 */
	private static List<String> getDefaultCommands() {
		String[] cmds = {
				"word(frequencies)xpath:@count>20~w.stopwords:pmcstop.txt_stopwords.txt",
				"sequence(dnaprimer)",
				"gene(human)",
				"species(genus)",
				"species(binomial)"
		};
		return Arrays.asList(cmds);
	}

	private static void help() {
		System.err.println("Command processor: \n"
				+ "   cproject projectDir [command [command]...]");
	}

	public List<AMIPluginOption> getPluginOptions() {
		return pluginOptions;
	}

	public void runLegacyPluginOptions(AbstractAMITool amiTool) {
		List<AMIPluginOption> pluginOptions = this.getPluginOptions();
		AMISearchTool amiSearchTool = (amiTool != null && amiTool instanceof AMISearchTool) ? (AMISearchTool) amiTool : null;
		for (AMIPluginOption pluginOption : pluginOptions) {
			String plugin = pluginOption.getPlugin();
			System.out.println("running: " + plugin + "; " + pluginOption);
			if (amiSearchTool != null && amiSearchTool.getIgnorePluginList().contains(plugin)) {
				System.out.println("ignored: " + plugin);
				continue;
			}
			try {
				pluginOption.run();
			} catch (Exception e) {
				System.err.println("cannot run command: "+pluginOption +"; " + e.getMessage());
				continue;
			}
			System.out.println("filter: "+pluginOption);
			pluginOption.runFilterResultsXMLOptions();
			System.out.println("summary: "+pluginOption);
			pluginOption.runSummaryAndCountOptions(); 
		}
		LOG.trace(pluginOptions);
	}
	
	public void runCommands(AbstractAMITool amiTool) {
		runNormaIfNecessary();
		runLegacyPluginOptions(amiTool);
	}



	public void runJsonBibliography() {
		CProject cProject = new CProject(projectDir);
		metadataByCTreename = new HashMap<>();
		EPMCConverter epmcConverter = new EPMCConverter();
		for (CTree cTree : cProject.getOrCreateCTreeList()) {
			AbstractMetadata metadataEntry = cTree.readMetadata(epmcConverter, cTree.getAllowedChildFile(CTree.EUPMC_RESULT_JSON));
			metadataByCTreename.put(cTree.getName(), metadataEntry);
		}
		LOG.trace("keys "+metadataByCTreename.keySet());
	}

	public Map<String, AbstractMetadata> getMetadataByCTreename() {
		return metadataByCTreename;
	}

	public void setAbstractAMITool(AbstractAMITool abstractAMITool) {
		this.abstractAMITool = abstractAMITool;
	}



}
