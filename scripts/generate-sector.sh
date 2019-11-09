#!/bin/sh
# Generate random file of size determined 

APPLICATION_PROPERTIES="$PWD/../application-server/conf/application.properties"

function prop {
	grep "${1}" ${APPLICATION_PROPERTIES}|cut -d'=' -f2
}

SECTOR_SIZE=$(prop "sector.boundary.size")
FILENAME=$(prop "hdfs.file.location")

if [ -z "$SECTOR_SIZE" ]; then
	echo "ERROR: could not load sector.boundary.size from $APPLICATION_PROPERTIES"
	exit 1
fi

if [ -z "$FILENAME" ]; then
	echo "ERROR: could not load hdfs.file.location from $APPLICATION_PROPERTIES"
	exit 1
fi

DIRNAME=$(dirname $FILENAME)
TMP_FILE=$(mktemp -u)

# total file size is square of "sector.size"
FILE_SIZE=$(echo "${SECTOR_SIZE}^2" | bc)

# generate temporary file of specified size
openssl rand -out "${TMP_FILE}" "${FILE_SIZE}"

# make sure directory exists
$HADOOP_HOME/bin/hdfs dfs -mkdir -p "${DIRNAME}"

# store file to hdfs
$HADOOP_HOME/bin/hdfs dfs -put "${TMP_FILE}" "${FILENAME}"

# cleanup tmp
rm "${TMP_FILE}"
