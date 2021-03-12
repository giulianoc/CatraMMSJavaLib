#!/bin/bash

mvn --projects catraMMSLib clean compile install
mvn install:install-file -Dfile=catraMMSLib/target/catraMMSLib.jar -DgroupId=com.catra -DartifactId=catraMMSLib -Dversion=1.0.0-SNAPSHOT -Dpackaging=jar
if [ $? -ne 0 ]; then
  exit 1
fi


echo "Delivery: ./catraMMSLib/target/catraMMSLib.jar"

exit 0

