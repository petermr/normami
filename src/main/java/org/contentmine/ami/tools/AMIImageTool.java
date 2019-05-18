package org.contentmine.ami.tools;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.Arrays;
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
import org.contentmine.eucl.euclid.Real;
import org.contentmine.eucl.euclid.util.CMFileUtil;
import org.contentmine.eucl.xml.XMLUtil;
import org.contentmine.graphics.svg.util.ImageIOUtil;
import org.contentmine.image.ImageUtil;
import org.contentmine.image.ImageUtil.SharpenMethod;
import org.contentmine.image.ImageUtil.ThresholdMethod;

import com.google.common.collect.HashMultiset;
import com.google.common.collect.Multiset;

import nu.xom.Element;
import picocli.CommandLine.Command;
import picocli.CommandLine.Option;

/** analyses bitmaps
 * 
 * @author pm286
 *
 */



@Command(
		//String name() default "<main class>";
name = "ami-image", 
		//String[] aliases() default {};
aliases = "image",
		//Class<?>[] subcommands() default {};
version = "ami-image 0.1",
		//Class<? extends IVersionProvider> versionProvider() default NoVersionProvider.class;
description = "	first FILTERs images (initally from PDFimages), but does not transform the contents."
		+ " Services include %n"
		+ ""
		+ "%n identification of duplicate images, and removal<.li>"
		+ "%n rejection of images less than gven size</li>"
		+ "%n rejection of monochrome images (e.g. all white or all black) (NB black and white is 'binary/ized'"
		+ ""

		+ "Then TRANSFORMS contents"
		+ " geometric scaling of images using Imgscalr, with interpolation. Increasing scale on small fonts can help OCR, "
		+ "decreasing scale on large pixel maps can help performance."
		+ ""
		+ "NOTE: a missing option means it is not applied (value null). Generally no defaults"
		
)

public class AMIImageTool extends AbstractAMITool {
	private static final String IMAGE = "image";

	private static final Logger LOG = Logger.getLogger(AMIImageTool.class);
	static {
		LOG.setLevel(Level.DEBUG);
	}

	public enum ImageToolkit {
		Boofcv,
		Scalr,
		Pmr,
	}
	
	interface AbstractDest {}
	
	
	public enum DuplicateDest implements AbstractDest {
		_delete,
		duplicate,
		;
	}
	
	public enum MonochromeDest implements AbstractDest {
		_delete,
		monochrome,
		;
	}
	
	public enum SmallDest implements AbstractDest {
		_delete,
		small,
		;
	}
	
	public enum AMIImageType {
		NONE("none", 0, new AMIImageType[]{}),
		RAW("raw", NONE.priority + 1, new AMIImageType[]{}),
		BORDER("border", RAW.priority + 1, AMIImageType.RAW),
		SCALE("scale", RAW.priority + 1, AMIImageType.BORDER, AMIImageType.RAW),
		ROTATE("rotate", SCALE.priority + 1, AMIImageType.SCALE, AMIImageType.RAW),
		SHARPEN("sharpen", ROTATE.priority + 1, AMIImageType.ROTATE, AMIImageType.SCALE, AMIImageType.RAW),
		POSTERIZE("posterize", SHARPEN.priority + 1, AMIImageType.SHARPEN, AMIImageType.ROTATE, AMIImageType.SCALE, AMIImageType.RAW),
		BINARIZE("binarize", POSTERIZE.priority + 1, AMIImageType.SHARPEN,AMIImageType.ROTATE, AMIImageType.SCALE, AMIImageType.RAW),
		ERODE_DILATE("erodeDilate", BINARIZE.priority + 1, AMIImageType.BINARIZE, AMIImageType.SHARPEN, AMIImageType.ROTATE, AMIImageType.SCALE, AMIImageType.RAW),
		;
		private AMIImageType[] imageTypes;
		private String name;
		private int priority;

