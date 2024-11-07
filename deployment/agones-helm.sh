#!/bin/bash

export KUBECONFIG=/etc/rancher/k3s/k3s.yaml
helm repo add agones https://agones.dev/chart/stable
helm repo update

RELEASE_NAME="spydle-agones"
NAMESPACE="agones-system"
SET_ARGS="\
  --set agones.allocator.disableMTLS=true \
  --set agones.allocator.disableTLS=true \
  --set agones.allocator.service.http.enabled=false \
  --set agones.allocator.service.grpc.port=80 \
  --set gameservers.namespaces={spydle} \
  --set agones.allocator.replicas=1 \
  --set agones.extensions.replicas=1 \
  --set agones.controller.replicas=1 \
  --set agones.ping.replicas=1 \
  --set agones.allocator.service.serviceType=ClusterIP \
  --set agones.ping.http.serviceType=ClusterIP \
  --set agones.ping.udp.serviceType=ClusterIP"

if kubectl get namespace $NAMESPACE &>/dev/null; then
  echo "Namespace $NAMESPACE already exists."
else
  echo "Creating namespace $NAMESPACE"
  kubectl create namespace $NAMESPACE
fi

if helm status $RELEASE_NAME -n $NAMESPACE &>/dev/null; then
  echo "Upgrading existing Helm release: $RELEASE_NAME"
  helm upgrade $RELEASE_NAME agones/agones --namespace $NAMESPACE $SET_ARGS
else
  echo "Installing new Helm release: $RELEASE_NAME"
  helm install $RELEASE_NAME agones/agones --namespace $NAMESPACE $SET_ARGS
fi
