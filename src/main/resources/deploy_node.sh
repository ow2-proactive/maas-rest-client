#!/usr/bin/env bash

NODE_JAR_URL="http://192.168.1.136:8080/rest/node.jar"
RM_HOSTNAME="192.168.1.136"
INSTANCE_ID="423c70a4-7d6d-7e35-5884-854d1f84f052"
NODE_NAME="NewNode"
PNP_ACCESS="pnp://192.168.1.136:64738/"

wget -nv ${NODE_JAR_URL}/rest/node.jar
nohup java -jar node.jar -Dproactive.communication.protocol=pnp -Dproactive.pamr.router.address=${RM_HOSTNAME} -DinstanceId=${INSTANCE_ID} -Dproactive.useIPaddress=true -r ${PNP_ACCESS} -s ${NODE_NAME} -w 1  &