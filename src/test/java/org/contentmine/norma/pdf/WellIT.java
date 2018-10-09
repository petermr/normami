package org.contentmine.norma.pdf;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.List;

import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.contentmine.cproject.files.CProject;
import org.contentmine.cproject.files.CTree;
import org.contentmine.cproject.files.CTreeList;
import org.contentmine.cproject.util.CMineTestFixtures;
import org.contentmine.eucl.euclid.Real;
import org.contentmine.eucl.euclid.Real2Range;
import org.contentmine.eucl.euclid.RealRange;
import org.contentmine.eucl.xml.XMLUtil;
import org.contentmine.graphics.html.HtmlHtml;
import org.contentmine.graphics.html.HtmlStyle;
import org.contentmine.graphics.html.HtmlTable;
import org.contentmine.graphics.html.HtmlTd;
import org.contentmine.graphics.html.HtmlTr;
import org.contentmine.graphics.svg.SVGElement;
import org.contentmine.graphics.svg.SVGG;
import org.contentmine.graphics.svg.SVGLine;
import org.contentmine.graphics.svg.SVGSVG;
import org.contentmine.graphics.svg.SVGText;
import org.contentmine.image.diagram.DiagramAnalyzer;
import org.contentmine.image.pixel.PixelEdge;
import org.contentmine.image.pixel.PixelEdgeList;
import org.contentmine.image.pixel.PixelGraph;
import org.contentmine.image.pixel.PixelIsland;
import org.contentmine.image.pixel.PixelIslandList;
import org.contentmine.image.pixel.PixelRingList;
import org.contentmine.pdf2svg2.PDFDocumentProcessor;
import org.junit.Ignore;
import org.junit.Test;

import junit.framework.Assert;


public class WellIT {
	private static final Logger LOG = Logger.getLogger(WellIT.class);
	static {
		LOG.setLevel(Level.DEBUG);
	}

	@Test
	public void testWell() throws Exception {
		File sourceDir = new File("/Users/pm286/ContentMine/well/testfiles");
		if (!sourceDir.exists()) {
			LOG.info("skipped pmr only test");			return;
		}
		File targetDir = new File("target/well/");
		boolean skipSVG = false;
		CProject cProject = null;
		CTreeList cTreeList = null;
		CMineTestFixtures.cleanAndCopyDir(sourceDir, targetDir);
		cProject = new CProject(targetDir);
		cTreeList = cProject.getOrCreateCTreeList();
		for (CTree cTree : cTreeList) {
			LOG.debug("******* "+cTree+" **********");
			List<File> svgFiles = cTree.getExistingSVGFileList();
		    PDFDocumentProcessor documentProcessor = new PDFDocumentProcessor();
		    documentProcessor.setMinimumImageBox(100, 100);
		    documentProcessor.readAndProcess(cTree.getExistingFulltextPDF());
		    File outputDir = new File(targetDir, cTree.getName());
			documentProcessor.writeSVGPages(outputDir);
	    	documentProcessor.writeRawImages(outputDir);
		}
	}
	
	@Test
	public void testWellTable() throws Exception {
		File sourceDir = new File("/Users/pm286/ContentMine/well/testfiles");
		File targetDir = sourceDir;
		CProject cProject = new CProject(targetDir);
		CTreeList cTreeList = cProject.getOrCreateCTreeList();
		for (CTree cTree : cTreeList) {
			LOG.debug("******* "+cTree+" **********");
			List<File> svgFiles = cTree.getExistingSVGFileList();
		    PDFDocumentProcessor documentProcessor = new PDFDocumentProcessor();
		    documentProcessor.setMinimumImageBox(100, 100);
		    documentProcessor.readAndProcess(cTree.getExistingFulltextPDF());
		    File outputDir = new File(targetDir, cTree.getName());
			documentProcessor.writeSVGPages(outputDir);
	    	documentProcessor.writeRawImages(outputDir);
		}
	}

