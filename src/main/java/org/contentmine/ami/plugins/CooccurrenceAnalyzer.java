package org.contentmine.ami.plugins;

import java.io.File;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.contentmine.eucl.euclid.IntMatrix;

import com.google.common.collect.Multiset.Entry;

public class CooccurrenceAnalyzer {
	private static final Logger LOG = Logger.getLogger(CooccurrenceAnalyzer.class);
	static {
		LOG.setLevel(Level.DEBUG);
	}

	private OccurrenceAnalyzer rowAnalyzer;
	private OccurrenceAnalyzer colAnalyzer;

	public CooccurrenceAnalyzer() {
		
	}
	
	public CooccurrenceAnalyzer setRowAnalyzer(OccurrenceAnalyzer rowAnalyzer) {
		this.rowAnalyzer = rowAnalyzer;
		return this;
	}

	public CooccurrenceAnalyzer setColAnalyzer(OccurrenceAnalyzer colAnalyzer) {
		this.colAnalyzer = colAnalyzer;
		return this;
	}

	public OccurrenceAnalyzer getRowAnalyzer() {
		return rowAnalyzer;
	}

	public OccurrenceAnalyzer getColAnalyzer() {
		return colAnalyzer;
	}

	public IntMatrix analyze() {
		OccurrenceAnalyzer rowAnalyzer = getRowAnalyzer();
		OccurrenceAnalyzer colAnalyzer = getColAnalyzer();
		List<File> rowCTreeFiles = rowAnalyzer.getOrCreateCTreeFiles();
		Map<File, List<Entry<String>>> rowEntryListByCTreeFile = rowAnalyzer.getOrCreateEntryListByCTreeFile();
		Map<File, List<Entry<String>>> colEntryListByCTreeFile = colAnalyzer.getOrCreateEntryListByCTreeFile();
		
		IntMatrix coocurrenceMatrix = new IntMatrix(rowAnalyzer.getMaxCount(), colAnalyzer.getMaxCount());
		for (File rowCTreeFile : rowCTreeFiles) {
			List<Entry<String>> colEntryList = colEntryListByCTreeFile.get(rowCTreeFile);
			if (colEntryList != null) {
				List<Integer> colSerialList = colAnalyzer.getSerialList(colEntryList);
				List<Entry<String>> rowEntryList = rowEntryListByCTreeFile.get(rowCTreeFile);
				List<Integer> rowSerialList = rowAnalyzer.getSerialList(rowEntryList);
				for (Integer rowSerial : rowSerialList) {
					if (rowSerial < rowAnalyzer.getMaxCount()) {
						for (Integer colSerial : colSerialList) {
							if (colSerial < colAnalyzer.getMaxCount()) {
								int count = coocurrenceMatrix.elementAt(rowSerial, colSerial);
								count++;
								coocurrenceMatrix.setElementAt(rowSerial, colSerial, count);
							}
						}
					}
				}
			}
		}
		return coocurrenceMatrix;
	}

}
