package org.contentmine.ami.tools;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.io.FilenameUtils;
import org.apache.commons.io.filefilter.RegexFileFilter;
import org.apache.commons.lang3.tuple.Pair;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.contentmine.cproject.files.CProject;
import org.contentmine.cproject.files.CTree;
import org.contentmine.cproject.files.DebugPrint;
import org.contentmine.cproject.util.CMineGlobber;
import org.contentmine.graphics.svg.SVGSVG;
import org.contentmine.graphics.svg.SVGText;
import org.contentmine.graphics.svg.SVGUtil;

import picocli.CommandLine.Command;
import picocli.CommandLine.Option;

/** Translates SVG from PDF into structured text and graphics
 * 
 * @author pm286
 *
 */
@Command(
		//String name() default "<main class>";
name = "ami-svg", 
		//String[] aliases() default {};
aliases = "svg",
		//Class<?>[] subcommands() default {};
version = "ami-svg 0.1",
		//Class<? extends IVersionProvider> versionProvider() default NoVersionProvider.class;
description = "Takes raw SVG from PDF2SVG and converts into structured HTML and higher graphics primitives."
)

public class AMISVGTool extends AbstractAMITool {
	private static final Logger LOG = Logger.getLogger(AMISVGTool.class);
	static {
		LOG.setLevel(Level.DEBUG);
	}
	
    @Option(names = {"--pages"},
    		arity = "1..*",
            description = "pages to extract")
    private List<Integer> pages = null;

    @Option(names = {"--regex"},
    		arity = "1..*",
            description = "regexes to search for in svg pages. format (integerWeight space regex)."
            		+ "If regex starts with uppercase (e.g. Hedge's) forces"
            		+ " case sensitivity , else case-insensitive")
    private List<String> regexList = null;

    @Option(names = {"--regexfile"},
    		arity = "1",
            description = "file to read (weight-regex) pairs from. May contain ${CM_ANCILLARY} variable")
    private String regexFilename = null;

    public static Pattern PAGE_EXTRACT = Pattern.compile(".*\\/fulltext\\-page\\.(\\d+)\\.svg");

	private List<Pattern> patternList;
    
    /** used by some non-picocli calls
     * obsolete it
     * @param cProject
     */
	public AMISVGTool(CProject cProject) {
		this.cProject = cProject;
	}
	
	public AMISVGTool() {
	}
	
    public static void main(String[] args) throws Exception {
    	new AMISVGTool().runCommands(args);
    }

    @Override
	protected void parseSpecifics() {
		System.out.println("pages                " + pages);
		System.out.println("regexes              " + regexList);
		System.out.println("regexfile            " + regexFilename);
		System.out.println();
	}


    @Override
    protected void runSpecifics() {
    	createPatterns();
    	if (processTrees()) { 
    	} else {
			DebugPrint.debugPrint(Level.ERROR, "must give cProject or cTree");
	    }
    }

	private void createPatterns() {
		if (regexFilename != null) {
			createPatternsFromFile();
		} else if (regexList != null) {
			createPatternsFromRegexes();
		}
	}

	private void createPatternsFromFile() {
		File jsonRegexFile = AMIUtil.getFileWithExpandedVariables(regexFilename);
		if (!jsonRegexFile.exists()) {
			throw new RuntimeException("File does not exist "+jsonRegexFile);
		}
	}

	private void createPatternsFromRegexes() {
		patternList = new ArrayList<Pattern>();
		if (regexList != null) {
			int i = 0;
			String ws;
			String regex = null;
			while (i < regexList.size()) {
				ws = regexList.get(i++);
				if (i == regexList.size() || !Character.isDigit(ws.charAt(0))) {
					System.err.println("badly formatted regexList at "+ws+" | "+regex);
					break;
				}
				int weight = Integer.parseInt(ws);
				regex = regexList.get(i++);
				Pattern pattern = (Character.isUpperCase(regex.charAt(0))) ? 
					Pattern.compile(regex) : Pattern.compile(regex, Pattern.CASE_INSENSITIVE);
				WeightedPattern weightedPattern = new WeightedPattern(pattern, weight);
				patternList.add(pattern);
			}
		}
	}

	protected void processTree(CTree cTree) {
		System.out.println("cTree: "+cTree.getName());
		File svgDir = cTree.getExistingSVGDir();
		if (svgDir == null || !svgDir.exists()) {
			LOG.warn("no svg/ dir");
		} else {
			List<File> svgFiles = CMineGlobber.listSortedChildFiles(svgDir, CTree.SVG);
			for (File svgFile : svgFiles) {
				Matcher pageMatcher = PAGE_EXTRACT.matcher(svgFile.toString());
				int page = pageMatcher.matches() ? Integer.parseInt(pageMatcher.group(1)): -1;
				if ((pages == null || pages.size() == 0) || pages.contains(new Integer(page))) {	
					try {
						runSVG(svgFile);
					} catch (IOException e) {
						LOG.error("***, cannot process "+svgFile+" "+e.getMessage());
					}
				}
			}
		}
	}
	
	private void runSVG(File svgFile) throws IOException {
		String basename = FilenameUtils.getBaseName(svgFile.toString());
		if (!svgFile.exists()) {
			System.err.println("!not exist "+basename+"!");
		} else {
			SVGSVG svg = (SVGSVG) SVGUtil.parseToSVGElement(new FileInputStream(svgFile));
			List<SVGText> textList = SVGText.extractSelfAndDescendantTexts(svg);
			LOG.debug("S "+textList.size());
			for (SVGText text : textList) {
				String s = text.getValue();
				matchPatterns(s);
			}
			System.out.println();
		}
	}

	private void matchPatterns(String s) {
		for (Pattern pattern : patternList) {
			Matcher matcher = pattern.matcher(s);
			if (matcher.find()) {
				System.out.println(">>> "+s);
			}
		}
	}

}
class WeightedPattern {
	Pattern pattern;
	Integer weight;
	
	public WeightedPattern(Pattern pattern, Integer weight) {
		this.pattern = pattern;
		this.weight = weight;
	}
}
