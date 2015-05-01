#!/bin/bash

if [ "$TRAVIS_REPO_SLUG" == "TomasMikula/Flowless" ] && [ "$TRAVIS_PULL_REQUEST" == "false" ] && [ "$TRAVIS_BRANCH" == "master" ]; then
  openssl aes-256-cbc -d -k "$ENC_PWD" -in gradle.properties.enc -out gradle.properties
  openssl aes-256-cbc -d -k "$ENC_PWD" -in secring.gpg.enc -out secring.gpg

  echo -e "Starting publish to Sonatype...\n"

  gradle uploadArchives
  RETVAL=$?

  if [ $RETVAL -eq 0 ]; then
    echo 'Publish completed!'
  else
    echo 'Publish failed.'
    return 1
  fi

  # publish Javadoc for non-SNAPSHOT versions
  VERSION=$(gradle -q getVersion)
  if [[ $VERSION != *SNAPSHOT* ]]; then

    echo -e "Publishing javadoc...\n"

    git config --global user.email "travis@travis-ci.org"
    git config --global user.name "travis-ci"
    git config --global push.default simple
    git clone --quiet https://${GH_TOKEN}@github.com/FXMisc/fxmisc.github.io fxmisc

    cd fxmisc
    JAVADOC_DIR=./flowless/javadoc/$VERSION
    git rm -rf $JAVADOC_DIR
    cp -Rf ../build/docs/javadoc $JAVADOC_DIR
    git add -f $JAVADOC_DIR
    git commit -m "Javadoc for Flowless v$VERSION"
    git push

  fi

fi
