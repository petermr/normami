package org.contentmine.ami.plugins;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.contentmine.ami.plugins.OccurrenceAnalyzer.SubType;
import org.contentmine.ami.plugins.OccurrenceAnalyzer.OccurrenceType;
import org.contentmine.eucl.euclid.IntMatrix;
import org.contentmine.norma.Norma;

/** analyzes chunks/caches for entities in context.
 * starting with documents and sentences
 * 
 * @author pm286
 *
 */
public class EntityAnalyzer {
	private static final String ANCESTORS = ".*/";
	private static final String RESULTS_XML = "results\\.xml";
	private static final Logger LOG = Logger.getLogger(EntityAnalyzer.class);
	static {
		LOG.setLevel(Level.DEBUG);
	}
	
	private List<OccurrenceAnalyzer> occurrenceAnalyzerList; 
	private List<CooccurrenceAnalyzer> cooccurrenceAnalyzerList;
	private File projectDir; 
	private String code;
	private boolean forceRun = true;
	private boolean writeCsv = false;

	public EntityAnalyzer() {
		
	}

	/** creates {@link OccurrenceAnalyzer} with default type OccurrenceType.STRING. and no {@link SubType}
	 * use this for most dictionaries
	 * 
	 * @param name
	 * @return
	 */
	public OccurrenceAnalyzer createAndAddOccurrenceAnalyzer(String name) {
		OccurrenceAnalyzer occurrenceAnalyzer = this.createOccurrenceAnalyzer(OccurrenceType.STRING, name);
		occurrenceAnalyzer.setName(name);
		return occurrenceAnalyzer;
	}
	
	public OccurrenceAnalyzer createOccurrenceAnalyzer(OccurrenceType occurrenceType, String name) {
		String resultsXMLFile = createResultsXMLFile(occurrenceType, (SubType) null, name);
		return createOccurrenceAnalyzerAndAdd(occurrenceType, resultsXMLFile);
	}

	/** create {@link OccurrenceAnalyzer} for types without subType or name
	 * Example is {@link OccurrenceAnalyzer}.BINOMIAL
	 * 
	 * @param type
	 * @return
	 */
	public OccurrenceAnalyzer createAndAddOccurrenceAnalyzer(OccurrenceType type) {
		return createOccurrenceAnalyzer(type, (SubType)null, null);
	}
	
	/** create {@link OccurrenceAnalyzer} for types with subType but no name
	 * Example is {@link OccurrenceAnalyzer}.GENE with {@link SubType}.HUMAN
	 * 
	 * @param type
	 * @return
	 */
	public OccurrenceAnalyzer createAndAddOccurrenceAnalyzer(OccurrenceType type, SubType subType) {
		String resultsXMLFile = createResultsXMLFile(type, subType, null);
		OccurrenceAnalyzer occurrenceAnalyzer = createOccurrenceAnalyzerAndAdd(type, resultsXMLFile);
		occurrenceAnalyzer.setSubType(subType);
		return occurrenceAnalyzer;
	}
	
	/** creates {@link OccurrenceAnalyzer} with SubType and name 
	 * never called by user as either SubType or name is usually null
	 * 
	 * @param occurrenceType
	 * @param subType may be null
	 * @param name may be null
	 * @return
	 */
	private OccurrenceAnalyzer createOccurrenceAnalyzer(OccurrenceType occurrenceType, SubType subType, String name) {
		String resultsXMLFile = createResultsXMLFile(occurrenceType, subType, name);
		OccurrenceAnalyzer occurrenceAnalyzer = createOccurrenceAnalyzerAndAdd(occurrenceType, resultsXMLFile).setSubType(subType);
		return occurrenceAnalyzer;
	}

	/** create the actual {@link OccurrenceAnalyzer}
	 * all methods come through this
	 * 
	 * @param occurrenceType
	 * @param resultsXMLFile
	 * @return
	 */
	private OccurrenceAnalyzer createOccurrenceAnalyzerAndAdd(OccurrenceType occurrenceType, String resultsXMLFile) {
		OccurrenceAnalyzer occurrenceAnalyzer = new OccurrenceAnalyzer();
		occurrenceAnalyzer.setType(occurrenceType);
		occurrenceAnalyzer.setResultsDirRegex(resultsXMLFile);
		occurrenceAnalyzer.setEntityAnalyzer(this);
		ensureOccurrenceAnalyzerLists();
		occurrenceAnalyzerList.add(occurrenceAnalyzer);
		return occurrenceAnalyzer;
	}

