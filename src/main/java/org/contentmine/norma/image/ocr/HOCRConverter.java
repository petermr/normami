package org.contentmine.norma.image.ocr;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.io.FileExistsException;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.contentmine.ami.tools.AMIOCRTool;
import org.contentmine.ami.tools.AMIOCRTool.LineDir;
import org.contentmine.ami.tools.ImageDirProcessor;
import org.contentmine.cproject.files.CTree;
import org.contentmine.eucl.xml.XMLUtil;
import org.contentmine.graphics.svg.SVGElement;
import org.contentmine.graphics.svg.SVGSVG;
import org.contentmine.graphics.svg.text.SVGTextLineList;
import org.contentmine.image.ocr.HOCRReader;
import org.contentmine.norma.util.CommandRunner;

public class HOCRConverter extends CommandRunner {


	public final static Logger LOG = Logger.getLogger(HOCRConverter.class);
	static {LOG.setLevel(Level.DEBUG);}

	private static final String HOCR = "hocr";
	private static final String USR_LOCAL_BIN_TESSERACT = "/usr/local/bin/tesseract";
	private final static String TESS_CONFIG = "phylo";
	private String tesseractPath = USR_LOCAL_BIN_TESSERACT;
	private static final String TEXT_LINE_LIST_SVG = "textLineList.svg";
	
	private File hocrHtmlFile;
	private AMIOCRTool amiocrTool;
	private File outputHtmlFile;
	
	public HOCRConverter() {
		setDefaults();
	}
	
	public HOCRConverter(AMIOCRTool amiocrTool) {
		this();
		this.amiocrTool = amiocrTool;
	}
	
	/** converts Image to HOCR.
     * relies on Tesseract.
     * 
     * Note - creates a *.hocr.html file from output root.
     * 
     * commandline is
     *  tesseract inputimagefile outputdirectory hocr    // the  'hocr' creates a file *.hocr
     * @param inputImageFile
     * @return HOCR.HTML file created (null if failed to create)
     * @throws IOException // if Tesseract not present
     * @throws InterruptedException ??
     */
    public File convertImageToHOCR(File inputImageFile, File outputHocrFile) throws IOException, InterruptedException {

    	this.outputFileRoot = outputHocrFile;
        // tesseract performs the initial Image => HOCR conversion,
    	
    	outputHocrFile.getParentFile().mkdirs();
		String inputFilename = inputImageFile.getAbsolutePath();
		String outputFilename = outputHocrFile.getAbsolutePath();
		// Tesseract arguments are very fragile; I don't know how to vary this
		List<String> tessConfig = new ArrayList<>();
		tessConfig.add(getProgram());
		tessConfig.add(inputFilename);
		tessConfig.add(outputFilename);
//		tessConfig.add(option); // there might be an option but I haven't got them to work
		tessConfig.add(HOCR);
		
		builder = new ProcessBuilder(tessConfig);
		LOG.debug("builder "+builder.command());
        runBuilderAndCleanUp();
        
    	hocrHtmlFile = convertToHtmlFile(outputHocrFile);
    	return hocrHtmlFile;

    }
    
    

	public File getHocrHtmlFile() {
		return hocrHtmlFile;
	}

	private File convertToHtmlFile(File output) throws IOException {
		
		outputHtmlFile = createOutputHtmlFileDescriptorForHOCR_HTML(output);
//		File outputHtmlFile = createOutputHtmlFileDescriptorForHOCR_HTML(output);
		File outputHocr = createOutputHtmlFileDescriptorForHOCR_HOCR(output);
		if (!outputHocr.exists()) {	
			LOG.trace("failed to create HOCR: "+outputHtmlFile+" or "+outputHocr);
		} else {
			LOG.debug("copying "+outputHocr+" to "+outputHtmlFile);
			FileUtils.copyFile(outputHocr, outputHtmlFile);
			FileUtils.deleteQuietly(outputHocr);
		}
		return outputHtmlFile;
	}

	private File createOutputHtmlFileDescriptorForHOCR_HTML(File output) {
		return new File(new File(output.getParentFile(), HOCR), HOCR+"."+CTree.HTML);
	}

//	private File createOutputHtmlFileDescriptorForHOCR_HTML(File output) {
//		String filename = output.getAbsolutePath()+"."+CTree.HTML; // will be renamed later
//		return new File(filename);
//	}

	private File createOutputHtmlFileDescriptorForHOCR_HOCR(File output) {
		String filename = output.getAbsolutePath()+".hocr";
		return new File(filename);
	}
	
