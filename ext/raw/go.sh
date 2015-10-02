#!/bin/sh

rm -rf drawable-hdpi drawable-mdpi drawable-ldpi
mkdir drawable-hdpi drawable-mdpi drawable-ldpi

for f in menu*; do

inkscape $f -e drawable-hdpi/`basename $f .svg`.png -d 72 -w 96 -h 96
inkscape $f -e drawable-mdpi/`basename $f .svg`.png -d 72 -w 64 -h 64
inkscape $f -e drawable-ldpi/`basename $f .svg`.png -d 72 -w 48 -h 48

done

for f in pencil* icon*; do

inkscape $f -e tmp.png -d 72 -w 128 -h 128
convert tmp.png -colors 64 -quality 90 -depth 8 drawable/`basename $f .svg`.png

done


for f in desktop* color*; do
inkscape $f -e tmp.png -d 72 -w 96 -h 96
convert tmp.png -colors 64 -quality 90 -depth 8 drawable/`basename $f .svg`.png
done

for f in mime*; do
inkscape $f -e tmp.png -d 72 -w 96 -h 96
convert tmp.png -colors 64 -quality 90 -depth 8 drawable/`basename $f .svg`.png
done

for f in title*; do
inkscape $f -e tmp.png -d 72 -w 480 -h 72
convert tmp.png -colors 64 -quality 90 -depth 8 drawable/`basename $f .svg`.png
done

#inkscape sketchbook.svg -e drawable/icon.png -d 72 -w 72 -h 72

inkscape quit02.svg -e tmp.png -d 72 -w 400 -h 400
convert tmp.png -colors 64 -quality 90 -depth 8 drawable/exit.png

convert splash.png -colors 64 -quality 90 -depth 8 drawable/splash.png

