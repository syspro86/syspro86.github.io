---
layout: post
title: "Websphere 오류 해결 정리"
date: 2018-06-29 19:42:00 +0900
categories: java
tags: websphere
---

## class cache 지우기

WebSphere설치경로\AppServer\bin\clearClassCache.bat  
WebSphere설치경로\AppServer\profiles\AppSrv01\bin\clearClassCache.bat  
WebSphere설치경로\AppServer\profiles\AppSrv01\bin\osgiCfgInit.bat  
del /S %userprofile%\AppData\Local\javasharedresources\*

## EJBTimerDB 오류

    EJB Timer Service not available for TimerObjectEJB

WebSphere설치경로\AppServer\profiles\AppSrv01\databases\EJBTimers\server1\EJBTimerDB 폴더를 삭제한다
