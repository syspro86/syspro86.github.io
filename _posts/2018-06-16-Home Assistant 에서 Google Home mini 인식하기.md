---
layout: post
title:  "Home Assistant 에서 Google Home mini 인식하기"
date:   2018-06-16 01:09:00 +0900
categories: home assistant
---

1. `configuration.yaml` 파일 설정을 통한 자동 인식

    {% highlight yaml %}
    discovery:
    {% endhighlight %}

    위 내용은 Home assistant 설치하면 자동으로 켜져 있지만 다시 한번 확인해보고 주석처리 되어있다면 해제한다.

1. 기기명 변경 or `entity_registry.yaml` 변경

    Google Home mini 기기의 장치 이름이 한글로 되어있다면 Home Assistant에서 자동으로 entity id 를 부여하다가
    잘못된 이름을 부여하여 등록이 안되고 있을 가능성이 있다.

    `entity_registry.yaml` 파일을 열어봐서 `media_player.:` 로 등록된게 있다면 . 뒤에 직접 이름을 부여하면 인식될 것이다

    혹은 기기 이름을 영문으로 변경 후 파일 내용을 전부 지우고 저장하면 영문명으로 인식된다

    {% highlight yaml %}
    media_player.my_google_home_mini:
      config_entry_id:
      name:
      platform: cast
      unique_id: xxxxxxxx-xxxx-xxxx-xxxx-xxxxxxxxxxxx
    {% endhighlight %}

