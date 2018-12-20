package org.contentmine.ami;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.nio.file.FileStore;
import java.nio.file.FileSystem;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import org.apache.commons.io.FilenameUtils;
import org.apache.commons.io.IOUtils;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.contentmine.ami.dictionary.CMJsonDictionary;
import org.contentmine.ami.dictionary.DefaultAMIDictionary;
import org.contentmine.ami.dictionary.DictionaryTerm;
import org.contentmine.ami.lookups.WikipediaDictionary;
import org.contentmine.cproject.files.DebugPrint;
import org.contentmine.cproject.util.CMineGlobber;
import org.contentmine.cproject.util.RectangularTable;
import org.contentmine.eucl.euclid.Util;
import org.contentmine.eucl.xml.XMLUtil;
import org.contentmine.graphics.html.HtmlA;
import org.contentmine.graphics.html.HtmlDiv;
import org.contentmine.graphics.html.HtmlElement;
import org.contentmine.graphics.html.HtmlLi;
import org.contentmine.graphics.html.HtmlTable;
import org.contentmine.graphics.html.HtmlTbody;
import org.contentmine.graphics.html.HtmlTd;
import org.contentmine.graphics.html.HtmlTr;
import org.contentmine.graphics.html.HtmlUl;
import org.contentmine.norma.NAConstants;
import org.contentmine.norma.picocli.AbstractAMIProcessor;

import com.google.common.collect.Lists;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import nu.xom.Attribute;
import nu.xom.Element;
import picocli.CommandLine;
import picocli.CommandLine.Command;
import picocli.CommandLine.Mixin;
import picocli.CommandLine.Option;
import picocli.CommandLine.Parameters;

/** mainly to manage help and parse dictionaries.
 * 
 * @author pm286
 *
 */

@Command(
		description = "Manages AMI dictionaries",
		name = "ami-dictionary",
		mixinStandardHelpOptions = true,
		version = "ami 0.1"
		)

public class AMIDictionary extends AbstractAMIProcessor {
	public static final Logger LOG = Logger.getLogger(AMIDictionary.class);
	static {
		LOG.setLevel(Level.DEBUG);
	}
	
	public static final String ALL = "ALL";
	public static final String FULL = "FULL";
	public static final String HELP = "HELP";
	public static final String LIST = "LIST";
	public static final String SEARCH = "search";
	private static final String XML = "xml";
	private static final int DEFAULT_MAX_ENTRIES = 20;
	
	private static final File DICTIONARY_TOP = NAConstants.DICTIONARY_DIR;
	private static final String HTTPS_EN_WIKIPEDIA_ORG = "https://en.wikipedia.org";
	public final static String WIKIPEDIA_BASE = HTTPS_EN_WIKIPEDIA_ORG + "/wiki/";
	private static final String WIKITABLE = "wikitable";

	private static final String HTTP = "http";

	public enum LinkField {
		HREF,
		VALUE,
	}

	public enum Operation {
		create,
		display,;
		public static Operation getOperation(String operationS) {
			for (int i = 0; i < values().length; i++) {
				Operation operation = values()[i];
				if (operation.toString().equalsIgnoreCase(operationS)) {
					return operation;
				}
			}
			return null;
		}
		
	}
	
	/** ugly lowercase, but I don't yet know how to use
	 * 		CommandLine::setCaseInsensitiveEnumValuesAllowed=true
	 * @author pm286
	 *
	 */
	enum DictionaryFileFormat {
		 xml,
		 html,
		 json,
		 }
	
	/** ugly lowercase, but I don't yet know how to use
	 * 		CommandLine::setCaseInsensitiveEnumValuesAllowed=true
	 * @author pm286
	 *
	 */
	public enum RawFileFormat {
		 html,
		 pdf,
		 xml,
		 }
	
	enum InputFormat {
		csv,
		wikipage,
		wikitable
	}

    @Parameters(index = "0",
    		arity="0..*",
    		split=",",
    		description = "Operation to apply (${COMPLETION-CANDIDATES}); absence runs help"
    		)
    private Operation operation;

    @Option(names = {"--booleanquery"}, 
    		arity="0..1",
   		    description = "generate query as series of chained OR phrases"
    		)
    private boolean booleanQuery;
    
