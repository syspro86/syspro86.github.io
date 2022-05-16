---
layout: post
title: "깃허브 블로그 윈도우 10에서 로컬환경 구성하기"
date: 2018-07-04 22:36:00 +0900
categories:
---

깃허브 블로그에 글을 올리기 전에 로컬에서 먼저 확인하려면 jekyll을 설치해야 하는데, 많은 글들이 linux 환경을 기준으로 작성 되어있어 이번에 윈도우10 환경에 설치하면서 내용을 정리했다.

- Ubuntu 18.04 설치
  - 시작메뉴 -> Microsoft Store를 들어가 Ubuntu 18.04 를 검색하고 설치한다
- Ubuntu 18.04 실행
  - 초기 설치 시 아이디와 비밀번호를 설정한다
- sudo apt update
- sudo apt install ruby ruby-dev nodejs make gcc g++
  - jekyll을 설치하는 과정에서 필요하다
- sudo gem install jekyll
- cd /mnt/c/깃헙페이지\_소스경로/
  - /mnt/ + 윈도우 경로
- jekyll serve
- 브라우저에서 http://127.0.0.1:4000 으로 접속하여 확인할 수 있다
  - 글 수정 사항은 바로 반영 된다.
