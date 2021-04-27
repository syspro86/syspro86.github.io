---
layout: post
title:  "Proxmox 서버 운영"
date:   2020-12-06 00:00:00 +0900
categories: home-server
permalink: /post/proxmox.html
---

### Proxmox

#### swap 설정하기

```
# 현재 값 확인
cat /proc/sys/vm/swappiness

# 최대한 swap을 활용하지 않도록 한다. 끄는것이 아님.
sysctl vm.swappiness=0

# swap을 껐다켜서 적용한다.
swapoff -a
swapon -a

# 바뀐 값 확인
cat /proc/sys/vm/swappiness
```

### 스토리지

- [LVM 볼륨 관리](/post/proxmox-lvm.html)

### 장치 할당

- [USB Passthrough](/post/proxmox-usb-passthrough.html)

### 클러스터

절반 이상 노드가 down 되어 vm 시작/종료 등이 안될때 votes 수를 임시 조정한다.
```
pvecm expected 1
# 1 대신 온라인 노드 수를 입력한다.
```
