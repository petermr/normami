package org.contentmine.ami.lookups;

import java.io.File;
import java.net.URL;
import java.util.Arrays;
import java.util.List;

import org.apache.commons.io.FileUtils;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.contentmine.ami.AMIFixtures;
import org.contentmine.ami.dictionary.DefaultAMIDictionary;
import org.contentmine.ami.plugins.AMIArgProcessor;
import org.contentmine.ami.plugins.species.SpeciesArgProcessor;
import org.contentmine.eucl.euclid.IntArray;
import org.contentmine.eucl.xml.XMLUtil;
import org.contentmine.graphics.html.HtmlElement;
import org.contentmine.norma.NAConstants;
import org.junit.Assert;
import org.junit.Ignore;
import org.junit.Test;

import nu.xom.Element;

/** I think the API is outdates so some of these fail, especially for species.
 * 
 * @author pm286
 *
 */
// @Ignore // unless testing Lookup
public class WikipediaLookupIT {

	
	public static final Logger LOG = Logger.getLogger(WikipediaLookupIT.class);
	static {
		LOG.setLevel(Level.DEBUG);
	}
	

	@Test
//	@Ignore // LONG
	public void testGetWikidataForDictionariesAndUpdate() throws Exception {
		// /normami/src/main/resources/org/contentmine/ami/plugins/dictionary/invasive.xml
		DefaultAMIDictionary dictionary = new DefaultAMIDictionary();
		dictionary.setDictionaryName("invasive");
		dictionary.setInputDir(NAConstants.PLUGINS_DICTIONARY_DIR);
		dictionary.setOutputDir(NAConstants.LOCAL_DICTIONARIES);
		dictionary.annotateDictionaryWithWikidata(0, 100000);
	}

	@Test
	@Ignore // VERY LONG
	public void testGetWikidataForDictionariesAndUpdate1() throws Exception {
		DefaultAMIDictionary dictionary = new DefaultAMIDictionary();
		dictionary.setDictionaryName("funders");
		dictionary.setInputDir(NAConstants.PLUGINS_DICTIONARY_DIR);
		dictionary.setOutputDir(NAConstants.LOCAL_DICTIONARIES);
		dictionary.annotateDictionaryWithWikidata();
	}

	
}
