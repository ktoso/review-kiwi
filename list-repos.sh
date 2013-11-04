#!/bin/sh

mongo <<HERE
use review_kiwi
db.repositories.find({}, {fetch_url:1})
HERE

