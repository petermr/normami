# makeproject

Makes a project from raw files in a single (CProject) directory

## minimal default command
```
ami-makeproject --project foo -rawfiletypes <type1>[,<type2>...]
```

### commandline help
Date: 201901134

```
Usage: ami-makeproject [OPTIONS]
Description
===========
Processes a directory (CProject) containing files (e.g.*.pdf, *.html, *.xml) to
be made into CTrees.
Assuming a directory foo/ with files

  a.pdf
  b.pdf
  c.html
  d.xml

makeproject -p foo -f pdf,html,xml
will create:
foo/
  a/
    fulltext.pdf
  b/
    fulltext.pdf
  c/
    fulltext.html
  d/
    fulltext.xml

The directories can contain multiple filetypes

Assuming a directory foo/ with files

  a.pdf
  b.pdf
  a.html
  b.xml
  c.pdf

makeproject -p foo -f pdf,html,xml
will create:
foo/
  a/
    fulltext.pdf
    fulltext.html
  b/
    fulltext.pdf
    fulltext.xml
  c/
    fulltext.pdf

raw filename changes occur in CProject.makeProject()Files with uppercase
characters, spaces, punctuation, long names, etc. may give problems. By default
they
(a) are lowercased,
(b) have punctuation set to '_'
(c) are truncated to --length characters.
 If any of these creates ambiguity, then numeric suffixes are added. By default
a logfile of the conversions is created in make_project.json. The name can be
changed
Options
=======
      --basename=<userBasename>
                            User's basename for outputfiles (e.g. foo/bar/<basename>.
                              png. By default this is computed by AMI. This allows
                              users to create their own variants, but they won't be
                              known by default to subsequentapplications
      --compress[=<compress>]
                            compress and lowercase names.
                              Default: 25
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
      --rawfiletypes=<rawFileFormats>[,<rawFileFormats>...]...
                            suffixes of included files (html, pdf, xml): can be
                              concatenated with commas
  -h, --help                Show this help message and exit.
  -p, --cproject[=CProject] CProject (directory) to process
  -t, --ctree[=CTree]       single CTree (directory) to process
  -v, --verbose             Specify multiple -v options to increase verbosity.
                            For example, `-v -v -v` or `-vvv`We map ERROR or WARN ->
                              0 (i.e. always print), INFO -> 1(-v), DEBUG->2 (-vv)
                              Default: []
  -V, --version             Print version information and exit.

Generic values (AMIMakeProjectTool)
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

Specific values (AMIMakeProjectTool)
================================
```
