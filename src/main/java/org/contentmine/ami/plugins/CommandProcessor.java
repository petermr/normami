package org.contentmine.ami.plugins;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.io.FilenameUtils;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.contentmine.ami.plugins.ResultsAnalysis.SummaryType;
import org.contentmine.cproject.files.CProject;
import org.contentmine.cproject.files.OptionFlag;
import org.contentmine.cproject.files.ResourceLocation;
import org.contentmine.cproject.util.CellRenderer;
import org.contentmine.cproject.util.DataTablesTool;
import org.contentmine.graphics.html.HtmlHtml;
import org.contentmine.graphics.html.HtmlTable;
import org.contentmine.graphics.html.HtmlTd;
import org.contentmine.norma.NAConstants;
import org.contentmine.norma.Norma;
import org.contentmine.norma.biblio.json.EPMCConverter;
import org.contentmine.eucl.xml.XMLUtil;

import nu.xom.Element;

/** processes commandline , higher level functions
 * 
 * @author pm286
 *
 */
public class CommandProcessor {


	private static final Logger LOG = Logger.getLogger(CommandProcessor.class);
	static {
		LOG.setLevel(Level.DEBUG);
	}
	
	public final static String SYMBOLS = NAConstants.PLUGINS_RESOURCE+"/symbols.xml";
	private static final String EXPAND = "expand";
	private static final String ABBREVIATION = "abbreviation";
	private static final String ARTICLES = "articles";

	private List<AMIPluginOption> pluginOptions;
	private File projectDir = null;
	private List<String> cmdList = new ArrayList<String>();

	private Map<String, String> symbolMap;
	private String helpString;
	
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
		runCommands();
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

	public void runCommands() {
		runNormaIfNecessary();
		for (AMIPluginOption pluginOption : pluginOptions) {
			LOG.debug("running: "+pluginOption);
			try {
				pluginOption.run();
			} catch (Exception e) {
				LOG.error("cannot run command: "+pluginOption +"; " + e.getMessage());
				continue;
			}
			System.out.println("filter: "+pluginOption);
			pluginOption.runFilterResultsXMLOptions();
			System.out.println("summary: "+pluginOption);
			pluginOption.runSummaryAndCountOptions(); 
		}
		LOG.trace(pluginOptions);
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
		if (projectDir == null) {
			throw new RuntimeException("projectDir must be set");
		}
		String project = FilenameUtils.getBaseName(projectDir.toString());
		DataTablesTool dataTablesTool = new DataTablesTool(ARTICLES);
		dataTablesTool.setTitle(project);
		ResultsAnalysis resultsAnalysis = new ResultsAnalysis(dataTablesTool);
		resultsAnalysis.addDefaultSnippets(projectDir);
		resultsAnalysis.setRemoteLink0(EPMCConverter.HTTP_EUROPEPMC_ORG_ARTICLES);
		resultsAnalysis.setRemoteLink1("");
		resultsAnalysis.setLocalLink0("");
		resultsAnalysis.setLocalLink1(ResultsAnalysis.SCHOLARLY_HTML);
		resultsAnalysis.setRowHeadingName("EPMCID");
		for (SummaryType cellType : ResultsAnalysis.SUMMARY_TYPES) {
			resultsAnalysis.setCellContentFlag(cellType);
			HtmlTable table = resultsAnalysis.makeHtmlDataTable();
			HtmlHtml html = dataTablesTool.createHtmlWithDataTable(table);
			File outfile = new File(projectDir, cellType.toString()+"."+CProject.DATA_TABLES_HTML);
			XMLUtil.debug(html, outfile, 1);
		}
		LOG.trace(dataTablesTool.cellRendererList);
		List<HtmlTd> footerList = new ArrayList<HtmlTd>();
		for (CellRenderer cellRenderer : dataTablesTool.cellRendererList) {
			HtmlTd td = new HtmlTd();
			td.appendChild(cellRenderer.getHeading());
			footerList.add(td);
		}
		HtmlTd caption = new HtmlTd();
		caption.appendChild("coun-ts");
		dataTablesTool.setFooterCaption(caption);
		dataTablesTool.setFooterCells(footerList);
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

}
