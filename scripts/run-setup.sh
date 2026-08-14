#!/bin/bash
export JAVA_HOME=$HOME/.forge-build/jdk8
export PATH=$JAVA_HOME/bin:$PATH
cd /home/z/my-project/heat-client
$HOME/.forge-build/gradle-2.14/bin/gradle setupDecompWorkspace --no-daemon --console=plain > /home/z/my-project/heat-client/setup.log 2>&1
echo "EXIT_CODE=$?" >> /home/z/my-project/heat-client/setup.log
