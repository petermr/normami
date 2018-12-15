package org.contentmine.ami;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
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

import org.apache.commons.io.FilenameUtils;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.contentmine.ami.dictionary.DefaultAMIDictionary;
import org.contentmine.ami.dictionary.DictionaryTerm;
import org.contentmine.ami.lookups.WikipediaDictionary;
import org.contentmine.cproject.files.DebugPrint;
import org.contentmine.cproject.util.CMineGlobber;
import org.contentmine.eucl.xml.XMLUtil;
import org.contentmine.graphics.html.HtmlA;
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

import nu.xom.Attribute;
import nu.xom.Element;
import picocli.CommandLine.Command;
import picocli.CommandLine.Option;

/** mainly to manage help and parse dictionaries.
 * 
 * @author pm286
 *
 */

/**
@Command(description = "Prints the checksum (MD5 by default) of a file to STDOUT.",
         name = "checksum", mixinStandardHelpOptions = true, version = "checksum 3.0")
public class PicocliTest implements Callable<Void> {

    @Parameters(index = "0", description = "The file whose checksum to calculate.")
    private File file;

    @Option(names = {"-a", "--algorithm"}, description = "MD5, SHA-1, SHA-256, ...")
    private String algorithm = "SHA-1";

    public static void main(String[] args) throws Exception {
    	args = new String[]{"-a", "MD5", "README.md"}; 
        CommandLine.call(new PicocliTest(), args);
    	args = new String[]{}; 
        CommandLine.call(new PicocliTest(), args);
    }

//    @Override
    public Void call() throws Exception {
    	System.out.println("called on "+file+" with "+algorithm);
        byte[] fileContents = Files.readAllBytes(file.toPath());
        byte[] digest = MessageDigest.getInstance(algorithm).digest(fileContents);
        System.out.println(javax.xml.bind.DatatypeConverter.printHexBinary(digest));
        return null;
    }
 */

@Command(description = "Manages AMI dictionaries",
name = "ami-dictionary", mixinStandardHelpOptions = true, version = "ami 0.1")

public class AMIDictionary extends AbstractAMIProcessor /*implements HasAMICLI, Callable<Void> */{
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

	private List<File> files;
	private List<Path> paths;
	private File dictionaryDir = DICTIONARY_TOP;
	private int maxEntries = 0;
	private AMICLI cli;

	private static final String HTTP = "http";

	public enum LinkField {
		HREF,
		VALUE,
	}

//    @Parameters(index = "0", description = "The file whose checksum to calculate.")
//    private File dictionaryTop;

    @Option(names = {"-d", "--dictionary"}, 
    		arity="1",
    		description = "input or output dictionary")
    private String dictionary;

    @Option(names = {"-b", "--directory"}, 
    		arity="1",
    		description = "directory containing dictionary")
    private String directory;

    @Option(names = {"-fo", "--format"}, 
    		arity="1..*",
    		description = "directory containing dictionary")
    private String formatList;

    @Option(names = {"-e", "--terms"}, 
    		arity="1..*",
    		split=",",
    		description = "list of terms (entries), comma-separated")
    private String[] terms;

    @Option(names = {"-l", "--linkcol"}, 
    		arity="1",
    		description = "column to extract link to wikipedia pages. Often the 'name' column"
    		)
	public String linkCol;

    @Option(names = {"-n", "--namecol"}, 
    		arity="1",
    		description = "column(s) to extract name (from Wikipedia); use exact case (e.g. Name)"
    		)
	public String nameCol;
    
    @Option(names = {"-u", "--urlcol"}, 
    		arity="1",
   		description = "hyperlink column from wikipedia table"
    		)
    private String hrefCol;
    
    @Option(names = {"-ur", "--urlref"}, 
    		arity="0",
    		description = "hyperlinks from wikipedia text (maybe excludes tables); still under test"
    		)
	public String href;
    
    @Option(names = {"-w", "--wikipedia"}, 
    		description = "wikipedia page or URL. If does not start with 'http' prepend WP-en base"
    		)
    private String wikipediaUrl;
    
// converted from args    
	private List<String> termList;
	private List<String> nameList;
	private List<String> hrefList;
	private HtmlTable simpleTable;
	public HtmlUl simpleList;
	private Element dictionaryElement;
	private HtmlTbody tBody;
	private WikipediaDictionary wikipediaDictionary;

	public static void main(String[] args) {
        AbstractAMIProcessor amiDictionary = new AMIDictionary();
		amiDictionary.runCommands(args);
	}
	
    @Override
    public void runCommands(String[] args) {
    	super.runCommands(args);
        runDictionary();
    }
	
//	public Void call() throws Exception {
//    	super.call();
//    	runDictionary();
//        return null;
//    }

