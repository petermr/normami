# ForestPlot Tutorial 
This tutorial arises from a project sponsored by EPPICentre at UCL to extract Forest Plots from PDF files. 
Even if you don't know what a FP is, we hope you will find the basic operations clear and useful. The initial 
names of files and directories will depend on your project. For more detail refer to command-specific files in this directory.


## assemble a corpus
Create a directory  in a suitable part of your system (e.g. `myprojects/devtest` under you home directory) with **copies** of the (25) raw PDF files. 
```
devtest/
├── Bowmann-Perrott etal_2013.pdf
├── Buzick & Stone_2014_read aloud.pdf
...
├── Zheng etal_2016.pdf
├── case_systematic_review_arts_participation.pdf
└── rui2009_meta_detracking.pdf
```
The name `devtest` will be referred to as the `CProject` name. It should have only alphanumerics (no punctuation/spaces). You may need to know
its absolute filename (e.g. `/Users/pm286/workspace/uclforest/devtest`).

## makeproject
See [makeproject](./makeproject.md)

This is normally automatic and run **from the parent directory of the <cproject>** 
```
cd devtest
cd ..
ami-makeproject -p devtest
```
NOTE: `makeproject` **RENAMES** the PDF files to `fulltext.pdf`. If you want to kep the originals, copy the 




