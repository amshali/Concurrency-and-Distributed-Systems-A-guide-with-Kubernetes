FROM openjdk:18-alpine

WORKDIR /workspace

COPY ./target ./

EXPOSE 80

ENV RESPONSE_STORE_CLASS ''
ENV MAX_CONCURRENCY 5
ENV QUEUE_URL ''
ENV REQUEST_TIMEOUT_SEC 30

CMD java -server -Xms512M -Xmx512M -XX:InitiatingHeapOccupancyPercent=70 \
    -XX:+UseG1GC -XX:+ParallelRefProcEnabled -XX:MaxGCPauseMillis=200 \
    -jar server-1.0.jar --server.port=80 \
    --response_store_class=${RESPONSE_STORE_CLASS} \
    --max_concurrency=${MAX_CONCURRENCY} --queue_url=${QUEUE_URL} \
    --request_timeout_sec=${REQUEST_TIMEOUT_SEC}
