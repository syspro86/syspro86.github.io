---
layout: post
title: "Proxmox - VM에 USB 할당 (usb passthrough)"
date: 2021-04-27 00:00:00 +0900
categories: home-server
permalink: /post/proxmox-usb-passthrough.html
---

### LXC 컨테이너

사용하려는 장치의 bus id, device id를 확인한다.

```
root@pve9:~# lsusb
Bus 002 Device 002: ID 1058:25e2 Western Digital Technologies, Inc. My Passport (WD40NMZW)
Bus 002 Device 001: ID 1d6b:0003 Linux Foundation 3.0 root hub
Bus 001 Device 003: ID 8087:0a2b Intel Corp.
Bus 001 Device 002: ID 2040:b123 Hauppauge
Bus 001 Device 001: ID 1d6b:0002 Linux Foundation 2.0 root hub

root@pve9:~# ls -al /dev/bus/usb/001/002
crw-rw-r-- 1 root root 189, 1 Dec  4 15:52 /dev/bus/usb/001/002
```

ls 명령으로 나온 숫자 (189)를 확인하고 컨테이너 설정 파일에 내용을 추가한다.

```
root@pve9:~# vi /etc/pve/lxc/146.conf

lxc.cgroup.devices.allow: c 189:* rwm
lxc.mount.entry: /dev/bus/usb/001/002 dev/bus/usb/001/002 none bind,optional,create=file
```

usb tv수신기를 추가하는 경우 /dev/dvb 하위에 있는 장치 정보를 추가로 작성한다.

```
root@pve9:/dev/dvb/adapter0# ls -al
total 0
drwxr-xr-x 2 root root     120 Dec  4 15:52 .
drwxr-xr-x 3 root root      60 Dec  4 15:52 ..
crw-rw---- 1 root video 212, 1 Dec  4 15:52 demux0
crw-rw---- 1 root video 212, 2 Dec  4 15:52 dvr0
crw-rw---- 1 root video 212, 0 Dec  4 15:52 frontend0
crw-rw---- 1 root video 212, 3 Dec  4 15:52 net0

root@pve9:~# vi /etc/pve/lxc/146.conf

lxc.cgroup.devices.allow: c 212:* rwm
lxc.mount.entry: /dev/dvb dev/dvb none bind,optional,create=dir
```

작성내용중 두번째 나오는 경로는 /로 시작하지 않는 것에 주의해야 한다.

```
lxc.mount.entry: /dev/bus/usb/001/002 dev/bus/usb/001/002 none bind,optional,create=file
lxc.mount.entry: /dev/dvb dev/dvb none bind,optional,create=dir
```
