#!/bin/bash
docker build -f gameserver.Dockerfile -t gameserver:latest .
docker save gameserver:latest | sudo k3s ctr images import -
docker build -f matchmaker.Dockerfile -t matchmaker:latest .
docker save matchmaker:latest | sudo k3s ctr images import -
kubectl apply --server-side -k deployment/env/dev --force-conflicts
