package org.contentmine.norma.image.ocr;

import java.io.File;
import java.io.IOException;

import org.apache.commons.io.FileUtils;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.contentmine.cproject.files.CTree;
import org.contentmine.cproject.util.CMineUtil;

public class ImageToHOCRConverter {


	private final static Logger LOG = Logger.getLogger(ImageToHOCRConverter.class);
	static {LOG.setLevel(Level.DEBUG);}

	private static final String HOCR = "hocr";
	private static final String USR_LOCAL_BIN_TESSERACT = "/usr/local/bin/tesseract";
	private final static String TESS_CONFIG = "phylo";
//	private static final String ENCODING = "-Dfile.encoding=UTF8";
	private static final String ENCODING = "UTF-8";
	private static final int SLEEP_TIME = 1500;
	private static final int NTRIES = 20;
	public static final String RAW_HTML = "raw.html";
	
	private int tryCount;
	private int sleepTimeMsec;
	private String encoding = ENCODING;
	private File outputFileRoot;
	
	public ImageToHOCRConverter() {
		setDefaults();
	}
	
    private void setDefaults() {
    	tryCount = NTRIES;
    	encoding = "";
    	sleepTimeMsec = SLEEP_TIME;
	}

	public int getTryCount() {
		return tryCount;
	}

	public void setTryCount(int tryCount) {
		this.tryCount = tryCount;
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
		String tessConfig = "";
		ProcessBuilder tesseractBuilder = new ProcessBuilder(
		USR_LOCAL_BIN_TESSERACT, inputImageFile.getAbsolutePath(), outputHocrFile.getAbsolutePath(), tessConfig, HOCR, encoding );
        tesseractBuilder.redirectError(ProcessBuilder.Redirect.INHERIT);
    	Process tesseractProc = startTesseractAndCloseOutputStream(tesseractBuilder);
        int exitValue = exitAfterTrying(tesseractProc);

		if (exitValue != 0) {
			tesseractProc.destroy();
			LOG.error("Process failed to terminate after :"+tryCount);
		}
		LOG.trace("creating "+outputHocrFile);
    	File htmlFile = convertToHtmlFile(outputHocrFile);
    	return htmlFile;

    }

	private int exitAfterTrying(Process tesseractProc) throws InterruptedException {
		int exitValue = -1;
        int itry = 1;
        for (; itry <= tryCount; itry++) {
			Thread.sleep(sleepTimeMsec);
		    try {
		    	exitValue = tesseractProc.exitValue();
		    	if (exitValue == 0) {
		    		LOG.trace("tesseract terminated OK");
		    		break;
		    	}
			} catch (IllegalThreadStateException e) {
//				LOG.debug("still not terminated after: " + itry * sleepTimeMsec + " msec; keep going");
				System.err.print(">"+itry * sleepTimeMsec + " ms ");
			}
		}
		LOG.trace("tries: "+itry);
		return exitValue;
	}

	private Process startTesseractAndCloseOutputStream(ProcessBuilder tesseractBuilder) {
		Process tesseractProc = null;
        try {
        	tesseractProc = tesseractBuilder.start();
            tesseractProc.getOutputStream().close();
        } catch (IOException e) {
        	CMineUtil.catchUninstalledProgram(e, USR_LOCAL_BIN_TESSERACT);
        }
		return tesseractProc;
	}

	private File convertToHtmlFile(File output) throws IOException {
		File outputHtmlFile = createOutputHtmlFileDescriptorForHOCR_HTML(output);
    	LOG.trace("HTML creating output "+outputHtmlFile);
		File outputHocr = createOutputHtmlFileDescriptorForHOCR_HOCR(output);
		if (!outputHocr.exists()) {	
			LOG.trace("failed to create HOCR: "+outputHtmlFile+" or "+outputHocr);
		} else {
			LOG.trace("copying "+outputHocr+" to "+outputHtmlFile);
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
		LOG.trace("wrote to "+hocrHtmlFile);
		return hocrHtmlFile;
	}



	public int getSleepTimeMsec() {
		return sleepTimeMsec;
	}

	public void setSleepTimeMsec(int sleepTimeMsec) {
		this.sleepTimeMsec = sleepTimeMsec;
	}

	public String getEncoding() {
		return encoding;
	}

	public void setEncoding(String encoding) {
		this.encoding = encoding;
	}

	public File getOutputFileRoot() {
		return outputFileRoot;
	}

	public void setOutputFileRoot(File outputFileRoot) {
		this.outputFileRoot = outputFileRoot;
	}


//    public class ProcMon implements Runnable {
//
//    	  private final Process _proc;
//    	  private volatile boolean _complete;
//
//    	  public boolean isComplete() { return _complete; }
//
//    	  public void run() {
//    	    _proc.waitFor();
//    	    _complete = true;
//    	  }
//
//    	  public static ProcMon create(Process proc) {
//    	    ProcMon procMon = new ProcMon(proc);
//    	    Thread t = new Thread(procMon);
//    	    t.start();
//    	    return procMon;
//    	  }
//    	}
}