    @Option(names = {"--datacols"}, 
    		split=",",
    		arity="1..*",
    	    paramLabel = "datacol",
   		description = "data column/s from table; not hyperlinked"
    		)
    private String[] dataCols;
    
    @Option(names = {"-d", "--dictionary"}, 
    		arity="1",
//    	    paramLabel = "THE DICT",
    		description = "input or output dictionary")
    private String dictionary;

    @Option(names = {"--directory"}, 
    		arity="1",
//   	    	paramLabel = "directory",
    		description = "directory containing dictionary")
    private String directory;

    @Option(names = {"--hrefcols"}, 
    		split=",",
    		arity="1..*",
    	 	paramLabel = "hrefcol",
   		description = "external hyperlink column from table; might be Wikidata or remote site(s)"
    		)
    private String[] hrefCols;
        
    @Option(names = {"--hreftext"}, 
    		arity="0",
    		description = "hyperlinks from text (maybe excludes tables);"
    				+ " requires wikipedia or wikitable input at present; still under test"
    		)
	public String href;
    
    @Option(names = {"--informat"}, 
    		arity="1",
    		paramLabel = "input format",
    		description = "input format (${COMPLETION-CANDIDATES})"
    		)
    private InputFormat informat;
    
    @Option(names = {"-i", "--input"}, 
    		arity="1",
    		description = "input stream; URL if starts with 'http' else file"
    		)
    private String input;
    
    @Option(names = {"--linkcol"}, 
    		arity="1",
    		description = "column to extract link to internal pages. main use Wikipedia. Defaults to the 'name' column"
    		)
	public String linkCol;

    @Option(names = {"--namecol"}, 
    		split=",",
    		arity="1..*",
    		description = "column(s) to extract name; use exact case (e.g. Common name)"
    		)
	public String nameCol;
    
    @Option(names = {"--outformats"}, 
    		arity="1..*",
    		split=",",
    	    paramLabel = "output format",
    		description = "output format (${COMPLETION-CANDIDATES})"
    		)
    private DictionaryFileFormat[] outformats = new DictionaryFileFormat[] {DictionaryFileFormat.xml};
        
    @Option(names = {"--splitcol"}, 
    		arity="1",
    		paramLabel="input separator",
    		description = "character to split input values; (default: ${DEFAULT-VALUE})"
    		)
    private String splitCol=",";
        
    @Option(names = {"--termcol"}, 
    		arity="1",
    		description = "column(s) to extract term; use exact case (e.g. Term). Could be same as namecol"
    		)
	public String termCol;
    
    @Option(names = {"--terms"}, 
    		arity="1..*",
    		split=",",
    		description = "list of terms (entries), comma-separated")
    private String[] terms;

    @Mixin CProjectTreeMixin proTree;
    
    DictionaryData dictionaryData;
    
// converted from args    
	private List<String> termList;
	private List<String> nameList;
	private List<String> linkList;
	private List<List<String>> hrefColList;
	private List<List<String>> dataColList;
//	private List<String> outformatList;
	
	private HtmlTable simpleTable;
	public HtmlUl simpleList;
	private Element dictionaryElement;
	private HtmlTbody tBody;
	private WikipediaDictionary wikipediaDictionary;
	private InputStream inputStream;
	private RectangularTable rectangularTable;

	private List<File> files;
	private List<Path> paths;
	private File dictionaryDir = DICTIONARY_TOP;
	private int maxEntries = 0;
	private AMICLI cli;



	public static void main(String[] args) {
        AMIDictionary amiDictionary = new AMIDictionary();
        amiDictionary.initDictionaryData();
        
        testing(args, amiDictionary);
//		amiDictionary.runCommands(args);
	}

	private static void testing(String[] args, AMIDictionary amiDictionary) {
		CommandLine commandLine = new CommandLine(amiDictionary);
        commandLine.addMixin("xyzMixin ", new CProjectTreeMixin());
        commandLine.parse(new String[]{"--help", "--wombatx", "176"});
//        LOG.debug("MIX "+commandLine.xyzMixin.getWombatx());
//        if (1 ==1) return;
        commandLine.parse(args);
        Map<String, Object> map = commandLine.getMixins();
        for (String k : map.keySet()) {
        	Object o = map.get(k);
        	if (o instanceof CProjectTreeMixin) {
        		CProjectTreeMixin mixin = (CProjectTreeMixin) o;
        		LOG.debug("wombx "+mixin.getWombatx());
        	}
        	LOG.debug("O: "+o.getClass());
        }
	}
	
