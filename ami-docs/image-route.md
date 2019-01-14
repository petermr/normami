# Processing images in AMI

This is a tutorial showing the basic steps during the Forest Plot analysis

## ami-image filtering images

```
MacBook-Pro-3:uclforest pm286$ ami-image -p devtest/

Generic values (AMIImageTool)
================================
basename            null
cproject            /Users/pm286/workspace/uclforest/devtest

cTree               

cTreeList           [devtest/bowmann-perrottetal_2013, devtest/buzick_stone_2014_readalo, devtest/campbell_systematic_revie, devtest/case-systematic-review-ju, devtest/case_systematic_review_ar, devtest/cole_2014, devtest/davis2010_dissertation, devtest/dietrichsonb_gfilgesj_rge, devtest/donkerdeboerkostons2014_l, devtest/ergen_canagli_17_, devtest/fanetal_2017_meta_science, devtest/fauzan03, devtest/goldbergetal03, devtest/higginshallbaumfieldmosel, devtest/kunkel_2015, devtest/marulis_2010-300-35review, devtest/mcarthur_etal2012_cochran, devtest/paietal_14_meta, devtest/pearson_al05, devtest/puziocolby2013_co-operati, devtest/rui2009_meta_detracking, devtest/shenderovichetal_2016_pub, devtest/steenbergen-hu09, devtest/tamim-2009-effectsoftechn, devtest/torgersonetal_2011dferepo, devtest/zhengetal_2016]
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


cTree: bowmann-perrottetal_2013
.small: image.10.1.325_329.575_581
... 

.small: image.10.9.322_327.547_550

cTree: buzick_stone_2014_readalo
..
cTree: campbell_systematic_revie
......monochrome: image.19.1.63_185.120_313
.monochrome: image.19.2.210_361.126_313
.small: image.19.3.74_165.66_96
...
..small: image.72.1.183_317.459_489
.small: image.93.1.417_446.645_662

cTree: case-systematic-review-ju
...small: image.1.3.322_568.46_96
.....small: image.2.1.484_558.90_129
..............
cTree: case_systematic_review_ar
...duplicate: image.1.3.59_567.1047_1134
.duplicate: image.1.4.59_567.1047_1134
[...]
.small: image.7.8.90_99.651_664
.small: image.7.9.90_99.669_682

cTree: cole_2014
...
cTree: davis2010_dissertation
.small: image.111.1.378_404.517_584
.small: image.125.1.263_346.356_391
[...]
.small: image.216.8.0_8.792_800

cTree: dietrichsonb_gfilgesj_rge

cTree: donkerdeboerkostons2014_l
.small: image.1.1.446_467.223_243
.......
cTree: ergen_canagli_17_
.....
cTree: fanetal_2017_meta_science
......
cTree: fauzan03
..................... [...] ..........duplicate: image.87.1.0_3105.-3791_800
.............
cTree: goldbergetal03
.small: image.1.1.289_543.452_485
[...]
.small: image.9.2.0_576.47_78
.small: image.9.3.17_71.733_760

cTree: higginshallbaumfieldmosel
.small: image.1.1.412_537.11_72
.......small: image.37.2.125_539.199_230
.small: image.37.3.125_539.230_261
.small: image.37.4.125_539.261_291
.
cTree: kunkel_2015
............small: image.190.1.709_727.710_745
.small: image.191.1.721_739.722_757
.small: image.192.1.704_723.717_752
[...]
.small: image.54.1.659_678.710_745
...
cTree: marulis_2010-300-35review
.small: image.1.1.174_264.460_505
.small: image.1.2.176_262.542_556
.
cTree: mcarthur_etal2012_cochran
.small: image.1.1.76_538.702_703
.small: image.10.1.76_538.702_703
[...ZILLIONS of ultra-small images ..]
.small: image.98.1.76_538.702_703
.small: image.99.1.76_538.702_703

cTree: paietal_14_meta
.
cTree: pearson_al05
.small: image.1.1.72_287.448_473
.small: image.1.2.72_287.473_497

cTree: puziocolby2013_co-operati
....
cTree: rui2009_meta_detracking
.......
cTree: shenderovichetal_2016_pub
...small: image.1.3.451_472.223_245
.
cTree: steenbergen-hu09
..
cTree: tamim-2009-effectsoftechn
............................[...]..............
cTree: torgersonetal_2011dferepo
......
cTree: zhengetal_2016
...MacBook-Pro-3:uclforest pm286$ 

```
