# ami-pdf

converts `fulltext.pdf` files in `<cproject>` to `svg/` and `pdfimages/`directories.

## command
```
ami-pdf --project foo 
```

### commandline help
Date: 201901134
```
Usage: ami-pdf [OPTIONS]
Description
===========
Convert PDFs to SVG-Text, SVG-graphics and Images. Does not process images,
graphics or text.often followed by ami-image and ami-xml?
Options
=======
      --basename=<userBasename>
                            User's basename for outputfiles (e.g. foo/bar/<basename>.
                              png. By default this is computed by AMI. This allows
                              users to create their own variants, but they won't be
                              known by default to subsequentapplications
      --dryrun=<dryrun>     for testing runs a single phase without output, deletion
                              or transformation.(NYI).
      --excludetree=<excludeTrees>...
                            exclude the CTrees in the list. (only works with
                              --cproject). Currently must be explicit but we'll add
                              globbing later.
      --forcemake           force 'make' regardless of file existence and dates.
      --imagedir[=IMAGE_DIR]
                            Directory for Image files created from PDF. Do not
                              use/change this unless you are testing or developing
                              AMI as other components rely on this.
                              Default: pdfimages/
      --includetree=<includeTrees>...
                            include only the CTrees in the list. (only works with
                              --cproject). Currently must be explicit but we'll add
                              globbing later.
      --log4j=<log4j> <log4j>
                            format: <classname> <level>; sets logging level of
                              class, e.g.
                             org.contentmine.ami.lookups.WikipediaDictionary INFO
      --logfile=<logfile>   log file for each tree/file/image analyzed.
      --maxpages[=<maxpages>]
                            maximum PDF pages. If less than actual pages, will
                              repeat untill all pages processed. (The normal reason
                              is that lists get full (pseudo-memory leak, this is a
                              bug). If you encounter out of memory errors, try
                              setting this lower.
                              Default: 25
      --pages=<pages>...    pages to extract
      --pdfimages[=<outputPdfImages>]
                            output PDFImages pages.
      --rawfiletypes=<rawFileFormats>[,<rawFileFormats>...]...
                            suffixes of included files (html, pdf, xml): can be
                              concatenated with commas
      --svgdir[=<svgDirectoryName>]
                            Directory for SVG files created from PDF. Do not
                              use/change this unless you are testing or developing
                              AMI as other components rely on this.
                              Default: svg/
      --svgpages[=<outputSVG>]
                            output SVG pages.
  -h, --help                Show this help message and exit.
  -p, --cproject[=CProject] CProject (directory) to process
  -t, --ctree[=CTree]       single CTree (directory) to process
  -v, --verbose             Specify multiple -v options to increase verbosity.
                            For example, `-v -v -v` or `-vvv`We map ERROR or WARN ->
                              0 (i.e. always print), INFO -> 1(-v), DEBUG->2 (-vv)
                              Default: []
  -V, --version             Print version information and exit.

Generic values (AMIPDFTool)
================================
basename            null
cproject            
ctree               
cTreeList           null
dryrun              false
excludeTrees        null
file types          []
forceMake           false
includeTrees        null
log4j               
logfile             null
verbose             0

Specific values (AMIPDFTool)
================================
maxpages            25
svgDirectoryName    svg/
outputSVG           true
imgDirectoryName    pdfimages/
outputPDFImages     true

```
