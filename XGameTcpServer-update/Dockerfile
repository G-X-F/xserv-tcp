FROM ccr.ccs.tencentyun.com/common/java:1.8


RUN mkdir /usr/local/XGameServer/
RUN mkdir /usr/local/XGameServer/conf/
RUN mkdir /usr/local/XGameServer/conf/register/

ADD target/classes/register/*  /usr/local/XGameServer/conf/register/
ADD target/classes/config.properties  /usr/local/XGameServer/conf/config.properties
ADD target/classes/logback.xml  /usr/local/XGameServer/conf/logback.xml

ADD target/xgamehttpserver-jar-with-dependencies.jar /usr/local/XGameServer/XGameHttpServer.jar
ADD target/classes/xgamestart.sh /usr/local/XGameServer/xgamestart.sh

CMD bash  /usr/local/XGameServer/xgamestart.sh
