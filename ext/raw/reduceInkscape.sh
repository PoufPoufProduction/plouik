#!/bin/sh

# delete the end of lines (can not be done easily with sed)
tr -d '\n' < $1 > tmp.svg;                                              mv tmp.svg $1

# add an end of line after each tag end
sed -e "s/> */>\n/g" $1 > tmp.svg;                                      mv tmp.svg $1

# remove the commentary
sed -e "s/^<\!--[^>]*>//g" $1 > tmp.svg;                                mv tmp.svg $1

# change version for SVG validation
sed -e "s/version\(....\)0/version\11/g" $1 > tmp.svg;                  mv tmp.svg $1

# change the size stuff
sed -e "/^<svg/s/width=\"\([0-9]*\)\" height=\"\([0-9]*\)\"/width=\"100%\" height=\"100%\" viewBox=\"0 0 \1 \2\"/g" $1 > tmp.svg; mv tmp.svg $1

sed -e "/^<svg/s/width=\"\([0-9]*\)px\" height=\"\([0-9]*\)px\"/width=\"100%\" height=\"100%\" viewBox=\"0 0 \1 \2\"/g" $1 > tmp.svg; mv tmp.svg $1

# remove all the inkscape and sodipodi private values
sed -e "s/^<\/*inkscape[^>]*>//g" $1 > tmp.svg;                         mv tmp.svg $1
sed -e "s/^<\/*sodipodi[^>]*>//g" $1 > tmp.svg;                         mv tmp.svg $1
sed -e "s/sodipodi[:][^=]*[=][^ >]*//g" $1 > tmp.svg;                   mv tmp.svg $1
sed -e "s/inkscape[:][^=]*[=][^ >]*//g" $1 > tmp.svg;                   mv tmp.svg $1
sed -e "s/xmlns[:]sodipodi[=][^ ]*//g" $1 > tmp.svg;                    mv tmp.svg $1
sed -e "s/xmlns[:]inkscape[=][^ ]*//g" $1 > tmp.svg;                    mv tmp.svg $1

# reduce the id names
sed -e "s/linearGradient\([0-9][0-9][0-9][0-9]\)/lg\1/g" $1 > tmp.svg;  mv tmp.svg $1
sed -e "s/radialGradient\([0-9][0-9][0-9][0-9]\)/rg\1/g" $1 > tmp.svg;  mv tmp.svg $1
sed -e "s/rect\([0-9][0-9][0-9][0-9]\)/r\1/g" $1 > tmp.svg;             mv tmp.svg $1
sed -e "s/path\([0-9][0-9][0-9][0-9]\)/p\1/g" $1 > tmp.svg;             mv tmp.svg $1
sed -e "s/stop\([0-9][0-9][0-9][0-9]\)/s\1/g" $1 > tmp.svg;             mv tmp.svg $1

# reduce the value precision (important!)
sed -e "s/\([.][0-9][0-9]\)[0-9]*/\1/g" $1 > tmp.svg;                   mv tmp.svg $1

# remove the not used values
sed -e '/stroke[:]none/s/stroke[-][^;"]*;*//g' $1 > tmp.svg;            mv tmp.svg $1
sed -e '/marker[:]none/s/marker[-][^;"]*;*//g' $1 > tmp.svg;            mv tmp.svg $1

# remove not necessary spaces
sed -e "s/ \/>/\/>/g" $1 > tmp.svg;                                     mv tmp.svg $1
sed -e "s/  */ /g" $1 > tmp.svg;                                        mv tmp.svg $1
