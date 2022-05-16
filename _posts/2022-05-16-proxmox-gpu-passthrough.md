---
layout: post
title: "Proxmox - VM에 GPU 할당 (GPU Passthrough)"
date: 2021-04-27 00:00:00 +0900
categories: home-server
---

https://www.reddit.com/r/homelab/comments/b5xpua/the_ultimate_beginners_guide_to_gpu_passthrough/

### PVE 에서 설정

/etc/default/grub 파일 수정

```
GRUB_CMDLINE_LINUX_DEFAULT="quiet amd_iommu=on iommu=pt pcie_acs_override=downstream,multifunction nofb video=efifb:off vga=off"
```

grub 업데이트

```
update-grub
```

vfio 를 읽어오도록 /etc/modules 파일 수정

```
vfio
vfio_iommu_type1
vfio_pci
vfio_virqfd
```

host 에서 gpu를 인식하지 않도록 blacklist 등록

```
echo "blacklist radeon" >> /etc/modprobe.d/blacklist.conf
echo "blacklist nouveau" >> /etc/modprobe.d/blacklist.conf
echo "blacklist nvidia" >> /etc/modprobe.d/blacklist.conf
```

vfio 정보를 등록

```
lspci -v # 결과에서 GPU 이름앞 01:00 형태 숫자를 찾는다.
lspci -n -s 01:00 # 결과에서 aaaa:bbbb 형태 값을 찾는다.

# ids 부분을 위 명령 결과값으로 바꾼다.
echo "options vfio-pci ids=10de:1b81,10de:10f0 disable_vga=1"> /etc/modprobe.d/vfio.conf
```

initramfs 를 업데이트하고 재부팅

```
update-initramfs -u
reset
```

이후 GPU 를 사용할 VM 설정

- machine 유형을 q35로 설정
- Hardware - Add - PCI Device - GPU 선택
  - All Function: 체크
  - Primary GPU: X
  - Rom-BAR: 체크
  - PCI-Express: 체크
