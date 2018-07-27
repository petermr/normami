package org.contentmine.ami.plugins;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.contentmine.ami.plugins.OccurrenceAnalyzer.GeneType;
import org.contentmine.ami.plugins.OccurrenceAnalyzer.OccurrenceType;

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

	public EntityAnalyzer() {
		
	}

	public OccurrenceAnalyzer createOccurrenceAnalyzer(OccurrenceType occurrenceType) {
		OccurrenceAnalyzer occurrenceAnalyzer = null;
		if (occurrenceType != null) {
			occurrenceAnalyzer = new OccurrenceAnalyzer();
			occurrenceAnalyzer.setCode(code);
			occurrenceAnalyzer.setType(occurrenceType);
			occurrenceAnalyzer.setResultsDirRegex(createResultsXMLFile(code));
			occurrenceAnalyzer.setEntityAnalyzer(this);
			ensureOccurrenceAnalyzerLists();
			occurrenceAnalyzerList.add(occurrenceAnalyzer);
		}
		return occurrenceAnalyzer;
	}

	/**
	public OccurrenceAnalyzer createOccurrenceAnalyzer(String code, OccurrenceType type) {
		OccurrenceAnalyzer occurrenceAnalyzer = new OccurrenceAnalyzer();
		occurrenceAnalyzer.setCode(code);
		occurrenceAnalyzer.setType(type == null ? OccurrenceType.STRING : type);
		occurrenceAnalyzer.setResultsDirRegex(createResultsXMLFile(code));
		occurrenceAnalyzer.setEntityAnalyzer(this);
		ensureOccurrenceAnalyzerLists();
		occurrenceAnalyzerList.add(occurrenceAnalyzer);
		return occurrenceAnalyzer;
	}
*/
	public OccurrenceAnalyzer createOccurrenceAnalyzer(OccurrenceType type, GeneType geneType) {
		OccurrenceAnalyzer occurrenceAnalyzer = new OccurrenceAnalyzer();
		occurrenceAnalyzer.setType(type == null ? OccurrenceType.STRING : type);
		occurrenceAnalyzer.setResultsDirRegex(createResultsXMLFile(type, geneType));
		occurrenceAnalyzer.setEntityAnalyzer(this);
		ensureOccurrenceAnalyzerLists();
		occurrenceAnalyzerList.add(occurrenceAnalyzer);
		return occurrenceAnalyzer;
	}
	
	private String createResultsXMLFile(OccurrenceType type, GeneType geneType) {
		return createResultsXMLFile(type., subCode)
		// TODO Auto-generated method stub
		return null;
	}

	/** creates OccurrenceAnalyzer with default type OccurrenceType.STRING.
	 * 
	 * @param code
	 * @return
	 */
	public OccurrenceAnalyzer createOccurrenceAnalyzer(String code) {
		OccurrenceAnalyzer occurrenceAnalyzer = this.createOccurrenceAnalyzer(OccurrenceType.STRING);
		occurrenceAnalyzer.setCode(code);
		return occurrenceAnalyzer;
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

	public static EntityAnalyzer createEntityAnalyzer(String code, File projectDir) {
		EntityAnalyzer entityAnalyzer = new EntityAnalyzer().setCode(code).setProjectDir(projectDir);
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
		CooccurrenceAnalyzer coocurrenceAnalyzer = new CooccurrenceAnalyzer().setRowAnalyzer(rowAnalyzer).setColAnalyzer(colAnalyzer);
		cooccurrenceAnalyzerList.add(coocurrenceAnalyzer);
		return coocurrenceAnalyzer;
	}

	
}
