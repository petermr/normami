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

This is normally automatic and run **from the parent directory of the cproject** 
  
```
cd devtest
cd ..
ami-makeproject -p devtest -rawfiletypes pdf
```
  
NOTE: `makeproject` **RENAMES** the PDF files to `fulltext.pdf`. If you want to kep the originals, copy the whole directory as a backup.
  
### output
After `makeproject` the CProject structure now looks like:

```
devtest
├── bowmann-perrottetal_2013
│   ├── fulltext.pdf
├── buzick_stone_2014_readalo
│   ├── fulltext.pdf
[snipped]
├── kunkel_2015
│   ├── fulltext.pdf
├── make_project.json           
├── marulis_2010-300-35review   
│   ├── fulltext.pdf
├── mcarthur_etal2012_cochran
│   ├── fulltext.pdf
[snipped]
├── torgersonetal_2011dferepo
│   ├── fulltext.pdf
└── zhengetal_2016
    ├── fulltext.pdf
[snipped]

```

## ami-pdf


Because our input is PDF, both routes below require the initial analyses by `ami-pdf`.



This creates:
 * a subdirectory `svg/` with one SVG file per page in the original document: (`fulltext-page-1.svg` ...). The SVG holds the text and the vectors.
 * a sudirectory `pdfimages` which contains all the bitnap images as PNG. Pages can have any nnumber of images (0...) and the image files
 are numbered as: `image.<page>_<imagenumber>_<coordinates>`. 

### output
There are zillions of files! One document creates over 1000 PNGs with 1-pixel in each (we'll remove those later). Here just the first few are shown. `fulltext-page.0.svg` is the first page (though I think we'll change that to 1-indexing) and `image.10.8.322_326.545_552.png` is the 8th image on the 10th page. it's located at (x1=322,x2=326; y1=545,y2=552.png - inclusive), i.e. it's 5 * 8 pixels.

 ```
 .
├── bowmann-perrottetal_2013
│   ├── fulltext.pdf
│   ├── pdfimages
│   │   ├── image.10.1.325_329.575_581.png
│   │   ├── image.10.10.325_328.530_537.png
... many files - ca 100 - omitted. They are simply page decorations and will be removed
│   │   ├── image.10.8.322_326.545_552.png
│   │   └── image.10.9.322_327.547_550.png
│   └── svg
    ├── fulltext-page.0.svg
    ├── fulltext-page.1.svg
    ├── fulltext-page.10.svg
... snipped - note files are lexically ordered in this display
    ├── fulltext-page.15.svg
    ├── fulltext-page.16.svg
    ├── fulltext-page.2.svg
    ├── fulltext-page.3.svg
... snipped
├── fulltext-page.8.svg
    └── fulltext-page.9.svg
├── buzick_stone_2014_readalo
│   ├── fulltext.pdf
│   ├── pdfimages
│   │   ├── image.5.1.94_527.51_584.png
│   │   └── image.6.1.91_512.50_408.png
│   └── svg
│       ├── fulltext-page.0.svg
│       ├── fulltext-page.1.svg
... snipped (note that files are lexically ordered and lots are snipped
│       ├── fulltext-page.8.svg
│       └── fulltext-page.9.svg
├── campbell_systematic_revie
... 20 articles snipped
└── zhengetal_2016
    ├── fulltext.pdf
    ├── pdfimages
    │   ├── image.13.1.71_336.262_680.png
    │   ├── image.14.1.64_356.227_445.png
    │   └── image.9.1.76_368.225_560.png
    └── svg
        ├── fulltext-page.0.svg
        ├── fulltext-page.1.svg
... snipped
        ├── fulltext-page.8.svg
        └── fulltext-page.9.svg
```

## Images, text and vectors
Diagrams are published in two forms:

 * **Images**: where the diagram is held as pixels (often black on white, but sometimes oligo-coloured) in `bitmaps`. These can be detected by magnifying e.g. in a PDF viewer, when the jaggy edges can be seen.
 * **Vectors**: where the diagram is held as `paths` (lines, curves, etc.). If this is magnified the objects retain clean edges.
 
The scholarly literature uses both (sometimes on a per-journal basis). We can't choose, so we have to support both. In this project we therefore show both routes (the details may change). The default approach will run both.

## image route
**Image** processing requires a sequence like:
 
  * [ami-makeproject](./makeproject.md)
  * [ami-pdf](ami-pdf.md)
  * [ami-image](ami-image.md)
  * [ami-bitmap](ami-bitmap.md)
  * [ami-ocr](ami-ocr.md) (if we want to extract text)
  * [ami-pixel](ami-pixel.md) (creates lines, etc.)
  * [ami-svg](ami-svg.md) (as we have created SVG from pixel analysis)
  * [ami-plot](ami-plot.md)
  * [ami-forest](ami-forest.md)
  
## vector route
**Vector** processing requires a sequence like:
 
  * [ami-makeproject](./makeproject.md)
  * [ami-pdf](ami-pdf.md)
  * [ami-svg](ami-svg.md) (from `ami-pdf`)
  * [ami-plot](ami-plot.md)
  * [ami-forest](ami-forest.md)
  
  
  


