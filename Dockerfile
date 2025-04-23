FROM ubuntu:22.04

RUN apt-get update && apt-get install -y \
    openjdk-17-jre \
    inkscape \
    curl \
    unzip \
    && apt-get clean

WORKDIR /app

COPY target/abaixarversaocdronline-1.0-SNAPSHOT.jar app.jar

EXPOSE 8080

ENTRYPOINT ["java", "-jar", "app.jar"]