		/** imageTypes are ordered list of files to be processed in decreasingly level of processing.
		 * They may be of the form rotate_180,.
		 * the tool searches back till it finds the first existing lower level
		 * thus binarize would act on sharpen_1.png rather than raw.png
		 * @param name
		 * @param imageTypes
		 */
		private AMIImageType(String name, int priority, AMIImageType ...imageTypes) {
			this.name = name;
			this.priority = priority;
			this.imageTypes = imageTypes;
		}
		/** image type is determined by leading string in filename.
		 * 
		 * @param filename
		 * @return
		 */
		public final static AMIImageType getImageType(String filename) {
			for (AMIImageType imageType : values()) {
				if (filename != null && filename.startsWith(imageType.name)) {
					LOG.debug("type "+imageType);
					return imageType;
				}
			}
			return (AMIImageType) null;
		}
		
		/**
		 * find File in files that has the highest priority.
		 * iterates over all files to find the one with highest AMIImageType priority,
		 * if priorityLimitType is set, excludes files with priorities above this value.
		 * if priorityLimitType is set to RAW, forces the use of RAW files.
		 * 
		 * @param files
		 * @param priorityLimitType
		 * @return
		 */
		public static File getHighestLevelFile(List<File> files, AMIImageType priorityLimitType) {
			// crude
			int highestPriority = -1;
			int priorityLimit = priorityLimitType == null ? Integer.MAX_VALUE : priorityLimitType.priority;
			File highestFile = null;
			for (File file : files) {
				AMIImageType imageType = getImageType(FilenameUtils.getBaseName(file.getName()));
				int priority = imageType == null ? -1 : imageType.priority;
				if (priority > highestPriority && priority <= priorityLimit) {
					highestPriority = priority;
					highestFile = file;
				}
			}
			return highestFile;
		}
	}
	
	private static final String _DELETE = "_delete";

    // FILTER OPTIONS

    @Option(names = {"--borders"},
    		arity = "1..2",
//    		defaultValue = "10",
            description = "add borders: 1 == all; 2 : top/bottom, edges, "
            + "4 vals = top, right bottpm, left; ")
	private List<Integer> borders = Arrays.asList(new Integer[] {10}) ;

    @Option(names = {"--duplicate"},
    		arity = "0..1",
    		defaultValue = "duplicate",
            description = "FILTER: move duplicate images to <duplicate>; default = ${DEFAULT-VALUE}; "+_DELETE+" means delete")
	private DuplicateDest duplicateDirname;

    @Option(names = {"--minheight"},
    		arity = "0..1",
    		defaultValue = "100",
            description = "minimum height (pixels) to accept")
    private int minHeight;

    @Option(names = {"--minwidth"},
    		arity = "0..1",
    		defaultValue = "100",
            description = "minimum width (pixels) to accept")
    private int minWidth;
    
    @Option(names = {"--monochrome"},
    		arity = "0..1",
    		defaultValue = "monochrome",
            description = "FILTER: move monochrome images to <monochrome>; default ${DEFAULT-VALUE}; "+_DELETE+" means delete"
            )
	private MonochromeDest monochromeDirname;

    @Option(names = {"--small"},
    		arity = "1",
    		defaultValue = "small",
            description = "FILTER: move small images to <monochrome>; default ${DEFAULT-VALUE}; "+_DELETE+" means delete"
            )
	private SmallDest smallDirname;
    
    // TRANSFORM OPTIONS
    
    @Option(names = {"--binarize"},
    		arity = "1",
//    		defaultValue = "LOCAL_MEAN",
            description = "TRANSFORM: create binary (normally black and white); methods local_mean ... (default: ${DEFAULT-VALUE})")
    private ThresholdMethod binarize = null;

    @Option(names = {"--erodedilate"},
    		arity = "0..1",
//    		defaultValue = "false",
            description = "TRANSFORM: erode 1-pixel layer and then dilate. "
            		+ "Removes minor spikes (default: ${DEFAULT-VALUE}); generally destroys fine details")
    private Boolean erodeDilate = false;

    @Option(names = {"--maxheight"},
    		arity = "0..1",
    		defaultValue = "1000",
            description = "maximum height (pixels) to accept. If larger, scales the image (default: ${DEFAULT-VALUE})")
    private Integer maxHeight;

    @Option(names = {"--maxwidth"},
    		arity = "1",
    		defaultValue = "1000",
            description = "maximum width (pixels) to accept. If larger, scales the image (default: ${DEFAULT-VALUE})")
    private Integer maxWidth;
    
