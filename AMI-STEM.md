# AMI-STEM 

A universal scientific search engine based on semantic documents and dictionaries

## Installation

AMI-STEM is provided as 
  
  * a single JAR file (with dependencies). This can be downoaded and run from your commandline. Needs JRE 8 or later. Not yet tested.
  * an `appassembler` tree with `bin` files for both Linux/MACOSX and Windows `*.bat`. The AMI options are runnable from the commandline.
  
### appassembler download

Choose an area in your disk where you want to download the software. I have a `projects` subdirectory / folder and the same for `software`. Try to keep them separate.

The appassembler tree is provided in `https://github.com/petermr/normami/blob/master/ami20180904.zip`. 

Github allows you to download it by 
 * clicking the link 
 * clicking Download 

On MACOSX this puts the file in `Downloads` . Copy/move this to your `software` folder and unzip it. Your directories should look like:
```
ami20180904
├── ami20180904.zip
└── appassembler
    ├── bin
    │   ├── ami-all
    │   ├── ami-all.bat
    │   ├── ami-frequencies
    │   ├── ami-frequencies.bat
    │   ├── ami-gene
    │   ├── ami-gene.bat
    │   ├── ami-identifier
    │   ├── ami-identifier.bat
    │   ├── ami-regex
    │   ├── ami-regex.bat
    │   ├── ami-search
    │   ├── ami-search.bat
    │   ├── ami-sequence
    │   ├── ami-sequence.bat
    │   ├── ami-species
    │   ├── ami-species.bat
    │   ├── ami-word
    │   ├── ami-word.bat
    │   ├── cmine
    │   ├── cmine.bat
    │   ├── contentMine
    │   ├── contentMine.bat
    │   ├── cproject
    │   ├── cproject.bat
    │   ├── makeProject
    │   ├── makeProject.bat
    │   ├── norma
    │   └── norma.bat
    └── repo
        ├── Saxon-HE-9.6.0-3.jar
        ├── asm-1.0.2.jar
        ├── asm-3.3.1.jar
        ├── calibration-0.17.jar
        ├── cephis-0.1-SNAPSHOT.jar
        ├── commons-codec-1.10.jar
        [... and 100 more]
```

## appassembler

## getting started

`ami-all` displays the help screen and a list of directories on the system.
```
amiProcessor <projectDirectory> [dictionary [dictionary]]
    projectDirectory can be full name or relative to currentDir

list of dictionaries taken from AMI dictionary list:

    auxin               cochrane            compchem            country             crystal             
    disease             distributions       drugs               elements            epidemic            
    funders             illegaldrugs        inn                 insecticide         invasive            
    magnetism           nal                 obesity             organization        pectin              
    phytochemicals-old  phytochemicals1     phytochemicals2     plantDevelopment    plantparts          
    poverty             refugeeUNHCR        statistics          statistics2         synbio              
    tropicalVirus       
also:
    gene     
    species     

```

`ami-all` takes the project name as first arg. `cd` to the directory **containing** the project top ("the CProject")
```
ami-all marchantia
```
