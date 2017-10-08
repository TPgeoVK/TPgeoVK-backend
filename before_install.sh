#!/usr/bin/env bash
set -x
openssl aes-256-cbc -K $encrypted_f955482a5e6e_key -iv $encrypted_f955482a5e6e_iv -in travis_rsa.enc -out travis_rsa -d
rm travis_rsa.enc
chmod 600 travis_rsa
mv travis_rsa ~/.ssh/travis_rsa
mv ssh_config ~/.ssh/config

