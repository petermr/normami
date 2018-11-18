package org.contentmine.norma.sections;

import java.io.File;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.contentmine.cproject.files.CProject;
import org.contentmine.cproject.files.CTree;
import org.contentmine.cproject.files.CTreeList;
import org.contentmine.cproject.files.ResourceLocation;
import org.contentmine.eucl.xml.XMLUtil;
import org.contentmine.graphics.html.HtmlDiv;
import org.contentmine.graphics.html.HtmlElement;
import org.contentmine.graphics.html.HtmlFactory;
import org.contentmine.graphics.html.HtmlHead;
import org.contentmine.graphics.html.HtmlHtml;
import org.contentmine.graphics.html.HtmlSpan;
import org.contentmine.graphics.html.HtmlTable;
import org.contentmine.norma.NAConstants;

import com.google.common.collect.HashMultiset;
import com.google.common.collect.Multiset;

import nu.xom.Element;

/** sections in JATS and similar documents
 * 
 * @author pm286
 *
 */
public class JATSSectionTagger {

	private static final String TAG = "tag";

	private static final Logger LOG = Logger.getLogger(JATSSectionTagger.class);
	static {
		LOG.setLevel(Level.DEBUG);
	}
	
	/**
	 * 
	 * SENAY KAFKAS tags
	 *  @ClassList=(
"Introduction&Background",
"Materials&Methods",
"Results",
"Discussion",
"Conclusion&FutureWork",
"CaseStudyReport",
"Acknowledgement&Funding",
"AuthorContribution",
"CompetingInterest",
"SupplementaryData",
"Abbreviations",
"Keywords",
"References",
"Appendix",
"Figures",
"Tables",
"Other",
"Back_NoRef"
);
	 * 
	 */

	/** these are JATS reserved names
	 * 
	 * @author pm286
	 *
	 */
	public enum SectionTag {
		ABBREVIATION("Authors abbreviations", "abbreviations?"),
		ABSTRACT("Abstract", "abstract"),
		ACK_FUND("Acknowledgements including funders", "(Acknowledge?ments?|Fund(ers)?|ing)"),
		APPENDIX("Appendix", "Appendix"),
		ARTICLE_META("Html meta", ""),
		   ARTICLE_TITLE("Article title", "title"),
		   CONTRIB("Contributors", "Contributors"),
		AUTH_CONT("Author contributions", "Author contributions"),
		BACK("Backmatter", "Back"),
		CASE("Case study", "Case stud(y|ies)"),
		CONCL("Conclusions", "Conclusions"),
		COMP_INT("Conflict of interests", "(Conflicts of interest|Competing interests)"),
		DISCUSS("Discussion", "Discussion"),
		FINANCIAL("Financial?", "Financial"),
		FIG("Figure (often caption)", "Fig(ure)?"),
		FRONT("Frontmatter", "front"),
		INTRO("Introduction", "Introduction|Babkground"),
		JOURNAL_META("", ""),
      		JOURNAL_TITLE("Journal title", "title"),
      		PUBLISHER_NAME("Publisher name", "publisher"),
		KEYWORD("Author keywords", "keywords"),
		METHODS("Methods and materials", "methods|methods(and|&)materials|experimental"),
		OTHER("Sections not in list", ""),
		PMCID("PMCID", "pmcid"),
		REF("References/citations", "references|citations"),
		RESULTS("Results", "results"),
		SUPPL("Supplementary material/supporting information", "(Supplementary|supporting)(material|information)"),
		TABLE("Table", ""),
		SUBTITLE("Subtitle of article", "subtitle"),
		TITLE("Title of article", "title"),
		
		;
		private String description;
		private Pattern pattern;
		
		private SectionTag(String description, String regex) {
			this.description = description;
			this.pattern = Pattern.compile(regex);
		}
		
		private static Map<String, SectionTag> tagByTagName = new HashMap<String, SectionTag>();
		static {
			for (SectionTag sectionTag : values()) {
				tagByTagName.put(sectionTag.toString(), sectionTag);
			}
		}
		private SectionTag() {
		}

		public static SectionTag getSectionTag(String tagName) {
			return tagByTagName.get(tagName);
		}

		public String getDescription() {
			return description;
		}

		public String[] getNames() {
			String[] names = new String[]{this.toString().toLowerCase()};
//			LOG.debug("N "+names[0]);
			return names;
		}
		
	};

