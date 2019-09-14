# Create kubernetes namespace for MFramework !

```shell
kubectl apply -f kube-test.yml
```

# Deploy ElasticSearch and Kibana V7.1.1

```shell
kubectl apply -f kube-logging.yml
kubectl apply -f pv.yaml
kubectl apply -f elasticsearch_svc.yml
kubectl apply -f elasticsearch_statefulset.yaml
kubectl apply -f kibana.yaml
```
