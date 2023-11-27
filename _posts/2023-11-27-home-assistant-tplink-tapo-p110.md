---
layout: post
title: "Home Assistant - TPLink Tapo P100/P110 연동"
date: 2023-11-27T00:00:00
categories: home-assistant
---

아직 Home assistant 에서 공식으로 P110 제품이 지원되지는 않는 것 같다. HACS 를 통해 설치해야 한다.

HACS 는 통합구성요소 추가를 통해 설치할 수 있다.

## TPLink 애드온 설치

HACS 화면에서 ```tapo``` 로 검색한 후 ```Tapo Controller``` 를 설치한다.

![Image]({{ site.url }}{{ site.baseurl }}/assets/images/tapo-p110/hacs.png){: width="100%"}

## 통합구성요소 설정

설정 > 기기 및 서비스 > 통합구성요소 에서 ```통합구성요소 추가하기``` 클릭 후 ```tapo``` 를 검색한다.

![Image]({{ site.url }}{{ site.baseurl }}/assets/images/tapo-p110/integration.png){: width="100%"}

P110 장치의 IP 와 TPLink 의 계정 정보를 입력한다.

![Image]({{ site.url }}{{ site.baseurl }}/assets/images/tapo-p110/tplink.png){: width="100%"}

등록 성공

![Image]({{ site.url }}{{ site.baseurl }}/assets/images/tapo-p110/success.png){: width="100%"}

## 에너지 대시보드에 추가

설정 > 대시보드 > 에너지

전력 그리드의 그리드 소비량에 ```소비량 추가``` 클릭 후, 추가한 장치의 Today Engery를 선택하여 저장한다.

![Image]({{ site.url }}{{ site.baseurl }}/assets/images/tapo-p110/add_dashboard.png){: width="100%"}

에너지 대시보드에서 시간별 전력 사용량을 확인할 수 있다.

![Image]({{ site.url }}{{ site.baseurl }}/assets/images/tapo-p110/dashboard.png){: width="100%"}