	private void initDictionaryData() {
		dictionaryData = new DictionaryData();
		
        dictionaryData.dataCols    = dataCols;
        dictionaryData.dictionary  = dictionary;
        dictionaryData.directory   = directory;
        dictionaryData.href        = href;
        dictionaryData.hrefCols    = hrefCols;
        dictionaryData.informat    = informat;
        dictionaryData.input       = input;
        dictionaryData.linkCol     = linkCol;
        dictionaryData.log4j       = log4j;
        dictionaryData.nameCol     = nameCol;
        dictionaryData.operation   = operation;
        dictionaryData.outformats  = outformats;
        dictionaryData.termCol     = termCol;
        dictionaryData.terms       = terms;

	}
	
	@Override
	protected void parseSpecifics() {
		printDebug();
	}

	@Override
	protected void runSpecifics() {
        runDictionary();
	}


	private void runDictionary() {
		if (Operation.display.equals(operation)) {
			displayDictionaries();
		} else if (Operation.create.equals(operation)) {
			createDictionary();
		} else {
			LOG.debug("no operation given: "+operation);
		}
	}

//	private void createDictionaryData() {
//	    String[] dataCols;
//	    String   dictionary;
//	    String   directory;
//		String   href;
//	    String[] hrefCols;
//	    String   informat;
//	    String   input;
//		String   linkCol;
//		String   nameCol;
//	    String   operation;
//	    String[] outformats;
//	    String   splitCol=",";
//		String   termCol;
//	    String[] terms;
//
//	}

	private void displayDictionaries() {
		LOG.debug("display NYI");
	}

	private void createDictionary() {
		openInputStream();
    	if (inputStream != null) {
    		if (informat == null) {
    			throw new RuntimeException("no input format given ");
    		} else if (InputFormat.wikipage.equals(informat)) {
	    		wikipediaDictionary = new WikipediaDictionary();
	    		readWikipediaPage(wikipediaDictionary, inputStream);
    		} else if (InputFormat.wikitable.equals(informat)) {
	    		wikipediaDictionary = new WikipediaDictionary();
	    		readWikipediaPage(wikipediaDictionary, inputStream);
    		} else if (InputFormat.csv.equals(informat)) {
    			readCSV(inputStream);
    		} else {
    			throw new RuntimeException("unknown inputformat: "+informat);
    		}
    	} else {
    		termList = terms == null ? null : Arrays.asList(terms);
    	}
    	synchroniseTermsAndNames();
//    	printDebug();
    	dictionaryElement = DefaultAMIDictionary.createDictionaryWithTitle(dictionary);
    	writeNamesAndLinks();
		
	}

	private void openInputStream() {
		try {
			inputStream = input.startsWith("http") ? new URL(input).openStream() : new FileInputStream(new File(input));
		} catch (IOException e) {
			throw new RuntimeException("cannot read/open stream", e);
		}
	}

	private RectangularTable readCSV(InputStream inputStream) {
		boolean useHeader = true;
		rectangularTable = null;
		try {
			rectangularTable = RectangularTable.readCSVTable(inputStream, useHeader);
		} catch (IOException e) {
			throw new RuntimeException("cannot read table", e);
		}
		if (termCol == null) {
			throw new RuntimeException("must give termCol");
		}
		termList = rectangularTable.getColumn(termCol);
		if (termList == null) {
			throw new RuntimeException("Cannot find term column");
		}
		nameList = rectangularTable.getColumn(nameCol);
		if (dataCols != null) {
			dataColList = rectangularTable.getColumnList(dataCols);
			checkColumnsNotNull(dataColList, dataCols);
		}
		if (hrefCols != null) {
			hrefColList = rectangularTable.getColumnList(hrefCols);
			checkColumnsNotNull(hrefColList, hrefCols);
		}
		return rectangularTable;
	}

	private void checkColumnsNotNull(List<List<String>> colList, String[] colNames) {
		for (int i = 0; i < colList.size(); i++) {
			if (colList.get(i) == null) {
				LOG.warn("Cannot find column: "+colNames[i]);
			}
		}
	}

