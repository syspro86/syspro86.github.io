---
layout: post
title: "Eclipse 오류 해결 정리"
date: 2018-06-27 20:57:00 +0900
categories: java
tags: eclipse
---

## eclipse 기동 시 java was started but returned exit code = xxx 오류 발생

- memory 설정이 잘못된 경우 (메모리 설정이 너무 큰 경우)
  - eclipse.ini 내용중 -Xmx 사이즈를 줄인다.
- workspace cache 파일이 깨진 경우 (code -805306369)
  - `eclipse -clean` 명령어로 실행한다.
  - 위 명령어로도 안되면 workspace 폴더를 지우거나 옮기고 실행하여 새로 구성한다.

## cannot be read or is not a valid ZIP file 오류 해결 방법

자바 프로젝트에서 참조하고 있는 jar 파일이 정상적인 파일임에도 cannot be read or is not a valid ZIP file 오류가 발생하며 빌드가 안되는 경우, 이클립스를 종료 한 후 워크스페이스 폴더 밑의 아래 두개 파일을 삭제한 후 이클립스를 재기동하면 해결된다.

```
.metadata\.plugins\org.eclipse.jdt.core\invalidArchivesCache
.metadata\.plugins\org.eclipse.jdt.core\nonChainingJarsCache
```

## plugin 이 인식되지 않는 경우

```
eclipse -clean
```

이클립스가 플러그인 설치 정보를 다시 인식하게 된다. 대부분 이 명령으로 해결할 수 있다.

위 명령으로 인식되지 않을 경우 아래 파일을 직접 수정한다.

```
eclipse/configuration/org.eclipse.equinox.simpleconfigurator/bundles.info
```

목록에 있는 포맷에 맞춰 정보를 수정하고 eclipse를 재시작한다.

```
org.apache.ant,1.9.6.v201510161327,plugins/org.apache.ant_1.9.6.v201510161327/,4,false
plugin id,버전,설치경로,4,false
```

뒤의 4,false의 경우 정확한 값의 의미는 알 수 없지만 4,false 외에는 안보이기 때문에 그대로 적으면 될 것 같다

## plugin update site를 archive 로 저장하기

인터넷이 되지 않는 환경에서는 Update Site 등록을 통한 플러그인 설치가 불가능할 수 있다.  
그럴경우 zip 으로 된 아카이브 파일이 제공되면 쉽게 해결할 수 있지만 제공되지 않을 경우 Update Site 전체를 구조 그대로 내려받아야하는 번거로움이 있다.  
아래의 방법을 사용하면 Update Site 전체를 편하게 내려받을 수 있다.

1. update site 경로 확인

예제로 nodeclipse를 받아보자 (사실 nodeclipse는 zip파일을 제공하여 이 방법으로 할 필요는 없다)  
홈페이지 (http://www.nodeclipse.org) 의 DOWNLOAD 메뉴를 통해 경로를 확인

https://dl.bintray.com/nodeclipse/nodeclipse/1.0.2f/

2. cmd 명령을 통해 eclipse 설치된 곳으로 이동하여 아래 명령어 입력

```
eclipsec.exe -application org.eclipse.equinox.p2.artifact.repository.mirrorApplication -source https://dl.bintray.com/nodeclipse/nodeclipse/1.0.2f/ -destination D:\Nodeclipse
eclipsec.exe -application org.eclipse.equinox.p2.metadata.repository.mirrorApplication -source https://dl.bintray.com/nodeclipse/nodeclipse/1.0.2f/ -destination D:\Nodeclipse
```

즉 아래와 같이 실행하면 된다

```
eclipsec.exe -application org.eclipse.equinox.p2.artifact.repository.mirrorApplication -source 내려받을 사이트URL -destination 로컬경로
eclipsec.exe -application org.eclipse.equinox.p2.metadata.repository.mirrorApplication -source 내려받을 사이트URL -destination 로컬경로
```

3. eclipse 로고가 떴다가 사라지는데 cmd 명령창의 eclipse가 종료될때까지 기다린다.

-destination 에 지정한 폴더에 가보면 파일이 하나씩 내려받아지는걸 확인가능하다.

4. 다운받아진 폴더 전체를 zip으로 압축하여 플러그인을 설치하려는 환경에서 UpdateSite를 zip파일 아카이브로 등록하여 설치한다.

## plugin update site를 통해 설치 중 오류 발생 시

워크스페이스 폴더 하위의 `.metadata/.log` 내용 중

    IBM\SDP\p2\org.eclipse.equinox.p2.engine\profileRegistry\bootProfile.profile\xxxx.profile.gz 프로파일을 구문 분석하는 중에 오류가 발생했습니다.

내용이 있는 경우 해당파일을 삭제 후 eclipse를 재시작한다.
