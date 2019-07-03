package org.contentmine.ami.tools;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.apache.commons.io.FilenameUtils;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.contentmine.eucl.euclid.Real;
import org.contentmine.eucl.euclid.RealArray;
import org.contentmine.graphics.svg.SVGElement;
import org.contentmine.graphics.svg.SVGUtil;

/**
 * 
 * @author pm286
 *
 */
public class OcrMerger {
	private static final Logger LOG = Logger.getLogger(OcrMerger.class);
	static {
		LOG.setLevel(Level.DEBUG);
	}
	
	public enum MeanType {
		arithmetic,
		geometric,
	}
	
	private ArrayList<File> fileList;
	private ArrayList<String> fileTypeList;
	private ArrayList<SVGElement> svgList;
	private ArrayList<SVGElement> parsedList;
	
	public OcrMerger() {
		ensureLists();
	}
	
	private void ensureLists() {
		if (this.fileList == null) {
			this.fileList = new ArrayList<>();
		}
		if (this.fileTypeList == null) {
			this.fileTypeList = new ArrayList<>();
		}
		if (this.svgList == null) {
			this.svgList = new ArrayList<>();
		}
		if (this.parsedList == null) {
			this.parsedList = new ArrayList<>();
		}
	}
	public void addFile(File mergeFile) {
		ensureLists();
		String mergeType = FilenameUtils.getBaseName(mergeFile.toString());
		fileList.add(mergeFile);
		fileTypeList.add(mergeType);
	}

	public void merge() {
		ensureLists();
		addSVGFromFileList(0);
		addSVGFromFileList(1);
		return;
		
	}

	private void addSVGFromFileList(int i) {
		File file = fileList.get(i);
//		System.out.println(">>file "+file);
		svgList.add(SVGUtil.parseToSVGElement(file));
		SVGElement svg = svgList.get(i);
		String value = svg.getValue().replaceAll("\\s+", " ").trim();
		value = value.replaceAll(",",  ".");
		if (!value.contains(".")) {
			value = "." + value;
		}
		System.out.println("svg ____________________________________ "+value);
		String[] values = value.split("\\s+");
		List<String> valueList = Arrays.asList(values);
		LOG.debug(valueList);
		LOG.debug("VAL "+values.length);
		RealArray realArray = null;
		try {
			realArray = new RealArray(values);
		} catch (Exception e) {
			System.out.println("bad array : "+e.getMessage());
		}
		
		if (realArray != null) {
			LOG.debug("*****REALARRAY: "+realArray+" ********");
			Double mean = null;
			MeanType type = null;
			
			if (realArray.size() == 1) {
				LOG.debug("***** SINGLE VALUE ****");
			} else if (realArray.size() == 3) {
				if (Real.isEqual(realArray.get(1),0.0,0.01)) {
					mean = 0.0;
					type = MeanType.arithmetic;
				} else if (Real.isEqual(realArray.get(1),0.0,1.01)) {
					mean = 1.0;
					type = MeanType.geometric;
				}
				realArray.deleteElement(1);
			} else if (realArray.size() == 2) {
				mean = Double.NaN;
			}
			if (realArray.size() == 2) {
				double prod = realArray.get(0) * realArray.get(1);
				double sum = realArray.get(0) + realArray.get(1);
				if (Real.isEqual(prod, 1., 0.01)) {
					type = MeanType.geometric;
				} else if (Real.isEqual(sum, 0., 0.01)) {
					type = MeanType.arithmetic;
				} else {
					type = null;
					LOG.debug("cannot find mean: "+realArray);
				}
			}
			if (mean != null) {
				LOG.debug("******** MEAN ****** "+type+": "+realArray);
			} 
		}
		
	}
	
}