	public final static String[] CLASSTAGS = {
		"abstract",  // 11
		"ack",  // 7
		"addr-line",  // 6
		"aff",  // 27
		"alt-title",  // 5
		"alternatives",  // 6
		"app",  // 2
		"app-group",
		"article",  // 9
		"article-categories",  // 9
		"article-id",  // 30
		"article-meta",  // 9
		"article-title",  // 424
		"author-notes",  // 7
		"back",  // 9
		"bio",  // 4
		"body",  // 9
		"bold",  // 168
		"caption",  // 68
		"citation",  // 71
		"collab", 
		"comment",  // 14
		"contrib",  // 65
		"contrib-group",  // 11
		"copyright-holder",  // 2
		"copyright-statement",  // 6
		"copyright-year",  // 5
		"corresp",  // 7
		"country",  // 2
		"counts",  // 3
		"date",  // 12
		"day",  // 19
		"edition",  // 2
		"element-citation",  // 108
		"elocation-id",  // 3
		"email",  // 20
		"equation-count",
		"etal",  // 71
		"fig",  // 41
		"fig-count",
		"fn",  // 31
		"fn-group",  // 5
		"fpage",  // 414
		"front",  // 9
		"given-names",  // 1857
		"graphic",  // 47
		"history",  // 6
		"institution",  // 2
		"issn",  // 14
		"issue",  // 9
		"issue-title",  // 8
		"italic",  // 952
		"journal-id",  // 22
		"journal-meta",  // 9
		"journal-title",  // 9
		"journal-title-group",  // 7
		"kwd",  // 36
		"kwd-group",  // 5
		"label",  // 232
		"license",  // 3
		"license-p",  // 2
		"list",  // 8
		"list-item",  // 25
		"lpage",  // 409
		"media",  // 13
		"mixed-citation",  // 285
		"month",  // 23
		"name",  // 1688
		"named-content",  // 717
		"object-id",  // 12
		"page-count",  // 3
		"permissions",  // 6
		"person-group",  // 425
		"pub-date",  // 16
		"pub-id",  // 455
		"publisher",  // 9
		"publisher-loc",  // 12
		"publisher-name",  // 21
		"ref",  // 464
		"ref-count",
		"ref-list",  // 9
		"role",  // 12
		"sec",  // 164
		"size",
		"source",  // 461
		"string-name",  // 175
		"subj-group",  // 38
		"subject",  // 40
		"suffi //",  // 5
		"sup",  // 69
		"supplementary-material",  // 13
		"surname",  // 1863
		"table-count",
		"table-wrap",  // 10
		"table-wrap-foot",  // 7
		"title",  // 199
		"title-group",  // 9
		"volume",  // 404
		"word-count",
		"year",  // 485
	};
	
	private static final SectionTag[] MAJOR_SECTIONS_ARRAY =
		{
			SectionTag.ABBREVIATION,
			SectionTag.ABSTRACT,
			SectionTag.ACK_FUND,
			SectionTag.APPENDIX,
			SectionTag.ARTICLE_META,
				SectionTag.CONTRIB,
			SectionTag.AUTH_CONT,
			SectionTag.BACK,
			SectionTag.CASE,
			SectionTag.CONCL,
			SectionTag.COMP_INT,
			SectionTag.DISCUSS,
			SectionTag.FINANCIAL,
			SectionTag.FIG,
			SectionTag.FRONT, // frontMatter (not title, article, authors, journal)
			SectionTag.INTRO,
			SectionTag.JOURNAL_META,
			SectionTag.KEYWORD,
			SectionTag.METHODS,
			SectionTag.OTHER,
			SectionTag.PMCID,
			SectionTag.REF,
			SectionTag.RESULTS,
			SectionTag.SUPPL,
			SectionTag.TABLE,
			SectionTag.SUBTITLE,
		};
	
	public static final List<SectionTag> MAJOR_SECTIONS = Arrays.asList(MAJOR_SECTIONS_ARRAY);
	public static final String PUB_ID = "pub-id";
	public static final String HELP = "help";
	
