#!/bin/bash
#
# Compile and start each of the applications
#

DIR="$( cd "$( dirname "$0" )" && pwd )"
APPLICATION_PROPERTIES="$DIR/../application-server/conf/application.properties"

# Functions

function usage {
cat << EOF
    
    script usage: $( basename $0 ) [-c num clients] [-s num sector]

    -c num clients : integer number of clients to start in configuration - must be > s
    -s num sector  : integer number of sectors to start clients in - must be < c
    
EOF
    exit 1
}

function prop {
	grep "${1}" ${APPLICATION_PROPERTIES}|cut -d'=' -f2
}

echo 'STARTING APPLICATION SERVER'
pushd "$DIR/../application-server/"

bash start.sh

popd

sleep 3

# Initialize Clients

NUM_CLIENTS=1
NUM_SECTORS=1
SECTOR_BOUNDARY_SIZE=$(prop "sector.boundary.size")
SECTOR_MAP_SIZE=$(prop "sector.map.size")

while getopts c:s: option
do
    case "${option}" in
        c) NUM_CLIENTS=${OPTARG};;
        s) NUM_SECTORS=${OPTARG};;
        ?) usage;;
    esac
done

# Configure Settings for Client

if (( NUM_SECTORS > SECTOR_MAP_SIZE * SECTOR_MAP_SIZE )); then
    NUM_SECTORS=$(( SECTOR_MAP_SIZE * SECTOR_MAP_SIZE ))
    echo '[-s num sectors] updated to' $NUM_SECTORS
fi

if (( NUM_SECTORS > NUM_CLIENTS )); then
    usage
fi

SECTORS=()
for x in $(seq 1 $SECTOR_MAP_SIZE)
do
    ((--x))
    for y in $(seq 1 $SECTOR_MAP_SIZE)
    do    
        ((--y))
        SECTORS+=($x,$y)
    done
done

SECTORS=($(printf "%s\n" "${SECTORS[@]}" | shuf))

RATIO=$((NUM_CLIENTS / NUM_SECTORS))
REM=$((NUM_CLIENTS % NUM_SECTORS))

echo 'STARTING CLIENTS'
pushd "$DIR/../client-server/"

bash start.sh -o compile

POSITION=$(( SECTOR_BOUNDARY_SIZE / 2 )),$(( SECTOR_BOUNDARY_SIZE / 2 ))
OPERATION="execute"

for s in $(seq 1 $NUM_SECTORS)
do
    CLIENTS=$RATIO
    if (( ((REM--)) > 0 )); then
        ((CLIENTS++))
    fi
    ((s--))
    NEW_SECTOR=${SECTORS[$s]}
    echo sector: $NEW_SECTOR , number of clients in sector: $CLIENTS, at position: $POSITION
    bash start.sh -o $OPERATION -n $CLIENTS -s $NEW_SECTOR -p $POSITION
    
    sleep 1
done

popd
