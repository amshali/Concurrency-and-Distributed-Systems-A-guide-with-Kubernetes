FROM openjdk:18-alpine

WORKDIR /workspace

COPY ./target ./

EXPOSE 80

CMD java -server -Xms512M -Xmx1G -XX:InitiatingHeapOccupancyPercent=70 \
    -XX:+UseG1GC -XX:+ParallelRefProcEnabled -XX:MaxGCPauseMillis=200 \
    -jar server-1.0.jar --server.port=80

