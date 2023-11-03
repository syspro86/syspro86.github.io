---
layout: post
title: "Proxmox - VM에 GPU 할당 (GPU Passthrough)"
date: 2022-05-16 00:00:00 +0900
categories: home-server
---

원본글: https://www.reddit.com/r/homelab/comments/b5xpua/the_ultimate_beginners_guide_to_gpu_passthrough/

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

### Win10 UEFI 로 설치하기

- General
  - Name: win10
- OS
  - ISO image: windows10.iso
  - Guest OS Type : Win10/2016/2019
- System
  - Machine: q35
  - BIOS: OVMF
  - Add EFI Disk: check
  - EFI Storage: local-lvm
  - Pre-Enroll keys: 체크해제
- Disks
  - 기본

부팅 후 윈도우 설치 진행. 윈도우 업데이트 22H2 까지 완료 후 VM 종료.

- Hardware - Add - PCI Device - GPU 선택
  - All Function: 체크 (체크하지 않으면 오디오 장치가 잡히지 않음)
  - Primary GPU: X
  - Rom-BAR: 체크
  - PCI-Express: 체크

윈도우 기동 후, NVIDIA driver 설치. 원격 접속 환경 설정 steam link 혹은 parsec 까지 설치.

윈도우 VM 종료 후, GPU PCI Device 의 Primary GPU 체크 적용한 후 VM 기동.

Hardware 옵션 중 Display를 none으로 바꾸면 proxmox host가 재부팅되는 현상이 있어 Default 값 유지함.

### Win10 UEFI 설치 Disk 를 SCSI 방식으로 사용하려면 (선택사항)

설치를 위해 VM 기동하기 전에 CD/DVD Drvie를 하나 더 추가하고 virtio-win iso 파일을 지정해준다.

윈도우 설치 중 디스크를 찾지 못하면 virtio iso 내에 vioscsi/w10/amd64 폴더를 지정하여 드라이버를 설치하면 디스크가 인식된다.

### GPU ROM 수정/지정하기 (선택사항)

GPU ROM dump
```
cd /sys/bus/pci/devices/0000:08:00.0/
echo 1 > rom
cat rom > /tmp/image.rom
echo 0 > rom
```

ROM 파일 수정
```
git clone https://github.com/Matoking/NVIDIA-vBIOS-VFIO-Patcher
cd NVIDIA-vBIOS-VFIO-Patcher
python nvidia_vbios_vfio_patcher.py -i /tmp/image.rom -o /tmp/image.pached.rom --disable-footer-strip
cp /tmp/image.pached.rom /usr/share/kvm/gpu.pached.rom
```

qemu conf 수정 (/etc/pve/qemu-server/xxx.conf)
```
cpu: host,hidden=1
hostpci0: 0000:08:00,pcie=1,romfile=gpu.patched.rom
```

### 오류 해결

#### vfio-pci 0000:08:00.0: BAR 3: can't reserve [mem 0xf0000000-0xf1ffffff 64bit pref]

장치를 제거한 후 다시 검색하여 추가한다.

```
echo 1 > /sys/bus/pci/devices/0000\:08\:00.0/remove
echo 1 > /sys/bus/pci/rescan
```

필요 시 crontab에 @reboot 로 추가하여 실행.
