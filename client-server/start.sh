#!/bin/bash

DIR="$( cd "$( dirname "$0" )" && pwd )"
JAR_PATH="$DIR/conf/:$DIR/build/libs/client-server-1.0-SNAPSHOT.jar"
MACHINE_LIST="$DIR/conf/machine_list"
CLIENT_PROPERTIES="$DIR/conf/client.properties"

function prop {
    grep "${1}" ${CLIENT_PROPERTIES}|cut -d'=' -f2
}

LINES=`find . -name "*.java" -print | xargs wc -l | grep "total" | awk '{$1=$1};1'`
echo Project has "$LINES" lines

gradle clean; gradle build

# Launch clients

clients:

NUM_CLIENTS=1

if [[ $# -gt 0 ]]; then
    NUM_CLIENTS=$1
fi

SCRIPT="java -cp $JAR_PATH distributed.client.node.Client"
COMMAND='gnome-terminal --geometry=200x40'
NUM_MACHINES=`wc -l < ${MACHINE_LIST}`

k=1

for i in $(seq 1 $NUM_CLIENTS)
do
    n=$(($i%$NUM_MACHINES + 1))
    machine="$(sed -n ${n}'p' < $MACHINE_LIST)"

    echo 'logging into '$machine
    
    OPTION='--tab -t "'$machine'" -e "ssh -t '$machine' cd '$DIR'; sleep '$k'; echo '$SCRIPT'; '$SCRIPT'"'
    COMMAND+=" $OPTION"
    
    k=`echo $k + 1.50 | bc`
done

eval $COMMAND &
