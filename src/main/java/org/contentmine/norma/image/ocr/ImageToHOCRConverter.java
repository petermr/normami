package org.contentmine.norma.image.ocr;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.io.FileUtils;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.contentmine.cproject.files.CTree;
import org.contentmine.norma.util.CommandRunner;

public class ImageToHOCRConverter extends CommandRunner {


	public final static Logger LOG = Logger.getLogger(ImageToHOCRConverter.class);
	static {LOG.setLevel(Level.DEBUG);}

	private static final String HOCR = "hocr";
	private static final String USR_LOCAL_BIN_TESSERACT = "/usr/local/bin/tesseract";
	private final static String TESS_CONFIG = "phylo";
	private String tesseractPath = USR_LOCAL_BIN_TESSERACT;
	
	public ImageToHOCRConverter() {
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
		
		builder = new ProcessBuilder(/*getProgram(), inputFilename, outputFilename, */ tessConfig /*, HOCR, encoding */);
//		LOG.debug("builder "+builder.command());
        runBuilderAndCleanUp();
        
    	File htmlFile = convertToHtmlFile(outputHocrFile);
    	return htmlFile;

    }

	private File convertToHtmlFile(File output) throws IOException {
		File outputHtmlFile = createOutputHtmlFileDescriptorForHOCR_HTML(output);
		File outputHocr = createOutputHtmlFileDescriptorForHOCR_HOCR(output);
		if (!outputHocr.exists()) {	
			LOG.trace("failed to create HOCR: "+outputHtmlFile+" or "+outputHocr);
		} else {
			FileUtils.copyFile(outputHocr, outputHtmlFile);
			FileUtils.deleteQuietly(outputHocr);
		}
		return outputHtmlFile;
	}

	private File createOutputHtmlFileDescriptorForHOCR_HTML(File output) {
		String filename = output.getAbsolutePath()+"."+CTree.HTML; // will be renamed later
		return new File(filename);
	}

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

}
