package org.contentmine.ami;

import java.awt.image.BufferedImage;
import java.io.File;
import java.util.Collections;
import java.util.List;

import org.apache.commons.io.FilenameUtils;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.contentmine.cproject.files.CProject;
import org.contentmine.cproject.files.CTree;
import org.contentmine.cproject.files.DebugPrint;
import org.contentmine.cproject.util.CMineGlobber;
import org.contentmine.graphics.svg.util.ImageIOUtil;
import org.contentmine.image.ImageProcessor;
import org.contentmine.image.diagram.DiagramAnalyzer;
import org.contentmine.image.processing.HilditchThinning;
import org.contentmine.image.processing.Thinning;
import org.contentmine.image.processing.ZhangSuenThinning;
import org.contentmine.norma.picocli.AbstractAMIProcessor;

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
description = "processes bitmaps - generally binary, but may be oligochrome. "
)

public class AMIBitmap extends AbstractAMIProcessor {
	private static final Logger LOG = Logger.getLogger(AMIBitmap.class);
	static {
		LOG.setLevel(Level.DEBUG);
	}
	
    @Option(names = {"--threshold"},
    		arity = "1",
    		defaultValue = "180",
            description = "maximum value for black pixels (non-background)")
    private int threshold;

    @Option(names = {"--binarize"},
    		arity = "1",
    		defaultValue = "true",
            description = "create binary (normally black and white); 'monochrome' is ambiguous")
    private boolean binarize = true;

	private File derivedImagesDir;

    /** used by some non-picocli calls
     * obsolete it
     * @param cProject
     */
	public AMIBitmap(CProject cProject) {
		this.cProject = cProject;
	}
	
	public AMIBitmap() {
	}
	
    public static void main(String[] args) throws Exception {
    	new AMIBitmap().runCommands(args);
    }

    @Override
	protected void parseSpecifics() {
		System.out.println("threshold           " + threshold);
		System.out.println("binarize            " + binarize);
		System.out.println();
	}


    @Override
    protected void runSpecifics() {
    	if (cProject != null) {
    		for (CTree cTree : cProject.getOrCreateCTreeList()) {
    			runBitmap(cTree);
    		}
    	} else if (cTree != null) {
   			runBitmap(cTree);
   		    		
    	} else {
			DebugPrint.debugPrint(Level.ERROR, "must give cProject or cTree");
	    }
    }

	private void runBitmap(CTree cTree) {
		System.out.println("cTree: "+cTree.getName());
		File pdfImagesDir = cTree.getExistingPDFImagesDir();
		if (pdfImagesDir == null || !pdfImagesDir.exists()) {
			LOG.warn("no pdfimages/ dir");
		} else {
			derivedImagesDir = cTree.getOrCreateDerivedImagesDir();
//			List<File> imageFiles = new CMineGlobber("**/*.png", pdfImagesDir).listFiles();
			List<File> imageFiles = CMineGlobber.listSortedChildFiles(pdfImagesDir, CTree.PNG);
			Collections.sort(imageFiles);
			for (File imageFile : imageFiles) {
				System.err.print(".");
				runBitmap(imageFile);
			}
		}
	}
	
	private void runBitmap(File imageFile) {
		String basename = FilenameUtils.getBaseName(imageFile.toString());
		File binarizedFile = new File(derivedImagesDir, basename + "." + CTree.PNG);
		if (binarizedFile.exists()) {
			System.err.println("!"+basename+"!");
		} else {
			System.err.println("?"+basename+"?");
			BufferedImage image = UtilImageIO.loadImage(imageFile.toString());
			DiagramAnalyzer diagramAnalyzer = new DiagramAnalyzer();
			diagramAnalyzer.setBinarize(binarize);
			// we do this in AMIPixel
			diagramAnalyzer.setThinning(null);
			diagramAnalyzer.readAndProcessImage(image);
			BufferedImage image1 = diagramAnalyzer.getBinarizedImage();
			ImageIOUtil.writeImageQuietly(image1, binarizedFile);
		}
	}

}
