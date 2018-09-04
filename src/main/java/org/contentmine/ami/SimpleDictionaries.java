package org.contentmine.ami;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.apache.commons.io.FilenameUtils;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.contentmine.eucl.xml.XMLUtil;
import org.contentmine.norma.NAConstants;

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
		List<File> files = Arrays.asList(dictionaryHead.listFiles());
		List<File> newFiles = new ArrayList<File>();
		for (File file : files) {
			String filename = file.toString();
			if (XML.equals(FilenameUtils.getExtension(filename))) {
				newFiles.add(file);
			}
		}
		Collections.sort(newFiles);
		return newFiles;
	}

}