	private List<Integer> getColIndexList(List<String> headers, String[] colNamesArray) {
		List<Integer> hrefIndexList = new ArrayList<Integer>();
		for (String colName : colNamesArray) {
			int colIndex = headers.indexOf(colName);
			if (colIndex == -1) {
				LOG.error("Unknown column heading: " + colName);
			}
			hrefIndexList.add(new Integer(colIndex));
		}
		return hrefIndexList;
	}

	private void synchroniseTermsAndNames() {
		if (nameList == null && termList != null) {
    		nameList = termList;
    	} else if (termList == null && nameList != null) {
    		termList = nameList;
    	}
	}

	private void writeNamesAndLinks() {
		if (nameList == null) {
			LOG.debug("no names to create ditionary");
			return;
		}
		addEntriesToDictionaryElement();
		createAndAddQueryElement();
		writeDictionaries();
		return;
	}

	private void createAndAddQueryElement() {
		Element query = new Element("query");
		StringBuilder sb = new StringBuilder();
		for (int i = 0; i < nameList.size(); i++) {
			String name = nameList.get(i);
			if (i > 0) {
				sb.append(" OR ");
			}
			sb.append("('" + name + "')");
		}
		query.appendChild(sb.toString());
		dictionaryElement.appendChild(query);
	}

	private void addEntriesToDictionaryElement() {
		for (int i = 0; i < nameList.size(); i++) {
			Element entry = DefaultAMIDictionary.createEntryElementFromTerm(termList.get(i));
			entry.addAttribute(new Attribute(DictionaryTerm.ID, "CM."+dictionary+"."+i));
			entry.addAttribute(new Attribute(DictionaryTerm.NAME, nameList.get(i)));
			String link = linkList == null ? null : linkList.get(i);
			if (link != null && !link.equals("")) {
				entry.addAttribute(new Attribute(DictionaryTerm.URL, link));
			}
			dictionaryElement.appendChild(entry);
		}
		return;
	}

	private void writeDictionaries() {
		if (directory == null) {
			throw new RuntimeException("must give directory for dictionaries");
		}
		if (outformats != null) {
			List<DictionaryFileFormat> outformatList = Arrays.asList(outformats);
			for (DictionaryFileFormat outformat : outformatList) {
				File outfile = new File(new File(directory), dictionary+"."+outformat);
				LOG.debug("writing to "+outfile);
				try {
					outputDictionary(outfile, outformat);
				} catch (IOException e) {
					throw new RuntimeException("cannot write file "+outfile, e);
				}
			}
		}
	}
	
	private void outputDictionary(File outfile, DictionaryFileFormat outformat) throws IOException {
		LOG.debug("writing dictionary to "+outfile.getAbsolutePath());
		FileOutputStream fos = new FileOutputStream(outfile);
		if (outformat.equals(DictionaryFileFormat.xml)) {
			XMLUtil.debug(dictionaryElement, fos, 1);
		} else if (outformat.equals(DictionaryFileFormat.json)) {
			String jsonS = createJson(dictionaryElement);
			IOUtils.write(jsonS, fos, "UTF-8");
		} else if (outformat.equals(DictionaryFileFormat.html)) {
			HtmlDiv div = createHtml();
			if (div != null) {
				String xmlS = div.toXML();
				IOUtils.write(xmlS, fos, "UTF-8");
			}
		}
		try {
			fos.close();
		} catch (Exception e) {
			throw new RuntimeException("cannot close ", e);
		}
		return;
	}

	/** 
	 // FIXME messy to use DefaultAMIDictionary
	 * 
	 * @return
	 */
	private HtmlDiv createHtml() {
		DefaultAMIDictionary dictionary = new DefaultAMIDictionary();
		dictionary.readDictionaryElement(dictionaryElement);
		HtmlDiv div = dictionary.createHtmlElement();
		return div;
	}

	/** prettyPrinting is done here.
	 * 
	 * @param dictionaryElement
	 * @return
	 */
	private String createJson(Element dictionaryElement) {
		CMJsonDictionary cmJsonDictionary = CMJsonDictionary.createCMJsonDictionary(dictionaryElement);
		// this may be overkill
		JsonObject json = new JsonParser().parse(cmJsonDictionary.toString()).getAsJsonObject();
	    return Util.prettyPrintJson(json);
	}

	public Element getDictionaryElement() {
		return dictionaryElement;
	}
    
