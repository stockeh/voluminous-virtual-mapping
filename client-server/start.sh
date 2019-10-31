#!/usr/bin/env bash
DIR="$( cd "$( dirname "$0" )" && pwd )"
JAR_PATH="$DIR/conf/:$DIR/build/libs/client-server-1.0-SNAPSHOT.jar"
CLIENT_PROPERTIES="$DIR/conf/client.properties"

function prop {
    grep "${1}" ${CLIENT_PROPERTIES}|cut -d'=' -f2
}

LINES=`find . -name "*.java" -print | xargs wc -l | grep "total" | awk '{$1=$1};1'`
echo Project has "$LINES" lines

gradle clean; gradle build


gnome-terminal --geometry=170x50 -t "Client" -e "ssh -t $(prop 'client.host') cd '$DIR'; 'java -cp $JAR_PATH distributed.client.node.Client; bash;'"

