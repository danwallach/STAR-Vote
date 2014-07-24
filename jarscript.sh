#!/bin/bash
# This is a file for automatically copies jars from our out directory
# into our root directory, and then commits and pushes them to git. 

cp out/artifacts/Votebox/Votebox.jar ./Votebox.jar
cp out/artifacts/Supervisor/Supervisor.jar ./Supervisor.jar
cp out/artifacts/BallotScanner/BallotScanner.jar ./BallotScanner.jar
cp out/artifacts/Tap/Tap.jar ./Tap.jar


git add *.jar
git commit -am "Updating the jars!"
git push origin $1
