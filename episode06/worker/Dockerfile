FROM openjdk:18-alpine

WORKDIR /workspace

COPY ./target ./

EXPOSE 80

ENV COORDINATOR ''

CMD java -server -Xms512M -Xmx512M -XX:InitiatingHeapOccupancyPercent=70 \
    -XX:+UseG1GC -XX:+ParallelRefProcEnabled -XX:MaxGCPauseMillis=200 \
    -jar worker-1.0.jar --server.port=80 --coordinator=${COORDINATOR}

