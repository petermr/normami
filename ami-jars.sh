#! /bin/sh

VERSION=ami20190219
DIR=../ami-jars/${VERSION}/
mkdir ${DIR}
cp -R target/appassembler/bin ${DIR}/bin
cp -R target/appassembler/repo ${DIR}/repo
cp target/normami-0.1-SNAPSHOT-jar-with-dependencies.jar  ${DIR}/${VERSION}-jar-with-dependencies.jar

cd ../ami-jars
git pull
git add $VERSION/
git commit -am "added "$VERSION
git push






