#!/bin/sh
# 脚本使用方式
# 启动： sh xxxx.sh start'启动参数'
# 停止： sh xxxx.sh stop
# 重启： sh xxxx.sh restart'启动参数'
# 获取状态： sh xxxx.sh status

# JDK
JAVA_BOOT="/usr/bin/java"
if [ ! -z "$JAVA_HOME" ]; then
	JAVA_BOOT=$JAVA_HOME"/bin/java"
fi
# JVM参数
JAVA_OPTS=${JAVA_OPTS}
# PARAMS
JAVA_PARAMS=${JAVA_PARAMS};
# 是否后台启动
IS_DEAMON=0


# 第一个参数是方法名，如果没有传，直接退出
if [ "$1" = "" ];
then
    echo "please enter operation name: {start|stop|restart|status}"
    exit 1
fi

# 启动
start()
{
    #获取脚本名称
    script=$(readlink -f "$0")
    #获取脚本绝对路径
    scriptPath=$(dirname "$script")
    #改变工作路径（到app的根路径下-不管脚本在哪个路径下执行，都适用）
    cd $scriptPath
    cd ..

    homePath=`pwd`
    #获取当前要启动的应用
    bootJar=$homePath/$(ls app/*.jar)
    #启动错误日志文件
    bootlog=$homePath"/logs/boot-error.log"

    count=`ps -ef |grep java|grep $bootJar|grep -v grep|wc -l`
    if [ $count != 0 ]; then
        echo "$bootJar is already running..."
    else

        echo "=============================================================="
        echo "=====  Java home：$JAVA_BOOT"
	      echo "=====  Program：$bootJar"
        echo "=====  VM options：$JAVA_OPTS"
        echo "=====  Program arguments：$JAVA_PARAMS"
        echo "=====  启动异常日志：$bootlog"
        echo "=====  启动状态：请查看日志文件"
        echo "=============================================================="

        # command format: java [vm option] -jar x.jar [program arguments]
        # springboot properties not use --xxx=yyy and use -Dxxx=yyy
	if [ $IS_DEAMON -eq 1 ]; then
	  nohup $JAVA_BOOT $JAVA_OPTS -jar $bootJar $JAVA_PARAMS > /dev/null 2>$bootlog &
	else
		$JAVA_BOOT $JAVA_OPTS -jar $bootJar $JAVA_PARAMS
	fi

    fi
}

# 停止
stop()
{

    #获取脚本名称
    script=$(readlink -f "$0")
    #获取脚本绝对路径
    scriptPath=$(dirname "$script")
    #改变工作路径（到app的根路径下-不管脚本在哪个路径下执行，都适用）
    cd $scriptPath
    cd ..
    homePath=`pwd`
    #获取当前要启动的应用
    bootJar=$homePath/$(ls app/*.jar)

    boot_id=`ps -ef |grep java|grep $bootJar|grep -v grep|awk '{print $2}'`
    count=`ps -ef |grep java|grep $bootJar|grep -v grep|wc -l`

    if [ $count != 0 ]; then

 	echo "Stoping $bootJar"

	kill $boot_id
        count=`ps -ef |grep java|grep $bootJar|grep -v grep|wc -l`
        boot_id=`ps -ef |grep java|grep $bootJar|grep -v grep|awk '{print $2}'`
        kill -9 $boot_id

	sleep 3s
	echo ""

        status
    fi
}

# 重启
restart()
{
    stop
    sleep 3s
    start
}

# 状态
status()
{
    #获取脚本名称
    script=$(readlink -f "$0")
    #获取脚本绝对路径
    scriptPath=$(dirname "$script")
    #改变工作路径（到app的根路径下-不管脚本在哪个路径下执行，都适用）
    cd $scriptPath
    cd ..
    homePath=`pwd`
    #获取当前要启动的应用
    bootJar=$homePath/$(ls app/*.jar)

    count=`ps -ef |grep java|grep $bootJar|grep -v grep|wc -l`
    if [ $count != 0 ]; then
        echo "=============================================================="
        echo "=====  应用程序：$bootJar"
        echo "=====  状态：running"
        echo "=============================================================="
    else
        echo "=============================================================="
        echo "=====  应用程序：$bootJar"
        echo "=====  状态：not running"
        echo "=============================================================="
    fi
}


#处理参数
j=2
for i in $@
do
    if [ "$i" = "-d" ]; then
      IS_DEAMON=1
      j=2
      continue
    fi
    if [ "$i" = "-JAVA_OPTS" ]; then
      j=0
	    JAVA_OPTS="-Dlog4j2.formatMsgNoLookups=true"
      continue
    fi
    if [ "$i" = "-JAVA_PARAMS" ]; then
      j=1
	    JAVA_PARAMS=""
      continue
    fi
    if [ $j = 0 ]; then
	    JAVA_OPTS="${JAVA_OPTS} ${i}"
      continue
    fi
    if [ $j = 1 ]; then
	    JAVA_PARAMS="${JAVA_PARAMS} $i"
	    continue
    fi
done



# 调用具体函数
case $1 in
    start)
    start;;
    stop)
    stop;;
    restart)
    restart;;
    status)
    status;;
    *)

    echo "Usage: sh $0 {start|stop|restart|status} [-d -JAVA_OPTS '-Xmx512m xxx' -PARAMS '--server.port=8080 xxx']"
    echo "Example: sh $0 start"
esac
