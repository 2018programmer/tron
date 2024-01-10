FROM openjdk:21
COPY target/*.jar /opt/app.jar
RUN ln -sf /usr/share/zoneinfo/$TZ /etc/localtime    && echo $TZ > /etc/timezone
CMD java -jar -Dspring.profiles.active="" /opt/app.jar
