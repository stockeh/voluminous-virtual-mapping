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

function prop {
    grep "${1}" ${APPLICATION_PROPERTIES}|cut -d'=' -f2
}

function jumpto
{
    label=$1
    cmd=$(sed -n "/$label:/{:a;n;p;ba};" $0 | grep -v ':$')
    eval "$cmd"
    exit
}

# Launch Switch

LINES=`find . -name "*.java" -print | xargs wc -l | grep "total" | awk '{$1=$1};1'`
echo Project has "$LINES" lines

gradle clean; gradle build
gnome-terminal --geometry=170x50 -t "Switch" -e "ssh -t $(prop 'switch.host') cd '$DIR'; 'java -cp $JAR_PATH distributed.application.node.Switch; bash;'"

# Launch servers
sleep 3
server:

SCRIPT="java -cp $JAR_PATH distributed.application.node.Server"

COMMAND='gnome-terminal --geometry=200x40'
for machine in `cat $MACHINE_LIST`
do
    echo 'logging into '$machine
    
    OPTION='--tab -t "'$machine'" -e "ssh -t '$machine' cd '$DIR'; echo '$SCRIPT'; '$SCRIPT'"'
    COMMAND+=" $OPTION"
done

eval $COMMAND &
