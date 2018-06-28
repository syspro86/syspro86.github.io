---
layout: post
title:  "Home Assistant (홈 어시스턴트) 설정 및 응용"
date:   2018-06-17 21:52:00 +0900
categories: home_assistant
permalink: /post/home_assistant.html
---

## google home mini

아래 내용은 Home assistant 설치하면 자동으로 켜져 있지만 다시 한번 확인해보고 주석처리 되어있다면 해제한다.

`configuration.yaml`

{% highlight yaml %}
discovery:
{% endhighlight %}

Google Home mini 기기의 장치 이름이 한글로 되어있다면 Home Assistant에서 자동으로 entity id 를 부여하다가
잘못된 이름을 부여하여 등록이 안되고 있을 가능성이 있다.

`entity_registry.yaml` 파일을 열어봐서 `media_player.:` 로 등록된게 있다면 . 뒤에 직접 이름을 부여하면 인식될 것이다

혹은 기기 이름을 영문으로 변경 후 파일 내용을 전부 지우고 저장하면 영문명으로 인식된다

{% highlight yaml %}
media_player.mymini:
  config_entry_id:
  name:
  platform: cast
  unique_id: xxxxxxxx-xxxx-xxxx-xxxx-xxxxxxxxxxxx
{% endhighlight %}


## telegram

텔레그램 봇을 통해 명령을 내리거나, 특정 조건에 따라 알림을 받을 수 있다.

텔레그램 봇의 API KEY와 본인 텔레그램 계정의 chat_id 를 알아야 진행할 수 있다.

API_KEY와 chat_id를 얻는 절차는 아래와 같다

* 텔레그램에서 @BotFather 를 검색하여 대화창을 연다
* /newbot 를 입력한다
* 원하는 bot 이름을 입력한다 (이름이 bot으로 끝나야 한다) 등록이 되지 않으면 이름을 변경하여 재시도 해본다
* 생성이 완료되면 생성한 bot의 주소와 API KEY를 알 수 있다
* 주소 ex) t.me/MyBot
* API KEY ex) 012345678:abcDEfghijKlmnOP123AB435-Zdf23fd2345rf
* 내 bot의 주소를 클릭하면 대화를 시작할 수 있고 '시작' 버튼을 누른다
* 대화창에 아무 텍스트나 입력하고 전송한다
* 인터넷 브라우저를 열어 https://api.telegram.org/bot012345678:abcDEfghijKlmnOP123AB435-Zdf23fd2345rf/getUpdates 와 같이 입력한다 (bot 다음에 본인의 API KEY 로 변경)
* 브라우저에 표시된 내용중 "chat": { "id": 다음에 나타나는 숫자가 본인의 chat_id 이다 (브라우저에 내용이 표시되지 않는 경우, 다른 브라우저로 다시 열어보거나 다운로드 되는 파일을 메모장으로 열어 확인할 수 있다)
* API KEY와 chat id 를 home assistant의 configuration.yaml 안에 아래와 같이 작성한다

`configuration.yaml`

{% highlight yaml %}
telegram_bot:
  - platform: polling
    api_key: API_KEY
    allowed_chat_ids:
      - CHAT_ID
{% endhighlight %}


## 샤오미 공기 청정기

공식 사이트 경로: https://www.home-assistant.io/components/vacuum.xiaomi_miio/#retrieving-the-access-token

샤오미 제품을 등록하기 위해서는 장치의 token 값을 알아야 하며, 공기 청정기의 token 값은 miio 프로그램으로 확인할 수 있다.

* miio를 사용하려면 Node.js 가 설치되어 있어야 한다. (https://nodejs.org/ko/ 에서 설치)

* `npm install -g miio` (miio 설치)

* `miio discover`

실행하면 같은 네트워크내에 있는 샤오미 장비들이 목록으로 표시된다.  
그중 zhimi.airpurifier가 들어간 항목의 Token 값을 복사하면 된다.

* `configuratoin.yaml` 내용에 추가한다

{% highlight yaml %}
fan:
  - platform: xiaomi_miio
    host: 192.168.x.x (공기청정기의 ip주소)
    token: 공기청정기token값
{% endhighlight %}

등록하면 공기청정기를 제어(켜기/끄기/모드)가 가능하고, 습도,미세먼지 농도등을 가져올 수 있다.


## 샤오미 로봇 청소기

공기 청정기와 마찬가지로 장치의 token 값을 알아낸 후에 아래와 같이 등록할 수 있다.

* `configuratoin.yaml` 내용에 추가한다

{% highlight yaml %}
vacuum:
  - platform: xiaomi_miio
    host: 192.168.x.x (로봇청소기의 ip주소)
    token: 로봇청소기token값
{% endhighlight %}

하지만 miio 프로그램으로 token 값이 나오지 않는 경우가 있는데, 이 경우에는 MiHome 앱의 구버전을 설치하여 확인해야 한다.

* https://www.apkmirror.com/apk/xiaomi-inc/mihome/mihome-5-0-0-release/ 링크로 부터 MiHome 앱을 다시 설치한다 (기존 앱 삭제)

* 설치한 앱에 로봇 청소기를 연동한다

* 그 이후는 https://www.home-assistant.io/components/vacuum.xiaomi_miio/#retrieving-the-access-token 페이지의 OS별 방법을 따라하여 확인하면 된다

* 윈도우의 경우 프로그램을 다운받아 추출하게끔 설명되어 있는데, 안드로이드 SDK가 설치 되어 있는 경우 Linux and Android (not rooted) 의 숫자 1번부터 따라해도 문제 없다 (3번 생략)


## telegram + google home mini

telegram bot을 통해 google home mini에서 특정 url 을 재생시키거나 볼륨을 제어하는 기능을 적용하는 스크립트이다

우선 볼륨을 제어하거나 특정 url를 재생시키는 서비스를 각각 생성한다 (내용중 entity_id 는 본인에 맞게 수정)

`scripts.yaml`

{% highlight yaml %}
'mini_play_url':
  alias: mini mp3 재생
  sequence:
  - service: media_player.play_media
    data_template:
      entity_id: "media_player.mymini"
      media_content_id: "{% raw %}{{ param }}{% endraw %}"
      media_content_type: "audio/mp3"
'mini_set_volume':
  alias: mini set volume
  sequence:
  - service: media_player.volume_set
    data_template:
      entity_id: "media_player.mymini"
      volume_level: "{% raw %}{{ param|int(0) / 100 }}{% endraw %}"
{% endhighlight %}

텔레그램으로 부터 메시지를 받았을 때 두개의 서비스를 호출하도록 등록한다

텔레그램 봇에게 보낸 메시지가 0~100 범위의 숫자라면 해당 숫자의 볼륨으로 변경하고

숫자가 아닌 경우 url이라 판단하여 해당 url를 재생하도록 하는 스크립트이다

`automations.yaml`

{% highlight yaml %}
- id: telegram_ontext
  alias: telegram_ontext
  trigger:
    platform: event
    event_type: telegram_text
  action:
  - service_template: >
      {% raw %}{% if trigger.event.data.text|int(-1) >= 0 and trigger.event.data.text|int(-1) <= 100 %}{% endraw %}
      script.mini_set_volume
      {% raw %}{% else %}{% endraw %}
      script.mini_play_url
      {% raw %}{% endif %}{% endraw %}
    data_template:
      param: "{% raw %}{{ trigger.event.data.text }}{% endraw %}"
{% endhighlight %}

