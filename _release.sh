#!/usr/bin/env bash

echo Starting

mvn clean install

cd dep-ui
mvn clean install assembly:assembly
cd ../

scp ./dep-ear/target/dep-ear.ear ebs-bridge.grnet.gr:apella/
scp ./dep-ui/target/dep-ui-distribution.zip ebs-bridge.grnet.gr:apella/

echo Finished