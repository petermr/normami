# AMI
The name 'AMI' is becoming the standard for all operations using the AMI system; (`norma`) will be increasingly phased out. The impetus comes from changing to the `picocli` commandline tool which allows much better organisation of commands, subcommands, parameters and options. Much of the initial `help` will be generated from the Picocli help tool - the `help` is almost certain to be more up-to-date than this document.

## philosophy
AMI consists of 10+ major commands, often run consecutively. The philosophy is driven by `make` (https://en.wikipedia.org/wiki/Make_(software) ) and `map-reduce` (https://en.wikipedia.org/wiki/MapReduce ), although not universally implemented (yet). The "data" are held on the filestore (as a supertree) and generally exposed as files. The system is therefore stateless - the data in the `CProject` should define what has happened to it and what can be done with it. 

## CProject and CTrees
The core data are collections of documents and their components (often "assets"). A `CProject` is simply a directory with well-defined (reserved) names for most parts of the system. `AMI` has many tools for traversing and transforming the `CProject`. But also common tools (Python, shell, etc.) outside the `AMI` system can modify the `CProject` without breaking it - it is the data specification that is key, not the software.

A `CProject` normally consists of a set of "child" `CTree`s - themselves directories. A command such as:

```
ami-pdf -p myCProject <parameters/options>
```

is implemented as:

```
for-each ctree in myCProject {
        ami-pdf -t ctree <parameters/options>
   }
```

Most commands therefore either have a `-p` option (`CProject`) or a `-t` option (`CTree`).

## CProject structure

The normal `CProject` structure is a list of directories , with management files such as logging:
```
├── bowmann          // raw input only (PDF)
│   ├── fulltext.pdf
├── buzick           // no input yet
├── campbell         // analyzed with ami-image and ami-ocr 
│   └── pdfimages    // derived files (could be clean'ed and regenerated
│       └── derived
│           ├── hocr // OCR analysis of 3 images
│           │   ├── 41.2
│           │   │   └── 41.2.svg   // created SVG from OC
│           │   ├── 41.2.raw.html  // HOCR html
│           │   ├── 42.1.57_367.204_386
│           │   │   └── 42.1.57_367.204_386.svg
│           │   ├── 42.1.57_367.204_386.raw.html
│           │   ├── 42.5.277_492.217_334
...
// log file from a `make-project` command
├── make_project.json // project metadata
|
...
└── zheng
    ├── fulltext.pdf
    └── pdfimages
        └── derived
            ├── image.9.1.76_368.225_560.png
            └── scale1_7  // image which as been scaled
                └── image.9.1.76_368.225_560.png

138 directories, 565 files
```
Generally the child directories of the `CProject` will be `CTree`s, log files and summary files (e.g. aggregations of the output of each `CTree`). 

## CTree structure
The CTree structure is very powerful, and is driven by:

 * the structure of the document itself (images, vectors, text, ancillary data)
 * the processing carried out by `ami`, either automatically or requested by the user
  
The `CTree`s depend on the documents/data analysed and will not necessarily contain the same type or level of files. For example some PDF files contain embedded bitmaps ("images") while others contain vector strokes. The `campbell` tree shows OCR analysis of 3 embedded bitmaps. The `zheng` tree contains a scaled image (enlarged by a factor of 1.7). There are probably 100 different potential sub-directory types in a `CTree`, though few will have all of them. They include:

### document structure
Most of our core work is on scientific/medical articles which has natural sections and components. We get input as XML, HTML, PDF, Word, images and many have numerous assets. `ami` can contain:

 * document sections, e.g. head, body, back. These can have subsections (e.g. head contains title, authors, journal, metadata, abstract and several more). 
 * images and their processing. Images are normally extracted as PNG , and can be further analysed for (a) text/characters using Tesseract or other OCR tools, creating hOCR (HTML) output; (b) image processing (sharpeneing, contrast, bitmap and pixel analysis, etc.) often leading to SVG graphics.
 * XML text (e.g. JATS) can be converted into HTML and moved to appropriate `CTree` subdirectories.
 * Vector graphics can be exported as SVG and used ot create semantic objects.
 * ancillary / supplemental files (e.g. of data) can be moved into the CTree.
 * and more. `CTree`s can contain anything, but are only guaranteed to recognize reserved files or syntax.
 
## AMI commands
(The commands are still evolving and it's better to ask `ami` herself what the current ones are). At present (20190109) these are separate comm but we'll move them into `picocli` subcommands as soon as we find how.

For a typical command (e.g. `AMIPDFTool`):
`ami-pdf` would run `AMIPDFTool` and provess the arguments with `picocli` commandline interpreter. If no arguments are given, then the system gives command-specific help.

The `ami` stack currently (20190109) contains at least the following commands:

### ami-makeproject
<<<<<<< HEAD
See [makeproject](./makeproject.md)
=======
See [make-project](./make-project.md)
>>>>>>> fda063b24934e5ad80002bcb67190ad6670ccc88
Takes a set of PDF (HTML, XML) files and converts each into a subdirectory `CTree` with child `fulltext.pdf` (`xml`, `html`). The subdirectory name is normalised to remove whitespace and punctuation.
### ami-pdf
See [ami-pdf](./ami-pdf.md)
Processes `fulltext.pdf` (in `cTree` or `CProject`) to create:

  * svg/ with page-%d.svg for each page in document
  * pdfimages/ with image%d.png for every embedded image
 
### ami-image 
See [ami-image](./ami-image.md)
Filters (but does not edit) `pdfimages/image%d.png` above into various subdirectories (e.g. `monochrome`, `small`, etc).
### ami-bitmap
See [ami-bitmap](./ami-bitmap.md)
Performs image processing on the retained *.png such as 

  * thresholding
  * sharpening
  * geometric scaling
  * rotating
  * dilating/eroding
  
### ami-pixel
See [ami-pixel](./ami-pixel.md)
Extracts objects (lines, blocks, etc.) from processed bitmaps 

### ami-ocr
See [ami-ocr](./ami-ocr.md)
Extracts textfrom processed bitmaps 

### ami-svg
See [ami-svg](./ami-svg.md)
processes the SVG created from PDF into HTML and vercor graphics objects (NYI) 
  
### ami-forest (and similar)
See [ami-forest](./ami-forest.md)
Analyzes bitmaps and vector graphics as Forest plots (NYI)


  
  



 
 







