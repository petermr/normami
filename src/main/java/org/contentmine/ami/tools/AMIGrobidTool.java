package org.contentmine.ami.tools;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.Collections;
import java.util.List;

import javax.imageio.ImageIO;

import org.apache.commons.io.FilenameUtils;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.contentmine.cproject.files.CProject;
import org.contentmine.cproject.files.CTree;
import org.contentmine.cproject.files.DebugPrint;
import org.contentmine.cproject.util.CMineGlobber;
import org.contentmine.graphics.svg.util.ImageIOUtil;
import org.contentmine.image.ImageUtil;
import org.contentmine.image.ImageUtil.SharpenMethod;
import org.contentmine.image.ImageUtil.ThresholdMethod;
import org.contentmine.image.diagram.DiagramAnalyzer;
import org.contentmine.norma.pdf.GrobidRunner;

import boofcv.io.image.UtilImageIO;
import picocli.CommandLine.Command;
import picocli.CommandLine.Option;

/** analyses bitmaps
 * 
 * @author pm286
 *
 */
@Command(
		//String name() default "<main class>";
name = "ami-bitmap", 
		//String[] aliases() default {};
aliases = "bitmap",
		//Class<?>[] subcommands() default {};
version = "ami-bitmap 0.1",
		//Class<? extends IVersionProvider> versionProvider() default NoVersionProvider.class;
description = "	Run grobid using:"
		+ "java -jar /Users/pm286/workspace/grobid/grobid-0.5.3/grobid-core/build/libs/grobid-core-0.5.3-onejar.jar "
		+ "  -gH /Users/pm286/workspace/grobid/grobid-0.5.3/grobid-home"
		+ "  -teiCoordinates "
		+ "  -exe processFullText"
		+ ""
		+ "This is very slow as grobid has to boot each time but it only has to be done once. "
		+ "We can set up a server if it becomes useful. "
		+ ""
)

public class AMIGrobidTool extends AbstractAMITool {
	private static final Logger LOG = Logger.getLogger(AMIGrobidTool.class);
	static {
		LOG.setLevel(Level.DEBUG);
	}
	
    @Option(names = {"--exe"},
    		arity = "1",
    		defaultValue = "processFullText",
            description = "Grobid option from: 	"
        		+ "close,"
        		+ " processFullText, processHeader, processDate, processAuthorsHeader,"
        		+ " processAuthorsCitation, processAffiliation, processRawReference, processReferences,"
        		+ " createTraining, createTrainingMonograph, createTrainingBlank, createTrainingCitationPatent,"
        		+ " processCitationPatentTEI, processCitationPatentST36, processCitationPatentTXT, processCitationPatentPDF, processPDFAnnotation")
    private String exeOption = null;

	private File pdfImagesDir;

    /** used by some non-picocli calls
     * obsolete it
     * @param cProject
     */
	public AMIGrobidTool(CProject cProject) {
		this.cProject = cProject;
	}
	
	public AMIGrobidTool() {
	}
	
    public static void main(String[] args) throws Exception {
    	new AMIGrobidTool().runCommands(args);
    }

    @Override
	protected void parseSpecifics() {
		System.out.println("exeOption            " + exeOption);
		System.out.println();
	}


    @Override
    protected void runSpecifics() {
    	if (processTrees()) { 
    	} else {
			DebugPrint.debugPrint(Level.ERROR, "must give cProject or cTree");
	    }
    }

	protected void processTree(CTree cTree) {
		this.cTree = cTree;
		System.out.println("cTree: "+cTree.getName());
		try {
			runGrobid();
		} catch (Exception e) {
			LOG.error("Bad read: "+cTree+" ("+e.getMessage()+")");
		}
	}
	
	private void runGrobid() {
		
		File inputDir = cTree.getDirectory();
		File outputDir = new File(cTree.getDirectory(), "tei/");
		outputDir.mkdirs();
		GrobidRunner grobidRunner = new GrobidRunner();
		grobidRunner.convertPDFToTEI(inputDir, outputDir, exeOption);
	}
	
	


}