	private HtmlElement htmlElement;
	private Element jatsHtmlElement;
	public static final String DEFAULT_SECTION_TAGGER_RESOURCE = NAConstants.NORMA_RESOURCE+"/pubstyle/sectionTagger.xml";
	private Element tagsElement;
	private Map<SectionTag, TagElement> tagElementsByTag;
	private JATSArticleElement jatsArticleElement;
	private Multiset<String> tagClassMultiset;
	private List<List<HtmlDiv>> divListList;

	private CTree cTree;


	
	public JATSSectionTagger() {
		
	}

	public JATSSectionTagger(CTree cTree) {
		this.cTree = cTree;
		this.readJATS(cTree);
	}

	public HtmlElement readScholarlyHtml(File scholarlyHtmlFile) {
		testNotNullAndExists(scholarlyHtmlFile);
		HtmlFactory htmlFactory = new HtmlFactory();
		htmlElement = htmlFactory.parse(XMLUtil.parseQuietlyToDocument(scholarlyHtmlFile).getRootElement());
		return htmlElement;
	}

	public HtmlElement getHtmlElement() {
		LOG.debug("X "+htmlElement == null ? "null" : htmlElement.toXML());
		return htmlElement;
	}

	public List<HtmlTable> getHtmlTables() {
		HtmlElement htmlElement = getHtmlElement();
		return HtmlTable.extractSelfAndDescendantTables(htmlElement);
	}

	public List<HtmlDiv> getDivs() {
		HtmlElement htmlElement = getHtmlElement();
		return HtmlDiv.extractSelfAndDescendantDivs(htmlElement);
	}

	public List<HtmlSpan> getSpans() {
		HtmlElement htmlElement = getHtmlElement();
		return HtmlSpan.extractSelfAndDescendantSpans(htmlElement);
	}
	/**
			ABBREVIATIONS,
			ABSTRACT,
			ACKNOWLEDGEMENT,
			APPENDIX,
			ARTICLE_META,
			AUTHOR_CONTRIB,
			AUTHOR_META,
			BACK,
			CASE_STUDY,
			CONCLUSION,
			CONFLICT,
			DISCUSSION,
	 */

	public List<HtmlDiv> getAbbreviations() {
		return getDivsForCSSClass(SectionTag.ABBREVIATION);
	}

	public List<HtmlDiv> getAbstracts() {
		return getDivsForCSSClass(SectionTag.ABSTRACT);
	}

	public List<HtmlDiv> getAcknowledgements() {
		return getDivsForCSSClass(SectionTag.ACK_FUND);
	}

	public List<HtmlDiv> getAppendix() {
		return getDivsForCSSClass(SectionTag.APPENDIX);
	}

	public List<HtmlDiv> getArticleMeta() {
		return getDivsForCSSClass(SectionTag.ARTICLE_META);
	}

	public List<HtmlDiv> getAuthorContrib() {
		return getDivsForCSSClass(SectionTag.AUTH_CONT);
	}

//	public List<HtmlDiv> getAuthorMeta() {
//		return getDivsForCSSClass(SectionTag.AUTHOR_META);
//	}

	public List<HtmlDiv> getBackMatter() {
		return getDivsForCSSClass(SectionTag.BACK);
	}
	
	public List<HtmlDiv> getCaseStudies() {
		return getDivsForCSSClass(SectionTag.CASE);
	}
	
	public List<HtmlDiv> getConclusions() {
		return getDivsForCSSClass(SectionTag.CONCL);
	}

	public List<HtmlDiv> getConflicts() {
		return getDivsForCSSClass(SectionTag.COMP_INT);
	}

	public List<HtmlDiv> getDiscussions() {
		return getDivsForCSSClass(SectionTag.DISCUSS);
	}
	
	/**
			FIG,
			FINANCIAL,
			FRONT, // frontMatter (not title, article, authors, journal)
			INTRODUCTION,
			JOURNAL_META,
			KEYWORDS,
			METHODS,
			OTHER,
			REF_LIST,
			RESULTS,
			SUPPLEMENTAL,
			TABLE,
			TITLE,
	 */

	public List<HtmlDiv> getFigures() {
		return getDivsForCSSClass(SectionTag.FIG);
	}

	public List<HtmlDiv> getFinancialSupport() {
		return getDivsForCSSClass(SectionTag.FINANCIAL);
	}

	public HtmlHead getFrontMatter() {
		HtmlHead head = (HtmlHead) HtmlElement.getSingleChildElement(htmlElement, HtmlHead.TAG);
		return head;
	}

