package org.contentmine.norma.sections;

import org.contentmine.graphics.html.HtmlDiv;

public class FrontExtractor implements SingleDivExtractor {
	public HtmlDiv getSingleDiv(JATSSectionTagger tagger) { 
		return tagger.getFront();
	}
}
