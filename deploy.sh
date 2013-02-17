#!/bin/sh
scp repo-worker/target/scala-2.9.1/repo-worker_2.9.1-0.1-SNAPSHOT.min.jar ubuntu@ec2-54-246-0-236.eu-west-1.compute.amazonaws.com:/home/ubuntu/ 
scp web/target/scala-2.9.1/web-kiwi_2.9.1-0.1.0.war ubuntu@ec2-54-246-0-236.eu-west-1.compute.amazonaws.com:/home/ubuntu/ 
