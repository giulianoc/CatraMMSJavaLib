#!/bin/bash

mvn --projects catraMMSLib clean compile install

echo "mvn install"
mvn install:install-file -Dfile=catraMMSLib/target/catraMMSLib.jar -DgroupId=com.catra -DartifactId=catraMMSLib -Dversion=1.0.0-SNAPSHOT -Dpackaging=jar
if [ $? -ne 0 ]; then
  exit 1
fi

cp catraMMSLib/target/catraMMSLib.jar ../cibortv/libs
cp catraMMSLib/target/catraMMSLib.jar ../catrammswebservices/libs

echo "Delivery: ./catraMMSLib/target/catraMMSLib.jar"

echo "In case of push: git push https://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@github.com/giulianoc/CatraMMSJavaLib.git"

exit 0