	private void printDebug() {
		System.out.println("dataCols      "+dataCols);
		System.out.println("dictionary    "+dictionary);
		System.out.println("directory     "+directory);
		System.out.println("href          "+href);
		System.out.println("hrefCols      "+hrefCols);
		System.out.println("input         "+input);
		System.out.println("informat      "+informat);
		System.out.println("linkCol       "+linkCol);
		System.out.println("log4j         "+makeArrayList(log4j));
		System.out.println("nameCol       "+nameCol);
		System.out.println("operation     "+operation);
		System.out.println("outformats    "+makeArrayList(outformats));
		System.out.println("splitCol      "+splitCol);
		System.out.println("termCol       "+termCol);
		System.out.println("terms         "+(termList == null ? null : "("+termList.size()+") "+termList));
	}

	private List<?> makeArrayList(Object[] list) {
		return list == null ? null : Arrays.asList(list);
	}
	

	public HtmlUl getSimpleList() {
		return simpleList;
	}
	

	/** create from page with hyperlinks
	 * recommended to trim rubbish from HtmlElement first
	 * 
	 * @param htmlElement
	 * @return
	 */
	public HtmlUl createListOfHyperlinks(HtmlElement htmlElement) {
		nameList = new ArrayList<String>();
		linkList = new ArrayList<String>();
		List<HtmlA> aList = HtmlA.extractSelfAndDescendantAs(htmlElement);
		HtmlUl ul = new HtmlUl();
		for (HtmlA a : aList) {
			nameList.add(a.getValue());
			linkList.add(a.getHref());
			HtmlLi li = new HtmlLi();
			li.appendChild(a.copy());
			ul.appendChild(li);
		}
		return ul;
	}

	public void createFromEmbeddedWikipediaTable(HtmlElement htmlElement) {
		List<HtmlTable> tableList = HtmlTable.extractSelfAndDescendantTables(htmlElement);

		nameList = new ArrayList<String>();
		linkList = new ArrayList<String>();
		for (HtmlTable table : tableList) {
			if (table.getClassAttribute().contains(WIKITABLE)) {
				addTableNamesAndHrefs(table);
			}
		}
		simpleTable = new HtmlTable();
		createSingleRowTableFromNamesHref();
	}

	/** probably superfluous now
	 * 
	 */
	private void createSingleRowTableFromNamesHref() {
		for (int i = 0; i < nameList.size(); i++) {
			String name = nameList.get(i);
			String href = linkList.get(i);
			HtmlTr row = new HtmlTr();
			HtmlTd td = new HtmlTd();
			if (href == null || href.equals("")) {
				td.setValue(name);
			} else {
				HtmlA a = new HtmlA();
				a.setHref(href);
				a.setValue(name);
				td.appendChild(a);
			}
			row.appendChild(td);
			simpleTable.appendChild(row);
		}
	}

	public HtmlElement readHtmlElement(InputStream inputStream) {
		HtmlElement htmlElement = null;
		try {
			Element rootElement = XMLUtil.parseQuietlyToRootElement(inputStream);
			boolean ignoreNamespaces = true;
			htmlElement = HtmlElement.create(rootElement, false, ignoreNamespaces);
		} catch (Exception e) {
			throw new RuntimeException("cannot find/parse URL ", e);
		}
		return htmlElement;
	}
	
	public HtmlElement getSimpleDictionary() {
		return simpleTable != null ? simpleTable : simpleList;
	}

	private void addTableNamesAndHrefs(HtmlTable table) {
		tBody = table.getTbody();
		String colName = nameCol.trim();
		int nameColIndex = tBody.getColumnIndex(colName);
		if (nameColIndex < 0) {LOG.debug("cannot find column: "+colName);
			return;
		}
		List<String> names = this.getColumnValues(nameColIndex);
		int linkColIndex = tBody.getColumnIndex(linkCol.trim());
		List<String> hrefs = this.getColumnHrefs(LinkField.HREF, linkColIndex, HTTPS_EN_WIKIPEDIA_ORG);
		if (names.size() == 0) {
			LOG.debug("no names found");
			return;
		}
		//remove first element as it's the columnn heading
		removeFirstElementsOfColumns(names, hrefs);

		if (names.size() != hrefs.size()) {
			LOG.warn("names and hrefs do not balance");
		}
		addUniqueNamesAndHrefs(names, hrefs);
	}
	