	public List<HtmlDiv> getIntroductions() {
		return getDivsForCSSClass(SectionTag.INTRO);
	}

	public List<HtmlDiv> getJournalMeta() {
		return getDivsForCSSClass(SectionTag.JOURNAL_META);
	}

	public List<HtmlDiv> getKeywords() {
		return getDivsForCSSClass(SectionTag.KEYWORD);
	}

	public List<HtmlDiv> getMethods() {
		return getDivsForCSSClass(SectionTag.METHODS);
	}

	public List<HtmlDiv> getOther() {
		return getDivsForCSSClass(SectionTag.OTHER);
	}

	public List<HtmlDiv> getRefLists() {
		return getDivsForCSSClass(SectionTag.REF);
	}

	public List<HtmlDiv> getResults() {
		return getDivsForCSSClass(SectionTag.RESULTS);
	}

	public List<HtmlDiv> getSubtitles() {
		return getDivsForCSSClass(SectionTag.SUBTITLE);
	}

	public List<HtmlDiv> getSupplemental() {
		return getDivsForCSSClass(SectionTag.SUPPL);
	}

	public List<HtmlDiv> getTables() {
		return getDivsForCSSClass(SectionTag.TABLE);
	}

	public List<HtmlDiv> getTitles() {
		return getDivsForCSSClass(SectionTag.TITLE);
	}

	public List<HtmlDiv> getDivsForCSSClass(SectionTag sectionTag) {
		return getDivsForCSSClass(sectionTag.getNames());
	}
	
	public List<HtmlDiv> getDivsForCSSClass(String ... names) {
		String xpath = createXPath("div", names);
//		HtmlElement htmlElement = getHtmlElement();
		HtmlElement htmlElement = (HtmlElement) jatsHtmlElement;
//		LOG.debug(htmlElement.toXML());
		List<HtmlDiv> divs = HtmlDiv.extractDivs(htmlElement, xpath);
		return divs;
	}

//	public List<HtmlSpan> getSpansForCSSClass(SectionTag sectionTag) {
//		return getSpansForCSSClass(sectionTag.getNames());
//	}
	
	public List<HtmlSpan> getSpansForCSSClass(String ... names) {
		String xpath = createXPath("span", names);
		List<HtmlSpan> spans = HtmlSpan.extractSpans(htmlElement, xpath);
		return spans;
	}
	
	// ==============================
	
	private String createXPath(String tag, String ...names) {
		if (names == null || names.length == 0) {
			throw new RuntimeException("get"+tag+" forCSSClass must have at least one arg");
		}
		String xpath = "//*[local-name()='"+tag+"' and (@class='"+names[0]+"'";

		for (int i = 1; i < names.length; i++) {
			xpath += " or @class='"+names[i]+"'";
		}
		xpath +=")]";
		LOG.trace("XPATH: "+xpath);
		return xpath;
	}

	public void readJATS(File jatsXml) {
		Element rawElement = XMLUtil.parseQuietlyToDocumentWithoutDTD(jatsXml).getRootElement();
		readJATS(rawElement);
	}

	private void readJATS(Element rawElement) {
		JATSFactory jatsFactory = new JATSFactory();
		jatsHtmlElement = jatsFactory.createHtml(rawElement);
		HtmlElement bodyHtmlElement = (HtmlElement) ((HtmlHtml)jatsHtmlElement).getBody();
		jatsArticleElement = (JATSArticleElement) bodyHtmlElement.getChild(0);
	}

	public Element getJATSHtmlElement() {
		return jatsHtmlElement;
	}

	public JATSArticleElement getJATSArticleElement() {
		return jatsArticleElement;
	}

	public Element readSectionTags(String resource) {
		ResourceLocation location = new ResourceLocation();
		InputStream is = location.getInputStreamHeuristically(resource);
		tagsElement = XMLUtil.parseQuietlyToDocument(is).getRootElement();
		return tagsElement;
	}

	/** reads element with contains the tag definitions
	 * from DEFAULT_SECTION_TAGGER_RESOURCE
	 * NAConstants.NORMA_RESOURCE+"/pubstyle/sectionTagger.xml
	 * @return
	 */
	public Element readSectionTags() {
		return readSectionTags(DEFAULT_SECTION_TAGGER_RESOURCE);
	}

