FROM openjdk:19-jdk-alpine3.15
VOLUME /tmp
RUN java -version

RUN addgroup -g 888 mte-g
#-D for user without password
RUN adduser -u 888 -G mte-g -D mte-u

USER mte-u
ADD target/mte-0.0.1.jar mte.jar

ENTRYPOINT ["java","-jar","/mte.jar"]