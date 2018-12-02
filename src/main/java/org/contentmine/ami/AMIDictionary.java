package org.contentmine.ami;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FilenameFilter;
import java.nio.file.FileStore;
import java.nio.file.FileSystem;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.MessageDigest;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.Callable;

import org.apache.commons.io.FilenameUtils;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.contentmine.ami.misc.PicocliTest;
import org.contentmine.cproject.files.DebugPrint;
import org.contentmine.cproject.util.CMineGlobber;
import org.contentmine.eucl.xml.XMLUtil;
import org.contentmine.norma.NAConstants;

import com.google.common.collect.Lists;

import nu.xom.Element;
import picocli.CommandLine;
import picocli.CommandLine.Command;
import picocli.CommandLine.Option;
import picocli.CommandLine.Parameters;

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

public class AMIDictionary implements HasAMICLI, Callable<Void> {
	private static final Logger LOG = Logger.getLogger(AMIDictionary.class);
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

	private List<File> files;
	private List<Path> paths;
	private File dictionaryDir = DICTIONARY_TOP;
	private int maxEntries = 0;
	private AMICLI cli;
	
    @Parameters(index = "0", description = "The file whose checksum to calculate.")
    private File dictionaryTop;

    @Option(names = {"-d", "--dictionanries"}, description = "list of dictionaries")
    private String dictionaries = "dicts";

	public static void main(String[] args) {
		LOG.debug("dictionaries under: "+DICTIONARY_TOP);
        CommandLine.call(new AMIDictionary(), args);
	}

    public Void call() throws Exception {
    	System.out.println("called on "+dictionaryTop+" with "+dictionaries);
//        byte[] fileContents = Files.readAllBytes(file.toPath());
//        byte[] digest = MessageDigest.getInstance(algorithm).digest(fileContents);
//        System.out.println(javax.xml.bind.DatatypeConverter.printHexBinary(digest));
        return null;
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

}
