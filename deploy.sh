#!/bin/bash
set -x
if [ $TRAVIS_BRANCH == 'master' ] ; then
    cd target
    git init

    git remote add deploy "ssh://tpgeovk@student.bmstu.cloud:12103/home/tpgeovk/deploy"
    git config user.name "Travis CI"
    git config user.email "rentgeny05@gmail.com"

    git add .
    git commit -m "Deploy"
    git push deploy master --force
else
    echo "Not deploying, since this branch isn't master."
fi
