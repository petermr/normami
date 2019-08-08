#! /bin/sh
# run complete stack over SPSS/Stata CTree/CProject

echo ========== RUN AMI STACK ==========


# === DEFAULTS ===

# from _stataok, _statabad, _spss
CPROJECT=_stataok

SPSS_TREE=PMC5502154
STATA_TREE=PMC6127950

# from stata, spss
TYPE="stata"

# from tree, project
SCOPE_CMD="tree"

# from "", clean
CLEAN=""

# overwrite with type, project, tree

while getopts ":cd:p:s:t:h" opt; do
  case ${opt} in
    c ) CLEAN=$OPTARG;;
    d ) TYPE=$OPTARG;;
    p ) CPROJECT=$OPTARG;;
    s ) SCOPE_CMD=$OPTARG;;
    t ) 
       echo CTREE OPTION
       echo OPTARG $OPTARG
       CTREE=$OPTARG
       echo CTREE: $CTREE
       ;;
    h )
      echo "Usage:"
      echo "    -h                      Display this help message."
      echo "    -c                      clean"
      echo "    -d                      type"
      echo "    -p                      project"
      echo "    -t                      tree"
      exit 0
      ;;
   \? )
     echo "Invalid Option: -$OPTARG" 1>&2
     exit 1
     ;;
    : )
      echo "Invalid option: $OPTARG requires an argument" 1>&2
      ;;  
  esac
done

echo bbbbbbbbbbbbbb $CTREE bbbbbbbbbbbbbb

# edit this to your own directory
WORKSPACE=$HOME/workspace/
FOREST_TOP=$WORKSPACE/projects/forestplots
MID_DIR=test20190804
FOREST_MID=$FOREST_TOP/$MID_DIR
PROJECT_DIR=$FOREST_MID/$CPROJECT


#constants
EXTLINES_GOCR=" --extractlines gocr"
EXTLINES_HOCR=" --extractlines hocr"
TESSERACT=" --tesseract /usr/local/bin/tesseract"
GOCR=" --gocr /usr/local/bin/gocr"
GOCR_ALPHA2NUM=" o 0 O 0 d 0   e 2   q 4 A 4   s 5 S 5 $ 5   d 6 G 6  J 7   T 7   Z 2   a 4 a 0"
SHARP4=" --sharpen sharpen4 "
THRESHOLD=" --threshold "
TEMPLATE_XML=" --template template.xml"
FORCEMAKE=" --forcemake"
TESS_CMD="${TESSERACT} ${EXTLINES_HOCR} ${FORCEMAKE}"
GOCR_CMD="${GOCR} ${EXTLINES_GOCR} ${FORCEMAKE}"

# executables
AMI_FILTER="ami-filter" 
AMI_FOREST_PLOT="ami-forestplot" 
AMI_IMAGE="ami-image" 
AMI_OCR="ami-ocr"
AMI_PIXEL="ami-pixel"
AMI_PDF="ami-pdf"

if [ $TYPE == "spss" ]
then 
#  PROJECT_DIR="${TOP_DIR}/spssSimple"
  TREE_NAME=$CTREE
  TEMPLATE_XSL=spssTemplate1
  SUBIMAGE=""
  YPROJECT=0.4
  XPROJECT=0.7
  THRESH=150
  SHARPENED="s4_thr_${THRESH}_ds"
  RAW_LIST="raw.header.tableheads raw.header.graphheads raw.body.table raw.footer.summary raw.footer.scale"
  SHARP_LIST="raw.header.tableheads_${SHARPENED} raw.header.graphheads_${SHARPENED} raw.body.table_${SHARPENED}  raw.footer.summary_${SHARPENED} raw.footer.scale_${SHARPENED}"
  
elif [ $TYPE == "stata" ]
then
#  PROJECT_DIR="${TOP_DIR}/stataSimple/"
  TREE_NAME=$CTREE
  TEMPLATE_XSL=stataTemplate1
  SUBIMAGE="--subimage statascale y LAST delta 10 projection x"
  YPROJECT=0.8
  XPROJECT=0.6
  THRESH=150
  SHARPENED="s4_thr_${THRESH}_ds"
  RAW_LIST="raw.header raw.body.ltable raw.body.rtable raw.scale"
  SHARP_LIST="raw.header_${SHARPENED} raw.body.ltable_${SHARPENED} raw.body.rtable_${SHARPENED} raw.scale_${SHARPENED}"
  
else
  echo "Must set STAT_TYPE to 'spss' or 'stata'"
fi

# create tree directory
TREE_DIR="${PROJECT_DIR}/${TREE_NAME}"
echo TREE ${TREE_DIR}

# if clean, make a new test tree directory
# needs a child tree: ${PROJECT_DIR}/_original/${TREE_NAME}/fulltext.pdf 

# if [ $CLEAN != "" ]
# then
# 	TEST_DIR="${PROJECT_DIR}/_test"
# 	ORIGINAL_DIR="${PROJECT_DIR}/_original"
# 	rm -rf ${TEST_DIR}
# 	cp -R ${ORIGINAL_DIR} ${TEST_DIR}
# 	TREE_DIR=${TEST_DIR}/${TREE_NAME}
# fi

THRESHOLD=" --threshold ${THRESH} "
DS=" --despeckle "
SHARPEN="${SHARP4} ${THRESHOLD} ${DS} "

# choose between tree and project
if [ $SCOPE_CMD == "project" ]
then 
  SCOPE=" -p ${PROJECT_DIR} "
elif [ $SCOPE_CMD == "tree" ]
then
  SCOPE=" -t ${TREE_DIR} "
fi
echo "SCOPE "${SCOPE}

# initial and sharpeneed directories
RAW="raw"
SHARPBASE=${RAW}_${SHARPENED}

# START OF EXECUTION
# ==================

# parse PDFs and create images
${AMI_PDF} ${SCOPE}
#remove non-meaningful images
${AMI_FILTER} ${SCOPE}
#sharpen/threshold images 
${AMI_IMAGE} ${SCOPE} --inputname ${RAW} ${SHARPEN}
#segment 
${AMI_PIXEL} ${SCOPE} \
     --projections --yprojection ${YPROJECT} --xprojection ${XPROJECT} --lines \
     --minheight -1 --rings -1 --islands 0 \
     --inputname ${SHARPBASE} ${SUBIMAGE} --templateinput ${SHARPBASE}/projections.xml \
     --templateoutput template.xml \
     --templatexsl /org/contentmine/ami/tools/${TEMPLATE_XSL}.xsl 

    ${AMI_FOREST_PLOT} ${SCOPE}  --inputname ${RAW} --segment --template ${SHARPBASE}/template.xml
    ${AMI_IMAGE} ${SCOPE} --inputnamelist ${RAW_LIST} ${SHARPEN}
    ${AMI_OCR} ${SCOPE} --inputnamelist ${SHARP_LIST} ${TESS_CMD}
    ${AMI_OCR} ${SCOPE} --inputnamelist ${SHARP_LIST} ${GOCR_CMD}
    ${AMI_FOREST_PLOT} ${SCOPE} --inputnamelist ${SHARP_LIST} --table hocr/hocr.svg --tableType hocr
    ${AMI_FOREST_PLOT} ${SCOPE} --inputnamelist ${SHARP_LIST} --table gocr/gocr.svg --tableType gocr

