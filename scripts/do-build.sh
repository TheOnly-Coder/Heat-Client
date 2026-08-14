#!/bin/bash
export JAVA_HOME=/home/z/.forge-build/jdk8
export PATH=$JAVA_HOME/bin:$PATH
PROJECT=/home/z/my-project/heat-client
GRADLE=/home/z/.forge-build/gradle-2.14/bin/gradle
LOG=/home/z/my-project/heat-client/build.log

echo "START $(date)" > $LOG

echo "=== setupDecompWorkspace ===" >> $LOG 2>&1
cd $PROJECT && $GRADLE setupDecompWorkspace --no-daemon --console=plain >> $LOG 2>&1
RC1=$?
echo "SETUP_EXIT=$RC1" >> $LOG

if [ $RC1 -eq 0 ]; then
  echo "=== build ===" >> $LOG 2>&1
  cd $PROJECT && $GRADLE build --no-daemon --console=plain >> $LOG 2>&1
  RC2=$?
  echo "BUILD_EXIT=$RC2" >> $LOG
fi

echo "END $(date)" >> $LOG
