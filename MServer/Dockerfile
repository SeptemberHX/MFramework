FROM openjdk:8-jre-alpine
ENV APP_FILE MServer-1.0-SNAPSHOT.jar
ENV APP_HOME /usr/apps
COPY target/$APP_FILE $APP_HOME/
WORKDIR $APP_HOME
ENTRYPOINT ["sh", "-c"]
CMD ["exec java -jar -agentlib:jdwp=transport=dt_socket,server=y,suspend=n,address=5005 $APP_FILE"]