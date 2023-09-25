---
layout: post
title: "Proxmox 위에 Home Assistant OS 설치 구성하기"
date: 2023-09-25T00:00:00
categories: home-assistant
---

기존 home assistant 를 container 방식으로 사용중이었는데, addon 사용불가 및 업데이트 시 적용 번거로움 등으로 home assistant os 버전으로 다시 설치하기로 했다.

## 설치 진행

home assistant os 는 별도 설치 iso 가 있지 않고, 이미 설치 구성된 vm disk 를 사용하여 만든다.

공식 다운로드 페이지 [https://www.home-assistant.io/installation/linux](https://www.home-assistant.io/installation/linux)

proxmox는 qemu/kvm 방식이므로 페이지 KVM 혹은 [이 링크](https://github.com/home-assistant/operating-system/releases/download/10.5/haos_ova-10.5.qcow2.xz) 를 서버에서 직접 내려받고 압축을 해제한다.

```
root@pve:~# cd /var/lib/vz/images/
root@pve:/var/lib/vz/images# wget https://github.com/home-assistant/operating-system/releases/download/10.5/haos_ova-10.5.qcow2.xz
...생략
2023-09-25 21:09:00 (58.2 MB/s) - ‘haos_ova-10.5.qcow2.xz’ saved [333641700/333641700]

root@pve:/var/lib/vz/images# unxz haos_ova-10.5.qcow2.xz 
root@pve:/var/lib/vz/images# ls
haos_ova-10.5.qcow2
```

## VM 생성하기

아래 그림과 같이 VM 을 생성한다.

![Image]({{ site.url }}{{ site.baseurl }}/assets/images/proxmox-haos/vm1.png){: width="100%"}

- VMID와 이름을 지정한다.

![Image]({{ site.url }}{{ site.baseurl }}/assets/images/proxmox-haos/vm2.png){: width="100%"}

- Do no use any media 를 선택한다.

![Image]({{ site.url }}{{ site.baseurl }}/assets/images/proxmox-haos/vm3.png){: width="100%"}

- BIOS: OVMF (UEFI) 로 변경
- EFI Storage: local-lvm (혹은 원하는 스토리지로)
- Pre-Enroll keys: 체크해제
- SCSI Controller: VirtIO SCSI

![Image]({{ site.url }}{{ site.baseurl }}/assets/images/proxmox-haos/vm4.png){: width="100%"}

- 기본 Disk 를 제거한다.
- 이후 CPU core 2이상 Memory 2048 이상으로 설정하여 생성한다.


## Disk 추가하기

처음에 내려받은 qcow2 디스크 파일을 vm 에 추가한다.

```
root@pve:/var/lib/vz/images# qm importdisk 100 /var/lib/vz/images/haos_ova-10.5.qcow2 local-lvm
importing disk '/var/lib/vz/images/haos_ova-10.5.qcow2' to VM 100 ...
  Logical volume "vm-100-disk-1" created.
transferred 0.0 B of 32.0 GiB (0.00%)
transferred 370.3 MiB of 32.0 GiB (1.13%)
...
transferred 32.0 GiB of 32.0 GiB (100.00%)
Successfully imported disk as 'unused0:local-lvm:vm-100-disk-1'
root@pve:/var/lib/vz/images# 
```
![Image]({{ site.url }}{{ site.baseurl }}/assets/images/proxmox-haos/vm5.png){: width="100%"}

- Hardware 화면의 Unused Disk 0 를 더블클릭 후 Add 해준다.

![Image]({{ site.url }}{{ site.baseurl }}/assets/images/proxmox-haos/vm6.png){: width="100%"}

- Options 화면에서 Boot Order 를 기존 항목 모두 체크하고, 방금 추가한 scsi0 만 체크해준다.

## VM 시작

이제 VM 을 시작하고 잠시 기다린 후에 http://homeassistant.local:8123 로 접속한다.
연결이 안된다면 VM 의 Console 화면에서 IP 를 확인할 수 있다. 확인된 IP 로 연결한다. http://192.168.1.xx:8123 

### IP 고정하기

계정을 생성하여 로그인한 후에, 설정 > 시스템 > 네트워크 화면에서 IPv4 클릭한 후 원하는 IP 를 지정한다.

혹은 VM 의 Console 화면에서 직접 변경할 수 있다.

```
ha > network update enp0s18 --ipv4-address 192.168.1.xx/24 --ipv4-gateway 192.168.1.1 --ipv4-method static --ipv4-nameserver 192.168.1.1
```

### 설치한 애드온 

- Studio Code Server: haos 내에서 visual studio code 를 사용하여 설정파일을 편집할 수 있다.
- Google Drive Backup: haos 설정을 google drive로 백업해준다. (https://github.com/sabeechen/hassio-google-drive-backup)

- Home Assistant Chrome 확장: 크롬브라우저의 확장으로 대시보드 화면을 바로 불러와서 제어할 수 있다. (https://chrome.google.com/webstore/detail/home-assistant/hpoiflhmfklhfcfpibmdmpeonphmdbda)

### 주요 설정값 (configuration.yaml)

- 크롬 확장에서 HAOS 화면을 띄울 때 차단되지 않게 한다.

```
http:
  use_x_frame_options: false
```

- cloudflare ddns proxy 를 사용할 경우, 그외 접근을 차단한다. (cloudflare ip 대역 + 공유기대역, docker, kubernetes)

```
http:
  use_x_forwarded_for: true
  trusted_proxies:
    - 192.168.0.0/16
    - 172.17.0.1
    - 10.42.0.0/16
    - 173.245.48.0/20
    - 103.21.244.0/22
    - 103.22.200.0/22
    - 103.31.4.0/22
    - 141.101.64.0/18
    - 108.162.192.0/18
    - 190.93.240.0/20
    - 188.114.96.0/20
    - 197.234.240.0/22
    - 198.41.128.0/17
    - 162.158.0.0/15
    - 104.16.0.0/13
    - 104.24.0.0/14
    - 172.64.0.0/13
    - 131.0.72.0/22
```
