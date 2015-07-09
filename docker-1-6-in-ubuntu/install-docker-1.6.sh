#!/bin/bash

sudo echo deb http://get.docker.io/ubuntu docker main > /etc/apt/sources.list.d/docker.list
sudo wget -qO- https://get.docker.com/gpg | sudo apt-key add -
sudo apt-get update
sudo apt-get install -y lxc-docker-1.6.2
docker -v
