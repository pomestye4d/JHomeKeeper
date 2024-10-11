#!/bin/bash
cd ${rootDir}

x=''

getPid()
{
    x=$(ps aux|grep "home-keeper-tag" | grep java |  awk '{print $2}' | head -1)
}

getPid
if [ ! "$x" ];then
   echo "application is not running"
   exit 0
fi

kill $x
for i in {1..5}
do
   sleep 2
   getPid
   if [ ! "$x" ];then
    echo "application was stopped"
    exit 0
   fi
done

kill -9 $x
echo "application was killed"