    @Option(names = {"--posterize"},
    		arity = "0",
//    		defaultValue = "true",
            description = "create a map of colors including posterization. NYI")
    private boolean posterize = false;

    @Option(names = {"--priority"},
    		arity = "0..1",
    		defaultValue = "RAW",
            description = "force transformations starting with the lowest priority (usually 'raw')")
    private AMIImageType priorityImage = AMIImageType.RAW;

    @Option(names = {"--rotate"},
    		arity = "1",
            description = "rotates image anticlockwise by <value> degrees. Currently 90, 180, 270 (default: ${DEFAULT-VALUE})")
    private Integer rotateAngle = null;
    
    @Option(names = {"--scalefactor"},
    		arity = "1",
    		description = "geometrical scalefactor. if missing, no scaling (don't use 1.0) Uses Imgscalr library. ")
	private Double scalefactor = null;

    @Option(names = {"--sharpen"},
    		arity = "0..1",
    		defaultValue = "sharpen4",
            description = "sharpen image using Laplacian kernel or sharpen4 or sharpen8 (BoofCV)..(default: ${DEFAULT-VALUE})")
    private String sharpen = "sharpen4";

    // will this be per image?
//    @Option(names = {"--split"},
//    		arity = "1..*",
//            description = "split ")
//    private String split = "00";
    @Option(names = {"--template"},
    		arity = "0..1",
//    		defaultValue = "template.xml",
            description = "use template in each image.*/ dir to process image")
    private String template = "template.xml";

    @Option(names = {"--thinning"},
    		arity = "0..1",
    		defaultValue = "null",
            description = "thinning algorithm. Currently under development. (default: ${DEFAULT-VALUE})")
    private String thinning = null;

    @Option(names = {"--threshold"},
    		arity = "1",
    		defaultValue = "180",
            description = "maximum value for black pixels (non-background) (default: ${DEFAULT-VALUE})")
    private Integer threshold;

    @Option(names = {"--toolkit"},
    		arity = "1",
//    		defaultValue = "Boofcv",
            description = "Image toolkit to use., "
            		+ "Scalr (Imgscalr), simple but no longer developed. Pmr (my own) when all else fails.(default: ${DEFAULT-VALUE}) (not yet fully worked out)")
//    private ImageToolkit toolkit = ImageToolkit.Boofcv;
    private ImageToolkit toolkit = null;

	public static final String DUPLICATES = "duplicates/";
	public static final String MONOCHROME = "monochrome/";
	public static final String LARGE = "large/";
	public static final String SMALL = "small/";
	private static final String ROT = "rot";
	private static final String RAW = "raw";

	private static final String BORDER = "border";
	private static final String SCALE = "scale";

	private Multiset<String> duplicateSet;



	private SharpenMethod sharpenMethod;

	private Element templateElement;

    /** used by some non-picocli calls
     * obsolete it
     * @param cProject
     */
	public AMIImageTool(CProject cProject) {
		this.cProject = cProject;
	}
	
	public AMIImageTool() {
	}
	
    public static void main(String[] args) throws Exception {
    	new AMIImageTool().runCommands(args);
    }

    @Override
	protected void parseSpecifics() {
		System.out.println("minHeight           " + minHeight);
		System.out.println("minWidth            " + minWidth);
		System.out.println("smalldir            " + smallDirname);
		System.out.println("monochromeDir       " + monochromeDirname);
		System.out.println("duplicateDir        " + duplicateDirname);

    	
		System.out.println("borders             " + borders);
		System.out.println("binarize            " + binarize);
		System.out.println("erodeDilate         " + erodeDilate);
		System.out.println("maxheight           " + maxHeight);
		System.out.println("maxwidth            " + maxWidth);
		System.out.println("posterize           " + posterize);
		System.out.println("priority            " + priorityImage);
		System.out.println("rotate              " + rotateAngle);
		System.out.println("scalefactor         " + scalefactor);
		System.out.println("sharpen             " + sharpen);
		System.out.println("template            " + template);
		System.out.println("threshold           " + threshold);
		System.out.println();
	}


