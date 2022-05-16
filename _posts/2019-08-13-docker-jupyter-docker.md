---
layout: post
title: "[Docker] Jupyter notebook 안에서 Docker 명령어 사용하기"
date: 2019-08-13 00:00:00 +0900
categories: docker
tags: jupyter
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

# sudo를 사용할 수 있게 해준다.
ENV GRANT_SUDO=yes
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

- `-v /var/run/docker.sock:/var/run/docker.sock` --> 안과 밖의 docker 를 연결

# jupyter 안에서 docker 사용

## 기능 동작 확인

jupyter에서 새로 노트북을 만들고

```
!sudo docker ps
```

위 문장을 실행시켜서 목록이 나오면 이제 docker를 사용할 수 있다.

## docker 이미지 생성

![image](https://user-images.githubusercontent.com/31230327/63405070-80c67c80-c420-11e9-8079-9920ac322b7f.png)

1. jupyter 안에서 폴더를 만들고 그 안에 Dockerfile 파일을 만들고 내용을 채운다.

```
FROM python
RUN pip install requests pymongo # 필요에 따라 추가

WORKDIR /app
ADD main.py /app/

CMD ["python", "main.py"]
```

2. 메인 소스파일 (예: main.py)를 만들고 내용을 채운다.
3. 같은 폴더에 신규노트북을 만들고 `!sudo docker build -t syspro86/my-python-app .` 와 같이 입력하여 이미지를 생성한다.
4. `!sudo docker run syspro86/my-python-app`를 입력하여 도커 컨테이너를 실행한다.
