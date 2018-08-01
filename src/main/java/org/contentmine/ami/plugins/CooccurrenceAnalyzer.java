package org.contentmine.ami.plugins;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.sql.RowSet;

import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.contentmine.eucl.euclid.IntMatrix;

import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.Multimap;
import com.google.common.collect.Multiset;
import com.google.common.collect.Multiset.Entry;

public class CooccurrenceAnalyzer {
	private static final Logger LOG = Logger.getLogger(CooccurrenceAnalyzer.class);
	static {
		LOG.setLevel(Level.DEBUG);
	}

	public static final String COOCCURRENCE = "cooccurrence";
	
	private OccurrenceAnalyzer rowAnalyzer;
	private OccurrenceAnalyzer colAnalyzer;
	private IntMatrix cooccurrenceMatrix;
	private EntityAnalyzer entityAnalyzer;

	public CooccurrenceAnalyzer(EntityAnalyzer entityAnalyzer) {
		setEntityAnalyzer(entityAnalyzer);
	}

	public void setEntityAnalyzer(EntityAnalyzer entityAnalyzer) {
		this.entityAnalyzer = entityAnalyzer;
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

	public IntMatrix analyze() { // new 
		LOG.trace("rowA number of elements "+rowAnalyzer.getOrCreateSerialByTermImportance().size());
		LOG.trace("colA number of elements "+colAnalyzer.getOrCreateSerialByTermImportance().size());
		
		List<File> rowCTreeFiles = rowAnalyzer.getOrCreateCTreeFiles();
		List<File> colCTreeFiles = colAnalyzer.getOrCreateCTreeFiles();
		LOG.trace("rows trees> "+rowCTreeFiles.size()+" / "+rowCTreeFiles);
		LOG.trace("cols trees> "+colCTreeFiles.size()+" / "+colCTreeFiles);
		
		Map<File, List<Entry<String>>> rowEntryListByCTreeFile = rowAnalyzer.getOrCreateEntryListByCTreeFile();
		Map<File, List<Entry<String>>> colEntryListByCTreeFile = colAnalyzer.getOrCreateEntryListByCTreeFile();
		LOG.trace("rows "+rowAnalyzer.getName()+" / "+rowEntryListByCTreeFile);
		LOG.trace("cols "+colAnalyzer.getName()+" / "+colEntryListByCTreeFile);
		
		Map<String, Set<File>> fileSetByRowEntryValue = createFileSetByEntryString(rowEntryListByCTreeFile);
		Map<String, Set<File>> fileSetByColEntryValue = createFileSetByEntryString(colEntryListByCTreeFile);
		LOG.trace("files by rows "+rowAnalyzer.getName()+" / "+fileSetByRowEntryValue);
		LOG.trace("files by cols "+colAnalyzer.getName()+" / "+fileSetByColEntryValue);
		
		int rowCount = Math.min(rowAnalyzer.getMaxCount(), rowAnalyzer.getEntriesSortedByImportance().size());
		int colCount = Math.min(colAnalyzer.getMaxCount(), colAnalyzer.getEntriesSortedByImportance().size());
		cooccurrenceMatrix = new IntMatrix(
			rowCount,
			colCount
			);
		LOG.trace("COOC: "+cooccurrenceMatrix);

		List<Entry<String>> rowList = rowAnalyzer.getEntriesSortedByImportance();
		for (int irow = 0; irow < rowCount; irow++) {
			Entry<String> entry = rowList.get(irow);
			String rowElement = entry.getElement();
			Set<File> rowFileSet = fileSetByRowEntryValue.get(rowElement);
			rowFileSet = rowFileSet == null ? new HashSet<File>() : rowFileSet;
			List<Entry<String>> colList = colAnalyzer.getEntriesSortedByImportance();
			for (int jcol = 0; jcol < colCount; jcol++) {
				Entry<String> colEntry = colList.get(jcol);
				String colElement = colEntry.getElement();
				Set<File> colFileSet = fileSetByColEntryValue.get(colElement);
				colFileSet = colFileSet == null ? new HashSet<File>() : new HashSet<File>(colFileSet);
				colFileSet.retainAll(rowFileSet);
				cooccurrenceMatrix.setElementAt(irow, jcol, colFileSet.size());
			}
		}
		System.out.println(""+
				rowAnalyzer.getEntriesSortedByImportance()+"\n"
				+colAnalyzer.getEntriesSortedByImportance()+"\n"
				+rowAnalyzer.getName()+"-"+colAnalyzer.getName()+"\n"
				+cooccurrenceMatrix);
		return cooccurrenceMatrix;
	}

	private Multimap<String, File> createFileListByEntryString(Map<File, List<Entry<String>>> entryListByCTreeFile) {
		Multimap<String, File> fileListByEntryString = ArrayListMultimap.create();
		for (File file : entryListByCTreeFile.keySet()) {
			List<Entry<String>> entryList = entryListByCTreeFile.get(file);
			for (Entry<String> entry : entryList) { // seems cumbersome, am I missing something?
				for (int i = 0; i < entry.getCount(); i++) {
					fileListByEntryString.put(entry.getElement(), file);
				}
			}
		}
		LOG.debug("inverted map "+fileListByEntryString);
		return fileListByEntryString;
	}

	private Map<String, Set<File>> createFileSetByEntryString(Map<File, List<Entry<String>>> entryListByCTreeFile) {
		Map<String, Set<File>> fileSetByEntryString = new HashMap<String, Set<File>>();
		for (File file : entryListByCTreeFile.keySet()) {
			List<Entry<String>> entryList = entryListByCTreeFile.get(file);
			for (Entry<String> entry : entryList) { // seems cumbersome, am I missing something?
				String entryElement = entry.getElement(); // don't weight by count
				Set<File> fileSet = fileSetByEntryString.get(entryElement);
				if (fileSet == null) {
					fileSet = new HashSet<File>();
					fileSetByEntryString.put(entryElement, fileSet);
				}
				fileSet.add(file);
			}
		}
		return fileSetByEntryString;
	}

	public IntMatrix analyzeOld() {
		List<File> colCTreeFiles = colAnalyzer.getOrCreateCTreeFiles();
		List<File> rowCTreeFiles = rowAnalyzer.getOrCreateCTreeFiles();
		Map<File, List<Entry<String>>> rowEntryListByCTreeFile = rowAnalyzer.getOrCreateEntryListByCTreeFile();
		Map<File, List<Entry<String>>> colEntryListByCTreeFile = colAnalyzer.getOrCreateEntryListByCTreeFile();
		LOG.debug("rows "+rowAnalyzer.getName()+" / "+rowEntryListByCTreeFile);
		LOG.debug("cols "+colAnalyzer.getName()+" / "+colEntryListByCTreeFile);
		
		cooccurrenceMatrix = new IntMatrix(
				Math.min(rowAnalyzer.getMaxCount(), rowAnalyzer.getSize()),
				Math.min(colAnalyzer.getMaxCount(), colAnalyzer.getSize())
				);
		for (File rowCTreeFile : rowCTreeFiles) {
			List<Entry<String>> colEntryList = colEntryListByCTreeFile.get(rowCTreeFile);
			if (colEntryList != null) {
				List<Integer> colSerialList = colAnalyzer.getSerialList(colEntryList);
				debugList("col", colSerialList);
				List<Entry<String>> rowEntryList = rowEntryListByCTreeFile.get(rowCTreeFile);
				List<Integer> rowSerialList = rowAnalyzer.getSerialList(rowEntryList);
				debugList("row", rowSerialList);
				for (Integer rowSerial : rowSerialList) {
					if (rowSerial < rowAnalyzer.getMaxCount()) {
						for (Integer colSerial : colSerialList) {
							if (colSerial < colAnalyzer.getMaxCount()) {
								int count = cooccurrenceMatrix.elementAt(rowSerial, colSerial);
								count++;
								cooccurrenceMatrix.setElementAt(rowSerial, colSerial, count);
							}
						}
					}
				}
			}
		}
		return cooccurrenceMatrix;
	}

	private void debugList(String title, List<?> list) {
		String s = list.toString();
		LOG.debug(title+">> "+s.substring(0, Math.min(30,  s.length())));
	}

	public void writeCSV() throws IOException {
		File cooccurrenceFile =  getCSVFileName();
		cooccurrenceFile.getParentFile().mkdirs();
		cooccurrenceMatrix.writeCSV(cooccurrenceFile /*, rowAnalyzer.getName(), colAnalyzer.getName()*/);
	}

	public File getCSVFileName() {
		File csvTop = new File(entityAnalyzer.getProjectDir(), "csv");
		String name = rowAnalyzer.getName() + "-" + colAnalyzer.getName();
		File csvDir = new File(csvTop, name);
		return new File(csvDir, COOCCURRENCE + ".csv");
	}

	public IntMatrix getCooccurrenceMatrix() {
		return cooccurrenceMatrix;
	}

	public void setCooccurrenceMatrix(IntMatrix cooccurrenceMatrix) {
		this.cooccurrenceMatrix = cooccurrenceMatrix;
	}

	public EntityAnalyzer getEntityAnalyzer() {
		return entityAnalyzer;
	}

}
