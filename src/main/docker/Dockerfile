FROM openjdk:8-jre-alpine

ENV JAVA_OPTS=""

RUN adduser -D -s /bin/sh meb
WORKDIR /home/meb

ADD entrypoint.sh entrypoint.sh
RUN chmod 755 entrypoint.sh && chown meb:meb entrypoint.sh
RUN mkdir /etc/meb
ADD template.json /etc/meb/template.json
RUN chmod 755 /etc/meb/template.json && chown meb:meb /etc/meb/template.json
USER meb

ADD mqtt-elasticsearch-bridge.jar bridge.jar

ENTRYPOINT ["./entrypoint.sh"]

