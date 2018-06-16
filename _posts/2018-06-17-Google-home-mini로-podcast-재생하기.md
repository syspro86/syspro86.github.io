---
layout: post
title:  "Google home mini로 podcast 재생하기"
date:   2018-06-17 02:53:00 +0900
categories: home_assistant
---

## 특정 url의 mp3를 구글홈미니로 재생하기

일단 mp3 url 경로를 아는 경우 구글홈미니를 통해 재생하는 스크립트이다.

* `scripts.yaml` 파일에 내용 추가

{% highlight yaml %}
'play_podcast':
  alias: 팟캐스트 재생
  sequence:
  - service: media_player.play_media
    data_template:
      entity_id: "media_player.my_google_home_mini"
      media_content_id: "mp3 url"
      media_content_type: "audio/mp3"
{% endhighlight %}

`'play_podcast'`는 임의로 지정하면 되고, 이후 자동화에서 `script.play_podcast` 의 형태로 사용한다.

`media_content_id` 부분에 mp3의 url 에 적으면 되는데 이후 sensor를 통해 url를 지정하게 되면

`media_content_id: "{{ states.sensor.sensor_id.state }}"` 형태로 변경하면 된다.

## 팟캐스트의 최신 에피소드 mp3 url 적용하기

듣고자 하는 팟캐스트의 rss 주소를 먼저 확인한다.
이 주소는 팟캐스트의 공식홈페이지에 들어가면 표시가 되어있고, 공식홈페이지가 없거나 rss 주소가 따로 표시 되지 않았다면 rss 를 직접 구현하거나 구현된 주소를 찾아야 한다

팟캐스트의 rss주소가 있다고 가정하고 진행하면

* `configuration.yaml` 파일에 내용 추가

{% highlight yaml %}
sensor:
  - platform: rest
    resource: https://api.rss2json.com/v1/api.json?rss_url=팟캐스트URL주소
    name: podcast_url
    value_template: '\{\{ value_json["items"][0]["enclosure"]["link"] \}\}'
{% endhighlight %}

이제 `sensor.podcast_url`은 가장 최신화의 mp3 경로를 표시하게 된다.

만약 최신의 3개 까지 표시 하고 싶다면

{% highlight yaml %}
sensor:
  - platform: rest
    resource: https://api.rss2json.com/v1/api.json?rss_url=팟캐스트URL주소
    name: podcast_url
    value_template: '\{\{ value_json["items"][0]["enclosure"]["link"] \}\}'
    json_attributes:
      - items
  - platform: template
    sensors:
      podcast_url_2:
        value_template: '\{\{ states.sensor.podcast_url.attributes["items"][1]["enclosure"]["link"] \}\}'
  - platform: template
    sensors:
      podcast_url_3:
        value_template: '\{\{ states.sensor.podcast_url.attributes["items"][2]["enclosure"]["link"] \}\}'
{% endhighlight %}

이렇게 등록할 수 있다. platform: rest를 여러개 복사할 수도 있지만 그렇게 하면 api 콜만 늘어나서 느려진다

sensor의 값이 제대로 나오는 것을 확인했다면 다시 `scripts.yaml`를 수정한다.

{% highlight yaml %}
'play_podcast':
  alias: 팟캐스트 재생
  sequence:
  - service: media_player.play_media
    data_template:
      entity_id: "media_player.my_google_home_mini"
      media_content_id: "\{\{ states.sensor.podcast_url.state \}\}"
      media_content_type: "audio/mp3"
{% endhighlight %}

이제 home assistant의 화면에서 '팟캐스트 재생' 버튼을 누르면 바로 구글홈미니를 통해 최신 에피소드를 들을 수 있다.


