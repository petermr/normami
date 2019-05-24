package org.contentmine.norma.image.ocr;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.imageio.ImageIO;

import org.apache.commons.io.FilenameUtils;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.contentmine.ami.tools.gocr.GOCRPageElement;
import org.contentmine.eucl.euclid.Int2;
import org.contentmine.eucl.euclid.Int2Range;
import org.contentmine.eucl.euclid.Real2;
import org.contentmine.eucl.euclid.RealSquareMatrix;
import org.contentmine.eucl.euclid.Util;
import org.contentmine.eucl.xml.XMLUtil;
import org.contentmine.graphics.svg.SVGElement;
import org.contentmine.graphics.svg.SVGG;
import org.contentmine.graphics.svg.SVGImage;
import org.contentmine.graphics.svg.SVGRect;
import org.contentmine.graphics.svg.SVGSVG;
import org.contentmine.graphics.svg.SVGText;
import org.contentmine.image.ImageUtil;
import org.contentmine.norma.util.CommandRunner;

import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.Multimap;

import nu.xom.Element;

/**
 * 
 * Optical Character Recognition --- gocr 0.52 20181015
 Copyright (C) 2001-2018 Joerg Schulenburg  GPG=1024D/53BDFBE3
 released under the GNU General Public License
 using: gocr [options] pnm_file_name  # use - for stdin
 options (see gocr manual pages for more details):
 -h, --help, -V --version
 -i name   - input image file (pnm,pgm,pbm,ppm,pcx,...)
 -o name   - output file  (redirection of stdout)
 -e name   - logging file (redirection of stderr)
 -x name   - progress output to fifo (see manual)
 -p name   - database path including final slash (default is ./db/)
 -f fmt    - output format (ISO8859_1 TeX HTML XML UTF8 ASCII)
 -l num    - threshold grey level 0<160<=255 (0 = autodetect)
 -d num    - dust_size (remove small clusters, -1 = autodetect)
 -s num    - spacewidth/dots (0 = autodetect)
 -v num    - verbose (see manual page)
 -c string - list of chars (debugging, see manual)
 -C string - char filter (ex. hexdigits: 0-9A-Fx, only ASCII)
 -m num    - operation modes (bitpattern, see manual)
 -a num    - value of certainty (in percent, 0..100, default=95)
 -u string - output this string for every unrecognized character
 examples:
	gocr -m 4 text1.pbm                   # do layout analyzis
	gocr -m 130 -p ./database/ text1.pbm  # extend database
	djpeg -pnm -gray text.jpg | gocr -    # use jpeg-file via pipe

 website: http://www-e.uni-magdeburg.de/jschulen/ocr/
 
 
       The verbosity is specified as a bitfield:

       1         print more info
       2         list shapes of boxes (see -c) to stderr
       4         list pattern of boxes (see -c) to stderr
       8         print pattern after recognition for debugging
       16        print debug information about recognition of lines to stderr
       32        create outXX.png with boxes and lines marked on each general OCR-step

       The operation modes are:

       2         use database to recognize characters which are not recognized by other algorithms, (early  develop-
                 ment)
       4         switching on layout analysis or zoning (development)
       8         don't compare unrecognized characters to recognized one
       16        don't try to divide overlapping characters to two or three single characters
       32        don't do context correction
       64        character  packing, before recognition starts, similar characters are searched and only one of this
                 characters will be send to the recognition engine (development)
       130       extend database, prompts user for unidentified characters  and  extends  the  database  with  users
                 answer (128+2, early development)
       256       switch off the recognition engine (makes sense together with -m 2)

http://www-e.uni-magdeburg.de/jschulen/ocr/


 * @author pm286
 *
 */
public class GOCRConverter extends CommandRunner {


	public final static Logger LOG = Logger.getLogger(GOCRConverter.class);
	static {LOG.setLevel(Level.DEBUG);}

	private static final String GOCR = "gocr";
	private static final String USR_LOCAL_BIN_GOCR = "/usr/local/bin/gocr";
	private static final String PNM = "pnm";
	private String gocrPath = USR_LOCAL_BIN_GOCR;
	private static final String PNGTOPNM = "/usr/local/bin/pngtopnm";
	
