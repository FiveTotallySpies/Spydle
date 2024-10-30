# Spydle

## Backend Setup

### Prerequisites
Instructions are tested on Ubuntu Jammy (22), but should work on most Linux distros.
If you are on Windows, WSL2 is recommended (and practically required).
- [Install minikube](https://minikube.sigs.k8s.io/docs/start)
- [Install docker](https://docs.docker.com/engine/install/ubuntu/)
- Set minikube config, this decides how much resources we allocate to the cluster as a whole:
  - `minikube config set memory 16G` (Can be less, recommend at least 4GB)
  - `minikube config set cpus 4` (Can be less, recommend at least 2 cores)
  - `mininkube config set driver docker` (Highly recommended: Runs minikube within docker containers, making it more contained and less likely to impact your entire system)
- (Optional, but highly recommended) [Install K9s](https://github.com/derailed/k9s?tab=readme-ov-file#installation)
	- This is a CLI tool that lets you observe all running pods in your Kubernetes cluster. Just type k9s.
- [Install helm](https://helm.sh/docs/intro/install/)
- [Install kubectl](https://kubernetes.io/docs/tasks/tools/install-kubectl-linux/)
- Run `minikube addons enable ingress`

### Starting Your Cluster
- `minikube start` (Downloads the images for running the K8s backend, runs on local)
- Build your matchmaker and gameserver images:
	- IMPORTANT: There are two docker registries that now exist on your computer: One is the default registry, and the other is minikube's internal one. In order for minikube to pull docker images that you have built, they need to be stored in the minikube registry.
		- By default, `docker build` will store this in the default registry.
			- You MUST run the command `eval $(minikube docker-env)` BEFORE you run any docker build commands for this project.
			- This stores the docker registry credentials for the minikube registry in your terminal's environment variables.
	- Now run `cd matchmaker` and `docker build -t matchmaker:latest .`
		- This may take a while the first time you execute it but will be faster later once the components are cached
	- Run `cd gameserver` and `docker build -t gameserver:latest .`
- Now, to apply all of the K8s resources in `deployment/`, run `cd deployment` and `kubectl apply --server-side -k env/dev --force-conflicts`
	- After doing this, you can now observe the deployments created using `k9s`.
### Stopping Your Cluster
- Run `minikube stop` to freeze all existing deployments in K8s. This does not delete them.
- Make sure to do this whenever you are done using the cluster! Otherwise minikube will continue consuming resources from your system to host the cluster.
### Deleting Resources
- Many times it may be useful to delete all resources in the cluster and start fresh. Here are some useful commands:
	- <b>Nuke Everything</b>: `minikube stop && minikube delete --all`: Deletes everything from your cluster, and also deletes the minikube containers themselves. They will be reinstalled on `minikube start`.
	- <b>Delete Spydle</b>: `kubectl delete all --all -n spydle`: Deletes all resources in the spydle namespace (gameservers and matchmakers).
