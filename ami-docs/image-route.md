# Processing images in AMI

This is a tutorial showing the basic steps during the image processing of the Forest Plot corpus

## ami-image filtering images

[ami-image](ami-image.md) filters out these types of image:

 * **duplicates** (often from publisher icons)
 * **monochrome** (a single colour which is probably a background or cover rectangle)
 * **small** (images smaller than a given limit. These may be decorations, rules, symbols, maths, etc.) in the worst cases 
  they are used to create larger images.
  
  These are all moved to their own directories and not further processed. They can be inspected, deleted or restored.
  
## typical job and output  

### command
**$ ami-image -p devtest/**

### echo input
```
Generic values (AMIImageTool)
================================
basename            null
cproject            /Users/pm286/workspace/uclforest/devtest

cTree               

cTreeList           [devtest/bowmann-perrottetal_2013, devtest/buzick_stone_2014_readalo, devtest/campbell_systematic_revie, devtest/case-systematic-review-ju, [...] devtest/steenbergen-hu09, devtest/tamim-2009-effectsoftechn, devtest/torgersonetal_2011dferepo, devtest/zhengetal_2016]
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
```
### output 
Note `cTree` echo.

**`cTree: bowmann-perrottetal_2013`** *has many "small" images (decorations)*
```
.small: image.10.1.325_329.575_581
... 
.small: image.10.9.322_327.547_550
```

**`cTree: buzick_stone_2014_readalo`**

**`cTree: campbell_systematic_revie`** *Note monochrome image/s* 
```
......monochrome: image.19.1.63_185.120_313
.monochrome: image.19.2.210_361.126_313
.small: image.19.3.74_165.66_96
...
..small: image.72.1.183_317.459_489
.small: image.93.1.417_446.645_662
```

**`cTree: case-systematic-review-ju`**
```
...small: image.1.3.322_568.46_96
.....small: image.2.1.484_558.90_129
..............
```
**`cTree: case_systematic_review_ar`**
```
...duplicate: image.1.3.59_567.1047_1134
.duplicate: image.1.4.59_567.1047_1134
[...]
.small: image.7.8.90_99.651_664
.small: image.7.9.90_99.669_682
```
**`cTree: cole_2014`**
```
...
cTree: davis2010_dissertation
.small: image.111.1.378_404.517_584
.small: image.125.1.263_346.356_391
[...]
.small: image.216.8.0_8.792_800
```
**`cTree: dietrichsonb_gfilgesj_rge`**
```
```
**`cTree: mcarthur_etal2012_cochran`**
```
.small: image.1.1.76_538.702_703
.small: image.10.1.76_538.702_703
[...ZILLIONS of ultra-small images ..]
.small: image.98.1.76_538.702_703
.small: image.99.1.76_538.702_703

```
**`cTree: paietal_14_meta`**
```
.
```
**`cTree: pearson_al05`**
```
.small: image.1.1.72_287.448_473
.small: image.1.2.72_287.473_497

```
**`cTree: puziocolby2013_co-operati`**
```
....
```
**`cTree: rui2009_meta_detracking`**
```
.......
```
**`cTree: shenderovichetal_2016_pub`**
```
...small: image.1.3.451_472.223_245
.
```
**`cTree: steenbergen-hu09`**
```
..
```
**`cTree: tamim-2009-effectsoftechn`**
```
............................[...]..............
```
**`cTree: torgersonetal_2011dferepo`**
```
......
```
**`cTree: zhengetal_2016`**
```

## ami-bitmap

