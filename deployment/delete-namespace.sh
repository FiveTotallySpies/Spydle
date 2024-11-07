#!/bin/bash

# Check if a namespace argument is provided
if [ -z "$1" ]; then
  echo "Usage: $0 <namespace>"
  exit 1
fi

NAMESPACE="$1"

kubectl get namespace "$NAMESPACE" -o json \
  | tr -d "\n" \
  | sed "s/\"finalizers\": \[[^]]\+\]/\"finalizers\": []/" \
  | kubectl replace --raw "/api/v1/namespaces/$NAMESPACE/finalize" -f -