	@Test
	public void test3brP1() throws Exception {
		File page2svg = new File("/Users/pm286/ContentMine/well/testfiles/3br/svg/fulltext-page.1.svg");
		SVGElement svgElement = SVGElement.readAndCreateSVG(
				XMLUtil.parseQuietlyToRootElement(new FileInputStream(page2svg)));
		SVGG g = new SVGG();
		List<SVGText> texts = SVGText.extractSelfAndDescendantTexts(svgElement);
		HtmlHtml html = new HtmlHtml();
		HtmlStyle style = html.getOrCreateHead().getOrCreateHtmlStyle();
		style.appendChild(" td {border : solid red; font-family : monospace;}");
		HtmlTable htmlTable = new HtmlTable();
		html.appendChild(htmlTable);
		double ylast = -10.;
		HtmlTr tr = new HtmlTr();
		htmlTable.addRow(tr);
		for (SVGText text : texts) {
			double y = text.getY();
			if (y > 90 && y < 240) {
				if(!Real.isEqual(y,  ylast, 0.2)) {
					tr = new HtmlTr();
					htmlTable.addRow(tr);
				}
				HtmlTd td = new HtmlTd();
				tr.appendChild(td);
				String value = text.getValue();
				td.appendChild(value);
				text.setFontSize(8.0);
				g.appendChild(text.copy());
			}
		}
		SVGSVG.wrapAndWriteAsSVG(g, new File("target/well/3brp1.svg"));
		XMLUtil.debug(html, new File("target/well/3brp1.html"), 1);
		
	}

	
	@Test
	public void testTraceIslands() throws IOException {
		String root = "1.1.clip";
		String project = "s.croce_001";
		File imageFile = new File("/Users/pm286/ContentMine/well/testfiles/" + project + "/images/page."+root+".png");
		Assert.assertTrue(""+imageFile, imageFile.exists());
		DiagramAnalyzer diagramAnalyzer = new DiagramAnalyzer();
		PixelIslandList pixelIslandList = diagramAnalyzer.createDefaultPixelIslandList(imageFile);
		pixelIslandList.removeIslandsLessThan(new Real2Range(new RealRange(0, 10), new RealRange(0, 10)));
		Assert.assertEquals(20, pixelIslandList.size());
		SVGSVG.wrapAndWriteAsSVG(pixelIslandList.getOrCreateSVGG(), new File("target/well/" + project + "/" + root + ".svg"));

	}


	
	@Test
	public void testTracePixelRings() throws IOException {
		String root = "1.1.clip";
		String project = "s.croce_001";
		File imageFile = new File("/Users/pm286/ContentMine/well/testfiles/" + project + "/images/page."+root+".png");
		Assert.assertTrue(""+imageFile, imageFile.exists());
		DiagramAnalyzer diagramAnalyzer = new DiagramAnalyzer();
		PixelRingList pixelRingList = diagramAnalyzer.createDefaultPixelRings(imageFile);
		Assert.assertEquals("pixelRings", 5, pixelRingList.size());
		SVGSVG.wrapAndWriteAsSVG(pixelRingList.plotPixels(), new File("target/well/" + project + "/" + root + ".rings.svg"));
	}

	@Test
	public void testGridIslands() throws IOException {
		String root = "1.1";
		String ctree = "nusco_002";
		File imageFile = new File("/Users/pm286/ContentMine/well/testfiles/"+ctree+"/images/page."+root+".png");
		Assert.assertTrue(""+imageFile, imageFile.exists());
		DiagramAnalyzer diagramAnalyzer = new DiagramAnalyzer();
		PixelIslandList pixelIslandList = diagramAnalyzer.createDefaultPixelIslandList(imageFile);
		pixelIslandList.removeIslandsLessThan(new Real2Range(new RealRange(0, 10), new RealRange(0, 10)));
		Assert.assertEquals(20, pixelIslandList.size());
		SVGSVG.wrapAndWriteAsSVG(pixelIslandList.getOrCreateSVGG(), new File("target/well/"+ctree+"/page."+root+".svg"));

	}
	
	@Test
	@Ignore // runs for too long
	public void testGridPixelRings() throws IOException {
		String root = "1.1";
		String project = "nusco_002";
		File imageFile = new File("/Users/pm286/ContentMine/well/testfiles/" + project + "/images/page."+root+".png");
		Assert.assertTrue(""+imageFile, imageFile.exists());
		DiagramAnalyzer diagramAnalyzer = new DiagramAnalyzer();
		PixelRingList pixelRingList = diagramAnalyzer.createDefaultPixelRings(imageFile);
		Assert.assertEquals("pixelRings", 5, pixelRingList.size());
		SVGSVG.wrapAndWriteAsSVG(pixelRingList.plotPixels(), new File("target/well/" + project + "/" + root + ".rings.svg"));
	}


	@Test
	public void testGridClipThinMinBoxMinSize() throws IOException {
		String root = "1.1.clip";
		String ctree = "nusco_002";
		File imageFile = new File("/Users/pm286/ContentMine/well/testfiles/"+ctree+"/images/page."+root+".png");
		Assert.assertTrue(""+imageFile, imageFile.exists());
		DiagramAnalyzer diagramAnalyzer = new DiagramAnalyzer();
		PixelIslandList pixelIslandList = diagramAnalyzer.createDefaultPixelIslandList(imageFile);
		pixelIslandList.removeIslandsLessThan(new Real2Range(new RealRange(0, 10), new RealRange(0, 10)));
		pixelIslandList.removeIslandsLessThan(50);
		SVGSVG.wrapAndWriteAsSVG(pixelIslandList.getOrCreateSVGG(), new File("target/well/"+ctree+"/page."+root+".svg"));
		for (PixelIsland pixelIsland : pixelIslandList) {
			PixelGraph graph = pixelIsland.getOrCreateGraph();
			pixelIsland.getOrCreateEdgeList();
			
			PixelEdgeList edgeList = graph.getOrCreateEdgeList();
			LOG.debug("E E"+edgeList.size());
			PixelEdgeList removeEdgeList = new PixelEdgeList();
			for (PixelEdge edge : edgeList) {
				PixelEdge edge1 = edge.createSegmentedEdge(1);
				SVGG line = edge1.createLineSVG();
				List<SVGLine> lineList = SVGLine.findHorizontalOrVerticalLines(line, 0.1);
				if (lineList.size() == 1 ) {
					removeEdgeList.add(edge);
				}
			}
			LOG.debug("REM "+removeEdgeList.size());
			pixelIsland.removePixelEdgeList(removeEdgeList);
		}

	}

}
