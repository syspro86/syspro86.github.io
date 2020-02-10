---
layout: post
title:  "ESXi + Linux 디스크 용량 증설"
date:   2020-02-10 00:00:00 +0900
categories: esxi vmware linux
---

esxi + linux + docker 환경을 계속 사용하며 이미지를 계속 만들다보니 금새 디스크 용량이 다 차버리게 되어,
디스크 용량을 늘리는 과정을 간단히 정리했다.
이 과정에서 서버 재시작은 필요하지 않았다.

전체적인 순서는 아래와 같다.

* 가상 디스크 용량 증설
* 파티션 크기 증설
* 파일시스템 크기 조정


## 가상 디스크 용량 증설

기존 40GB 에서 100 GB 로 증설하려 한다.
기존 VM의 편집 메뉴를 들어가면 하드 디스크 용량을 수정할 수 있다.
(기존보다 크게만 변경할 수 있다.)

![image](https://user-images.githubusercontent.com/31230327/74140301-2de3ba00-4c38-11ea-95aa-da14cfa766a5.png)

## 파티션 크기 증설

`lsblk` 과 `df` 명령어로 파티션과 파일시스템 크기를 확인해보면, 아직은 그대로 40G로 인식되고 있다. (증설하려는 파티션은 /dev/sda2 이다.)

```
kiho@ubuntu-worker2:~$ lsblk
NAME   MAJ:MIN RM  SIZE RO TYPE MOUNTPOINT
loop0    7:0    0 89.1M  1 loop /snap/core/8213
loop1    7:1    0 89.1M  1 loop /snap/core/8268
sda      8:0    0  100G  0 disk 
├─sda1   8:1    0    1M  0 part 
└─sda2   8:2    0   40G  0 part /
sr0     11:0    1 1024M  0 rom  

kiho@ubuntu-worker2:~$ df -h
Filesystem      Size  Used Avail Use% Mounted on
udev            7.4G     0  7.4G   0% /dev
tmpfs           1.5G  1.2M  1.5G   1% /run
/dev/sda2        40G   18G   20G  47% /
tmpfs           7.4G     0  7.4G   0% /dev/shm
tmpfs           5.0M     0  5.0M   0% /run/lock
tmpfs           7.4G     0  7.4G   0% /sys/fs/cgroup
/dev/loop0       90M   90M     0 100% /snap/core/8213
/dev/loop1       90M   90M     0 100% /snap/core/8268
tmpfs           1.5G     0  1.5G   0% /run/user/1000
```

parted 를 통해 파티션 크기를 늘려주어야 하는데, 대상 파티션이 다른 파티션 사이에 껴있으면 뒤쪽 파티션을 이동시켜야하기 때문에 켠 상태로는 작업이 불가능하다.
이 번 경우는 boot 파티션(sda1), root 파티션(sda2) 두가지 밖에 사용하지 않기 때문에 쉽게 늘릴 수 있었다.

* 우선 parted를 실행한다. `sudo parted`

```
kiho@ubuntu-worker2:~$ sudo parted
GNU Parted 3.2
Using /dev/sda
Welcome to GNU Parted! Type 'help' to view a list of commands.
```

* `print` 입력하여 현재 파티션 목록을 확인한다.

```                                                           
(parted) print
Model: VMware Virtual disk (scsi)
Disk /dev/sda: 107GB
Sector size (logical/physical): 512B/512B
Partition Table: gpt
Disk Flags: 

Number  Start   End     Size    File system  Name  Flags
 1      1049kB  2097kB  1049kB                     bios_grub
 2      2097kB  42.9GB  42.9GB  ext4
```

* 증설하려는 파티션은 2번이다. (42.9GB의 ext4파티션)
* `resizepart 2` 를 입력하고 파티션의 변경할 끝지점을 입력한다. (디스크가 107GB로 인식되어 107GB를 입력했다.)

```
(parted) resizepart 2
Warning: Partition /dev/sda2 is being used. Are you sure you want to continue?

Yes/No? y
End?  [42.9GB]? 107GB
```

* `q`를 입력하여 종료한다.

```
(parted) q
Information: You may need to update /etc/fstab.
```

* `lsblk` 명령어로 파티션 크기를 확인한다. (99.7G)

```
kiho@ubuntu-worker2:~$ lsblk
NAME   MAJ:MIN RM  SIZE RO TYPE MOUNTPOINT
loop0    7:0    0 89.1M  1 loop /snap/core/8213
loop1    7:1    0 89.1M  1 loop /snap/core/8268
sda      8:0    0  100G  0 disk 
├─sda1   8:1    0    1M  0 part 
└─sda2   8:2    0 99.7G  0 part /
sr0     11:0    1 1024M  0 rom  
```

## 파일시스템 크기 조정

파일시스템에서는 여전히 40G 로 인식되고 있다.

```
kiho@ubuntu-worker2:~$ df -h
Filesystem      Size  Used Avail Use% Mounted on
udev            7.4G     0  7.4G   0% /dev
tmpfs           1.5G  1.2M  1.5G   1% /run
/dev/sda2        40G   18G   20G  47% /
tmpfs           7.4G     0  7.4G   0% /dev/shm
tmpfs           5.0M     0  5.0M   0% /run/lock
tmpfs           7.4G     0  7.4G   0% /sys/fs/cgroup
/dev/loop0       90M   90M     0 100% /snap/core/8213
/dev/loop1       90M   90M     0 100% /snap/core/8268
tmpfs           1.5G     0  1.5G   0% /run/user/1000
```

* `resize2fs /dev/sda2` 를 입력하여 파일시스템 크기를 자동 조정한다. (파티션 크기에 맞게)

```
kiho@ubuntu-worker2:~$ sudo resize2fs /dev/sda2
resize2fs 1.44.1 (24-Mar-2018)
Filesystem at /dev/sda2 is mounted on /; on-line resizing required old_desc_blocks = 5, new_desc_blocks = 13
The filesystem on /dev/sda2 is now 26122535 (4k) blocks long.
```

이제 98G 로 잘 인식된다.

```
kiho@ubuntu-worker2:~$ df -h
Filesystem      Size  Used Avail Use% Mounted on
udev            7.4G     0  7.4G   0% /dev
tmpfs           1.5G  1.2M  1.5G   1% /run
/dev/sda2        98G   18G   77G  19% /
tmpfs           7.4G     0  7.4G   0% /dev/shm
tmpfs           5.0M     0  5.0M   0% /run/lock
tmpfs           7.4G     0  7.4G   0% /sys/fs/cgroup
/dev/loop0       90M   90M     0 100% /snap/core/8213
/dev/loop1       90M   90M     0 100% /snap/core/8268
tmpfs           1.5G     0  1.5G   0% /run/user/1000
```
