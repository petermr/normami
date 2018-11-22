package org.contentmine.norma.sections;

import org.contentmine.graphics.html.HtmlDiv;

public class BodyExtractor implements SingleDivExtractor {
	public HtmlDiv getSingleDiv(JATSSectionTagger tagger) { 
		return tagger.getBody();
	}
}