    /** get column values (as text)
     * skips header
     * 
     * @param colIndex
     * @return list of values
     */
	public List<String> getColumnValues(int colIndex) {
		return addCells(colIndex, LinkField.VALUE, (String) null);
	}

	private List<String> addCells(int colIndex, LinkField field, String base) {
		List<String> valueList = new ArrayList<String>();
		if (colIndex >= 0) {
			tBody.getOrCreateChildTrs();
			int ncols = -1;
			for (int i = 0; i < tBody.getRowList().size(); i++) {
				HtmlTr row = tBody.getRowList().get(i);
				// unfortunately Th is also found
				List<HtmlElement> tdthChildren = row.getTdOrThChildren();
				int size = tdthChildren.size();
				if (size == 0) {
					// skip header and empty rows
					continue;
				}
				if (ncols == -1) {
					ncols = size;
				} else if (size > ncols) {
					LOG.debug("probably split cells in row "+i);
					continue;
				} else if (size < ncols) {
					LOG.debug("probably fused cells in row "+i);
					continue;
				}
				List<String> linkFields = addValueFromContentOrHref(tdthChildren.get(colIndex), field, base);
				valueList.addAll(linkFields);
			}
		}
		return valueList;
	}

	/**
	 * 
	 * @param child
	 * @param type either "value" or "href"
	 * @param base if _
	 * @param valueList
	 */
	private List<String> addValueFromContentOrHref(HtmlElement child, LinkField field, String base) {
		List<HtmlA> aList = HtmlA.extractSelfAndDescendantAs(child);
		List<String> resultList = new ArrayList<String>();
		for (HtmlA a : aList) {
			String value = null;
			if (LinkField.VALUE.equals(field)) {
				value = a.getValue().trim();
			} else {
				value = a.getHref();
				if (value != null && !value.startsWith(HTTP)) {
					value = base + value;
				}
			}
			resultList.add(value);
		}
		return resultList;
	}
	
    /** get column links (hrefs)
     * skips header
     * 
     * @param base if not empty represents the base for the URL
     * @param colIndex
     * @return list of values
     */
	public List<String> getColumnHrefs(LinkField field, int colIndex, String base) {
		List<String> valueList = addCells(colIndex, field, base);
		return valueList;
	}
	

	private void removeFirstElementsOfColumns(List<String> names, List<String> hrefs) {
		names.remove(0);
		if (hrefs.size() == 0) {
			LOG.debug("no links found");
			hrefs = new ArrayList<String>(names.size());
		} else {
			hrefs.remove(0);
		}
	}

	private void addUniqueNamesAndHrefs(List<String> names, List<String> hrefs) {
		for (int i = 0; i < names.size(); i++) {
			String name = names.get(i);
			if (nameList.contains(name)) {
				LOG.debug("dup: " + name);
				continue;
			}
			nameList.add(names.get(i));
			linkList.add(hrefs.get(i));
		}
	}

	public void runHelp(List<String> argList) {
		if (argList.size() > 0) argList.remove(0);
		this.help(argList);
	}


	public AMIDictionary() {
		initDict();
	}
	
	private void initDict() {
		dictionaryDir = NAConstants.DICTIONARY_DIR;
		cli = new DictionaryCLI();
	}
	
	/** this uses FILES */
	public void listDictionaries(List<String> argList) {
		File dictionaryHead = getDictionaryHead();
		files = listDictionaryFiles(dictionaryHead);
		
		if (argList.size() == 1 && argList.get(0).toUpperCase().equals(LIST)) {
			DebugPrint.debugPrint("list all FILE dictionaries "+files.size());
			for (File file : files) {
				listDictionaryInfo(FilenameUtils.getBaseName(file.getName()));
			}
		} else if (argList.size() >= 1 && argList.get(0).toUpperCase().equals(FULL)) {
			argList.remove(0);
			setMaxEntries(DEFAULT_MAX_ENTRIES);
			if (argList.size() >= 1) {
				String arg = argList.get(0);
				try {
					setMaxEntries(Integer.parseInt(arg));
					argList.remove(0);
				} catch (NumberFormatException nfe) {
//					DebugPrint.debugPrintln(Level.ERROR, "Requires maxEntries, found: "+arg);
				}
			}
			for (String arg : argList) {
				listDictionaryInfo(arg);
			}
		} else {
			listAllDictionariesBriefly();
			for (String arg : argList) {
				listDictionaryInfo(arg);
			}
		}
	}
	