	/** create filename for xmlFile to be read.
	 * Everything is routed through this
	 * 
	 * @param type
	 * @param subType
	 * @param name
	 * @return
	 */
	private String createResultsXMLFile(OccurrenceType type, SubType subType, String name) {
		String xmlFile = null;
		if (OccurrenceType.STRING.equals(type)) {
			if (name == null) {
				throw new RuntimeException("name must not be null for STRING");
			}
			xmlFile = createResultsXMLFile(name, (String)null);
		} else if (subType != null) {
			xmlFile = createResultsXMLFile(type.getName(), subType.getName());
		} else {
			xmlFile = createResultsXMLFile(type.getName(), (String) null);
		}
		return xmlFile;
	}

	private String createResultsXMLFile(String code, String subCode) {
		String s = ANCESTORS;
		s += code + "/";
		if (subCode != null) {
			s += subCode + "/";
		}
		s += RESULTS_XML;
		return s;
	}

	private void ensureOccurrenceAnalyzerLists() {
		if (occurrenceAnalyzerList == null) {
			occurrenceAnalyzerList = new ArrayList<OccurrenceAnalyzer>();
		}
		if (cooccurrenceAnalyzerList == null) {
			cooccurrenceAnalyzerList = new ArrayList<CooccurrenceAnalyzer>();
		}
	}

	public EntityAnalyzer setProjectDir(File projectDir) {
		this.projectDir = projectDir;
		return this;
	}

	public File getProjectDir() {
		return projectDir;
	}

	public static EntityAnalyzer createEntityAnalyzer(File projectDir) {
		String fileroot = projectDir.getName();
		EntityAnalyzer entityAnalyzer = new EntityAnalyzer().setCode(fileroot).setProjectDir(projectDir);
		return entityAnalyzer;
	}

	public String getCode() {
		return code;
	}

	public EntityAnalyzer setCode(String code) {
		this.code = code;
		return this;
	}
	
	

	public CooccurrenceAnalyzer createCooccurrenceAnalyzer(OccurrenceAnalyzer rowAnalyzer,
			OccurrenceAnalyzer colAnalyzer) {
		CooccurrenceAnalyzer coocurrenceAnalyzer = new CooccurrenceAnalyzer(this).setRowAnalyzer(rowAnalyzer).setColAnalyzer(colAnalyzer);
		cooccurrenceAnalyzerList.add(coocurrenceAnalyzer);
		return coocurrenceAnalyzer;
	}

	public void createAllCooccurrences() {
		for (int irow = 0; irow < occurrenceAnalyzerList.size(); irow++) {
			OccurrenceAnalyzer rowAnalyzer = occurrenceAnalyzerList.get(irow);
			if (writeCsv) {
				try {
					rowAnalyzer.writeCSV();
				} catch (IOException e) {
					throw new RuntimeException("Cannot write row: "+rowAnalyzer, e);
				}
			}
			// allow diagonal entries
			for (int jcol = irow; jcol < occurrenceAnalyzerList.size(); jcol++) {
				OccurrenceAnalyzer colAnalyzer = occurrenceAnalyzerList.get(jcol);
				CooccurrenceAnalyzer rowColCoocAnalyzer = createCooccurrenceAnalyzer(rowAnalyzer, colAnalyzer);
				rowColCoocAnalyzer.analyze();
				if (writeCsv) {
					try {
						rowColCoocAnalyzer.writeCSV();
					} catch (IOException e) {
						throw new RuntimeException("Cannot write CSV", e);
					}
				}
			}
		}
							
	}

	/** writes CSV files for each occurrence analyzer.
	 * 
	 */
	public void writeCSVFiles() {
		for (int irow = 0; irow < occurrenceAnalyzerList.size(); irow++) {
			OccurrenceAnalyzer rowAnalyzer = occurrenceAnalyzerList.get(irow);
			try {
				rowAnalyzer.writeCSV();
			} catch (IOException e) {
				LOG.error("Cannot write: "+rowAnalyzer.getFullName());
			}
		}
	}

