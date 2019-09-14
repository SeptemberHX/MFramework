#!/usr/bin/env bash

mkdir "tmp_metrics_server"
cd tmp_metrics_server || exit

git clone https://github.com/kubernetes-incubator/metrics-server.git
cd metrics-server || exit

kubectl create -f deploy/1.8+/
rm -rf ./tmp_metrics_server