	public File getDictionaryHead() {
		return dictionaryDir;
	}
	
	public void listDictionaryPaths(List<String> argList) {
//		File dictionaryHead = new File(NAConstants.MAIN_AMI_DIR, "plugins/dictionary");
		try {
			String pathname = NAConstants.DICTIONARY_RESOURCE;
			LOG.debug("PATHNAME "+pathname);
			pathname = "/"+"org/contentmine/ami/plugins/dictionary";
			final Path path = Paths.get(String.class.getResource(pathname).toURI());
			LOG.debug("PATH "+path);
			FileSystem fileSystem = path.getFileSystem();
			List<FileStore> fileStores = Lists.newArrayList(fileSystem.getFileStores());
			LOG.debug(fileStores.size());
			for (FileStore fileStore : fileStores) {
				LOG.debug("F"+fileStore);
			}
			final byte[] bytes = Files.readAllBytes(path);
			String fileContent = new String(bytes/*, CHARSET_ASCII*/);
		} catch (Exception e) {
			LOG.error(e);
		}
	}
	
	public void help(List<String> argList) {
		LOG.error("shouldn't use this help?");
		System.err.println("Dictionary processor");
		System.err.println("    dictionaries are normally added as arguments to search "
				+ "(e.g. ami-search-cooccur [dictionary [dictionary ...]]");
		if (argList.size() == 0) {
			File parentFile = files == null || files.size() == 0 ? null : files.get(0).getParentFile();
			DebugPrint.debugPrint("\nlist of dictionaries taken from AMI dictionary list (" + parentFile + "):\n");
		} else {
			DebugPrint.debugPrint("\nlist of dictionaries taken from : "+argList+"\n");
		}
		AMIDictionary dictionaries = new AMIDictionary();
		files = dictionaries.getDictionaries();
//		paths = dictionaries.getDictionaryPaths();
		listAllDictionariesBriefly();
//		listAllDictionariesBrieflyPaths();
	}

	public List<File> getDictionaries() {
		DebugPrint.debugPrint(" * dictionaries from: "+dictionaryDir);
		File xmlDictionaryDir = getXMLDictionaryDir(dictionaryDir);
		files = new CMineGlobber().setRegex(".*\\.xml").setLocation(xmlDictionaryDir).setRecurse(true).listFiles();
//		File[] fileArray = xmlDictionaryDir.listFiles(new FilenameFilter() {
//			public boolean accept(File dir, String name) {
////				LOG.debug("d"+dir+"/"+name);
//				return name != null && name.endsWith(".xml");
//			}
//		});
//		files = fileArray == null ? new ArrayList<File>() : Arrays.asList(fileArray);
		Collections.sort(files);
		return files;
	}

	private File getXMLDictionaryDir(File dictionaryDir) {
		return new File(dictionaryDir, "xml/");
	}

	/** uses directories */
	private void listDictionaryInfo(String dictionaryName) {
		File dictionaryFile = null;
		for (File file : files) {
			String baseName = FilenameUtils.getBaseName(file.getName());
			if (dictionaryName.equals(baseName)) {
				listDictionaryInfo(file, baseName);
				dictionaryFile = file;
				break;
			} else {
			}
		}
		if (dictionaryFile == null) {
			System.err.println("\nUnknown dictionary: "+dictionaryName);
		}
	}

	/** not yet used */
	private void listDictionaryInfoPath(File file, String dictionary) {
		Element dictionaryElement = null;
		try {
			dictionaryElement = XMLUtil.parseQuietlyToRootElement(new FileInputStream(file));
		} catch (FileNotFoundException e) {
			throw new RuntimeException("Cannot find "+file);
		}
		listDictionaryInfo(dictionary, dictionaryElement);
		
	}

	private void listDictionaryInfoPath(String dictionaryName) {
		File dictionaryFile = null;
		for (File file : files) {
			String baseName = FilenameUtils.getBaseName(file.getName());
			if (dictionaryName.equals(baseName)) {
				listDictionaryInfo(file, baseName);
				dictionaryFile = file;
				break;
			} else {
			}
		}
		if (dictionaryFile == null) {
			System.err.println("\nUnknown dictionary: "+dictionaryName);
		}
	}

