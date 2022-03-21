FROM openjdk:11
VOLUME /tmp
RUN java -version
RUN addgroup --gid 888 mte_g
#-D for user without password
RUN adduser --home /home/mte_u --uid 888 --gid 888 --disabled-password mte_u

USER mte_u
ADD target/mte-0.0.1.jar mte.jar

ENTRYPOINT ["java","-jar","/mte.jar"]