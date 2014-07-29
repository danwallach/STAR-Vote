#!/bin/bash
FILES=*.html
for f in $FILES
do
    echo "Fixing $f..."
    python fixer.py $f > fixed/$f
done

