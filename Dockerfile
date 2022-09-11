FROM openjdk:18
VOLUME /tmp
EXPOSE 8080
COPY out/artifacts/IlpRestServer_jar/IlpRestServer.jar app.jar
ENTRYPOINT java -Djava.security.egd=file:/dev/./urandom -jar /app.jar