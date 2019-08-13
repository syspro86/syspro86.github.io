---
layout: post
title:  "Docker사용하여 Jupyter 구축하고 그안에서 다시 Docker 사용하기"
date:   2019-08-13 00:00:00 +0900
categories: docker jupyter
---

# Dockerfile

```
FROM jupyter/all-spark-notebook
  
USER root

# docker repository
RUN apt-get update && yes | apt-get install apt-transport-https ca-certificates curl software-properties-co
mmon
RUN curl -fsSL https://download.docker.com/linux/ubuntu/gpg | sudo apt-key add -
RUN apt-key fingerprint 0EBFCD88
RUN echo "deb [arch=amd64] https://download.docker.com/linux/ubuntu bionic edge" > /etc/apt/sources.list.d/
docker.list

# docker 설치
RUN apt-get update && yes | apt-get install pkg-config libssl-dev libsasl2-dev libcurl4-openssl-dev docker-
ce
```

# jupyter 시작

```
docker build -t syspro86/jupyter .
docker run -it --restart always -d \
        -p 8888:8888 \
        --name jupyter \
        -v /var/run/docker.sock:/var/run/docker.sock \
        syspro86/jupyter
```

* `-v /var/run/docker.sock:/var/run/docker.sock` --> 안과 밖의 docker 를 연결

컨테이너에 shell 접근하면 docker 연동이 되는데, 노트북 자체는 개인 계정으로 실행되기 때문에 노트북안에서 도커 사용이 안된다.. 방법 찾는중
