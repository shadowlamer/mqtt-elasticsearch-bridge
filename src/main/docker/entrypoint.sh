#!/bin/sh

echo "The application will start in ${MEB_SLEEP}s..." && sleep ${MEB_SLEEP}
exec java ${JAVA_OPTS} -Djava.security.egd=file:/dev/./urandom -jar "${HOME}/bridge.jar" -t /etc/meb/template.json "$@"
