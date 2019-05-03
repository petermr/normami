package org.contentmine.ami.tools;

import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.contentmine.eucl.euclid.Int2Range;
import org.contentmine.eucl.xml.XMLUtil;
import org.contentmine.graphics.html.HtmlElement;
import org.contentmine.graphics.html.HtmlP;
import org.contentmine.graphics.html.HtmlSpan;
import org.contentmine.image.ocr.HOCRReader;

import nu.xom.Element;

public class SPSSForestPlot {
	private static final Logger LOG = Logger.getLogger(SPSSForestPlot.class);
	static {
		LOG.setLevel(Level.DEBUG);
	}

	private HtmlElement htmlElement;
	private List<HtmlSpan> wordSpanList;
	
	private SPSSForestPlot() {
		
	}
	
	public static SPSSForestPlot createSPSSPlot(HtmlElement htmlElement) {
		SPSSForestPlot spssForestPlot = new SPSSForestPlot(); 
		if (htmlElement != null) {
			spssForestPlot.setHtmlElement(htmlElement);
			spssForestPlot.extractWordSpans();
			spssForestPlot.extractBBoxes();
			spssForestPlot.extractLines();
		} else {
			LOG.debug("null htmlElement");
		}
		return spssForestPlot;
	}
	
	private void extractWordSpans() {
		List<Element> elements = XMLUtil.getQueryElements(htmlElement, 
				"//*[local-name()='span' and @class='ocrx_word']");
		
		wordSpanList = new ArrayList<>();
		for (Element element : elements) {
			HtmlSpan spanElement = (HtmlSpan)element;
			String value = spanElement.getValue();
			
			wordSpanList.add(spanElement);
		}
		
	}

	private void extractBBoxes() {
		for (HtmlSpan wordSpan : wordSpanList) {
			String title = wordSpan.getTitle();
			Int2Range bbox = HOCRReader.getBboxFromTitle(title);
			String content = wordSpan.getValue();
			System.out.println(">>> "+bbox+": "+content);
		}
	}

	public void setHtmlElement(HtmlElement htmlElement) {
		this.htmlElement = htmlElement;
	}

	private void extractLines() {
		extractOddsRatio();
		extractStudyOrSubgroup();
		boolean subtotal = false;
		extractTotal(subtotal);
		subtotal = true;
		extractTotal(subtotal);
		extractTotalEvents(subtotal);
		extractHeterogeneity();
		extractTestForOverallEffect();
	}

	
	/**
    <span class='ocr_line' id='line_1_2' title="bbox 0 21 747 36; baseline 0 -1; x_size 19.5; x_descenders 5.5; x_ascenders 3">
     <span class='ocrx_word' id='word_1_5' title='bbox 0 0 1267 354; x_wconf 1'>Study</span>
     <span class='ocrx_word' id='word_1_6' title='bbox 0 0 1267 354; x_wconf 95'>or</span>
     <span class='ocrx_word' id='word_1_7' title='bbox 0 0 1267 354; x_wconf 91'>Subgroup</span>
     <span class='ocrx_word' id='word_1_8' title='bbox 0 0 1267 354; x_wconf 59'>__Events</span>
     <span class='ocrx_word' id='word_1_9' title='bbox 0 21 747 36; x_wconf 37'>_Total____Events</span>
     <span class='ocrx_word' id='word_1_10' title='bbox 0 0 1267 354; x_wconf 43'>_Total_Weight</span>
     <span class='ocrx_word' id='word_1_11' title='bbox 0 0 1267 354; x_wconf 61'>_M-H.</span>
     <span class='ocrx_word' id='word_1_12' title='bbox 0 0 1267 354; x_wconf 89'>Fixed.</span>
     <span class='ocrx_word' id='word_1_13' title='bbox 0 0 1267 354; x_wconf 96'>95%</span>
     <span class='ocrx_word' id='word_1_14' title='bbox 0 0 1267 354; x_wconf 72'>Cl</span>
     <span class='ocrx_word' id='word_1_15' title='bbox 0 0 1267 354; x_wconf 96'>Year</span>
    </span>
*/
	private void extractStudyOrSubgroup() {
		
	}
	
	/**
<span class='ocr_line' id='line_1_14' title="bbox 13 289 390 302; baseline -0.003 0; x_size 23.125; x_descenders 5.5; x_ascenders 5.875">
      <span class='ocrx_word' id='word_1_70' title='bbox 13 289 47 302; x_wconf 96'>Total</span>
      <span class='ocrx_word' id='word_1_71' title='bbox 52 290 97 302; x_wconf 96'>events</span>
      <span class='ocrx_word' id='word_1_72' title='bbox 212 290 228 302; x_wconf 87'>69</span>
      <span class='ocrx_word' id='word_1_73' title='bbox 366 290 390 302; x_wconf 96'>107</span>
     </span>	 * 
	 * @param subtotal
	 */
	private void extractTotalEvents(boolean subtotal) {
		LOG.debug("extractTotalEvents");
	}

