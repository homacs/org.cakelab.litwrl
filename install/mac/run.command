#!/bin/bash


#
# This is the run script for MacOS. 
#
# It calls the utils.sh script to delegate finding of
# a suitable Java installation and start of the launcher.
# The utils.sh script also allows to configure various 
# parameters.
#
# -homac


SOURCE="$( cd "$( dirname "${BASH_SOURCE[0]}" )" && pwd )"


/bin/bash -e ${SOURCE}/utils.sh --run

