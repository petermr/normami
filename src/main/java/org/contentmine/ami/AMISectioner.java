package org.contentmine.ami;

import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.contentmine.cproject.files.DebugPrint;
import org.contentmine.norma.picocli.AbstractAMIProcessor;
import org.contentmine.norma.sections.JATSSectionTagger;
import org.contentmine.norma.sections.JATSSectionTagger.SectionTag;

/** creates sections in CTree for scholarlyHTML and XM components
 * 
 * @author pm286
 *
 */
public class AMISectioner extends AbstractAMIProcessor {
	private static final Logger LOG = Logger.getLogger(AMISectioner.class);
	private List<SectionTag> sectionTagList;
	static {
		LOG.setLevel(Level.DEBUG);
	}
	
	public AMISectioner() {
		
	}

	public static void main(String[] args) {
		if (args.length == 0) {
			runHelp();
		} else {
//			parseOptions(args);
		}
		
	}
	
	@Override
	protected void parseSpecifics() {
		throw new RuntimeException("NYI");
	}

	@Override
	protected void runSpecifics() {
		throw new RuntimeException("NYI");
	}
	

	
	private static void runHelp() {
		DebugPrint.debugPrint("sections recognized in documents");
		for (SectionTag tag : JATSSectionTagger.SectionTag.values()) {
			DebugPrint.debugPrint(tag.name()+": "+tag.getDescription());
		}
	}

	public AMISectioner addSectionTag(SectionTag section) {
		getOrCreateSectionTagList();
		return this;
	}

	public List<SectionTag> getOrCreateSectionTagList() {
		if (sectionTagList == null) {
			sectionTagList = new ArrayList<SectionTag>();
		}
		return sectionTagList;
	}

	
}
