#!/bin/bash
#
###################################################################
# 2019 #
###################################################################
#
# Run script for Linux cluster.
#
# Configure the 'conf/application.properties' to specify the host for
# Discovery and the Store node. Configure the 'conf/machine_list' to
# specify the number of servers to join.

# Configurations

DIR="$( cd "$( dirname "$0" )" && pwd )"
JAR_PATH="$DIR/conf/:$DIR/build/libs/application-server-1.0-SNAPSHOT.jar"
MACHINE_LIST="$DIR/conf/machine_list"
APPLICATION_PROPERTIES="$DIR/conf/application.properties"

function usage {
cat << EOF
    
    script usage: $( basename $0 ) [-o operation] [-a num servers]

    -o operation   : 'execute' or 'compile' and exit
    -a num servers : integer number of application servers to start - must be > 0
    
EOF
    exit 1
}

function prop {
    grep "${1}" ${APPLICATION_PROPERTIES}|cut -d'=' -f2
}

NUM_SERVERS=1
OPERATION="compile"
EXIT=0

while getopts o:a: option
do
    case "${option}" in
        o) OPERATION=${OPTARG}
        if [ "$OPERATION" = "compile" ]
        then
            EXIT=1
        fi
        ;;
        a) NUM_SERVERS=${OPTARG};;
        ?) usage;;
    esac
done

# Compile

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

# Launch Switch

gnome-terminal --geometry=170x50 -t "Switch" -e "ssh -t $(prop 'switch.host') cd '$DIR'; 'java -cp $JAR_PATH distributed.application.node.Switch; bash;'"

# Launch servers

sleep 3

SCRIPT="java -cp $JAR_PATH distributed.application.node.Server"
COMMAND='gnome-terminal --geometry=200x40'
NUM_MACHINES=`wc -l < ${MACHINE_LIST}`

if (( NUM_SERVERS > NUM_MACHINES ))
then
    NUM_SERVERS=$NUM_MACHINES
    echo 'resetting num servers to number of machines available:' $NUM_SERVERS
fi

for i in $(seq 1 $NUM_SERVERS)
do
    n=$((i % NUM_MACHINES + 1))
    machine="$(sed -n ${n}'p' < $MACHINE_LIST)"

    title=server-$machine
    
    echo 'logging into '$machine
        
    OPTION='--tab -t "'$title'" -e "ssh -t '$machine' cd '$DIR'; echo '$SCRIPT'; '$SCRIPT'"'
    COMMAND+=" $OPTION"
done

eval $COMMAND &
