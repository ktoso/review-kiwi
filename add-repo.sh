#!/bin/sh

echo "repo name: "
read NAME

echo "fetch url: "
read URL

mongo <<HERE
use review_kiwi
db.repositories.insert({ "github_repo_id" : "$NAME", "name" : "$NAME", "fetch_url" : "$URL", "fetch_using_pooling" : true, "watchers" : [ ObjectId("5273ee48e4b052015422508d") ] })
HERE

