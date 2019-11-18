#!/bin/sh
# Generate random file of size determined 

APPLICATION_PROPERTIES="$PWD/../application-server/conf/application.properties"

function usage {
cat << EOF
    
    script usage: $( basename $0 ) [-o operation] [-h help]
    -o operation   : 'random' / 'grid' to specify how the map is formatted.
    -h help        : print the usage message
    
EOF
    exit 1
}

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

echo 'tmp: ' $TMP_FILE ', dir: ' $DIRNAME ', file: ' $FILENAME

OPERATION="random"

while getopts o:h: option
do
    case "${option}" in
        o) OPERATION=${OPTARG};;
        h) usage;;
        ?) usage;;
    esac
done

if [ "${OPERATION}" = "grid" ] ; then

    CENTER=$((SECTOR_SIZE - 2))

    for i in $(seq 1 ${SECTOR_SIZE})
    do
        if (( i == 1 | i == SECTOR_SIZE )); then
            yes '*' | awk '{ printf("%s", $0)}' | dd bs=${SECTOR_SIZE} count=1 >> "${TMP_FILE}" 2>/dev/null
        else
            yes '*' | awk '{ printf("%s", $0)}' | dd bs=1 count=1 >> "${TMP_FILE}" 2>/dev/null
            yes '-' | awk '{ printf("%s", $0)}' | dd bs=${CENTER} count=1>> "${TMP_FILE}" 2>/dev/null
            yes '*' | awk '{ printf("%s", $0)}' | dd bs=1 count=1 >> "${TMP_FILE}" 2>/dev/null
        fi

        if (( i % 100 == 0 )); then
            echo $i '/' ${SECTOR_SIZE}
        fi
    done

else

    # total file size is square of "sector.size"
    FILE_SIZE=$(echo "${SECTOR_SIZE}^2" | bc)

    # generate temporary file with random bytes of specified size
    openssl rand -out "${TMP_FILE}" "${FILE_SIZE}"

fi

# delete file to replace
$HADOOP_HOME/bin/hdfs dfs -rm "${FILENAME}"

# make sure directory exists
$HADOOP_HOME/bin/hdfs dfs -mkdir -p "${DIRNAME}"

# store file to hdfs
$HADOOP_HOME/bin/hdfs dfs -put "${TMP_FILE}" "${FILENAME}"

# cleanup tmp
rm "${TMP_FILE}"
