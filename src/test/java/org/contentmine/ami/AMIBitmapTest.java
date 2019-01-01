package org.contentmine.ami;

import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.junit.Test;

/** test cleaning.
 * 
 * @author pm286
 *
 */
public class AMIBitmapTest {
	private static final Logger LOG = Logger.getLogger(AMIBitmapTest.class);
	static {
		LOG.setLevel(Level.DEBUG);
	}

	@Test
	/** 
		BufferedImage image = UtilImageIO.loadImage(imageFile.toString());
		Assert.assertNotNull("image read", image);
		ImageProcessor imageProcessor = ImageProcessor.createDefaultProcessor();
		imageProcessor.setThinning(null);
		imageProcessor.setThreshold(180);
		imageProcessor.setBinarize(true);
		imageProcessor.processImage(image);
		BufferedImage image1 = imageProcessor.getBinarizedImage();
		ImageIOUtil.writeImageQuietly(image1, new File(targetDir, fileRoot + "/rawgray.png"));
	 */
	public void testBitmapForestPlotsSmall() throws Exception {
		String[] args = {
				"-p", "/Users/pm286/workspace/uclforest/forestplotssmall",
				"--threshold", "180",
				"--thinning", "none",
				"--binarize", "true",
				};
		AMIBitmap amiBitmap = new AMIBitmap();
		amiBitmap.runCommands(args);
	}
	



}
