#!/bin/bash
s=$(realpath $1);
a=$1
a=${a##*/}
a=${a%.*}
outputdir=$(dirname $s)
mkdir ${outputdir}/mipmap-hdpi ${outputdir}/mipmap-mdpi ${outputdir}/mipmap-xhdpi ${outputdir}/mipmap-xxhdpi ${outputdir}/mipmap-xxxhdpi
rsvg-convert $1 -o ${outputdir}/mipmap-mdpi/${a}.png -w 48 -h 48
rsvg-convert $1 -o ${outputdir}/mipmap-hdpi/${a}.png -w 72 -h 72
rsvg-convert $1 -o ${outputdir}/mipmap-xhdpi/${a}.png -w 96 -h 96
rsvg-convert $1 -o ${outputdir}/mipmap-xxhdpi/${a}.png -w 144 -h 144
rsvg-convert $1 -o ${outputdir}/mipmap-xxxhdpi/${a}.png -w 192 -h 192
