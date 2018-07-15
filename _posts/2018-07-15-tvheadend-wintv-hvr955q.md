---
layout: post
title:  "TVHeadend + WinTV HVR955Q 지상파 시청 및 자동 녹화 구성하기"
date:   2018-07-15 22:00:00 +0900
categories: home-server
---

이 글은 케이블TV 가입 없이 TV수신카드와 디지털 안테만을 설치한 경우 TVheadend를 활용하는 방법을 정리한 글이다.

인터넷의 많은 TVheadend 강좌글이 통신사 케이블TV 사용자 기준으로 작성되어 있어, 설치하는 과정에서 다른점을 위주로 작성하였다.

## WinTV HVR955Q

이 제품은 USB 형태의 TV수신카드이다. 

나의 경우에는 지상파용 디지털 안테나를 함께 구매하여 설치하였고

Ubuntu 18.04 LTS 가 설치된 NUC PC에 TV카드 + 안테나전원을 같이 연결하였다.

(Hauppauge사에서 나온 제품들은 동일한 방법으로 적용가능할 것으로 본다.)

### TV수신카드의 정상작동 여부 확인

우선 TV수신카드를 윈도우에 연결하여 정상적으로 채널이 검색되는지 확인하는 것을 추천한다. 전파가 정상적으로 수신되는지 확인해야 한다.

번들로 제공되는 WinTV플레이어 프로그램을 사용하거나, 다음팟플레이어로도 채널 검색이 가능하다.

정상여부를 확인했다면 다시 리눅스 머신에 연결한다.

### 장치 인식 여부 확인

* `lsusb`

{% highlight raw %}
Bus 002 Device 001: ID xxxx:xxxx Linux Foundation 3.0 root hub
Bus 001 Device 002: ID xxxx:xxxx Hauppauge
Bus 001 Device 001: ID xxxx:xxxx Linux Foundation 2.0 root hub
...
{% endhighlight %}

목록에 Hauppauge 항목이 있어야 한다.

목록에 없는 경우 아래 명령어를 입력한 후 다시 lsusb로 확인한다.

* `sudo modprobe cx231xx`
* `sudo modprobe tveeprom`
* `sudo modprobe cx25840`
* `sudo modprobe /lib/firmware/v4l-cx231xx-avcore-01.fw`

`/lib/firmware/v4l-cx231xx-avcore-01.fw` 파일이 없는 경우 찾아서 넣어야 한다. Ubuntu 18.04 LTS의 경우 기본으로 설치되어 있었다.

나의 경우 바로 인식되어있지만, 계속 인식되지 않는다면 아래 명령어로 나오는 로그를 검색하여 해결하기 바란다.

* `sudo dmesg -C`
* tv수신카드 usb 다시 연결
* `sudo dmesg`

### 채널 주파수 검색

인터넷으로 검색하여 받은 주파수 정보는 어느 것을 사용해도 제대로 채널이 잡히지 않았다.

아래 명령어를 통해 본인의 환경에서 잡히는 주파수 정보를 얻을 수 있다.

* `sudo w_scan -fa -A1 -c KR -X`

{% highlight raw %}
w_scan -fa -A1 -c KR -X 
w_scan version 20170107 (compiled for DVB API 5.10)
using settings for KOREA, REPUBLIC OF
Country identifier KR not defined. Using defaults.
scan type TERRCABLE_ATSC, channellist 1
output format czap/tzap/szap/xine
output charset 'UTF-8', use -C <charset> to override
Info: using DVB adapter auto detection.
 /dev/dvb/adapter0/frontend0 -> TERRCABLE_ATSC "LG Electronics LGDT3306A VSB/QAM Frontend": good :-)
Using TERRCABLE_ATSC frontend (adapter /dev/dvb/adapter0/frontend0)
-_-_-_-_ Getting frontend capabilities-_-_-_-_ 
Using DVB API 5.10
frontend 'LG Electronics LGDT3306A VSB/QAM Frontend' supports
INVERSION_AUTO
8VSB
QAM_64
QAM_256
FREQ (54.00MHz ... 858.00MHz)
-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_ 
57000: 8VSB(time: 00:00.200) 
63000: 8VSB(time: 00:03.360) 
... 생략
213000: 8VSB(time: 00:35.017) 
473000: 8VSB(time: 00:38.185)         signal ok: 8VSB     f=473000 kHz (0:0:0)
479000: 8VSB(time: 00:38.965)         signal ok: 8VSB     f=479000 kHz (0:0:0)
485000: 8VSB(time: 00:39.757)         signal ok: 8VSB     f=485000 kHz (0:0:0)
491000: 8VSB(time: 00:40.505)         signal ok: 8VSB     f=491000 kHz (0:0:0)
497000: 8VSB(time: 00:41.257)         signal ok: 8VSB     f=497000 kHz (0:0:0)
503000: 8VSB(time: 00:42.001) 
... 생략
..
MBC:473000000:VSB_8:17:20:1
:479000000:VSB_8:33:36:2
:485000000:VSB_8:17:20:1
:491000000:VSB_8:33:36:2
:497000000:VSB_8:17:20:1
:497000000:VSB_8:33:36:2
:659000000:VSB_8:33:36:2
:665000000:VSB_8:17:20:1
:665000000:VSB_8:33:36:2
Done, scan time: 03:33.006
{% endhighlight %}

