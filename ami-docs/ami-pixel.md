# ami-pixel
Analyzes bitmap images (after image processing) 

## minimal default command
```
ami-pixel --project foo
```

### commandline help
Date: 201901134

```
ami-pixel
Usage: ami-pixel [OPTIONS]
Description
===========
analyzes bitmaps - generally binary, but may be oligochrome. Creates
pixelIslands
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
      --imagefiles=<imageFilenames>
                            binarized file/s to be processed (I think)
      --includetree=<includeTrees>...
                            include only the CTrees in the list. (only works with
                              --cproject). Currently must be explicit but we'll add
                              globbing later.
      --islands=<maxIslandCount>
                            create pixelIslands and tabulate properties of first
                              $maxIslandCount islands sorted by size.0 means no
                              anaysis.
                              Default: 10
      --log4j=<log4j> <log4j>
                            format: <classname> <level>; sets logging level of
                              class, e.g.
                             org.contentmine.ami.lookups.WikipediaDictionary INFO
      --logfile=<logfile>   log file for each tree/file/image analyzed.
      --maxislands=<maxislands>
                            maximum number of pixelIslands. Only use if the original
                              is 'too spotty' and taking far too long. The output is
                              truncated.
                              Default: 500
      --minheight=<minheight>
                            minimum height range for islands
                              Default: 30
      --minwidth=<minwidth> minimum width for islands
                              Default: 30
      --outputDirectory=<outputDirname>
                            subdirectory for output of pixel analysis and diagrams
                              Default: pixels
      --rawfiletypes=<rawFileFormats>[,<rawFileFormats>...]...
                            suffixes of included files (html, pdf, xml): can be
                              concatenated with commas
      --rings=<minRingCount>
                            create pixelRings and tabulate properties. Islands are
                              only analyzed if they have more than minRingCount.
                              Default (negative) means analyze none. 0 means all
                              islands. Only '--islands' count are analyzed
                              Default: -1
      --thinning=<thinningName>
                            Apply thinning () (none, or absence -> no thinning)
                              Default: none
  -h, --help                Show this help message and exit.
  -p, --cproject[=CProject] CProject (directory) to process
  -t, --ctree[=CTree]       single CTree (directory) to process
  -v, --verbose             Specify multiple -v options to increase verbosity.
                            For example, `-v -v -v` or `-vvv`We map ERROR or WARN ->
                              0 (i.e. always print), INFO -> 1(-v), DEBUG->2 (-vv)
                              Default: []
  -V, --version             Print version information and exit.

Generic values (AMIPixelTool)
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

Specific values (AMIPixelTool)
================================
maxislands           500
imagefiles           null
minwidth             30
minheight            30
thinning             none
thinning             none
maxIslandCount       10
minRingCount         -1
outputDirname        pixels/
```
