#!/bin/sh

ROOSSTER_VERSION=0.1
RELEASE_DIR=roosster-$ROOSSTER_VERSION

mkdir $RELEASE_DIR
mkdir $RELEASE_DIR/dist

cp -r src/ doc/ resources/ lib/ $RELEASE_DIR
cp dist/roosster.jar $RELEASE_DIR/dist
cp ARTISTIC.txt LICENSE build.xml $RELEASE_DIR


find ./$RELEASE_DIR -name ".svn" | xargs rm -r

tar cfj $RELEASE_DIR.tar.bz2 $RELEASE_DIR

mv $RELEASE_DIR.tar.bz2 releases

rm -r $RELEASE_DIR