	private void runDictionary() {
		LOG.debug("runDictionary");
		wikipediaUrl = (wikipediaUrl == null) ? null : (wikipediaUrl.startsWith("http") ? wikipediaUrl : WIKIPEDIA_BASE);
    	if (wikipediaUrl != null) {
    		wikipediaDictionary = new WikipediaDictionary();
    		readWikipediaPage(wikipediaDictionary, wikipediaUrl);
    	} else {
    		termList = terms == null ? null : Arrays.asList(terms);
    	}
    	if (nameList == null && termList != null) {
    		nameList = termList;
    	} else if (termList == null && nameList != null) {
    		termList = nameList;
    	} else if (termList == null && nameList == null) {
    		throw new RuntimeException("must give either/both names and terms");
    	}
    	printDebug();
		DefaultAMIDictionary amiDictionary = new DefaultAMIDictionary();
    	dictionaryElement = amiDictionary.createDictionaryElement(dictionary);
    	writeNamesAndHrefs(amiDictionary);
	}

	private void writeNamesAndHrefs(DefaultAMIDictionary amiDictionary) {
		if (nameList == null) return;
		for (int i = 0; i < nameList.size(); i++) {
			Element entry = amiDictionary.createEntryElementFromTerm(termList.get(i));
			entry.addAttribute(new Attribute(DictionaryTerm.NAME, nameList.get(i)));
			String url = hrefList == null ? null : hrefList.get(i);
			if (url != null && !url.equals("")) {
				entry.addAttribute(new Attribute(DictionaryTerm.URL, url));
			}
			dictionaryElement.appendChild(entry);
		}
	}
	
	public Element getDictionaryElement() {
		return dictionaryElement;
	}
    
	private void printDebug() {
		System.out.println("dictionary    "+dictionary);
		System.out.println("linkCol       "+linkCol);
		System.out.println("nameCol       "+nameCol);
		System.out.println("terms  ("+termList.size()+")    "+termList);
		System.out.println("wikipedia     "+wikipediaUrl);
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
		hrefList = new ArrayList<String>();
		List<HtmlA> aList = HtmlA.extractSelfAndDescendantAs(htmlElement);
		HtmlUl ul = new HtmlUl();
		for (HtmlA a : aList) {
			nameList.add(a.getValue());
			hrefList.add(a.getHref());
			HtmlLi li = new HtmlLi();
			li.appendChild(a.copy());
			ul.appendChild(li);
		}
		return ul;
	}

	public void createFromEmbeddedWikipediaTable(HtmlElement htmlElement) {
		List<HtmlTable> tableList = HtmlTable.extractSelfAndDescendantTables(htmlElement);

		nameList = new ArrayList<String>();
		hrefList = new ArrayList<String>();
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
			String href = hrefList.get(i);
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

	public HtmlElement readHtmlElement(String wikipediaUrl) {
		HtmlElement htmlElement = null;
		try {
			Element rootElement = XMLUtil.parseQuietlyToRootElement(new URL(wikipediaUrl).openStream());
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
		List<String> resultList = new ArrayList<>();
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
			hrefList.add(hrefs.get(i));
		}
	}

	public void runHelp(List<String> argList) {
		if (argList.size() > 0) argList.remove(0);
		this.help(argList);
	}


	public AMIDictionary() {
		init();
	}
	
	private void init() {
		dictionaryDir = NAConstants.DICTIONARY_DIR;
		cli = new DictionaryCLI();
		
	}
	
	/** this uses FILES */
	public void listDictionaries(List<String> argList) {
//		File dictionaryHead = new File(NAConstants.MAIN_AMI_DIR, "plugins/dictionary");
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
//			for (File file : files) {
//				listDictionaryInfo(FilenameUtils.getBaseName(file.getName()));
//			}
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
		System.err.println("Dictionary processor");
		System.err.println("    dictionaries are normally added as arguments to search (e.g. ami-search-cooccur [dictionary [dictionary ...]]");
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

	/**
	@Deprecated // will continue to use Files
	private List<Path> getDictionaryPaths() {
		String resourceName = NAConstants.DICTIONARY_RESOURCE;
		paths = NIOResourceManager.listChildPaths(resourceName);
		return paths;
	}
	*/

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

	public void createAndSaveDictionary(String dict, String[] args) {
		runCommands(args);
		Element dictionaryElement = getDictionaryElement();
		try {
			File htmlFile = new File(AMIDictionaryTest.DICTIONARY_DIR, dict + ".html");
			XMLUtil.debug(getSimpleDictionary(), htmlFile, 1);
			File xmlFile = new File(getDirectory(), dict + ".xml");
			XMLUtil.debug(dictionaryElement, xmlFile, 1);
		} catch (IOException e) {
			throw new RuntimeException("cannot write: "+dict, e);
		}
	}

	public void readWikipediaPage(WikipediaDictionary wikipediaDictionary, String wikipediaUrl) {
		HtmlElement htmlElement = readHtmlElement(wikipediaUrl);
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
