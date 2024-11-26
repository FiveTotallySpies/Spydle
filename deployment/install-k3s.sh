curl -sfL https://get.k3s.io | INSTALL_K3S_EXEC="--disable traefik --disable-network-policy" sh -s -
sudo chmod 755 /etc/rancher/k3s/k3s.yaml
