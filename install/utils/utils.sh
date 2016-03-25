#!/bin/bash

#
# This is a helper script used by MacOS and Linux scripts.
# On Linux, call this script like this:
#
# > LINUX=true ./utils.sh [options]
#
# It provides these functions:
#
# + Find suitable Java installation and set env JAVA
# + Start launcher with java found executable
# + Create an App Bundle for mac
#
# Various global variables can be configured to be fixed 
# (see below).
#
# -homac




DEBUG=false
if [ -z "$LINUX" ] ; then LINUX=false ; fi


#
# ARCH
#
# Specifies the architecture type.
# Permitted values are amd64, x86, x86_64, IA64, etc.
# For more info refer to manpage of java_home (Xcode).
# You can set a fixed value like this:
# ARCH="x86_64"
ARCH=

#
# HERE YOU CAN SPECIFY YOUR JAVA EXECUTABLE MANUALLY
#
# The launcher needs an Oracle JVM 1.7+ (http://java.oracle.com)!
#
# Leave it empty to let the script find a suitable Java.
#
# Example MacOS:
# JAVA="/Library/Java/JavaVirtualMachines/jdk1.8.0_72.jdk/Contents/Home/jre/bin/java"
#
# Example Linux:
# JAVA="/usr/lib/jvm/java-8-oracle/bin/java"
# 
JAVA=

#
# JAVA_LIBPATH
# ============
# Standard installation path for JVMs
#
if $LINUX ; then
	JAVA_LIBPATH="/usr/lib/jvm"
else
	# macos
	JAVA_LIBPATH="/Library/Java/JavaVirtualMachines"
fi

#
# Current location of this script in user's file system.
#
SOURCE="$( cd "$( dirname "${BASH_SOURCE[0]}" )" && pwd )"


#
# Path to unzipped folder.
#
TARGET=`dirname "$SOURCE"`

#
# Path to desktop folder.
#
DESKTOP="$HOME/Desktop"

#
# LIBEXEC_JAVA_HOME
#
# Path to java_home executable (part of Xcode).
# 
LIBEXEC_JAVA_HOME=

#
# Whether to use java_home (s.a.) or not
#
HAVE_LIBEXEC=false

#
# database of locate for Linux and MacOS
#
if $LINUX ; then
	LOCATEDB="/var/lib/mlocate/mlocate.db"
else
	LOCATEDB="/var/db/locate.database"
fi


function log () {
	msg="$1"
	if $DEBUG ; then
		echo "info: $msg"
	fi
}

# immediately aborts with error code -1 (255)
function abort () {
	echo "abort."
	exit -1
}	
trap abort ERR

function error_exit () {
	echo "error: $1" 1>&2
	abort
}

#
# On 64bit systems, this function tests whether the 
# give java executable (param 1) supports 64bit 
#
function java_arch_supported () {
	exe="$1"
	if ${ARCH64} ; then
		is64bit=`"${exe}" -version 2>&1 | awk '/.* 64-Bit .*/{ print "true" }'`
		if [ -n "${is64bit}" ] ; then
			return 0
		else
			return -1
		fi
	else
		return 0
	fi
}

#
# Returns the version part of java -version
#
function java_version () {
	exe="$1"
	echo `"${exe}" -version 2>&1 | awk '/^java version \"[0-9]+\.[0-9]+\.[0-9]+(_[0-9]+)?\"$/{  gsub ("\"","") ; print $3 }'`
}

