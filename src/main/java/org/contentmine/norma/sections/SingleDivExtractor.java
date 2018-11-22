package org.contentmine.norma.sections;

import java.util.List;

import org.contentmine.cproject.files.CTree;
import org.contentmine.graphics.html.HtmlDiv;

/** defines an abstractor of a single div
 * normally required to be exactly one (or possibly none)
 * examples are, <front> <body> <back> which should never be more than one
 * @author pm286
 *
 */
public interface SingleDivExtractor {
	public HtmlDiv getSingleDiv(JATSSectionTagger tagger);
}
	
