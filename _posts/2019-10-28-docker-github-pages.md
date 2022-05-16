---
layout: post
title: "[Docker] jupyter 환경에서 github 글 작성하기"
date: 2019-10-28 00:00:00 +0900
categories: docker
tags: jupyter github-pages
---

윈도우의 WSL을 통해 깃헙 페이지를 로컬에서 띄워서 작성하다가 어느 순간부터 jekyll 이 먹통이 되어서 구성하게 되었다.
WSL의 불안정한 jekyll 실행을 리눅스 docker로 해결하고, 어디서나 편집할수 있도록 jupyter 를 사용했다.

## jupyter 실행

글을 편집할 환경인 jupyter 를 docker 위에 실행한다.
jekyll 컨테이너와 파일이 공유되어야 하기 때문에 volume 매핑을 꼭 추가한다.

dockerfile-compose.yml

```
version: '3'

services:
  jupyter:
    image: syspro86/jupyter_with_docker
    volumes:
      - /etc/localtime:/etc/localtime:ro
      - /var/run/docker.sock:/var/run/docker.sock
      - /home/kiho/jupyter_note/work:/home/jovyan/work
    ports:
      - 8888:8888
```

## github pages 프로젝트 받기

터미널을 열어서 git 명령을 입력하거나, 노트북을 하나 만들고 셀 안에 입력해서 내려받는다. (경로는 예시)

1. 터미널

- /work/src/ 경로에서 New > Terminal 을 열고 git clone https://github.com/syspro86/syspro86.github.io 입력하여 다운로드
  ![image](https://user-images.githubusercontent.com/31230327/67680880-88317580-f9cf-11e9-9e9d-0d512fde8c7e.png)

2. 노트북에서 내려받기

- /work/src/ 경로에서 New > Python 3 을 열고 !git clone https://github.com/syspro86/syspro86.github.io 입력하고 실행하여 다운로드
  ![image](https://user-images.githubusercontent.com/31230327/67681221-3c330080-f9d0-11e9-95e2-367bbfdd65e9.png)

## jekyll 컨테이너 실행

github 프로젝트를 내려받은 실제 경로 (jupyter 의 volume 매핑경로 + /work/src)를 jekyll 컨테이너의 /usr/src/app 와 매핑한다.

docker-compose.yml

```
version: '3'

services:
  github_pages:
    image: starefossen/github-pages
    volumes:
      - /etc/localtime:/etc/localtime:ro
      - /home/kiho/jupyter_note/work/src/syspro86.github.io:/usr/src/app
    ports:
      - "4000:4000"
```

## 글 작성하기

이제 jupyter 에서 /work/src/syspro86.github.io/\_posts/ 경로로 이동하여 New > Text File 로 새파일.md 를 생성하여 글을 작성한다.
http://192.168.1.2:4000/ 와 같이 리눅스 머신의 ip port 로 접속하여 새 글을 확인할 수 있다.

작성한 글은 터미널이나 노트북에서 git 명령을 통해 commit push 해도 되겠지만, 글이 완성된 경우 간단하게 github 사이트에서 직접 복붙하여 커밋하고 적당한 주기로 jupyter 에서 git pull 을 통해 최신 버전으로 내려받아 주면 될 것이다.
