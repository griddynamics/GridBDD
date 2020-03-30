FROM java:8-jdk-alpine
RUN apk update
RUN apk add ca-certificates wget
COPY ./mappings.json /usr/app/mappings/
COPY ./responseFiles/* /usr/app/__files/
WORKDIR /usr/app
RUN wget "https://repo1.maven.org/maven2/com/github/tomakehurst/wiremock-standalone/2.26.3/wiremock-standalone-2.26.3.jar"
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "wiremock-standalone-2.26.3.jar"]
