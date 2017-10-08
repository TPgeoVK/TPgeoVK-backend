#!/bin/bash
set -x
if [ $TRAVIS_BRANCH == 'master' ] ; then
    cd target
    git init
        
    git remote add deploy "ssh://tpgeovk@student.bmstu.cloud:12103/var/www/kjaermaxi.me"
    git config user.name "Travis CI"
    git config user.email "rentgeny05@gmail.com"
    
    git add .
    git commit -m "Deploy"
    git push --force deploy master
else
    echo "Not deploying, since this branch isn't master."
fi
