# ami-image

Filters unsuitable images (e.g. too small, monochrome, duplicates).

## minimal command
```
ami-image --project foo 
```

### commandline help
Date: 201901134
ami-image
Usage: ami-image [OPTIONS]
Description
===========
FILTERs images (initally from PDFimages), but does not transform the contents.
Services include<ul>  <li>identification of duplicate images, and removal<.
li><li>rejection of images less than gven size</li><li>rejection of monochrome
images (e.g. all white or all black) (NB black and white is 'binary/ized'</ul>
Options
=======
      --basename=<userBasename>
                            User's basename for outputfiles (e.g. foo/bar/<basename>.
                              png. By default this is computed by AMI. This allows
                              users to create their own variants, but they won't be
                              known by default to subsequentapplications
      --dryrun=<dryrun>     for testing runs a single phase without output, deletion
                              or transformation.(NYI).
      --duplicatedir=<duplicateDirname>
                            directory for duplicates.
                              Default: duplicates
      --duplicates=<discardDuplicates>
                            discard duplicate images
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
      --minheight=<minHeight>
                            minimum height (pixels) to accept
                              Default: 100
      --minwidth=<minWidth> minimum width (pixels) to accept
                              Default: 100
      --monochrome=<discardMonochrome>
                            discard monochrome images (i.r. only one color)
      --monochromedir=<monochromeDirname>
                            directory for monochrome images
                              Default: monochrome
      --rawfiletypes=<rawFileFormats>[,<rawFileFormats>...]...
                            suffixes of included files (html, pdf, xml): can be
                              concatenated with commas
      --smalldir=<smallDirname>
                            directory for small images.
                              Default: small
  -h, --help                Show this help message and exit.
  -p, --cproject[=CProject] CProject (directory) to process
  -t, --ctree[=CTree]       single CTree (directory) to process
  -v, --verbose             Specify multiple -v options to increase verbosity.
                            For example, `-v -v -v` or `-vvv`We map ERROR or WARN ->
                              0 (i.e. always print), INFO -> 1(-v), DEBUG->2 (-vv)
                              Default: []
  -V, --version             Print version information and exit.

Generic values (AMIImageTool)
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

Specific values (AMIImageTool)
================================
minHeight           100
minWidth            100
smalldir            small
discardMonochrome   true
monochromeDir       monochrome
discardDuplicates   true
duplicateDir        duplicates