	/** reads section tags and creates tagElementsByTag
	 * 
	 * @return
	 */
	public Map<SectionTag, TagElement> getOrCreateMap() {
		Element root = readSectionTags();
		tagElementsByTag = new HashMap<SectionTag, TagElement>();
		for (int i = 0; i < root.getChildElements().size(); i++) {
			Element child = root.getChildElements().get(i);
			String localName = child.getLocalName();
			if (localName.equals(JATSSectionTagger.HELP)) {
				continue;
			} else if (!localName.equals(TAG)) {
				LOG.error("Bad tag: "+localName);
			}
			TagElement tagElement = new TagElement(root.getChildElements().get(i));
			SectionTag tag = tagElement.getTag();
			tagElementsByTag.put(tag, tagElement);
		}
		return tagElementsByTag;
	}

	public TagElement get(SectionTag tag) {
		return (tagElementsByTag == null || tag == null) ? null : tagElementsByTag.get(tag.toString());
	}

	/** looks up in getOrCreateMap
	 * 
	 * @param tag
	 * @return
	 */
	public TagElement getTagElement(SectionTag tag) {
		Map<SectionTag, TagElement> tagElementByTag = getOrCreateMap();
		LOG.trace(tagElementByTag);
		TagElement tagElement = tagElementByTag.get(tag);
		return tagElement;
	}

	public String getXPath(SectionTag tag) {
		if (tag == null) return null;
		TagElement tagElement = getTagElement(tag);
		String xpath = tagElement == null ? null : tagElement.getXpath();
		return xpath;
	}

	public List<Element> getSections(SectionTag sectionTag) {
		String xpath = getXPath(sectionTag);
		LOG.trace("xpath for tag: "+xpath);
		return getSections(xpath);
	}

	private List<Element> getSections(String xpath) {
		List<Element> sections = new ArrayList<Element>();
		if (xpath != null) {
			Element jatsElement = getJATSHtmlElement();
			sections = XMLUtil.getQueryElements(jatsElement, xpath);
				for (Element section : sections) {
					String sectionS = section.toXML();
//					System.out.println("CLASS "+section.getAttributeValue("class")+" || "+sectionS.substring(0, Math.min(100, sectionS.length())));
				}
		}
		return sections;
	}
	
	public List<SectionTag> getSortedTags() {
		List<JATSSectionTagger.SectionTag> keys = new ArrayList<JATSSectionTagger.SectionTag> (tagElementsByTag.keySet());
		removeNulls(keys);
		Collections.sort(keys);
		return keys;
	}
	
	private void removeNulls(List<SectionTag> keys) {
		for (int i = keys.size() - 1; i >= 0; i--) {
			if (keys.get(i) == null) {
				keys.remove(i);
			}
		}
	}

	// ================================
	
	private void testNotNullAndExists(File scholarlyHtmlFile) {
		if (scholarlyHtmlFile == null) {
			throw new RuntimeException("null scholarlyHtml");
		} else if (!scholarlyHtmlFile.exists()) {
			throw new RuntimeException(scholarlyHtmlFile+" is not an existing file");
		} else if (scholarlyHtmlFile.isDirectory()) {
			throw new RuntimeException(scholarlyHtmlFile+" is a directory");
		}
	}

	public List<Element> getAllSections() {
		return getSections(".//*[@class]");
	}

	public Multiset<String> getOrCreateTagClassMultiset() {
		if (tagClassMultiset == null) {
			tagClassMultiset = HashMultiset.create();
			List<Element> tagElements = getAllSections();
			for (Element tagElement : tagElements) {
				String tagClass= tagElement.getAttributeValue(HtmlElement.CLASS);
				tagClassMultiset.add(tagClass);
			}
		}
		return tagClassMultiset;
	}

	/** at present this is just to create a Multiset from the project.
	 * each tree is read, tag multiset created and added to the tagMultiset in 'this'
	 * @param cProject
	 */
	public void readJATS(CProject cProject) {
		tagClassMultiset = HashMultiset.create();
		CTreeList cTreeList = cProject.getOrCreateCTreeList();
		for (CTree cTree : cTreeList) {
			JATSSectionTagger treeSectionTagger = new JATSSectionTagger();
			treeSectionTagger.readJATS(cTree);
			tagClassMultiset.addAll(getOrCreateTagClassMultiset());
		}
	}

