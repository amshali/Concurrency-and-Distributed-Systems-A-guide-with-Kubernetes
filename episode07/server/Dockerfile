FROM openjdk:18-alpine

WORKDIR /workspace

COPY ./target ./

EXPOSE 80

ENV ZOO_KEEPER ''
ENV ZOO_KEEPER_WORKERS_PATH '/cache/workers'
ENV WORKER_RING_REPLICAS 50

CMD java -server -Xms512M -Xmx512M -XX:InitiatingHeapOccupancyPercent=70 \
    -XX:+UseG1GC -XX:+ParallelRefProcEnabled -XX:MaxGCPauseMillis=200 \
    -jar worker-1.0.jar --server.port=80 --zookeeper=${ZOO_KEEPER} \
    --zk_workers_path=${ZOO_KEEPER_WORKERS_PATH} \
    --worker_ring_replicas=${WORKER_RING_REPLICAS}

