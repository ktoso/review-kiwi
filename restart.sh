#!/bin/sh
killall -9 java

cd $HOME/review-kiwi
echo "Restarting review-kiwi @ `date`..."

rm -rf /tmp/*

sbt <<HERE
project repo-worker
run-main com.reviewkiwi.repoworker.RepoWorkerRunner
HERE

