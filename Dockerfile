# Start with a base image containing Java runtime
FROM openjdk:17-jdk-slim

# Add Maintainer Info
LABEL maintainer="cedric10101980"

# Add a volume pointing to /tmp
VOLUME /tmp

# Make port 8080 available to the world outside this container
EXPOSE 443

# The application's jar file
ARG JAR_FILE=build/libs/AIImageProcessor-1.0.0.jar

# Add the application's jar to the container
ADD ${JAR_FILE} app.jar

#COPY zscaler.cer $JAVA_HOME/lib/security/
#RUN cd $JAVA_HOME/lib/security/ && keytool -keystore cacerts -storepass changeit -noprompt -trustcacerts -importcert -alias zscaler -file zscaler.cer

# Copy public and static folders
COPY src/main/resources/public /resources/public

# Run the jar file
ENTRYPOINT ["java","-Djava.security.egd=file:/dev/./urandom","-jar","/app.jar"]