	private void listDictionaryInfo(File file, String dictionaryName) {
		Element dictionaryElement = null;
		try {
			dictionaryElement = XMLUtil.parseQuietlyToRootElement(new FileInputStream(file));
		} catch (FileNotFoundException e) {
			throw new RuntimeException("Cannot find "+file);
		}
		listDictionaryInfo(dictionaryName, dictionaryElement);
		
	}

	private void listDictionaryInfo(String dictionary, Element dictionaryElement) {
		System.err.println("\nDictionary: "+dictionary);
		List<Element> entries = XMLUtil.getQueryElements(dictionaryElement, "./*[local-name()='entry']");
		System.err.println("entries: "+entries.size());
		printDescs(dictionaryElement);
		printEntries(dictionaryElement);
	}

	private void printDescs(Element dictionaryElement) {
		List<Element> descList = XMLUtil.getQueryElements(dictionaryElement, "./*[local-name()='desc']");
		for (Element desc : descList) {
			System.err.println(desc.getValue());
		}
	}

	private void printEntries(Element dictionaryElement) {
		List<Element> entryList = XMLUtil.getQueryElements(dictionaryElement, "./*[local-name()='entry']");
		for (int i = 0; i < Math.min(entryList.size(), maxEntries); i++) {
			Element entry =  entryList.get(i);
			System.err.println(entry.getAttributeValue("term"));
		}
	}

	public void listAllDictionariesBrieflyPaths() {
		int count = 0;
		int perLine = 5;
		System.err.print("\n    ");
		for (Path path : paths) {
//			LOG.debug(path);
			String name = FilenameUtils.getBaseName(path.toString());
			System.err.print((name + "                     ").substring(0, 20));
			if (count++ %perLine == perLine - 1) System.err.print("\n    ");
		}
		listHardcoded();
	}

	public void listAllDictionariesBriefly() {
		int count = 0;
		int perLine = 5;
		System.err.print("\n    ");
		for (File file : files) {
			String name = FilenameUtils.getBaseName(file.toString());
			System.err.print((name + "                     ").substring(0, 20));
			if (count++ %perLine == perLine - 1) System.err.print("\n    ");
		}
		listHardcoded();
	}

	private void listHardcoded() {
		System.err.println("\n\nalso hardcoded functions (which resolve abbreviations):\n");
		System.err.println("    gene    (relies on font/style) ");
		System.err.println("    species (resolves abbreviations) ");
	}

	public List<File> listDictionaryFiles(File dictionaryHead) {
		DebugPrint.debugPrint("dictionaries from "+dictionaryHead);
		List<File> newFiles = new ArrayList<File>();
		File[] listFiles = dictionaryHead.listFiles();
		if (listFiles == null) {
			LOG.error("cannot list dictionary files; terminated");
		} else {
			List<File> files = Arrays.asList(listFiles);
			for (File file : files) {
				String filename = file.toString();
				if (XML.equals(FilenameUtils.getExtension(filename))) {
					newFiles.add(file);
				}
			}
			Collections.sort(newFiles);
		}
		return newFiles;
	}

	public File getDictionaryDir() {
		return dictionaryDir;
	}

	public void setDictionaryDir(File dictionaryDir) {
		this.dictionaryDir = dictionaryDir;
	}

	public int getMaxEntries() {
		return maxEntries;
	}

	public void setMaxEntries(int maxEntries) {
		this.maxEntries = maxEntries;
	}

	public String getDirectory() {
		return directory;
	}

	public void setDirectory(String directory) {
		this.directory = directory;
	}

	public void readWikipediaPage(WikipediaDictionary wikipediaDictionary, InputStream inputStream) {
		HtmlElement htmlElement = readHtmlElement(inputStream);
		wikipediaDictionary.clean(htmlElement);
		if (this.nameCol != null && linkCol != null) {
			createFromEmbeddedWikipediaTable(htmlElement);
		} else if (this.href != null) {
			simpleList = createListOfHyperlinks(htmlElement);
		} else {
			AMIDictionary.LOG.error("must give either table(name, link) or list(href)");
		}
	}


}
class Data {
    String ss;	
}
