package org.contentmine.norma;

import java.io.File;

import org.contentmine.CHESConstants;

public class NAConstants {

	public final static String NORMA   = "norma";
	public final static String AMI     = "ami";
	
	public static final String ARGS_XML = "args.xml";
	public static final String HTML_TAGGER_XML = "htmlTagger.xml";

	// === norma ===
	public final static String ORG_CM_NORMA = CHESConstants.ORG_CM + "/" + NORMA;
	
	public static final String NORMA_RESOURCE = "/" + ORG_CM_NORMA;


	private static final String NORMAMI = "normami";

	public final static String IMAGES   = "images";
	public static final String PUBSTYLE = "pubstyle";
	
	public final static String IMAGES_RESOURCE   = NORMA_RESOURCE + "/" + IMAGES;
	public final static String OCR_RESOURCE = IMAGES_RESOURCE + "/" + "ocr/";
	public static final String PUBSTYLE_RESOURCE = NORMA_RESOURCE + "/"+ PUBSTYLE;

	public final static File MAIN_NORMA_DIR = new File(CHESConstants.SRC_MAIN_RESOURCES + "/" + CHESConstants.ORG_CM + "/" + NORMA + "/");
	public final static File TEST_NORMA_DIR = new File(CHESConstants.SRC_TEST_RESOURCES + "/" + CHESConstants.ORG_CM + "/" + NORMA + "/");

	// === ami ===
	
	public final static String ORG_CM_AMI   = CHESConstants.ORG_CM + "/" + AMI;
	public static final String AMI_RESOURCE = "/" + ORG_CM_AMI;

	

} 
