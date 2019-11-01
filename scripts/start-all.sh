#!/bin/bash
#
# Compile and start each of the applications. Add run arguments to
# start X number of clients, e.g., ./start-all.sh 5
#

DIR="$( cd "$( dirname "$0" )" && pwd )"

echo 'STARTING APPLICATION SERVER'
pushd "$DIR/../application-server/"
bash start.sh
popd

sleep 1

echo 'STARTING CLIENTS'
pushd "$DIR/../client-server/"
bash start.sh $@
popd
