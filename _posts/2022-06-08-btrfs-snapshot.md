---
layout: post
title: "btrfs 로 snapshot 관리하기"
date: 2022-06-08T00:00:00
categories: home-server
---

## btrfs

openmediavault 에서 사용하던 파일 시스템을 ext4 에서 btrfs 로 변경했다. 명령어 실수로 파일을 일부 삭제해버린 후 snapshot 기능이 필요하다 생각했기 때문인데, 혹시 발생할 랜섬웨어 공격에도 대응될 것으로 보인다.

## btrfs 서브볼륨 생성

btrfs 에는 서브볼륨이라는 개념이 있는데, 이 단위로 스냅샷을 만들고 롤백할 수 있다. btrfs 파티션을 마운트한 곳 하위에 서브볼륨을 만든다.

```
root@openmediavault:~/disk# btrfs su cr test
Create subvolume './test'
```

서브볼륨 생성을 확인한다. 일반 폴더처럼 보인다.

```
root@openmediavault:~/disk# ls -al
total 24
drwxr-xr-x  1 root root   136 Jun  8 21:03 .
drwxr-xr-x  7 root root  4096 Nov  1  2021 ..
drwxr-xr-x  1 root root     0 Jun  8 21:09 test
root@openmediavault:~/disk#
```

테스트용으로 파일을 하나 생성했다.

```
root@openmediavault:~/disk# echo "Test" > test/abc
root@openmediavault:~/disk# ls -al test
total 20
drwxr-xr-x 1 root root   6 Jun  8 21:11 .
drwxr-xr-x 1 root root 136 Jun  8 21:11 ..
-rw-r--r-- 1 root root   5 Jun  8 21:11 abc
```

## 스냅샷 생성

현재 test 서브볼륨 상태에서 testsnap 라는 이름으로 스냅샷을 생성했다. -r 옵션은 read only 를 뜻한다.

```
root@openmediavault:~/disk# btrfs su snap -r test testsnap
Create a readonly snapshot of 'test' in './testsnap'

root@openmediavault:~/disk# ls -al
total 24
drwxr-xr-x  1 root root   136 Jun  8 21:03 .
drwxr-xr-x  7 root root  4096 Nov  1  2021 ..
drwxr-xr-x  1 root root     0 Jun  8 21:09 test
drwxr-xr-x  1 root root     0 Jun  8 21:09 testsnap
```

testsnap 스냅샷 서브볼륨에도 동일하게 abc 파일이 보인다.

```
root@openmediavault:~/disk# ls -al testsnap
total 20
drwxr-xr-x 1 root root   6 Jun  8 21:11 .
drwxr-xr-x 1 root root 152 Jun  8 21:12 ..
-rw-r--r-- 1 root root   5 Jun  8 21:11 abc
```

원본인 test 서브볼륨의 파일을 수정하고 각각의 파일을 확인해보면, 스냅샷쪽은 이전상태 그대로 유지된다.

```
root@openmediavault:~/disk# echo "changed" > test/abc
root@openmediavault:~/disk# cat test/abc
changed
root@openmediavault:~/disk# cat testsnap/abc
Test
```

## 스냅샷 롤백

롤백과정은 현재의 서브볼륨을 지우고, 스냅샷에서 반대로 다시 서브볼륨을 만드는 것이다. 스냅샷 생성때와 다르게 -r 옵션이 없으며 read,write 가 모두 가능해진다. 즉 스냅샷 상태로 돌아가는 것이다.

```
root@openmediavault:~/disk# btrfs su del test
Delete subvolume (no-commit): '/srv/dev-disk-by-id-scsi-0QEMU_QEMU_HARDDISK_drive-scsi2-part1/test'
root@openmediavault:~/disk# btrfs su snap testsnap test
Create a snapshot of 'testsnap' in './test'
root@openmediavault:~/disk# cat test/abc
Test
```

## 매일 스냅샷 생성하기

내 파일 서버의 경우 btrfs 최상위 밑에 용도에 따라 서브볼륨들을 생성했으며, snapshot 폴더 밑에 서브볼륨과 동일한 폴더들을 만들어 각각 밑에 날짜별 스냅샷을 생성했다.

서브 볼륨 목록. snapshot 은 단순 폴더이다.

```
root@openmediavault:~/disk# ls -al
total 24
drwxr-xr-x  1 root root   128 Jun  8 21:25 .
drwxr-xr-x  7 root root  4096 Nov  1  2021 ..
-rwxr-xr-x  1 root root   313 Dec 11 03:41 daily_snapshot.sh
drwxr-xr-x  1 root root   122 Apr 17 02:47 dockerdata
drwxrwsrwx+ 1 root users  114 Apr 20 22:56 document
drwxr-sr-x  1 root users   70 Dec 11 03:40 snapshot
drwxrwsr-x+ 1 root users  180 Apr 18 19:33 workspace
```

snapshot 폴더 밑에는 서브볼륨과 동일한 폴더들을 만들었다.

```
root@openmediavault:~/disk# ls -al snapshot
total 16
drwxr-sr-x 1 root users   70 Dec 11 03:40 .
drwxr-xr-x 1 root root   128 Jun  8 21:25 ..
drwxr-sr-x 1 root users 2048 Jun  8 00:00 dockerdata
drwxr-sr-x 1 root users 3456 Jun  8 00:00 document
drwxr-sr-x 1 root users 3472 Jun  8 00:00 workspace
```

각 폴더 밑에는 날짜별 스냅샷이 생성된다.

```
root@openmediavault:~/disk# ls -al snapshot/dockerdata/
total 0
drwxr-sr-x 1 root users 2048 Jun  8 00:00 .
drwxr-sr-x 1 root users   70 Dec 11 03:40 ..
drwxr-xr-x 1 root root   222 Jan 10 21:42 20220201
drwxr-xr-x 1 root root   222 Jan 10 21:42 20220202
drwxr-xr-x 1 root root   222 Jan 10 21:42 20220203
drwxr-xr-x 1 root root   222 Jan 10 21:42 20220204
.............
생략
.............
drwxr-xr-x 1 root root   122 Apr 17 02:47 20220606
drwxr-xr-x 1 root root   122 Apr 17 02:47 20220607
drwxr-xr-x 1 root root   122 Apr 17 02:47 20220608
```

COW (copy on write) 방식으로 저장되기 때문에, 변경사항이 적다면 스냅샷이 차지하는 용량은 매우 적다.

아래와 같이 쉘파일을 만들어 crontab 혹은 스케줄러에 등록하면 매일 스냅샷이 생성된다.

```
#/bin/bash

ddd=`date +"%Y%m%d"`
cd $(dirname $0)

btrfs subvolume snapshot -r dockerdata snapshot/dockerdata/${ddd}
btrfs subvolume snapshot -r document   snapshot/document/${ddd}
btrfs subvolume snapshot -r workspace  snapshot/workspace/${ddd}
```

## 스냅샷 삭제

파일 변경이 누적되어 오래된 스냅샷들을 지우고 싶다면 btrfs su del 명령어를 사용한다. 아래와 같이 파일명 패턴으로 오래된 스냅샷들을 일괄 삭제할 수 있다.

```
btrfs su del snapshot/dockerdata/202202*
```

---

## 그외 명령어

## btrfs 사이즈 조정

btrfs 이 할당된 vm disk나 파티션 크기가 증가되었을 때, btrfs 에서 인식하도록 한다. 마지막 경로는 디스크가 mount 된 위치다.

```
root@openmediavault:~/disk# btrfs fi resize max .
Resize '.' of 'max'
```
