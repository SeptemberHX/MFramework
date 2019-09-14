#!/bin/bash

kubectl port-forward kibana-7ff54468d9-5hr7z 4000:5601 -n kube-logging --address 0.0.0.0 &
