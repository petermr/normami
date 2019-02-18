#! /bin/sh

VERSION=ami20190218b
DIR=../ami-jars/${VERSION}/
mkdir ${DIR}
cp -R target/appassembler/bin ${DIR}/bin
cp -R target/appassembler/repo ${DIR}/repo
cp target/normami-0.1-SNAPSHOT-jar-with-dependencies.jar  ${DIR}/${VERSION}-jar-with-dependencies.jar






