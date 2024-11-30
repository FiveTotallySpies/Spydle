# Spydle

## Backend Setup

### Prerequisites

Instructions are tested on Ubuntu Jammy (22), but should work on most Linux distros.
If you are on Windows, WSL2 is recommended (and practically required).

- [Install docker](https://docs.docker.com/engine/install/ubuntu/)
- [Install helm](https://helm.sh/docs/intro/install/)
- [Install kubectl](https://kubernetes.io/docs/tasks/tools/install-kubectl-linux/)
- (Optional, but highly recommended) [Install K9s](https://github.com/derailed/k9s?tab=readme-ov-file#installation)
    - This is a CLI tool that lets you observe all running pods in your Kubernetes cluster. Just type k9s.

### K3s Installation

- Run the installation script `deployment/install-k3s.sh`
    - WARNING: This immediately starts the K3s cluster in the background! To stop the cluster later, scroll down in
      these instructions.
    - Also note that by default K3s does not operate with any resource limits and will consume as needed.
- Point the kubectl KUBECONFIG variable to our k3s server using `export KUBECONFIG=/etc/rancher/k3s/k3s.yaml`
    - Without this envvar, `kubectl` won't work, and will fail to connect to k3s to find any resources.
    - You would need to run this in every new terminal, but it is recommended you add this to your bashrc file using
      `echo 'export KUBECONFIG=/etc/rancher/k3s/k3s.yaml' >> ~/.bashrc`.

- Now we should also install Agones within the cluster by running the `deployment/agones-helm.sh` script.

### Applying Our Kustomization

- To apply our set of K8s resources (defined in a kustomization stack), run
  `kubectl apply --server-side -k env/dev --force-conflicts`.
    - You should now be able to observe the pods deployed by running `k9s` and pressing `0` to view all namespaces.
        - Some will be failing, this will be solved by our next step.

### Building Our Images

- In order for our matchmaker and gameserver pods to have the correct images, we need to build them first.
- Run `docker build -f matchmaker.Dockerfile -t matchmaker:latest .`
    - This will add the docker image to your local registry. To save it to the K3s internal registry, you need to now
      run `docker save matchmaker:latest | sudo k3s ctr images import -`
- Now we do the same for gameserver:
    - `docker build -f gameserver.Dockerfile -t gameserver:latest .`
    - and `docker save gameserver:latest | sudo k3s ctr images import -`
- Anytime you want to deploy a change to the pods' codebase, re-run these commands.

### Starting and Stopping the Cluster

- To stop the cluster, run `./deployment/stop-k3s.sh`
- To start the cluster, run `./deployment/start-k3s.sh`
- To restart the cluster, run `./deployment/restart-k3s.sh`

### Deleting Resources

- Many times it may be useful to delete all resources in the cluster and start fresh. Here are some useful commands:
    - <b>Delete (most) resources</b>: `kubectl delete all --all --all-namespaces && ./deployment/restart-k3s.sh`:
      Deletes everything from your cluster, and restarts k3s (which will bring back the bare minimum.
        - This however does not remove things like CRDs and helm charts. To delete those as well, just reinstall k3s by
          running `sudo /usr/local/bin/uninstall-k3s.sh && ./deployment/install-k3s.sh`
    - <b>Delete Spydle</b>: `kubectl delete all --all -n spydle`: Deletes all resources in the spydle namespace (
      gameservers, matchmakers, and ingress).