#
# Compares to versions (param:v1 and param:v2)
# and tests if (v1 >= v2).
# Function returns with -1 (error) if (v1 < v2).
# Function returns with 0 (success) if (v1 >= v2).
#
function java_version_ge () {

	# no trap here!

	ver1="$1"
	ver2="$2"

	echo "${ver1} ${ver2}" 	| awk '{\
		v1_len=split ( $1, v1, "[^0-9]" ) ;\
		v2_len=split ( $2, v2, "[^0-9]" ) ;\
		\
		if ( v1[1] > v2[1] ) {\
			exit 0 ;\
		} else if ( v1[1] == v2[1] ) {\
			if ( v1[2] > v2[2] ) {\
				exit 0 ;\
			} else if ( v1[2] == v2[2] ) {\
				if ( v1[3] > v2[3] ) {\
					exit 0 ;\
				} else if ( v1[3] == v2[3] ) {\
					if ( v1[4] >= v2[4] ) {\
						exit 0 ;\
					}\
				}\
			}\
		}\
		exit -1 ;\
	}'
	
	# no more commands here!

	# exit value of awk is used as return value of function.
}

#
# Tests based on java -version whether the vendor is
# NOT Oracle. However, Apple Java has the same output
# as Oracle Java, but Apple stopped Java development
# at 1.6. Since we are using 1.7+, we won't have a 
# problem here.
#
function java_vendor_not_oracle () {
	exe="$1"
	if "${exe}" -version 2>&1 | awk '/.*OpenJDK.*/{ exit -1 }' ; then
		return -1 ;
	else
		return 0 ;
	fi
}

#
# Function searches for the newest java executable which 
# 1. is of version 1.7 or higher and
# 2. is suitable for the given architecture (32/64bit)
#
# It searches using different methods:
# + which java
# + locate */bin/java
# + find <JAVA_LIBPATH> -path "*/bin/java"
#
# Only the best executable will be returned or none.
#
function search_java_home () {
	#
	# create a list of all available java executables
	#
	list=`mktemp /tmp/litwrl-install-java-list.XXXXXX`
	javaexe=`mktemp /tmp/litwrl-install-javaexe.XXXXXX`
	
	# use 'which'
	which java > ${list}

	# use 'locate'
	LOCATE="`which locate`"
	if [ -x "${LOCATE}" ] && [ -r "${LOCATEDB}" ] ; then
		locate "*/bin/java" >> "${list}"
	else
		log "can't execute locate (${LOCATE})"
	fi
	
	# use 'find'
	if [ -d "${JAVA_LIBPATH}" ] ; then
		find "${JAVA_LIBPATH}" -path "*/bin/java" >> "${list}"
	else
		log "omitting missing dir: ${JAVA_LIBPATH}"
	fi
	# 
	# select the best
	#
	current_version="1.7.0"
	cat "${list}" | sort | while read exe ; do
		if [ "${previous}" == "${exe}" ] || ! [ -x "${exe}" ] ; then
			# ignore duplicates
			continue ;
		fi
		
		log "checking: ${exe}" ;
		v=`java_version ${exe}` ;
		
		if [ -z "$v" ] ; then continue ; fi
		
		if ! java_vendor_not_oracle "${exe}" ; then
			# apple java 1.6 will be filtered in version check
			if java_arch_supported "${exe}" ; then
				log "arch supported" ;
				if java_version_ge "$v" "${current_version}" ; then
					log "$v >= ${current_version}"
					echo "${exe}" > "${javaexe}";
					current_version="$v" ;
				else
					log "$v < ${current_version}" ;
				fi
			fi
		fi
		previous="${exe}" ;
	done
	if [ -e "${javaexe}" ] ; then
		JAVA=`cat "${javaexe}"`
	else
		JAVA=
	fi
	rm -f "${list}" ;
	rm -f "${javaexe}" ;
}

#
# Try to find suitable java installation using either java_home (Xcode)
# or function search_java_home.
#
function find_java () {
	trap abort ERR
	
	if [ "${JAVA}" ] ; then
		return
	fi
	if ${HAVE_LIBEXEC} ; then
		log "exec: java_home --failfast --version \"1.7+\" --arch \"${ARCH}\""
		
		JAVA_HOME=`java_home --failfast --version "1.7+" --arch "${ARCH}"`
		JAVA="${JAVA_HOME}/bin/java"
	else
		search_java_home
	fi
	
	
	if [ -z "${JAVA}" ] ; then
		error_exit "could not find suitable Java installation."
	else
		log "found: ${JAVA}"
	fi
}