    @Override
    protected void runSpecifics() {
    	getSharpenMethod();
    	if (processTrees()) { 
    	} else {
			DebugPrint.debugPrint(Level.ERROR, "must give cProject or cTree");
	    }
    }

	private void getSharpenMethod() {
		sharpenMethod = SharpenMethod.getMethod(sharpen);
	}

	protected void processTree() {
		processTreeFilter();
		processTreeTransform();
	}

	protected void processTreeFilter() {
		System.out.println("filterImages cTree: "+cTree.getName());
		File pdfImagesDir = cTree.getExistingPDFImagesDir();
		if (pdfImagesDir == null || !pdfImagesDir.exists()) {
			LOG.warn("no pdfimages/ dir");
		} else {
			duplicateSet = HashMultiset.create();
			List<File> imageFiles = CMineGlobber.listSortedChildFiles(pdfImagesDir, CTree.PNG);
			Collections.sort(imageFiles);
			for (File imageFile : imageFiles) {
				System.err.print(".");
				String basename = FilenameUtils.getBaseName(imageFile.toString());
				BufferedImage image = null;
				try {
					image = ImageIO.read(imageFile);
					// this has to cascade in order; they can be reordered if required
					if (false) {
					} else if (moveSmallImageTo(image, imageFile, smallDirname, pdfImagesDir)) {
						System.out.println("small: "+basename);
					} else if (moveMonochromeImagesTo(image, imageFile, monochromeDirname, pdfImagesDir)) {
						System.out.println("monochrome: "+basename);
					} else if (moveDuplicateImagesTo(image, imageFile, duplicateDirname, pdfImagesDir)) {
						System.out.println("duplicate: "+basename);
					} else {
						// move file to <pdfImagesDir>/<basename>/raw.png
						File imgDir = new File(pdfImagesDir, basename);
						File newImgFile = new File(imgDir, RAW + "." + CTree.PNG);
						try {
							CMFileUtil.forceMove(imageFile, newImgFile);
						} catch (Exception ioe) {
							throw new RuntimeException("cannot rename "+imageFile+" to "+newImgFile, ioe); 
						}
					}
				} catch(IndexOutOfBoundsException e) {
					LOG.error("BUG: failed to read: "+imageFile);
				} catch(IOException e) {
					e.printStackTrace();
					LOG.debug("failed to read file " + imageFile + "; "+ e);
				}
			}
		}
		return;
	}


	protected void processTreeTransform() {
		System.out.println("transformImages cTree: "+cTree.getName());
		File pdfImagesDir = cTree.getExistingPDFImagesDir();
		if (pdfImagesDir == null) {
			throw new RuntimeException("Cannot find pdfImages for cTree "+cTree.getName());
		}
		List<File> imageDirs = CMineGlobber.listSortedChildDirectories(pdfImagesDir);
		Collections.sort(imageDirs);
		for (File imageDir : imageDirs) {
			if (imageDir.getName().startsWith(IMAGE)) {
				System.err.print(".");
				if (!imageDir.exists()) {
					LOG.debug("Dir does not exist: "+imageDir);
				} else {
					if (template != null) {
						readTemplate(imageDir);
					}
					try {
						runTransform(imageDir);
					} catch (Exception e) {
						e.printStackTrace();
						LOG.error("Bad read: "+imageDir+" ("+e.getMessage()+")");
					}
				}
			}
		}
	}
	
	private void readTemplate(File imageDir) {
		File templateFile = new File(imageDir, template);
		if (!templateFile.exists()) {
			LOG.info("no template in: "+imageDir);
		}
		Element tElement = XMLUtil.parseQuietlyToRootElement(templateFile);
		TemplateElement templateElement = TemplateElement.read(tElement);
		
		
	}

	// ================= filter ============
	private boolean moveSmallImageTo(BufferedImage image, File srcImageFile, AbstractDest destDirname, File destDir) throws IOException {
		if (destDirname != null) {
			int width = image.getWidth();
			int height = image.getHeight();
			if (width < minWidth || height < minHeight) {
				copyOrDelete(srcImageFile, destDirname, destDir);
				return true;
			}
		}
		return false;
	}

