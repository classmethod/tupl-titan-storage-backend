#!/bin/bash

#
# Copyright 2016 Classmethod, Inc. or its affiliates. All Rights Reserved.
# Portions copyright 2014-2015 Amazon.com, Inc. or its affiliates. All Rights Reserved.
#
# Licensed under the Apache License, Version 2.0 (the "License").
# You may not use this file except in compliance with the License.
# A copy of the License is located at
#
# http://aws.amazon.com/apache2.0
#
# or in the "license" file accompanying this file. This file is distributed
# on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either
# express or implied. See the License for the specific language governing
# permissions and limitations under the License.
#

# BEGIN adaptation of:
# https://github.com/awslabs/dynamodb-titan-storage-backend/blob/1.0.0/src/test/resources/install-gremlin-server.sh

#collect the prereqs and build the plugin
mvn clean
mvn install

export ARTIFACT_NAME=`mvn org.apache.maven.plugins:maven-help-plugin:2.2:evaluate -Dexpression=project.artifactId | grep -v "INFO\|Downloading\|Downloaded"`
export TITAN_TUPL_HOME=${PWD}
export TITAN_TUPL_TARGET=${TITAN_TUPL_HOME}/target
export TITAN_VERSION=`mvn org.apache.maven.plugins:maven-help-plugin:2.2:evaluate -Dexpression=titan.version | grep -v "INFO\|Downloading\|Downloaded"`
export TUPL_PLUGIN_VERSION=`mvn org.apache.maven.plugins:maven-help-plugin:2.2:evaluate -Dexpression=project.version | grep -v "INFO\|Downloading\|Downloaded"`
export TITAN_VANILLA_SERVER_DIRNAME=titan-${TITAN_VERSION}-hadoop1
export TITAN_VANILLA_SERVER_ZIP=${TITAN_VANILLA_SERVER_DIRNAME}.zip
export TITAN_TUPL_SERVER_DIRNAME=${ARTIFACT_NAME}-${TUPL_PLUGIN_VERSION}-hadoop1
export TITAN_SERVER_HOME=${TITAN_TUPL_HOME}/server/${TITAN_TUPL_SERVER_DIRNAME}
export TITAN_TUPL_SERVER_ZIP=${TITAN_TUPL_SERVER_DIRNAME}.zip
export TITAN_SERVER_CONF=${TITAN_SERVER_HOME}/conf
export TITAN_GREMLIN_SERVER_CONF=${TITAN_SERVER_CONF}/gremlin-server
export TITAN_SERVER_BIN=${TITAN_SERVER_HOME}/bin
export TITAN_TUPL_EXT_DIR=${TITAN_SERVER_HOME}/ext/${ARTIFACT_NAME}
export TITAN_SERVER_YAML=${TITAN_GREMLIN_SERVER_CONF}/gremlin-server.yaml
export TITAN_SERVER_TUPL_PROPERTIES=${TITAN_GREMLIN_SERVER_CONF}/tupl.properties
export TITAN_TUPL_TEST_RESOURCES=${TITAN_TUPL_HOME}/src/test/resources
export TITAN_SERVER_SERVICE_SH=${TITAN_SERVER_BIN}/gremlin-server-service.sh

#download the server products
mkdir -p ${TITAN_TUPL_HOME}/server
pushd ${TITAN_TUPL_HOME}/server

#curl -s -O http://s3.thinkaurelius.com/downloads/titan/${TITAN_VANILLA_SERVER_ZIP}
if [ -e "${TITAN_TUPL_HOME}/${TITAN_VANILLA_SERVER_ZIP}" ]; then
    cp ${TITAN_TUPL_HOME}/${TITAN_VANILLA_SERVER_ZIP} .
else
    curl -s -O http://s3.thinkaurelius.com/downloads/titan/${TITAN_VANILLA_SERVER_ZIP} .
fi
curl -s -O http://s3.thinkaurelius.com/downloads/titan/${TITAN_VANILLA_SERVER_ZIP}.asc .
#Dan LaRoque signed the zip
gpg --keyserver x-hkp://pool.sks-keyservers.net --recv-keys 0xD98DF872617B3B80
gpg --verify ${TITAN_VANILLA_SERVER_ZIP}.asc ${TITAN_VANILLA_SERVER_ZIP} || { echo "signature did not match"; exit 1; }

#unpack
unzip -qq ${TITAN_VANILLA_SERVER_ZIP} -d ${TITAN_TUPL_HOME}/server
mv ${TITAN_VANILLA_SERVER_DIRNAME} ${TITAN_TUPL_SERVER_DIRNAME}
#rm ${TITAN_VANILLA_SERVER_ZIP}

#load extra dependencies
mkdir -p ${TITAN_TUPL_EXT_DIR}
cp ${TITAN_TUPL_TARGET}/${ARTIFACT_NAME}-${TUPL_PLUGIN_VERSION}.jar ${TITAN_TUPL_EXT_DIR}
cp -R ${TITAN_TUPL_TARGET}/dependencies/*.* ${TITAN_TUPL_EXT_DIR}

#copy over tupl configuration
cp ${TITAN_TUPL_TEST_RESOURCES}/gremlin-server.yaml ${TITAN_SERVER_YAML}
cp ${TITAN_TUPL_TEST_RESOURCES}/tupl.properties ${TITAN_SERVER_TUPL_PROPERTIES}
cp ${TITAN_TUPL_TEST_RESOURCES}/gremlin-server-service.sh ${TITAN_SERVER_SERVICE_SH}

#show how to call the startup script
echo ""
echo "Change directories to the server root:"
echo "cd server/${TITAN_TUPL_SERVER_DIRNAME}"
echo ""
echo "Start Gremlin Server against Tupl with the following command:"
echo "bin/gremlin-server.sh ${TITAN_SERVER_YAML}"
echo ""
echo "Connect to Gremlin Server using the Gremlin console:"
echo "bin/gremlin.sh"
echo ""
echo "Connect to the graph on Gremlin Server:"
echo ":remote connect tinkerpop.server conf/remote.yaml"

#repackage the server
zip -rq ${TITAN_TUPL_SERVER_ZIP} ${TITAN_TUPL_SERVER_DIRNAME}
popd

# END adaptation of:
# https://github.com/awslabs/dynamodb-titan-storage-backend/blob/1.0.0/src/test/resources/install-gremlin-server.sh
