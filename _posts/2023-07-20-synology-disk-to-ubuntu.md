---
layout: post
title: "synology 사용하던 디스크를 ubuntu 에서 복원하기"
date: 2023-07-20T00:00:00
categories: home-server
---

## 상황 및 문제

시놀로지에서 사용하던 디스크를 리눅스에서 복원하려 한다. 4개의 디스크를 SHR 레이드로 사용하였다.

우분투를 설치하고 디스크를 연결했지만 볼륨을 mount 할수 없다.

```
BTRFS critical (device md-2): corrupt leaf: root=1 block=1371553792 slot=17, invalid root flags, have 0x400000000 expect mask 0x1000000000001
```

## 해결 방법 요약

- 우분투를 반드시 18.04 버전을 설치 한다. 최신 버전이면 안된다.
- 4.15.0-108 버전의 리눅스 커널을 사용한다. (우분투 18.04를 써야 하는 이유)
- 커널 버전을 맞춘 후 btrfs 볼륨을 mount할 수 있다.

## 단계별 설명

### ubuntu 18.04 설치

시놀로지 하드 디스크를 연결하지 않은 상태로 우분투 18.04를 설치 한다. [ISO 링크](https://old-releases.ubuntu.com/releases/18.04/)

설치 마지막 단계에서 업데이트 설치는 생략한다. 필요이상 높은 커널 버전을 설치하지 않기 위함이지만, 설치되어도 다운그레이드할 수 있다.

설치 완료 후 `uname -a` 명령어로 커널버전을 확인할 수 있다.

```
kiho@bionic:~$ uname -a
Linux bionic 4.15.0-213-generic #224-Ubuntu SMP Mon Jun 19 13:30:12 UTC 2023 x86_64 x86_64 x86_64 GNU/Linux
```

### 커널 설치

linux-image-4.15.0-108-generic 버전의 커널을 설치한다.

```
sudo apt update
sudo apt install linux-image-4.15.0-108-generic
```

`uname -a ` 명령어 결과가 linux-image-4.15.0-108-generic 보다 높은 버전이라면 위 명령어로 낮은 버전의 커널을 설치해도 자동으로 해당 버전으로 부팅되지 않는다. 아래 작업으로 부팅 시 커널 버전을 선택할 수 있게 한다.

```
sudo vi /etc/default/grub
... 편집기에서 GRUB_TIMEOUT=-1 로 값을 수정하고 저장한다.
sudo update-grub
```

이제 부팅 시 Advanced 메뉴에서 커널 버전을 선택할 수 있다. 재부팅하여 4.15.0-108 버전으로 부팅한다.

![Image]({{ site.url }}{{ site.baseurl }}/assets/images/syno2ubuntu/grub.png){: width="100%"}
![Image]({{ site.url }}{{ site.baseurl }}/assets/images/syno2ubuntu/grub2.png){: width="100%"}

### 디스크 확인

시놀로지에서 분리할 디스크를 모두 연결한 후 부팅한다. 아래 명령어 수행시 md127과 같이 레이드 장치가 인식되어야 한다. md 뒤의 숫자는 다를 수 있다.

```
kiho@bionic:~$ cat /proc/mdstat 
Personalities : [raid6] [raid5] [raid4] [linear] [multipath] [raid0] [raid1] [raid10] 
md127 : active (auto-read-only) raid5 sdc5[1] sdd5[2] sdb5[0] sde5[3]
      68481984 blocks super 1.2 level 5, 64k chunk, algorithm 2 [4/4] [UUUU]
      
unused devices: <none>
```

레이드 장치가 인식되었다면 아래 명령어로 볼륨을 확인한다.

```
kiho@bionic:~$ sudo lvs
  LV                    VG        Attr       LSize  Pool Origin Data%  Meta%  Move Log Cpy%Sync Convert
  ubuntu-lv             ubuntu-vg -wi-ao---- <9.00g                                                    
  syno_vg_reserved_area vg1       -wi-a----- 12.00m                                                    
  volume_1              vg1       -wi-ao---- 65.00g 
```

volume_1 이 시놀로지에서 생성한 볼륨이다. volume_1 과 옆의 vg1 를 기억한다.

이제 mount할 폴더를 만들고 mount 한다.

```
kiho@bionic:~$ mkdir dsm
kiho@bionic:~$ sudo mount /dev/vg1/volume_1 dsm -o ro
kiho@bionic:~$ ls dsm
@database  @eaDir  @S2S  @synoconfd  @SynoFinder-etc-volume  @SynoFinder-log  test  @tmp  @userpreference
kiho@bionic:~$ ls dsm/test
 @eaDir   python-3.11.2-amd64.exe  '#recycle'
kiho@bionic:~$ 
```
mount 명령 뒤 vg1 volume_1 를 lvs 결과에 맞춰 바꿔준다. dsm 폴더 하위에 보이는 파일이 정상인지 확인한다.

커널 버전이 맞지 않은 경우 mount에 실패한다.

```
kiho@bionic:~$ uname -a
Linux bionic 4.15.0-213-generic #224-Ubuntu SMP Mon Jun 19 13:30:12 UTC 2023 x86_64 x86_64 x86_64 GNU/Linux
kiho@bionic:~$ sudo mount /dev/vg1/volume_1 dsm -o ro
mount: /home/kiho/dsm: wrong fs type, bad option, bad superblock on /dev/mapper/vg1-volume_1, missing codepage or helper program, or other error.
kiho@bionic:~$ 
```

## 정보 출처

https://kb.synology.com/tr-tr/DSM/tutorial/How_can_I_recover_data_from_my_DiskStation_using_a_PC

https://www.reddit.com/r/synology/comments/u6y5qm/has_anyone_found_a_solution_for_mounting_synology/?utm_source=share&utm_medium=android_app&utm_name=androidcss&utm_term=1&utm_content=1
