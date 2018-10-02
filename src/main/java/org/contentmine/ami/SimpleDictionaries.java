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
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.apache.commons.io.FilenameUtils;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.contentmine.cproject.util.CMineGlobber;
import org.contentmine.eucl.xml.XMLUtil;
import org.contentmine.norma.NAConstants;
import org.contentmine.norma.util.NIOResourceManager;

import com.google.common.collect.Lists;

import nu.xom.Element;

/** mainly to manage help and parse dictionaries.
 * 
 * @author pm286
 *
 */
public class SimpleDictionaries {
	private static final Logger LOG = Logger.getLogger(SimpleDictionaries.class);
	static {
		LOG.setLevel(Level.DEBUG);
	}
	private static final String XML = "xml";

	private List<File> files;
	private List<Path> paths;
	
	public SimpleDictionaries() {
		
	}
	
	public void listDictionaries(List<String> argList) {
		File dictionaryHead = new File(NAConstants.MAIN_AMI_DIR, "plugins/dictionary");
		files = listDictionaryFiles(dictionaryHead);
		
		if (argList.size() == 1 && argList.get(0).equals("dictionaries")) {
			for (File file : files) {
				listDictionaryInfo(FilenameUtils.getBaseName(file.getName()));
			}
		} else {
			listAllDictionariesBriefly();
			for (String arg : argList) {
				listDictionaryInfo(arg);
			}
		}
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
		System.err.println("amiProcessor <projectDirectory> [dictionary [dictionary]]");
		System.err.println("    projectDirectory can be full name or relative to currentDir");
		if (argList.size() == 0) {
			System.err.println("\nlist of dictionaries taken from AMI dictionary list:");
		}
		SimpleDictionaries dictionaries = new SimpleDictionaries();
		files = dictionaries.getDictionaries();
//		paths = dictionaries.getDictionaryPaths();
		listAllDictionariesBriefly();
//		listAllDictionariesBrieflyPaths();
	}

	@Deprecated // will continue to use Files
	private List<Path> getDictionaryPaths() {
		String resourceName = NAConstants.DICTIONARY_RESOURCE;
		paths = NIOResourceManager.listChildPaths(resourceName);
		return paths;
	}

	public List<File> getDictionaries() {
		File dictionaryDir = NAConstants.DICTIONARY_DIR;
		File[] fileArray = dictionaryDir.listFiles(new FilenameFilter() {
			@Override
			public boolean accept(File dir, String name) {
				return name != null && name.endsWith(".xml");
			}
		});
		files = fileArray == null ? new ArrayList<File>() : Arrays.asList(fileArray);
		return files;
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

	private void listDictionaryInfo(File file, String dictionary) {
		Element dictionaryElement = null;
		try {
			dictionaryElement = XMLUtil.parseQuietlyToRootElement(new FileInputStream(file));
		} catch (FileNotFoundException e) {
			throw new RuntimeException("Cannot find "+file);
		}
		listDictionaryInfo(dictionary, dictionaryElement);
		
	}

	private void listDictionaryInfo(String dictionary, Element dictionaryElement) {
		System.err.println("\nDictionary: "+dictionary);
		List<Element> entries = XMLUtil.getQueryElements(dictionaryElement, "./*[local-name()='entry']");
		System.err.println("entries: "+entries.size());
		List<Element> descList = XMLUtil.getQueryElements(dictionaryElement, "./*[local-name()='desc']");
		for (Element desc : descList) {
			System.err.println(desc.getValue());
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
		System.err.println("\nalso:");
		System.err.println("    gene     ");
		System.err.println("    species     ");
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
		System.err.println("\nalso:");
		System.err.println("    gene     ");
		System.err.println("    species     ");
	}

	public List<File> listDictionaryFiles(File dictionaryHead) {
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

}
