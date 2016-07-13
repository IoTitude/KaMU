#!/bin/bash

wget https://github.com/IoTitude/KaMU/archive/0.5.4.tar.gz
tar -xvzf 0.5.4.tar.gz
rm 0.5.4.tar.gz



cd KaMU-0.5.4/KaMU/
cd src/kamu

javac -d ../../out -classpath "../../imports/:../../imports/cassandra-driver-core-3.0.0.jar:../../imports/json-20160212.jar:../../imports/kaa-java-ep-sdk-oCN2bIAl3IdG13wybyBrBNlQTqs.jar:../../imports/pi4j-core.jar:../../imports/slf4j-simple-1.7.21.jar:../../imports/guava-16.0.1.jar:../../imports/netty-common-4.0.33.Final.jar:../../imports/netty-transport-4.0.33.Final.jar:../../imports/metrics-core-3.1.2.jar:../../imports/netty-buffer-4.0.33.Final.jar:../../imports/netty-codec-4.0.33.Final.jar:../../imports/netty-handler-4.0.33.Final.jar" *.java

cd ../../out

jar cmf MANIFEST.txt ../test/KaMU.jar kamu

cp -avr ../dist/lib/ ../test

java -jar ../test/KaMU.jar