	public File writeHOCRFile(File imageFile, File outputBase) {
		File hocrHtmlFile = null;
		try {
			LOG.info("running Tesseract on: " + imageFile+" to "+outputBase);
			hocrHtmlFile = convertImageToHOCR(imageFile, outputBase);
		} catch (IOException ioe) {
			throw new RuntimeException("Tesseract threw IOException", ioe);
		} catch (InterruptedException e) {
			throw new RuntimeException("Tesseract threw InterruptedException", e);
		}
		return hocrHtmlFile;
	}

    protected String getProgram() {
//    	return USR_LOCAL_BIN_TESSERACT;
    	return tesseractPath ;
    }

	public String getTesseractPath() {
		return tesseractPath;
	}

	public void setTesseractPath(String tesseractPath) {
		this.tesseractPath = tesseractPath;
	}

	public void processTesseractOutput(File imageDir) {
		boolean ok = true;
		try {
			createStructuredHtml();
		} catch (Exception e) {
//			e.printStackTrace();
			LOG.debug("Cannot read HOCR input: "+ e);
			ok = false;
		}
		if (ok && LineDir.horiz.equals(amiocrTool.extractLines)) {
			try {
				File hocrRawDir = HOCRConverter.getHocrRawFilename(imageDir);
				File rawSvgFile = new File(hocrRawDir, "raw.svg");
				SVGTextLineList textLineList = amiocrTool.createTextLineList(rawSvgFile);
				textLineList.getOrCreateTypeAnnotations();
				SVGSVG.wrapAndWriteAsSVG((SVGElement)textLineList.createSVGElement(),
						HOCRConverter.getTextLineListFilename(imageDir));
			} catch (FileNotFoundException e) {
				throw new RuntimeException("Cannot find file in ImageDir "+imageDir, e);
			}
		}
	}

	public void createStructuredHtml() {
		HOCRReader hocrReader = new HOCRReader();
		if (amiocrTool == null) {
			throw new RuntimeException("Need to call constructorwith amiocrTool");
		}
		if (amiocrTool.outputHOCRFile == null || !amiocrTool.outputHOCRFile.exists()) {
			throw new RuntimeException("Cannot find outputHOCRFile: "+amiocrTool.outputHOCRFile);
		}
		String filename = amiocrTool.outputHOCRFile.toString();
		LOG.debug("hocr output: "+filename);
		try {
			InputStream inputStream = new FileInputStream(filename);
			// analyze the HOCR
			hocrReader.readHOCR(inputStream);
		} catch (IOException e) {
			throw new RuntimeException("cannot read "+filename, e);
		}
		String basename = FilenameUtils.getBaseName(filename);
		//remove unnecessary "image."
		if (basename.startsWith(AMIOCRTool.IMAGE_DOT)) basename = basename.substring(AMIOCRTool.IMAGE_DOT.length());
		SVGSVG svgSvg = (SVGSVG) hocrReader.getOrCreateSVG();
		File parentFile = amiocrTool.outputHOCRFile.getParentFile();
		File svgFile = new File(parentFile, basename+"."+CTree.SVG);
		SVGSVG.wrapAndWriteAsSVG(svgSvg, svgFile);
		amiocrTool.htmlBody = hocrReader.getOrCreateHtmlBody();
		// debug
		if (false) {
		try {
			
			if (amiocrTool.outputHOCRFile.exists()) {
				File destFile = new File(parentFile, "hocr.html");
				if (destFile.exists()) FileUtils.deleteQuietly(destFile);
				FileUtils.moveFile(amiocrTool.outputHOCRFile, destFile);
	//				LOG.debug("raw html "+destFile);
			} else {
				System.err.println("html file does not exist "+amiocrTool.outputHOCRFile);
			}
		} catch (FileExistsException e) {
			LOG.warn("dest file already exists: "+e.getMessage());
		} catch (IOException e) {
			e.printStackTrace();
			throw new RuntimeException("Cannot move file ", e);
		}
		}
		try {
			if (false) {
				XMLUtil.debug(amiocrTool.htmlBody, new FileOutputStream(new File(parentFile, basename+".body.html")),1);
			}
	//			LOG.debug("raw html 1"+outputHOCRFile);
		} catch (IOException e) {
			throw new RuntimeException("cannot write file: "+amiocrTool.outputHOCRFile, e);
		}
	}

	public static File getTextLineListFilename(File imageDir) {
		File hocrRawDir = getHocrRawFilename(imageDir);
		return new File(hocrRawDir, TEXT_LINE_LIST_SVG);
	}

	private static File getHocrDirFilename(File imageDir) {
		File hocrDir = new File(imageDir, AMIOCRTool.OcrType.hocr.toString());
		return hocrDir;
	}

	public static File getHocrRawFilename(File imageDir) {
		File hocrDir = getHocrDirFilename(imageDir);
		File hocrRawDir = new File(hocrDir, AMIOCRTool.RAW);
		return hocrRawDir;
	}

}