마지막에 나타나는 MBC:473000000:VSB_8:17:20:1 와 같은 형태의 데이터가 중요하다

MBC 채널의 경우 473000000 주파수의 VSB8 방식이라는 의미이다. 이것을 기준으로 Mux 정보를 등록할 것이다.

위 내용을 일단 파일로 저장해둔다.

## tvheadend 설치

tvheadend의 경우 docker이미지도 여러 이미지가 공유되고 있지만

TV수신카드가 USB 제품이다 보니 인식이 되지 않았고

tvheadend의 가이드에 따라 docker가 아닌 ubuntu환경에 직접 설치 하였다.

(버전별로 UI환경의 명칭이 다소 차이가 있다. 내가 설치한 버전은 HTS Tvheadend 4.3-1251~gf4ebe3389 이다.)

* `wget -qO- https://doozer.io/keys/tvheadend/tvheadend/pgp | sudo apt-key add -`
* `echo "deb http://apt.tvheadend.org/unstable bionic main" | sudo tee -a /etc/apt/sources.list.d/tvheadend.list`
  * (ubuntu 18.04 = bionic,  ubuntu 16.04 = xenial)
* `sudo apt-get update`
* `sudo apt-get install tvheadend`

설치 시에 관리자 계정 ID,PW를 입력하게 되고, 완료 후 서버가 바로 실행된다.

* http://my-server:9981/ 자신의 서버로 접속하여 로그인한다
* Configuration > General > Base 화면으로 이동한다.
  * ![Image]({{ site.url }}{{ site.baseurl }}/assets/images/tvheadend/tvheadend(0).png){: height="400px"}
  * Web Interface Settings의 Default view level를 Expert로 변경하고 Save를 누른다.
* Configuration > DBV Inputs > Networks 화면으로 이동한다.
  * ![Image]({{ site.url }}{{ site.baseurl }}/assets/images/tvheadend/tvheadend(1).png){: height="400px"}
  * Add > ATSC-T 타입을 선택한다.
  * ![Image]({{ site.url }}{{ site.baseurl }}/assets/images/tvheadend/tvheadend(2).png){: height="400px"}
  * Network name을 ATSC-T Network 로 입력한다.
  * Character set을 UTF-8 로 선택한다.
  * EIT time offset을 UTF+ 9 로 선택한다.
  * Create를 누른다.
* Configuration > DBV Inputs > TV adapters 화면으로 이동한다.
  * ![Image]({{ site.url }}{{ site.baseurl }}/assets/images/tvheadend/tvheadend(3).png){: height="400px"}
  * LG Electronics LGDT3306A VSB/QAM Frontend #0 : ATSC-T #0를 선택하고 Enabled를 체크한다.
  * Networks를 ATSC-T Network 체크한 후 Save를 눌러 저장한다.
* Configuration > DBV Inputs > Muxes 화면으로 이동한다.
  * ![Image]({{ site.url }}{{ site.baseurl }}/assets/images/tvheadend/tvheadend(4).png){: height="400px"}
  * Add > ATSC-T Network 를 선택한다.
  * ![Image]({{ site.url }}{{ site.baseurl }}/assets/images/tvheadend/tvheadend(5).png){: height="400px"}
  * Frequency를 입력한다. (스캔 단계에서 저장해뒀던 주파수 입력)
  * Modulation를 VSB/8로 변경한다
  * Accept zero value for TSID를 체크한다.
  * EIT - skip TSID check를 체크한다.
  * Create를 누른다.
  * 목록에 추가된 항목은 Scan status 값이 PEND나 ACTIVE에서 IDLE로 변경되길 기다린다.
  * Scan result가 OK를 확인한다.
  * 저장해뒀던 정보를 주파수 별로 반복해서 등록한다. (지상파만 등록할 것이기 때문에 수작업으로 등록하였다.)
* Configuration > DBV Inputs > Services 화면으로 이동한다.
  * ![Image]({{ site.url }}{{ site.baseurl }}/assets/images/tvheadend/tvheadend(6).png){: height="400px"}
  * 지상파 채널별로 등록된 목록에서 맨왼쪽의 재생 버튼을 누르면 m3u 파일이 다운로드 되고 동영상 플레이어로 시청할 수 있다.

## EPG를 통해 편성표 정보 등록하기

![Image]({{ site.url }}{{ site.baseurl }}/assets/images/tvheadend/epg.png){: height="400px"}

추가 예정

## Kodi 를 통해 지상파 시청하기

추가 예정
