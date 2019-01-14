# ami-svg

Converts raw SVG (from PDFs) into HTML, graphics (vectors) etc.

## minimal default command
```
ami-svg --project foo
```

### commandline help
Date: 201901134

```
ami-svg
Usage: ami-svg [OPTIONS]
Description
===========
Takes raw SVG from PDF2SVG and converts into structured HTML and higher
graphics primitives.
Options
=======
      --basename=<userBasename>
                            User's basename for outputfiles (e.g. foo/bar/<basename>.
                              png. By default this is computed by AMI. This allows
                              users to create their own variants, but they won't be
                              known by default to subsequentapplications
      --caches=<cacheList>...
                            caches to process/create; values: line, page, path,
                              rect, text
      --dryrun=<dryrun>     for testing runs a single phase without output, deletion
                              or transformation.(NYI).
      --excludetree=<excludeTrees>...
                            exclude the CTrees in the list. (only works with
                              --cproject). Currently must be explicit but we'll add
                              globbing later.
      --forcemake           force 'make' regardless of file existence and dates.
      --includetree=<includeTrees>...
                            include only the CTrees in the list. (only works with
                              --cproject). Currently must be explicit but we'll add
                              globbing later.
      --log4j=<log4j> <log4j>
                            format: <classname> <level>; sets logging level of
                              class, e.g.
                             org.contentmine.ami.lookups.WikipediaDictionary INFO
      --logfile=<logfile>   log file for each tree/file/image analyzed.
      --pages=<pageList>... pages to extract
      --rawfiletypes=<rawFileFormats>[,<rawFileFormats>...]...
                            suffixes of included files (html, pdf, xml): can be
                              concatenated with commas
      --regex=<regexList>...
                            regexes to search for in svg pages. format
                              (integerWeight space regex).If regex starts with
                              uppercase (e.g. Hedge's) forces case sensitivity ,
                              else case-insensitive
      --regexfile=<regexFilename>
                            file to read (weight-regex) pairs from. May contain
                              ${CM_ANCILLARY} variable
      --tidysvg=<tidyList>...
                            tidy SVG (Valid values: emptypath, nomove, nullmove)
      --vectordir=<vectorDirname>
                            output pages with SVG vectors to <directory>
                              Default: vectors/
      --vectorlog=<vectorLog>
                            file to contain statistics on vectors (probably diagrams
                              or tables)
                              Default: vectors.log
  -h, --help                Show this help message and exit.
  -p, --cproject[=CProject] CProject (directory) to process
  -t, --ctree[=CTree]       single CTree (directory) to process
  -v, --verbose             Specify multiple -v options to increase verbosity.
                            For example, `-v -v -v` or `-vvv`We map ERROR or WARN ->
                              0 (i.e. always print), INFO -> 1(-v), DEBUG->2 (-vv)
                              Default: []
  -V, --version             Print version information and exit.

Generic values (AMISVGTool)
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

Specific values (AMISVGTool)
================================
pages                null
regexes              null
regexfile            null
tidyList             null
vectorLogfilename    vectors.log
vectorDir            vectors/

```
