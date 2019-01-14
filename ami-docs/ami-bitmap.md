# ami-bitmap
Image processing (sharpen, threshold, binarization, etc.) on bitmaps 

## minimal default command
```
ami-bitmap --project foo 
```

### commandline help
Date: 201901134

```
ami-bitmap
Usage: ami-bitmap [OPTIONS]
Description
===========
		MOVE scaling to bitmap<li>geometric scaling of images using Imgscalr, with
interpolation. Increasing scale on small fonts can help OCR, decreasing scale
on large pixel maps can help performance.
Options
=======
      --basename=<userBasename>
                            User's basename for outputfiles (e.g. foo/bar/<basename>.
                              png. By default this is computed by AMI. This allows
                              users to create their own variants, but they won't be
                              known by default to subsequentapplications
      --binarize=<binarize> create binary (normally black and white); methods
                              local_mean ...
                              Default: local_mean
      --dryrun=<dryrun>     for testing runs a single phase without output, deletion
                              or transformation.(NYI).
      --erodedilate=<erodeDilate>
                            erode 1-pixel layer and then dilate. Removes minor spikes
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
      --maxheight=<maxHeight>
                            maximum height (pixels) to accept. If larger, scales the
                              image
                              Default: 1000
      --maxwidth=<maxWidth> maximum width (pixels) to accept. If larger, scales the
                              image
                              Default: 1000
      --posterize           create a map of colors including posterization. NYI
      --rawfiletypes=<rawFileFormats>[,<rawFileFormats>...]...
                            suffixes of included files (html, pdf, xml): can be
                              concatenated with commas
      --rotate=<rotateAngle>
                            rotates image anticlockwise by <value> degrees.
                              Currently 90, 180, 270
                              Default: 0
      --scalefactor=<scalefactor>
                            geometrical scalefactor. if missing, no scaling (don't
                              use 1.0) Uses Imgscalr library.
      --sharpen=<sharpen>   sharpen image using Laplacian kernel or sharpen4 or
                              sharpen8 (BoofCV)..
                              Default: sharpen4
      --thinning[=<thinning>]
                            thinning algorithm. Currently under development.
                              Default: null
      --threshold=<threshold>
                            maximum value for black pixels (non-background)
                              Default: 180
      --toolkit=<toolkit>   Image toolkit to use. Boofcv (probable longterm choice),
                              Scalr (Imgscalr), simple but no longer deeveloped. Pmr
                              (my own) when all else fails.
                              Default: Boofcv
  -h, --help                Show this help message and exit.
  -p, --cproject[=CProject] CProject (directory) to process
  -t, --ctree[=CTree]       single CTree (directory) to process
  -v, --verbose             Specify multiple -v options to increase verbosity.
                            For example, `-v -v -v` or `-vvv`We map ERROR or WARN ->
                              0 (i.e. always print), INFO -> 1(-v), DEBUG->2 (-vv)
                              Default: []
  -V, --version             Print version information and exit.

Generic values (AMIBitmapTool)
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

Specific values (AMIBitmapTool)
================================
binarize            local_mean
maxheight           1000
maxwidth            1000
posterize           false
rotate              0
scalefactor         null
sharpen             sharpen4
threshold           180
```
