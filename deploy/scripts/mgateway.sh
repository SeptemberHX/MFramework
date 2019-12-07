#!/bin/bash

mkdir -p /data/test-dir/log

docker run -d -p 8081:8080 \
    -v /data/test-dir/log:/var/log/mclient \
    --env MCLUSTER_IP=144.34.200.189 \
    --env MCLUSTER_PORT=30761 \
    septemberhx/mgateway:v1.1.3

docker run -d -v /data/test-dir/log:/var/log/mclient \
    --env MCLIENT_LOG_DIR_PATH=/var/log/mclient \
    --env MCLIENT_LOGSTASH_IP=144.34.200.189 \
    --env MCLIENT_LOGSTASH_PORT=32001 \
    --env MCLIENT_CADVISOR_IP=144.34.200.189 \
    --env MCLIENT_CADVISOR_PORT=4042 \
    septemberhx/minfo-collector-service:v1.0.7