#
# Initialise global variables.
#
function init () {
	trap abort ERR
	
	if [ -z "${ARCH}" ] ; then
		ARCH=`uname -m`
	fi
	
	if [ -z "${ARCH64}" ] ; then
		# it is [amd64|x86_64|IA64]
		ARCH64=`echo ${ARCH} | awk '/.*64.*/{ print "true" ; }!/.*64.*/{ print "false" }'`
	fi
		
	if ${ARCH64} ; then
		log "running on 64bit arch"
	fi

	if [ -z "${HAVE_LIBEXEC}" ] || ${HAVE_LIBEXEC} ; then
		if [ -z "${LIBEXEC_JAVA_HOME}" ] ; then
			if [ -e "/usr/libexec/java_home" ] ; then
				LIBEXEC_JAVA_HOME="/usr/libexec/java_home"
			else
				LIBEXEC_JAVA_HOME="`which java_home | awk '/^\/.*java_home$/{ print $0 }'`"
			fi
		fi
		
		if [ -x "${LIBEXEC_JAVA_HOME}" ] ; then
			HAVE_LIBEXEC=false
		fi
	fi		
	log "have libexec: ${HAVE_LIBEXEC}"

	find_java
}

#
# start launcher. 
# 
# Function accepts a single, optional parameter
# to specify the jar file to be started.
#
function run () {
	trap abort ERR
	
	init
	
	if [ $# -eq 1 ] ; then
		JAR="$1"
	elif [ -e "${SOURCE}/litwrl.jar" ] ; then
		JAR="${SOURCE}/litwrl.jar"
	else
		JAR="${TARGET}/litwrl.jar"
	fi
	
	log "exec: ${JAVA} -jar ${JAR}"
	"${JAVA}" -jar "${JAR}"
}

#
# Create App Bundle for MacOS.
#
function install () {
	trap abort ERR
	
	init
	
	
	while [ -z "$answer" ] ; do
cat <<EOF
******************************************************************************
*   This script will create an App Bundle on your desktop.
*   
*   Do you want to proceed?
*
*   Type (y)es or (n)o and press RETURN.
******************************************************************************
EOF
		echo -n "> "
		read answer
		case "$answer" in
		( yes | y ) break ;;
		( no  | n ) exit 0 ;;
		( * ) unset answer ;;
		esac
	done
	
	# create app folder
	APPNAME="LifeInTheWoodsRenaissanceLauncher"
	DIR="${DESKTOP}/${APPNAME}.app/Contents/MacOS"
	RES="${DESKTOP}/${APPNAME}.app/Contents/Resources"
	
	mkdir -p "$DIR"
	cp "$TARGET/mac/run.command" "$DIR/$APPNAME"
	chmod +x "$DIR/$APPNAME"

	cp "$TARGET/utils/utils.sh" "$DIR"
	cp "$TARGET/litwrl.jar" "$DIR"
	
	mkdir -p "$RES"
	cp "$TARGET/utils/appicon.icns" "$RES/$APPNAME.icns"
	
	
	INFOPLIST="${DESKTOP}/${APPNAME}.app/Contents/Info.plist"
	cp "$TARGET/mac/Info.plist.xml" "$INFOPLIST"

cat <<EOF
******************************************************************************
*   Installation finished.
*
*   Created bundle:
*       ${DESKTOP}/${APPNAME}.app
*   
*   You can now move the bundle to any location you like.
*
*   Press RETURN to exit the shell.
******************************************************************************
EOF
	echo -n "> "
	read
}

if [ "$1" ] ; then
	case "$1" in 
	( --run ) shift ; run "$@" ;;
	( --install ) install ;;
	( --find-java ) find_java && export JAVA ;;
	( * ) error_exit "unknown command $1" ;;
	esac
else
	install
fi

