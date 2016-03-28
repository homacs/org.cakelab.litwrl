#!/bin/bash

#
# This script creates a desktop shortcut in the unzipped folder.
# 



#
# Location of the script
#
SOURCE="$( cd "$( dirname "${BASH_SOURCE[0]}" )" && pwd )"


#
# Location of the unzipped folder with litwrl.jar.
#
TARGET=`dirname "$SOURCE"`


#
# Name of the application to be used in the desktop link.
#
APP_NAME="Life in the Woods Renaissance"


# make sure, we have a terminal to interact with user.
if ! tty >/dev/null 2>&1 ; then
	if [ "${RECURSIVE_CALL}" ] ; then
		# still no tty .. then go ahead ..
		echo "can you read this?"
	else
	    # have no tty -> starting over with xterm
		export RECURSIVE_CALL=true
		xterm -e /bin/bash -e "${BASH_SOURCE[0]}"
	fi
fi


echo ""

# calling utils.sh to determine required java installation.
LINUX=true
source $TARGET/utils/utils.sh --find-java

if ! [ -x "$JAVA" ] ; then 
	echo ""
	echo "ERROR: Can't find java executable. " 1>&2
	echo "       Either you need to install Oracle's Java 1.7+ (http://java.oracle.com)" 1>&2
	echo "       or edit this script to point to your java executable." 1>&2
	exit -1
else

#
# Create desktop link
#
cat > "$TARGET/$APP_NAME.desktop" <<EOF
[Desktop Entry]
Version=1.0
Type=Application
Name=$APP_NAME
Categories=Game;
Comment=Starts the launcher of $APP_NAME
Exec=$TARGET/utils/utils.sh --run "$TARGET/litwrl.jar"
Icon=$TARGET/utils/appicon.png
Path=$TARGET
Terminal=false
StartupNotify=false
EOF

chmod a+x "$TARGET/$APP_NAME.desktop"
cat <<EOF
********************************************************************************
*                                                                              *
* Installation finished.                                                       *
*                                                                              *
*                        Created desktop link at:                              *
* '$TARGET/$APP_NAME.desktop'
*                                                                              *
* Move the desktop link to any location you want (e.g. on your desktop or in   *
* your menu folder). If you move this folder, you need to execute the install  *
* script again to create an updated desktop link.                              *
*                                                                              *
* Press RETURN to exit.                                                        *
********************************************************************************
EOF

fi

echo -n "> "
read
