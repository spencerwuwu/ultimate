#!/usr/bin/bash

#Based_dir="/home/ultimate/ultimate/releaseScripts/default/UTaipan-linux"
Based_dir="/home/ultimate/origin-ultimate/releaseScripts/default/UTaipan-linux"
Ultimate="$Based_dir/Ultimate"
Config="/home/ultimate/ultimate/script/Termination.xml"
file=$1
$Ultimate -tc $Config -i $file | egrep "RESULT|AUTO"
