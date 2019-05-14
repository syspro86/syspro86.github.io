---
layout: post
title:  "Docker + Jenkins + Maven 자동 빌드 구축하기"
date:   2019-05-14 00:00:00 +0900
categories: java
---

# Jenkins 컨테이너 설치

* jenkins docker 이미지를 설치한다.

```
docker run \
  -p 8080:8080 -p 50000:50000 \
  -v /home/jenkins/:/var/jenkins_home \
  --name my-jenkins \
  jenkins/jenkins
```

* /home/jenkins 폴더안에 jenkins 데이터가 저장되며, uid(1000) 유저 권한을 부여해야 한다. (이 경로는 원하는 경로로 변경할 수 있다.)

```
chown 1000 /home/jenkins/
```

* jenkins 실행 후 화면에 표시되는 비밀키를 복사하고, http://serverip:8080/ 접속시 표시되는 화면에 입력한다.
* 최초 사용자를 등록하고, 추천 플러그인들을 설치한다.

* http://serverip:8080/jenkins/ 와 같이 context path를 지정하려면 아래 옵션을 추가한다.

```
docker run \
  -p 8080:8080 -p 50000:50000 \
  -v /home/jenkins/:/var/jenkins_home \
  --name my-jenkins \
  -e "JENKINS_OPTS=--prefix=/jenkins" \
  jenkins/jenkins
```
