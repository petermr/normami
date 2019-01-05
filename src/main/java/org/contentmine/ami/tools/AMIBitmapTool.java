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
import org.contentmine.image.diagram.DiagramAnalyzer;

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
description = "		MOVE scaling to bitmap"
		+ "<li>geometric scaling of images using Imgscalr, with interpolation. Increasing scale on small fonts can help OCR, "
		+ "decreasing scale on large pixel maps can help performance."
)

public class AMIBitmapTool extends AbstractAMITool {
	private static final Logger LOG = Logger.getLogger(AMIBitmapTool.class);
	static {
		LOG.setLevel(Level.DEBUG);
	}
	
    @Option(names = {"--binarize"},
    		arity = "1",
    		defaultValue = "true",
            description = "create binary (normally black and white); 'monochrome' is ambiguous")
    private Boolean binarize;

    @Option(names = {"--maxheight"},
    		arity = "1",
    		defaultValue = "1000",
            description = "maximum height (pixels) to accept. If larger, scales the image")
    private Integer maxHeight;

    @Option(names = {"--maxwidth"},
    		arity = "1",
    		defaultValue = "1000",
            description = "maximum width (pixels) to accept. If larger, scales the image")
    private Integer maxWidth;
    
    @Option(names = {"--posterize"},
    		arity = "0",
    		defaultValue = "true",
            description = "create a map of colors including posterization. NYI")
    private boolean posterize = true;

    @Option(names = {"--rotate"},
    		arity = "1",
    		defaultValue = "0",
            description = "rotates image anticlockwise by <value> degrees. Currently 90, 180, 270")
    private Integer rotateAngle;
    
    @Option(names = {"--scalefactor"},
    		arity = "1",
    		description = "geometrical scalefactor. if missing, no scaling (don't use 1.0) Uses Imgscalr library. ")
	private Double scalefactor = null;

    @Option(names = {"--sharpen"},
    		arity = "1",
    		defaultValue = "true",
            description = "sharpen image using Gaussian kernel. Relatively simplistic at present.")
    private Boolean sharpen;

    @Option(names = {"--thinning"},
    		arity = "0..1",
    		defaultValue = "null",
            description = "thinning algorithm. Currently under development. ")
    private String thinning = null;

    @Option(names = {"--threshold"},
    		arity = "1",
    		defaultValue = "180",
            description = "maximum value for black pixels (non-background)")
    private Integer threshold;

	private File derivedImagesDir;

    /** used by some non-picocli calls
     * obsolete it
     * @param cProject
     */
	public AMIBitmapTool(CProject cProject) {
		this.cProject = cProject;
	}
	
	public AMIBitmapTool() {
	}
	
    public static void main(String[] args) throws Exception {
    	new AMIBitmapTool().runCommands(args);
    }

    @Override
	protected void parseSpecifics() {
		System.out.println("binarize            " + binarize);
		System.out.println("maxheight           " + maxHeight);
		System.out.println("maxwidth            " + maxWidth);
		System.out.println("posterize           " + posterize);
		System.out.println("rotate              " + rotateAngle);
		System.out.println("scalefactor         " + sharpen);
		System.out.println("sharpen             " + sharpen);
		System.out.println("threshold           " + threshold);
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
		File pdfImagesDir = cTree.getExistingPDFImagesDir();
		List<File> imageFiles = CMineGlobber.listSortedChildFiles(pdfImagesDir, CTree.PNG);
		Collections.sort(imageFiles);
		for (File imageFile : imageFiles) {
			System.err.print(".");
			if (!imageFile.exists()) {
				LOG.debug("File does not exist: "+imageFile);
			} else {
				runBitmap(imageFile);
			}
		}
	}
	
	private void runBitmap(File imageFile) {
		String inputBasename = FilenameUtils.getBaseName(imageFile.toString());
		String outputBasename = userBasename != null ? userBasename : inputBasename;
		derivedImagesDir = cTree.getOrCreateDerivedImagesDir();
		File inputBaseFile = new File(derivedImagesDir, inputBasename + "." + CTree.PNG);
		File outputBaseFile = new File(derivedImagesDir, outputBasename + "." + CTree.PNG);
		BufferedImage image = null;
		try {
			image = ImageIO.read(inputBaseFile);
		} catch (IOException e) {
			throw new RuntimeException("Cannot read image: "+inputBasename, e);
		}
		Double scale = createScale(image);
		if (false) {
		} else if (scale != null) {
			image = ImageUtil.scaleImage(scale, image);
		} else if (sharpen != null) {
			image = ImageUtil.laplacianSharpen(image);
		} else if (rotateAngle != null) {
			if (rotateAngle % 90 == 0) {
				image = ImageUtil.getRotatedImage(image, rotateAngle);
			}
		} else if (binarize) {
			DiagramAnalyzer diagramAnalyzer = new DiagramAnalyzer();
			if (threshold != null) diagramAnalyzer.setThreshold(threshold);
			diagramAnalyzer.setBinarize(binarize);
			// we do this in AMIPixel
			diagramAnalyzer.setThinning(null);
			diagramAnalyzer.readAndProcessImage(image);
			image = diagramAnalyzer.getBinarizedImage();
		} else if (posterize) {
			LOG.warn("posterize NYI");
		}
		ImageIOUtil.writeImageQuietly(image, outputBaseFile);
	}
	
	private Double createScale(BufferedImage image) {
		Double scale = null;
		if (maxWidth != null || maxHeight != null) {
			int width = image.getWidth();
			int height = image.getHeight();
			if (maxWidth != null && width > maxWidth || maxHeight != null && height > maxHeight) {
				double scalex = ((double) maxWidth / (double) width);
				double scaley = ((double) maxHeight / (double) height);
				scale = Math.max(scalex,  scaley);
			}
		} else if (scalefactor != null) {
			scale = scalefactor;
		}
		return scale;
	}



}
