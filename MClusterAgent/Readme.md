# MClusterAgent

Considering the differences between docker clusters, like Kubernetes and Docker swarm, it is different to send a request to the containers in the cluster.

`MClusterAgent` aims to provide a proxy between the `MServer` out of the cluster and `MClient` in the cluster:
* Send instructions to `MClient`
* Collect information about `MClient` and send it to `MServer`
* Collect the cluster status and notify it to `MServer`

`MServer` will be dependent on `MClusterAgent` and it will handle all the cluster things.

* Kubernetes `kubectl proxy server` is used to deploy/remove instances
* `yaml/template.yaml` should be prepared as the template yaml configure file for service deployment
* main APIs are located in `src/main/java/com.septemberhx.agent/controller`