	public void analyzePlantCoocurrences() throws IOException {
		if (forceRun ) {
			runNormaNLM();
		}
		String cmd = "word(frequencies)xpath:@count>20~w.stopwords:pmcstop.txt_stopwords.txt"
		+ " species(binomial)"
		+ " gene(human) "
		+ " search(auxin)"
		+ " search(plantDevelopment)"
		+ " search(pectin)"
		+ " search(plantparts)"
		+ " search(synbio)"
		
	
	    ;
		if (forceRun) {
			CommandProcessor.main((getProjectDir()+" "+cmd).split("\\s+"));
		}
		
		createAndAddOccurrenceAnalyzer(OccurrenceType.BINOMIAL).setMaxCount(25);
		createAndAddOccurrenceAnalyzer(OccurrenceType.GENE, SubType.HUMAN).setMaxCount(30);
		createAndAddOccurrenceAnalyzer("auxin").setMaxCount(20);
		
		createAllCooccurrences();
	}

	public void analyzeMosquitoCoocurrences() throws IOException {
		if (forceRun ) {
			runNormaNLM();
		}
		String cmd = "word(frequencies)xpath:@count>20~w.stopwords:pmcstop.txt_stopwords.txt"
		+ " species(binomial)"
		+ " gene(human) "
		+ " search(disease)"
		+ " search(country)"
		+ " search(funders)"
		+ " search(inn)"
		+ " search(insecticide)"
	
	    ;
		if (forceRun) {
			CommandProcessor.main((getProjectDir()+" "+cmd).split("\\s+"));
		}
		
		createAndAddOccurrenceAnalyzer(OccurrenceType.GENE, SubType.HUMAN).setMaxCount(30);
		createAndAddOccurrenceAnalyzer(OccurrenceType.BINOMIAL).setMaxCount(25);
		createAndAddOccurrenceAnalyzer("disease").setMaxCount(20);
		createAndAddOccurrenceAnalyzer("country").setMaxCount(20);
		createAndAddOccurrenceAnalyzer("funders").setMaxCount(20);
		createAndAddOccurrenceAnalyzer("inn").setMaxCount(20);
		createAndAddOccurrenceAnalyzer("insecticide").setMaxCount(20);
		
		createAllCooccurrences();
	}

	public void analyzeObesityCoocurrences() throws IOException {
		if (forceRun ) {
			runNormaNLM();
		}
		String cmd = "word(frequencies)xpath:@count>20~w.stopwords:pmcstop.txt_stopwords.txt"
		+ " gene(human) "
		+ " search(disease)"
		+ " search(country)"
		+ " search(funders)"
		+ " search(inn)"
		+ " search(niddk)"
		+ " search(cochrane)"
	
	    ;
		if (forceRun) {
			CommandProcessor.main((getProjectDir()+" "+cmd).split("\\s+"));
		}
		
		createAndAddOccurrenceAnalyzer(OccurrenceType.GENE, SubType.HUMAN).setMaxCount(30);
//		createAndAddOccurrenceAnalyzer(OccurrenceType.BINOMIAL).setMaxCount(25);
		createAndAddOccurrenceAnalyzer("disease").setMaxCount(20);
		createAndAddOccurrenceAnalyzer("country").setMaxCount(20);
		createAndAddOccurrenceAnalyzer("funders").setMaxCount(20);
		createAndAddOccurrenceAnalyzer("inn").setMaxCount(20);
		createAndAddOccurrenceAnalyzer("niddk").setMaxCount(20);
		createAndAddOccurrenceAnalyzer("cochrane").setMaxCount(20);
		
		createAllCooccurrences();
	}


	private void runNormaNLM() {
		// check for html and xml
		boolean runNorma = true;
		runNorma = false;
		if (runNorma) {
			LOG.debug("NORMA");
			String args = "-i fulltext.xml -o scholarly.html --transform nlm2html --project " + getProjectDir();
			new Norma().run(args);
			LOG.debug("NORMAX");
		}
	}

	public boolean isForceRun() {
		return forceRun;
	}

	public EntityAnalyzer setForceRun(boolean forceRun) {
		this.forceRun = forceRun;
		return this;
	}

	public EntityAnalyzer setWriteCSV(boolean writeCsv) {
		this.writeCsv = writeCsv;
		return this;
	}

	public boolean isWriteCsv() {
		return writeCsv;
	}

	
	
}
