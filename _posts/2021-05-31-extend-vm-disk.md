---
layout: post
title: "Proxmox - VM 디스크 사이즈 증가"
date: 2021-05-31T21:57:00
categories: home-server
permalink: /post/proxmox-extend-vm-disk.html
---

### VM 에 할당된 디스크 사이즈 증가

shell 에서 아래 스크립트를 수행하거나 웹에서 디스크 사이즈를 늘린다.

```
# 증가시킬 디스크를 우선 확인한다.

root@pve:~# qm config 121 | grep scsi
bootdisk: scsi0
scsi0: local-lvm:vm-121-disk-0,size=20G
scsi1: hdd_tp:vm-121-disk-0,backup=0,size=8T
scsihw: virtio-scsi-pci

# 변경할 사이즈를 입력한다.
root@pve:~# qm resize 121 scsi1 10T
  Size of logical volume hdd_vg/vm-121-disk-0 changed from 8.00 TiB (2097152 extents) to 10.00 TiB (2621440 extents).
  Logical volume hdd_vg/vm-121-disk-0 successfully resized.
root@pve:~#
```

### VM 내부에서 파티션 크기 및 파일 시스템 크기 증가

대상 파티션을 확인한다.

```
root@openmediavault:~# lsblk
NAME   MAJ:MIN RM  SIZE RO TYPE MOUNTPOINT
sda      8:0    0   20G  0 disk
├─sda1   8:1    0   16G  0 part /
├─sda2   8:2    0    1K  0 part
└─sda5   8:5    0    4G  0 part [SWAP]
sdb      8:16   0   10T  0 disk
└─sdb1   8:17   0    8T  0 part /srv/dev-disk-by-label-hard1
sr0     11:0    1 1024M  0 rom
```

/dev/sdb1 를 최대 크기로 확장한다.

```
root@openmediavault:~# fdisk /dev/sdb

Welcome to fdisk (util-linux 2.33.1).
Changes will remain in memory only, until you decide to write them.
Be careful before using the write command.

The backup GPT table is not on the end of the device. This problem will be corrected by write.

Command (m for help): d

Selected partition 1
Partition 1 has been deleted.

Command (m for help): n
Partition number (1-128, default 1):
First sector (34-21474836446, default 2048):
Last sector, +/-sectors or +/-size{K,M,G,T,P} (2048-21474836446, default 21474836446):

Created a new partition 1 of type 'Linux filesystem' and of size 10 TiB.
Partition #1 contains a ext4 signature.

Do you want to remove the signature? [Y]es/[N]o: n    <-- 반드시 n으로 입력

Command (m for help): w

The partition table has been altered.
Calling ioctl() to re-read partition table.
Syncing disks.
```

다음은 파티션 크기 증가인데, 왜인지 파일시스템 크기가 안늘어난다.

```
root@openmediavault:~# resize2fs /dev/sdb1
resize2fs 1.46.2 (28-Feb-2021)
The filesystem is already 2147483387 (4k) blocks long.  Nothing to do!
```

parted 에서 다시 파티션 크기를 지정해준다.

```
root@openmediavault:~# parted /dev/sdb
GNU Parted 3.2
Using /dev/sdb
Welcome to GNU Parted! Type 'help' to view a list of commands.
(parted) resizepart 1
Warning: Partition /dev/sdb1 is being used. Are you sure you want to continue?
Yes/No? y
End?  [11.0TB]?    <-- 엔터만 입력한다
(parted) q
Information: You may need to update /etc/fstab.

root@openmediavault:~#
```

이제 늘어난다.

```
root@openmediavault:~# resize2fs /dev/sdb1
resize2fs 1.46.2 (28-Feb-2021)
Filesystem at /dev/sdb1 is mounted on /srv/dev-disk-by-label-hard1; on-line resizing required
old_desc_blocks = 1024, new_desc_blocks = 1280
The filesystem on /dev/sdb1 is now 2684354299 (4k) blocks long.

root@openmediavault:~# lsblk
NAME   MAJ:MIN RM  SIZE RO TYPE MOUNTPOINT
sda      8:0    0   20G  0 disk
├─sda1   8:1    0   16G  0 part /
├─sda2   8:2    0    1K  0 part
└─sda5   8:5    0    4G  0 part [SWAP]
sdb      8:16   0   10T  0 disk
└─sdb1   8:17   0   10T  0 part /srv/dev-disk-by-label-hard1
sr0     11:0    1 1024M  0 rom

# sdb1도 10T로 확인.
```

만약 파티션이 파일시스템이 아닌 LVM 파티션이라면 아래 작업을 추가해준다.

```
kiho@ubuntu-54:/mnt$ sudo pvresize /dev/sdb1
  Physical volume "/dev/sdb1" changed
  1 physical volume(s) resized or updated / 0 physical volume(s) not resized

# -l+100%FREE 는 남은 디스크를 모두 할당해준다
kiho@ubuntu-54:/mnt$ sudo lvextend -l+100%FREE -r docker_vg/docker_lv
  Size of logical volume docker_vg/docker_lv changed from <50.00 GiB (12799 extents) to <100.00 GiB (25599 extents).
  Logical volume docker_vg/docker_lv successfully resized.
resize2fs 1.45.5 (07-Jan-2020)
Filesystem at /dev/mapper/docker_vg-docker_lv is mounted on /var/lib/docker; on-line resizing required
old_desc_blocks = 7, new_desc_blocks = 13
The filesystem on /dev/mapper/docker_vg-docker_lv is now 26213376 (4k) blocks long.
```