	private boolean moveMonochromeImagesTo(BufferedImage image, File srcImageFile, AbstractDest destDirname, File destDir) throws IOException {
		if (destDirname != null) {
			Integer singleColor = ImageUtil.getSingleColor(image);
			if (singleColor != null && srcImageFile.exists()) {
				copyOrDelete(srcImageFile, destDirname, destDir);
				return true;
			}
		}
		return false;
	}

	private boolean moveDuplicateImagesTo(BufferedImage image, File srcImageFile, AbstractDest destDirname, File destDir) throws IOException {
		if (destDirname != null) {
			String hash = ""+image.getWidth()+"-"+image.getHeight()+"-"+ImageUtil.createSimpleHash(image);
			duplicateSet.add(hash);
			if (duplicateSet.count(hash) > 1) {
				copyOrDelete(srcImageFile, destDirname, destDir);
				return true;
			}
		}
		return false;
	}

	private void copyOrDelete(File srcImageFile, AbstractDest destDirname, File destDir) throws IOException {
		if (_DELETE.equals(destDirname.toString())) {
			CMFileUtil.forceDelete(srcImageFile);
		} else {
			File fullDestDir = new File(destDir, destDirname.toString());
			fullDestDir.mkdirs();
			CMFileUtil.forceMoveFileToDirectory(srcImageFile, fullDestDir);
		}
	}

	// ================= transform ===============
	
	private void runTransform(File imageDir) {
		List<File> imageFiles = CMineGlobber.listSortedChildFiles(imageDir, CTree.PNG);
		File highestImageFile = AMIImageType.getHighestLevelFile(imageFiles, priorityImage);
		LOG.debug("transforming: "+highestImageFile);
		BufferedImage image = ImageUtil.readImageQuietly(highestImageFile);
		String basename = FilenameUtils.getBaseName(highestImageFile.toString());
		if (image != null) {
			if (rotateAngle != null) {
				image = rotateAndSave(image, imageDir);
			}
			if (scalefactor != null) {
				image = scaleAndSave(image, imageDir);
				basename += "_sc_"+(int)(double)scalefactor;
			}
			if (sharpen != null) {
				image = sharpenAndSave(image, imageDir);
				basename += "_s4";
			}
			if (borders != null) {
				image = bordersAndSave(image, imageDir);
				basename += "_b_"+borders.toString().replaceAll("(\\[|\\])", "");
			}
			if (erodeDilate) {
				image = erodeDilateAndSave(image, imageDir);
				basename += "_e";
			}
			if (binarize != null || threshold != null) {
				image = binarizeAndSave(image, imageDir);
				if (binarize != null) {
					basename += binarize.name();
				}
				if (threshold != null) {
					basename += "_thr_"+threshold.toString();
				}
			}
			if (posterize) {
				image = posterizeAndSave(image, imageDir);
			}
			File outfile = new File(imageDir, basename+"."+CTree.PNG);
			ImageIOUtil.writeImageQuietly(image, outfile);
		}
	}

	private BufferedImage posterizeAndSave(BufferedImage image, File imageDir) {
		if (posterize) {
			LOG.warn("posterize NYI");
		}
		return image;
	}

	private BufferedImage erodeDilateAndSave(BufferedImage image, File imageDir) {
		image = ImageUtil.thresholdBoofcv(image, erodeDilate);
		File outputPng = new File(imageDir, "erodeDilate"+"."+CTree.PNG);
		LOG.debug("wrtiing "+outputPng);
		ImageUtil.writeImageQuietly(image, outputPng);
		return image;
	}

