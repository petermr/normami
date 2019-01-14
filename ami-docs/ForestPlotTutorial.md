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
its absolute filename (e.g. `ami-pdf -p /Users/pm286/workspace/uclforest/devtest`).

## makeproject
See [makeproject](./makeproject.md)

This is normally automatic and run **from the parent directory of the <cproject>** 
```
cd devtest
cd ..
ami-makeproject -p devtest -rawfiletypes pdf
```
NOTE: `makeproject` **RENAMES** the PDF files to `fulltext.pdf`. If you want to kep the originals, copy the whole directory as a backup.
  
## ami-pdf
Because our input is PDF, both routes below require the initial analyses by `ami-pdf`.

``` 
ami-pdf -p devtest
```
This creates:
 * a subdirectory `svg/` with one SVG file per page in the original document: (`fulltext-page-1.svg` ...). The SVG holds the text and the vectors.
 * a sudirectory `pdfimages` which contains all the bitnap images as PNG. Pages can have any nnumber of images (0...) and the image files
 are numbered as: `image.<page>_<imagenumber>_<coordinates>`. 
 



## Images, text and vectors
Diagrams are published in two forms:

 * **Images**: where the diagram is held as pixels (often black on white, but sometimes oligo-coloured) in `bitmaps`. These can be detected by magnifying e.g. in a PDF viewer, when the jaggy edges can be seen.
 * **Vectors**: where the diagram is held as `paths` (lines, curves, etc.). If this is magnified the objects retain clean edges.
 
The scholarly literature uses both (sometimes on a per-journal basis). We can't choose, so we have to support both. In this project we therefore show both routes

## image route
Image processing requires a sequence like:
 
  * ami-

