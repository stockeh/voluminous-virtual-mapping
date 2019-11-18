#!/bin/bash

DIR="$( cd "$( dirname "$0" )" && pwd )"
JAR_PATH="$DIR/conf/:$DIR/build/libs/client-server-1.0-SNAPSHOT.jar"
MACHINE_LIST="$DIR/conf/machine_list"
CLIENT_PROPERTIES="$DIR/conf/client.properties"

# Functions

function usage {
cat << EOF
    
    script usage: $( basename $0 ) [-o operation] [-n num clients] [-s sector] [-p position]

    -o operation   : 'execute' or 'compile' and exit
    -n num clients : integer number of clients to start in configuration
    -s sector      : r,c coordinate of sector w/o spaces
    -p position    : r,c coordinate of position w/o spaces
    
EOF
    exit 1
}

function prop {
    grep "${1}" ${CLIENT_PROPERTIES}|cut -d'=' -f2
} 

# Entry configurations

NUM_CLIENTS=1
OPERATION="compile"
EXIT=0

while getopts o:n:s:p: option
do
    case "${option}" in
        o) OPERATION=${OPTARG}
        if [ "$OPERATION" = "compile" ]
        then
            EXIT=1
        fi
        ;;
        n) NUM_CLIENTS=${OPTARG};;
        s) SECTOR=${OPTARG};;
        p) POSITION=${OPTARG};;
        ?) usage;;
    esac
done

if [ "$OPERATION" = "compile" ]
then
    
    LINES=`find . -name "*.java" -print | xargs wc -l | grep "total" | awk '{$1=$1};1'`
    echo Project has "$LINES" lines

    gradle clean; gradle build
    
    if [ $EXIT == 1 ]
    then
        exit
    fi
fi

SCRIPT="java -cp $JAR_PATH distributed.client.node.Client $SECTOR $POSITION"
COMMAND='gnome-terminal --geometry=200x40'
NUM_MACHINES=`wc -l < ${MACHINE_LIST}`

k=1

for i in $(seq 1 $NUM_CLIENTS)
do
    ((i+=RANDOM))
    n=$((i % NUM_MACHINES + 1))
    machine="$(sed -n ${n}'p' < $MACHINE_LIST)"

    echo 'logging into '$machine
    
    title=client-$machine-$SECTOR
    
    OPTION='--tab -t "'$title'" -e "ssh -t '$machine' cd '$DIR'; sleep '$k'; echo '$SCRIPT'; '$SCRIPT'"'
    COMMAND+=" $OPTION"
    
    k=`echo $k + 1.50 | bc`
done

eval $COMMAND &
