curl -sfL https://get.k3s.io | INSTALL_K3S_EXEC="--disable traefik --node-external-ip 172.17.50.3" sh -s -
sudo chmod 755 /etc/rancher/k3s/k3s.yaml
