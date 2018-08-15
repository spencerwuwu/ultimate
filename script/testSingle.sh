#!/usr/bin/bash

Based_dir="/home/ultimate/ultimate/releaseScripts/default/UTaipan-linux"
Ultimate="$Based_dir/Ultimate"
Config="$Based_dir/config/Termination.xml"
file=$1
$Ultimate -tc $Config -i $file | grep "RESULT"
