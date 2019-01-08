package org.contentmine.ami.tools;

import java.io.File;

import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.contentmine.cproject.files.CProject;
import org.contentmine.eucl.euclid.Int2;
import org.contentmine.pdf2svg2.PDFDocumentProcessor;

import picocli.CommandLine.Command;
import picocli.CommandLine.Option;

	@Command(
			//String name() default "<main class>";
	name = "ami-pdf", 
			//String[] aliases() default {};
	aliases = "pdf",
			//Class<?>[] subcommands() default {};
	version = "ami-pdf 0.1",
			//Class<? extends IVersionProvider> versionProvider() default NoVersionProvider.class;
	description = "Convert PDFs to SVG-Text, SVG-graphics and Images. Does not process images, graphics or text."
			+ "often followed by ami-image and ami-xml?"
	)


public class AMIPDFTool extends AbstractAMITool {
	private static final Logger LOG = Logger.getLogger(AMIPDFTool.class);
	static {
		LOG.setLevel(Level.DEBUG);
	}
	
	public AMIPDFTool() {
	}
	
//    @Parameters(index = "0",
//    		arity="0..*",
//    		split=",",
//    		description = "primary operation: (${COMPLETION-CANDIDATES}); if no operation, runs help"
//    		)
//    private Operation operation = Operation.help;

    public AMIPDFTool(CProject cProject) {
    	this.cProject = cProject;
	}

	@Option(names = {"--maxpages"}, 
    		arity="0..1",
   		    description = "maximum PDF pages. If less than actual pages, will repeat untill all pages processed. "
   		    		+ "(The normal reason is that lists get full (pseudo-memory leak, this is a bug). If you encounter "
   		    		+ "out of memory errors, try setting this lower."
    		)
    private int maxpages = 25;
    
    @Option(names = {"--svgdir"}, 
    		arity="0..1",
   		    description = "Directory for SVG files created from PDF. Do not use/change this unless you are testing "
   		    		+ "or developing AMI as other components rely on this."
    		)
    private String svgDirectoryName = "svg/";
    
    @Option(names = {"--svgpages"}, 
    		arity="0..1",
   		    description = "output SVG pages. "
    		)
    private boolean outputSVG = true;
    
    @Option(names = {"--imagedir"}, 
    		arity="0..1",
    		paramLabel="IMAGE_DIR",
   		    description = "Directory for Image files created from PDF. Do not use/change this unless you are testing "
   		    		+ "or developing AMI as other components rely on this."
    		)
    private String pdfImagesDirname = "pdfimages/";
    
    @Option(names = {"--pdfimages"}, 
    		arity="0..1",
   		    description = "output PDFImages pages. "
    		)
    private boolean outputPdfImages = true;
    
    public static void main(String[] args) throws Exception {
    	AMIPDFTool amiProcessorPDF = new AMIPDFTool();
    	amiProcessorPDF.runCommands(args);
    }

	@Override
	protected void parseSpecifics() {
		printDebug();
	}

	@Override
	protected void runSpecifics() {
        runPDF();
	}

	private void printDebug() {
		System.out.println("maxpages            "+maxpages);
		System.out.println("svgDirectoryName    "+svgDirectoryName);
		System.out.println("outputSVG           "+outputSVG);
		System.out.println("imgDirectoryName    "+pdfImagesDirname);
		System.out.println("outputPDFImages     "+outputPdfImages);
		return;
	}

    public void runPDF() {
    	if (cProject != null) {
    		PDFDocumentProcessor pdfDocumentProcessor = cProject.getOrCreatePDFDocumentProcessor();
			pdfDocumentProcessor.setOutputSVG(outputSVG);
			pdfDocumentProcessor.setOutputPDFImages(outputPdfImages);
			pdfDocumentProcessor.setMaxPages(maxpages);
    		runPDF(cProject.getDirectory());
    	} else if (cTree != null) {
    		PDFDocumentProcessor pdfDocumentProcessor = cTree.getOrCreatePDFDocumentProcessor();
			pdfDocumentProcessor.setOutputSVG(outputSVG);
			pdfDocumentProcessor.setOutputPDFImages(outputPdfImages);
			pdfDocumentProcessor.setMaxPages(maxpages);
	        cTree.setPDFDocumentProcessor(pdfDocumentProcessor);
			cTree.processPDFTree();
    	} else {
    		System.err.println("NO CProject or CTree, no processing");
    	}
    }

//    public static void runPDF(CProject cProject) {
//    	runPDF(cProject == null ? null : cProject.getDirectory());
//    }

	private void runPDF(File projectDir) {
		if (projectDir != null) {
			String cmd = "-p " + projectDir + " --rawfiletypes pdf ";
			try {
				AMIMakeProjectTool.main(cmd);
			} catch (Exception e) {
				LOG.error("makeProject failed " + e.getMessage());
				throw new RuntimeException("cannot makeProject ", e);
			}
			this.convertPDFOutputSVGFilesImageFiles();
		}
	}

	private void convertPDFOutputSVGFilesImageFiles() {
		cProject.setCTreelist(null);
		cProject.convertPDFOutputSVGFilesImageFiles();
	}

}
