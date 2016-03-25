#!/bin/bash

#
# This is the installation script for MacOS. It creates 
# an App Bundle on the user's desktop.
#
# Most functionality can be found in the utils.sh script.
# The utils.sh script also allows to configure various 
# parameters.
#
# -homac


SOURCE="$( cd "$( dirname "${BASH_SOURCE[0]}" )" && pwd )"
TARGET=`dirname "$SOURCE"`

DIR="${TARGET}"

/bin/bash -e ${DIR}/utils/utils.sh --install

