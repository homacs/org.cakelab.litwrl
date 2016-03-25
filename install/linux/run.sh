#!/bin/bash


#
# This script was added for your convenience. It determines
# a suitable java installation and starts the launcher.
#
# You can specify a fixed location below.
#





#
# HERE YOU CAN SPECIFY YOUR JAVA EXECUTABLE MANUALLY
#
# The launcher needs an Oracle JVM (http://java.oracle.com)!
#
# Leave it empty to use the system default.
#
# Example:
# JAVA="/usr/lib/jvm/java-7-oracle/bin/java"
# 
JAVA=

# DO NOT EDIT THOSE 2 LINES
SOURCE="$( cd "$( dirname "${BASH_SOURCE[0]}" )" && pwd )"
# END OF DO NOT EDIT

TARGET=`dirname "$SOURCE"`

APP_NAME="Life in the Woods Renaissance"

echo ""

if [ -z "$JAVA" ] ; then
	LINUX=true
	source $TARGET/utils/utils.sh --find-java
fi

if ! [ -x "$JAVA" ] ; then 
	echo ""
	echo "ERROR: Can't find java executable. " 1>&2
	echo "       Either you need to install Oracle's Java 1.7+ (http://java.oracle.com)" 1>&2
	echo "       or edit this script to point to your java executable." 1>&2
	exit -1
else
	$JAVA -jar "$TARGET/litwrl.jar"
fi