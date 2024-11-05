#!/bin/bash
helm repo add agones https://agones.dev/chart/stable
helm repo update
kubectl create namespace agones-system
helm install spydle-agones agones/agones --namespace agones-system \
--set agones.allocator.disableMTLS=true \
--set agones.allocator.disableTLS=true \
--set agones.allocator.service.http.enabled=false \
--set agones.allocator.service.grpc.port=80 \
--set "gameservers.namespaces={spydle}" \
--set agones.allocator.replicas=1 \
--set agones.extensions.replicas=1 \
--set agones.controller.replicas=1 \
--set agones.ping.replicas=1 \
--set agones.allocator.service.serviceType=ClusterIP \
--set agones.ping.http.serviceType=ClusterIP \
--set agones.ping.udp.serviceType=ClusterIP
