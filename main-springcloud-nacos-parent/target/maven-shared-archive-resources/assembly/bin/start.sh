#!/bin/sh
#获取脚本名称
script=$(readlink -f "$0")
#获取脚本绝对路径
scriptPath=$(dirname "$script")

sh $scriptPath/boot.sh start $@