	/** binarize withotu explict threshold
	 * 
	 * @param image
	 * @return
	 */
	private BufferedImage binarizeAndSave(BufferedImage image, File imageDir) {
		int[] oldRGB = {0x000d0d0d};
		int[] newRGB = {0x00ffffff};
		
		// binarization follows sharpen
		String type = null;
		if (binarize != null) {
			image = ImageUtil.boofCVThreshold(image, binarize); // this fails
			image = ImageUtil.thresholdBoofcv(image, erodeDilate);
			image = ImageUtil.removeAlpha(image);
			image = ImageUtil.magnifyToWhite(image);
			LOG.debug("colors0 "+ImageUtil.createHexMultiset(image));
			image = ImageUtil.convertRGB(image, oldRGB, newRGB);
			
			Integer color = ImageUtil.getSingleColor(image);
			LOG.debug("colors "+ImageUtil.createHexMultiset(image));
			if (color != null) {
				throw new RuntimeException("Single color: "+color+" Corrupt conversion?");
			}
			type = binarize.toString().toLowerCase();
			// debug
		} else if (threshold != null) {
			image = ImageUtil.boofCVBinarization(image, threshold);
			type = "threshold"+"_"+threshold;
		}
		if (image != null) {
			ImageUtil.writeImageQuietly(image, new File(imageDir, type+"."+CTree.PNG));
		}
		return image;
	}

	private BufferedImage sharpenAndSave(BufferedImage image, File imageDir) {
		BufferedImage resultImage = null;
		if (ImageToolkit.Boofcv.equals(toolkit)) {
			resultImage = ImageUtil.sharpenBoofcv(image, sharpenMethod);
		} else if (SharpenMethod.LAPLACIAN.toString().equals(sharpen)) {
			resultImage = ImageUtil.laplacianSharpen(image);
		} else if (SharpenMethod.SHARPEN4.toString().equals(sharpen)) {
			resultImage = ImageUtil.sharpen(image, SharpenMethod.SHARPEN4);
		} else if (SharpenMethod.SHARPEN8.toString().equals(sharpen)) {
			resultImage = ImageUtil.sharpen(image, SharpenMethod.SHARPEN8);
		} 
		if (resultImage != null) {
			ImageUtil.writeImageQuietly(resultImage, new File(imageDir, sharpenMethod+"."+CTree.PNG));
		}
		return resultImage;
	}

	private BufferedImage rotateAndSave(BufferedImage image, File imageDir) {
		if (rotateAngle != null && rotateAngle % 90 == 0) {
			// can't find a boofcv rotate
			if (false && ImageToolkit.Boofcv.equals(toolkit)) {
//				image = ImageUtil.getRotatedImage(image, rotateAngle);
			} else if (true || ImageToolkit.Scalr.equals(toolkit)) {
				image = ImageUtil.getRotatedImageScalr(image, rotateAngle);
			}
			ImageUtil.writeImageQuietly(image, new File(imageDir, ROT + "_"+rotateAngle+"."+CTree.PNG));
		}
		return image;
	}

	private BufferedImage scaleAndSave(BufferedImage image, File imageDir) {
		Double scale = scalefactor != null ? scalefactor :
			ImageUtil.getScaleToFitImageToLimits(image, maxWidth, maxHeight);
		if (!Real.isEqual(scale,  1.0,  0.0000001)) {
			if (ImageToolkit.Scalr.equals(toolkit)) {
				image = ImageUtil.scaleImageScalr(scale, image);
			} else if (ImageToolkit.Boofcv.equals(toolkit)) {
				throw new RuntimeException("Boofcv scale NYI");
			} else if (scale > 1.9){
				int intScale = (int) Math.round(scale);
				image = ImageUtil.scaleImage(image, intScale, intScale);
			}
			String scaleValue = String.valueOf(scalefactor).replace(".",  "_");
			ImageUtil.writeImageQuietly(image, new File(imageDir, SCALE + " _ " + scaleValue + "." + CTree.PNG));
		}
		return image;
	}
	
	private BufferedImage bordersAndSave(BufferedImage image, File imageDir) {
		int color = 0x00FFFFFF;
		int xBorder = borders.get(0);
		int yBorder = borders.size() > 1 ? borders.get(1) : xBorder;
		image = ImageUtil.addBorders(image, xBorder, yBorder, color);
		String borderValue = String.valueOf(borders).replaceAll("(\\[|\\])",  "_");
		ImageUtil.writeImageQuietly(image, new File(imageDir, BORDER + " _ " + borderValue + "." + CTree.PNG));
		return image;
	}
	
	// ============== misc ============
	private String truncateToLastDot(String basename) {
		return basename.substring(0, basename.lastIndexOf("."));
	}





}