	/**
	 * These bits can be added as long as they are not repeated and 2 /130 are not both present
	 */
  // 2        use database to recognize characters which are not recognized by other algorithms, (early  development)
    public final static int USE_DATABASE = 2; 
  // 4         switching on layout analysis or zoning (development)
    public final static int LAYOUT = 4;
  // 8         don't compare unrecognized characters to recognized one
    public final static int DONT_COMPARE_UNRECOGNISED = 8;
  // 16        don't try to divide overlapping characters to two or three single characters
    public final static int DONT_DIVIDE_OVERLAPPING = 16;
  //  32        don't do context correction
    public final static int DONT_CONTEXT_CORRECT = 32;
  // 64        character  packing, before recognition starts, similar characters are searched and only one of this
  // characters will be send to the recognition engine (development)
    public final static int CHARACTER_PACKING = 64;
  // 130       extend database, prompts user for unidentified characters  and  extends  the  database  with  users
  //          answer (128+2, early development)
    public final static int EXTEND_DATABASE = 130;
  // 256       switch off the recognition engine (makes sense together with -m 2)
    public final static int SWITCH_OFF_RECOGNITION = 256;
private String inputFilename;
private BufferedImage inputImage;
private List<GOCRCharBox> gocrCharBoxList;
//  maps
private Multimap<String, GOCRCharBox> charBoxByText;
private Map<Int2Range, GOCRCharBox> charBoxByBBox;
private Multimap<String, Int2> bboxSizeByText;

private GOCRPageElement gocrElement;
private SVGElement svgElement;


	public GOCRConverter() {
		setDefaults();
	}
	
	/** converts Image to HOCR.
     * relies on Tesseract.
     * 
     * Note - creates a *.hocr.html file from output root.
     * 
     * @param inputImageFile
     * @return HOCR.HTML file created (null if failed to create)
     * @throws IOException // if Tesseract not present
     * @throws InterruptedException ??
     */
    public void convertImageToGOCR(File inputImageFile, File outputGocrFile) throws IOException, InterruptedException {

    	inputFilename = inputImageFile.getAbsolutePath();
		inputImage = ImageIO.read(inputImageFile);
		
    	this.outputFileRoot = outputGocrFile;
    	outputGocrFile.getParentFile().mkdirs();
		String fmt = PNM;
		File outputFile = new File(inputImageFile.getParentFile(), 
				FilenameUtils.getBaseName(inputFilename) + "." + fmt);
		String fmtFilename = outputFile.getAbsolutePath();
		File fmtFile = new File(fmtFilename);
		/** convert to intermediate format (e.g. PNM) and write file */
		if (!ImageUtil.writeImageQuietly(inputImage, fmtFile, fmt)) {
			throw new RuntimeException("Cannot write format: "+fmt);
		};
		/** now run GOCR */
		runGocr(fmtFilename, outputGocrFile.getAbsolutePath());

    }

	private void runGocr(String inputFilename, String outputFilename) throws InterruptedException {
		List<String> gocrConfig = new ArrayList<>();
		gocrConfig.add(getProgram());
		
		gocrConfig.add("-o");
		gocrConfig.add(outputFilename);
		gocrConfig.add("-f");
		gocrConfig.add("XML");
		gocrConfig.add("-C");
//		gocrConfig.add("0-9A-Za-z--[]().,%'="); // don't think this works
		gocrConfig.add("0123456789"); // don't think this works
		gocrConfig.add("-m");
		gocrConfig.add(String.valueOf(
				DONT_DIVIDE_OVERLAPPING
				+DONT_CONTEXT_CORRECT
				));
		
		gocrConfig.add("-i");
		gocrConfig.add(inputFilename);
		
		builder = new ProcessBuilder(gocrConfig);
        runBuilderAndCleanUp();
	}

    protected String getPngtopnmPath() {
    	return PNGTOPNM ;
    }

    protected String getProgram() {
    	return gocrPath ;
    }

	public String getGocrPath() {
		return gocrPath;
	}

	public void setGocrPath(String gocrPath) {
		this.gocrPath = gocrPath;
	}

	/** converts infile (maybe PNG) to gocrXML.
	 * hass to write the outpust to disk at present
	 * then read it in again
	 * 
	 * @param infile
	 * @param gocrXmlFile
	 * @return
	 * @throws IOException
	 * @throws InterruptedException
	 */
	public GOCRPageElement createGOCRElement(File infile, File gocrXmlFile) throws IOException, InterruptedException {
		convertImageToGOCR(infile, gocrXmlFile);
		Element element = XMLUtil.parseQuietlyToRootElement(gocrXmlFile);
		gocrElement = new GOCRPageElement();
		gocrElement.setTextOffset(5); // not working
		gocrElement.createGOCRDescendants(element);
		return gocrElement;
	}

