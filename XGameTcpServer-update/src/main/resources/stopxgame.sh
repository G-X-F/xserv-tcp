#!/usr/bin/env bash
# Use -Dlog4j.debug for Log4J startup debugging info
# Use -Xms512M -Xmx512M to start with 512MB of heap memory. Set size according to your needs.
# Use -XX:+CMSClassUnloadingEnabled -XX:+CMSPermGenSweepingEnabled for PermGen GC

# kill old java progress
ps -ef |grep XGameTcpServer |grep -v grep|cut -c 6-15|xargs kill -9
echo "=============XGameHttpServer is shutdown ......!======================================================"