	public void readJATS(CTree cTree) {
		this.readJATS(cTree.getExistingFulltextXML());
		this.getOrCreateTagClassMultiset();
	}

	/** at present this is just to create a Multiset from the project.
	 * each tree is read, tag multiset created and added to the tagMultiset in 'this'
	 * @param cProject
	 */
	public List<List<HtmlDiv>> getAbbreviations(CProject cProject) {
		List<List<HtmlDiv>> divListList = new ArrayList<List<HtmlDiv>>();
		CTreeList cTreeList = cProject.getOrCreateCTreeList();
		for (CTree cTree : cTreeList) {
			JATSSectionTagger treeSectionTagger = new JATSSectionTagger();
			treeSectionTagger.readJATS(cTree.getExistingFulltextXML());
			List<HtmlDiv> divList = getAbbreviations();
			divListList.add(divList);
		}
		return divListList;
	}

	/** at present this is just to create a Multiset from the project.
	 * each tree is read, tag multiset created and added to the tagMultiset in 'this'
	 * @param cProject
	 */
	public List<List<HtmlDiv>> getAbstracts(CProject cProject) {
		divListList = new ArrayList<List<HtmlDiv>>();
		CTreeList cTreeList = cProject.getOrCreateCTreeList();
		for (CTree cTree : cTreeList) {
			JATSSectionTagger treeSectionTagger = new JATSSectionTagger();
			treeSectionTagger.readJATS(cTree.getExistingFulltextXML());
			List<HtmlDiv> divList1 = new ArrayList<HtmlDiv>();
			divList1 = treeSectionTagger.getAbstracts();
			List<HtmlDiv> divList = divList1;
			divListList.add(divList);
		}
		return divListList;
	}

	/** creates a list of list of divs using the particular extractor on a CProject
	 * @param cProject
	 */
	public static List<List<HtmlDiv>> getDivList(CProject cProject, DivListExtractor extractor) {
		List<List<HtmlDiv>> divListList = new ArrayList<List<HtmlDiv>>();
		CTreeList cTreeList = cProject.getOrCreateCTreeList();
		for (CTree cTree : cTreeList) {
			JATSSectionTagger tagger = new JATSSectionTagger(cTree);
			List<HtmlDiv> divList = tagger.getDivList(extractor);
			divListList.add(divList);
		}
		return divListList;
	}

	/** get a list of Divs of given type.
	 * assume we have read a cTree
	 * @param extractor
	 * @return null if no cTree else list of divs of extractor type
	 */
	public List<HtmlDiv> getDivList(DivListExtractor extractor) {
		List<HtmlDiv> divList = null;
		if (cTree != null) {
			JATSSectionTagger tagger = new JATSSectionTagger();
			tagger.readJATS(cTree.getExistingFulltextXML());
			divList = extractor.getDivList(tagger);
		}
		return divList;
	}

	/** get a single HtmlElement.
	 * assume we have read a cTree
	 * @param extractor
	 * @return null if no cTree else list of divs of extractor type
	 */
	public HtmlElement getHtmlElement(HtmlElementExtractor extractor) {
		HtmlElement htmlElement = null;
		if (cTree != null) {
			JATSSectionTagger tagger = new JATSSectionTagger();
			tagger.readJATS(cTree.getExistingFulltextXML());
			htmlElement = extractor.getHtmlElement(tagger);
		}
		return htmlElement;
	}

//	public List<List<HtmlDiv>> getAbstracts1(CProject cProject) {
//		DivListExtractor abstr = new DivListExtractor() {
//			public List<HtmlDiv> getDivList(JATSSectionTagger treeSectionTagger, CTree cTree) { 
//				return treeSectionTagger.getAbstracts(cTree); }
//		};
//		return getListOfDivs(cProject, abstr);
//	}
	
//	public static class AbstractExtractor implements DivListExtractor {
//		public List<HtmlDiv> getDivList(JATSSectionTagger treeSectionTagger, CTree cTree) { 
//			return treeSectionTagger.getAbstracts(cTree); }
//	}
	
//	public List<List<HtmlDiv>> getAbstracts2(CProject cProject) {
//		return getListOfDivs(cProject, new AbstractExtractor());
//	}

//	public List<HtmlDiv> getAbstracts2(CTree cTree) {
//		return getListOfDivs(cTree, new AbstractExtractor());
//	}



}