	public List<GOCRCharBox> createGOCRCharBoxes(SVGElement svgElement) throws IOException {
		List<SVGRect> rectList = SVGRect.extractSelfAndDescendantRects(svgElement);
		gocrCharBoxList = new ArrayList<GOCRCharBox>();
		for (SVGRect rect : rectList) {
			if (SVGText.TAG.contentEquals(SVGRect.getClassAttributeValue(rect))) {
				Int2Range boundingBox = rect.createIntBoundingBox();
				SVGImage svgImage = SVGImage.createSVGSubImage(inputImage, boundingBox);
				svgImage.setOpacity(0.2);
				
				SVGG parentG = (SVGG) rect.getParent();
				parentG.appendChild(svgImage);
				
				GOCRCharBox charBox = GOCRCharBox.createFrom(parentG);
				gocrCharBoxList.add(charBox);
			}
		}
		return gocrCharBoxList;
	}
	
	public static void addGlyphsToRectsInG(SVGElement svgElement, BufferedImage inputImage) throws IOException {
		List<SVGRect> rectList = SVGRect.extractSelfAndDescendantRects(svgElement);
		for (SVGRect rect : rectList) {
			if (SVGText.TAG.contentEquals(SVGRect.getClassAttributeValue(rect))) {
				Int2Range boundingBox = rect.createIntBoundingBox();
				SVGImage svgImage = SVGImage.createSVGSubImage(inputImage, boundingBox);
				
				SVGG parentG = (SVGG) rect.getParent();
				parentG.appendChild(svgImage);
			}
		}
	}
	
	public void createMaps(SVGElement svgElement) throws IOException {
		createGOCRCharBoxes(svgElement);
		bboxSizeByText = ArrayListMultimap.create();
		charBoxByText = ArrayListMultimap.create();
		charBoxByBBox = new HashMap<>();
		for (GOCRCharBox gocrCharBox : gocrCharBoxList) {
			charBoxByText.put(gocrCharBox.getSvgText().getText(), gocrCharBox);
			bboxSizeByText.put(gocrCharBox.getSvgText().getText(), gocrCharBox.getBoundingBoxSize());
			charBoxByBBox.put(gocrCharBox.getBoundingBox(), gocrCharBox);
		}
		   
	}
	
	public void analyzeCharBoxes() {
		for (String text : charBoxByText.keys()) {
			List<GOCRCharBox> charBoxList = new ArrayList<GOCRCharBox>(charBoxByText.get(text));
			LOG.debug(text + ":" +charBoxList);
		}
		
	}
	
	public Multimap<String, Int2> getBboxSizeByText() {
		return bboxSizeByText;
	}

	public Multimap<String, GOCRCharBox> getCharBoxByText() {
		return charBoxByText;
	}

	public Map<Int2Range, GOCRCharBox> getCharBoxByBBox() {
		return charBoxByBBox;
	}

//	public void addSubImageGlyphs() {
//		
//	}

	public void correlateImagesForGlyphs() {
		List<String> charList = new ArrayList<String>(charBoxByText.keySet());
		Collections.sort(charList);
		for (String s : charList) {
			List<GOCRCharBox> charBoxList = new ArrayList<GOCRCharBox>(charBoxByText.get(s));
			List<SVGImage> glyphList = getGlyphList(charBoxList);
			RealSquareMatrix correlation = getMatrix(glyphList);
			System.out.println(" "+s+"\n"+correlation);
			SVGG g = createCorrelationMap(s, glyphList, correlation);
			char ch = s.charAt(0);
			SVGSVG.wrapAndWriteAsSVG(g, new File("target/gocr/correlation_"+ch+"_"+(int)ch+".svg"));
		}
	}

