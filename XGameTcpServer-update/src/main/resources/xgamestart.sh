#!/usr/bin/env bash
# Use -Dlog4j.debug for Log4J startup debugging info
# Use -Xms512M -Xmx512M to start with 512MB of heap memory. Set size according to your needs.
# Use -XX:+CMSClassUnloadingEnabled -XX:+CMSPermGenSweepingEnabled for PermGen GC

# kill old java progress
ps -ef |grep XGameTcpServer |grep -v grep|cut -c 8-15|xargs kill -9
echo "=============XGameTcpServer is shutdown ......!======================================================"



# start ##########
echo "=============XGameTcpServer is starting ......!======================================================"
JAVA_CMD="java"


JAVA_OPTS="${JAVA_OPTS} -Dfile.encoding=UTF-8 -Xmx3550m -Xms3550m -Xmn2g -Xss256k"
# JAVA_OPTS="${JAVA_OPTS} -Dio.netty.leakDetectionLevel=advanced"

${JAVA_CMD} -cp  ${JAVA_OPTS}  -jar XGameTcpServer.jar&

