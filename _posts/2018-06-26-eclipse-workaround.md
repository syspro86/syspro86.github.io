---
layout: post
title:  "Eclipse 오류 workaround"
date:   2018-06-27 20:57:00 +0900
categories: java eclipse
---

## cannot be read or is not a valid ZIP file 오류 해결 방법

자바 프로젝트에서 참조하고 있는 jar 파일이 정상적인 파일임에도 cannot be read or is not a valid ZIP file 오류가 발생하며 빌드가 안되는 경우, 이클립스를 종료 한 후 워크스페이스 폴더 밑의 아래 두개 파일을 삭제한 후 이클립스를 재기동하면 해결된다.

* .metadata\.plugins\org.eclipse.jdt.core\invalidArchivesCache
* .metadata\.plugins\org.eclipse.jdt.core\nonChainingJarsCache

## plugin 이 인식되지 않는 경우

* eclipse/configuration/org.eclipse.equinox.simpleconfigurator/bundles.info

위의 파일을 열어서 목록에 있는 포맷에 맞춰 정보를 추가하고 eclipse를 재시작한다.

> org.apache.ant,1.9.6.v201510161327,plugins/org.apache.ant_1.9.6.v201510161327/,4,false  
> *plugin id,버전,설치경로,4,false*

뒤의 4,false의 경우 정확한 값의 의미는 알 수 없지만 4,false 외에는 안보이기 때문에 그대로 적으면 될 것 같다