	private SVGG createCorrelationMap(String s, List<SVGImage> glyphList, RealSquareMatrix correlation) {
		SVGG g = new SVGG();
		int dx = 20;
		int dy = 20;
		int x = dx;
		int y = dy;
		for (int irow = 0; irow < glyphList.size(); irow++) {
			SVGImage imagei = glyphList.get(irow);
			SVGImage imageii = (SVGImage) imagei.copy();
			imageii.setX(0);
			imageii.setY(y);
			g.appendChild(imageii);
			x = dx;
			for (int jcol = 0; jcol < glyphList.size(); jcol++) {
				SVGImage imagej = glyphList.get(jcol);
				if (irow == 0) {
					g.appendChild(createSVGImage(x, 0, imagej));
				}
				Real2 deltaCofG = new Real2(ImageUtil.deltaCofG(imagei.getBufferedImage(), imagej.getBufferedImage()));
				double cc = correlation.elementAt(new Int2(irow, jcol));
				Real2 xy = new Real2((double)x, (double)y);
				SVGText text = new SVGText(xy, String.valueOf(cc));
				text.setFontSize(7.);
				text.setFill("blue");
				text.setFontFamily("monospace");
				text.setOpacity(0.5);
				g.appendChild(text);
				if (cc < 0.5) {
					// overlay images
					double opacity = 0.5;
					g.appendChild(createImage(imagei, xy, opacity));
					g.appendChild(createImage(imagej, opacity, xy.plus(deltaCofG)));
				}
				x += dx;
			}
			y += dy;
		}
		return g;
	}

	private SVGImage createImage(SVGImage imagei, Real2 xy, double opacity) {
		SVGImage imageix = (SVGImage) imagei.copy();
		imageix.setOpacity(opacity);
		imageix.setXY(xy);
		return imageix;
	}

	private SVGImage createImage(SVGImage imagej, double opacity, Real2 plus) {
		SVGImage imagejx = (SVGImage) imagej.copy();
		imagejx.setOpacity(opacity);
		imagejx.setXY(plus);
		return imagejx;
	}

	private SVGImage createSVGImage(int x,int y, SVGImage imagej) {
		SVGImage image = (SVGImage) imagej.copy();
		image.setX(x);
		image.setY(y);
		return image;
	}

	private RealSquareMatrix getMatrix(List<SVGImage> glyphList) {
		RealSquareMatrix rsm = new RealSquareMatrix(glyphList.size());
		for (int i = 0; i < glyphList.size(); i++) {
			SVGImage svgImage = glyphList.get(i);
			if(svgImage != null) {
				BufferedImage imagei = svgImage.getBufferedImage();
				for (int j = 0; j < glyphList.size(); j++) {
					BufferedImage imagej = glyphList.get(j).getBufferedImage();
					double cc = ImageUtil.correlateBlack(imagei, imagej);
					rsm.setElementAt(i, j, Util.format(cc, 2));
				}
			} else {
				throw new RuntimeException("null glyph");
			}
		}
		return rsm;
	}

	private List<SVGImage> getGlyphList(List<GOCRCharBox> charBoxList) {
		List<SVGImage> imageList = new ArrayList<SVGImage>();
		for (GOCRCharBox charBox : charBoxList) {
			imageList.add(charBox.getSvgImage());
		}
		return imageList;
	}

	public SVGElement createSVGElementWithGlyphs(File imageDir) throws IOException {
		svgElement = null;
		if (gocrElement != null) {
			svgElement = gocrElement.createSVGElement();
			addGlyphsToRectsInG(svgElement, inputImage);
			createIndividualGlyphFiles(inputImage, svgElement, new File(imageDir, "glyphDir"));
		} else {
			LOG.warn("Null gocr");
		}
		return svgElement;
	}
	
	
	public static List<File> createIndividualGlyphFiles(BufferedImage inputImage, SVGElement svgElement, File glyphDir) throws IOException {
		List<SVGRect> rectList = SVGRect.extractSelfAndDescendantRects(svgElement);
		List<File> glyphFileList = new ArrayList<File>();
		for (SVGRect rect : rectList) {
			if (SVGText.TAG.contentEquals(SVGRect.getClassAttributeValue(rect))) {
				Int2Range boundingBox = rect.createIntBoundingBox();
				SVGImage svgImage = SVGImage.createSVGSubImage(inputImage, boundingBox);
				String  filename = boundingBox.toString().replaceAll("(\\(|\\)|\\,)", "_")+".png";
				glyphDir.mkdirs();
				File glyphFile = new File(glyphDir, filename);
				ImageUtil.writeImageQuietly(svgImage.getBufferedImage(), glyphFile);
        				
			}
		}
		return glyphFileList;
	}

}