	private void extractOddsRatio() {
		LOG.debug("extractOddsRatio");
	}

	
	/** extract Totals or Subtotal
	 * @param subtotal use "Subtotal" else use "Total"
	 */
	private void extractTotal(boolean subtotal) {
		String first = "Total";
		for (int i = 0; i < wordSpanList.size(); i++) {
//			if (word)
		}
	}

	/**
   <div class='ocr_carea' id='block_1_3' title="bbox 13 310 387 345">
    <p class='ocr_par' id='par_1_4' lang='eng' title="bbox 13 310 387 345">
     <span class='ocr_line' id='line_1_15' title="bbox 14 310 387 325; baseline 0 -3; x_size 24.25; x_descenders 5.5; x_ascenders 6.25">
      <span class='ocrx_word' id='word_1_74' title='bbox 14 310 112 325; x_wconf 96'>Heterogeneity:</span>
      <span class='ocrx_word' id='word_1_75' title='bbox 118 310 146 322; x_wconf 50'>Chi?</span>
      <span class='ocrx_word' id='word_1_76' title='bbox 151 313 159 319; x_wconf 95'>=</span>
      <span class='ocrx_word' id='word_1_77' title='bbox 165 310 205 324; x_wconf 95'>14.61,</span>
      <span class='ocrx_word' id='word_1_78' title='bbox 210 310 224 322; x_wconf 95'>df</span>
      <span class='ocrx_word' id='word_1_79' title='bbox 228 313 236 319; x_wconf 86'>=</span>
      <span class='ocrx_word' id='word_1_80' title='bbox 241 310 249 322; x_wconf 86'>9</span>
      <span class='ocrx_word' id='word_1_81' title='bbox 254 310 269 325; x_wconf 92'>(P</span>
      <span class='ocrx_word' id='word_1_82' title='bbox 274 313 282 319; x_wconf 91'>=</span>
      <span class='ocrx_word' id='word_1_83' title='bbox 286 310 325 325; x_wconf 91'>0.10);</span>
      <span class='ocrx_word' id='word_1_84' title='bbox 331 310 340 322; x_wconf 1'>F*</span>
      <span class='ocrx_word' id='word_1_85' title='bbox 344 313 352 319; x_wconf 92'>=</span>
      <span class='ocrx_word' id='word_1_86' title='bbox 357 310 387 322; x_wconf 92'>38%</span>
     </span>
    </p>
	 */
	private void extractHeterogeneity() {
	}
	

		/** TestForOverallEffect
   <span class='ocr_line' id='line_1_16' title="bbox 13 330 303 345; baseline -0.003 -2; x_size 24.25; x_descenders 5.5; x_ascenders 6.25">
    <span class='ocrx_word' id='word_1_87' title='bbox 13 330 43 343; x_wconf 94'>Test</span>
    <span class='ocrx_word' id='word_1_88' title='bbox 47 330 66 343; x_wconf 94'>for</span>
    <span class='ocrx_word' id='word_1_89' title='bbox 70 330 114 343; x_wconf 93'>overall</span>
    <span class='ocrx_word' id='word_1_90' title='bbox 119 330 160 343; x_wconf 93'>effect:</span>
    <span class='ocrx_word' id='word_1_91' title='bbox 165 330 175 342; x_wconf 92'>Z</span>
    <span class='ocrx_word' id='word_1_92' title='bbox 180 334 188 340; x_wconf 87'>=</span>
    <span class='ocrx_word' id='word_1_93' title='bbox 193 330 222 343; x_wconf 87'>2.70</span>
    <span class='ocrx_word' id='word_1_94' title='bbox 227 330 242 345; x_wconf 91'>(P</span>
    <span class='ocrx_word' id='word_1_95' title='bbox 247 334 255 339; x_wconf 90'>=</span>
    <span class='ocrx_word' id='word_1_96' title='bbox 260 330 303 345; x_wconf 90'>0.007)</span>
   </span>
    */
	public HtmlP extractTestForOverallEffect() {
		HtmlP p = null;
		return p;
	}

	/**
     <span class='ocr_line' id='line_1_53' title="bbox 834 339 995 354; baseline 0 -3; x_size 24.25; x_descenders 5.5; x_ascenders 6.25">
      <span class='ocrx_word' id='word_1_166' title='bbox 834 339 888 351; x_wconf 96'>Favours</span>
      <span class='ocrx_word' id='word_1_167' title='bbox 893 339 947 354; x_wconf 93'>[Pedicle</span>
      <span class='ocrx_word' id='word_1_168' title='bbox 952 339 995 354; x_wconf 91'>screw]</span>
     </span>
	 */
	private void getFavours() {
		
	}

}
