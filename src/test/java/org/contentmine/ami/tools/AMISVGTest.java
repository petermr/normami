package org.contentmine.ami.tool;

import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.contentmine.ami.tools.AMISVGTool;
import org.junit.Test;

/** test SVG.
 * 
 * @author pm286
 *
 */
public class AMISVGTest {
	private static final Logger LOG = Logger.getLogger(AMISVGTest.class);
	static {
		LOG.setLevel(Level.DEBUG);
	}

	@Test
	/** 
	 * convert single file
	 */
	public void testSVGTree() throws Exception {
		String args = ""
				+ "-t /Users/pm286/workspace/uclforest/forestplots/bowmann"
				+ " --pages 1 9";
		new AMISVGTool().runCommands(args);
	}
	
	@Test
	/** 
	 * convert single file
	 */
	public void testSVGTreeRegex() throws Exception {
		String args = ""
				+ "-t /Users/pm286/workspace/uclforest/forestplots/bowmann"
				+ ""
				+ " --regex"
				+ " Hedge's\\s+g\\s+and\\s+95%\\s+CI"
				+ " Hedge's(\\s+g)?"
				+ " (control|treatment)\\s+group"
				+ " sample(\\s+|\\-)size"
				+ " statistics\\s+for\\s+each\\s+study"
				+ " st(andar)?d\\s+diff\\s+in\\s+means"
				+ " st(andar)d\\s+error"
				+ " std\\.\\s+mean\\s+difference"
				+ " correlation\\s+and\\s+95%\\s+CI"
				+ " confidence\\sinterval"
				+ " Forest\\s+plots?"
				+ " favou?rs\\s+(control|intervention|experiment(al)?|treatment|A|B)"
				+ " experimental"
				+ " (lower|upper)\\s+limit"
				+ " relative\\s+weight"
				+ " study\\s+(name|size)s?"
				+ " (weighted)?\\s+effect\\s+sizes?"
				+ ""
				+ " (z|p)\\-value"
				+ " control"
				+ " random"
				+ " variance"
				+ " correlations?"
				+ " measure"
				+ ""
				
				+ " stud(y|ies)"
				+ " LL"
				+ " ES"
				+ " UL"
				+ " CI"
				+ " effects?"
				+ " weights?"
				+ " sizes?"
				+ " subgroups?"
				+ " outcomes?"
				+ " interventions?"
				+ ""
//				+ "Weighted effect sizes in randomized controlled trials of reading interventions "
//				+ "    with outcomes measured at the end of interventions."
//				+ "Weighted effect sizes in quasi-experimental studies of reading interventions "
//				+ "    with outcomes measured at the end of interventions."
         ;
		new AMISVGTool().runCommands(args);
	}
	
	@Test
	/** 
	 * convert s
	 */
	public void testSVGDiagrams() throws Exception {
		String args = ""
				+ "-t /Users/pm286/workspace/uclforest/forestplots/dietrichson"
				+ " --pages 21 22 23 26"
				+ " -vv"
				;
		new AMISVGTool().runCommands(args);
	}

}
