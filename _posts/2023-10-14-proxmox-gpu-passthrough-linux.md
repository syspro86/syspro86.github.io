---
layout: post
title: "Proxmox - Ubuntu VM에 GPU 할당 (GPU Passthrough)"
date: 2023-10-14 00:00:00 +0900
categories: home-server
---

proxmox 에서 설정하는 것은 이전 글과 동일한데, ubuntu vm에서 gpu를 사용할 때 필요한 내용을 정리했다.

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

### Ubuntu 22.04 UEFI 로 설치하기

- General
  - Name: ubuntu-gpu
- OS
  - ISO image: ubuntu iso 선택
- System
  - Machine: q35
  - BIOS: OVMF
  - Add EFI Disk: check
  - EFI Storage: local-lvm

VM 을 생성한 후, 초기 부팅 시 BIOS 옵션을 수정해야 한다.

![Image]({{ site.url }}{{ site.baseurl }}/assets/images/proxmox-gpu-ubuntu/boot1.png){: width="100%"}

- Device Manager ->  Secure Boot Configuration -> Attempt Secure Boot 에서 Spacebar 를 눌러 체크 해제 한다.
- F10 -> Y 눌러 저장한 뒤, Esc Esc -> Reset 으로 재부팅한다.

이제 Ubuntu 를 설치 진행한 후에, VM 을 종료한다.

### GPU 추가하기

- Hardware - Add - PCI Device - GPU 선택
  - Primary GPU: X
  - Rom-BAR: 체크
  - PCI-Express: 체크

VM 을 기동한 후에 드라이버를 설치한다.

```
sudo apt install nvidia-driver-495 
sudo reboot
```

왜인지 495를 지정해도 525 버전이 설치된다. 재부팅 후에 드라이버가 인식되었는지 확인한다.

```
kiho@ubuntu-gpu:~$ nvidia-smi
Sat Oct 14 08:58:47 2023       
+-----------------------------------------------------------------------------+
| NVIDIA-SMI 525.125.06   Driver Version: 525.125.06   CUDA Version: 12.0     |
|-------------------------------+----------------------+----------------------+
| GPU  Name        Persistence-M| Bus-Id        Disp.A | Volatile Uncorr. ECC |
| Fan  Temp  Perf  Pwr:Usage/Cap|         Memory-Usage | GPU-Util  Compute M. |
|                               |                      |               MIG M. |
|===============================+======================+======================|
|   0  NVIDIA GeForce ...  Off  | 00000000:01:00.0 Off |                  N/A |
| 33%   36C    P8     6W / 215W |      1MiB /  8192MiB |      0%      Default |
|                               |                      |                  N/A |
+-------------------------------+----------------------+----------------------+

+-----------------------------------------------------------------------------+
| Processes:                                                                  |
|  GPU   GI   CI        PID   Type   Process name                  GPU Memory |
|        ID   ID                                                   Usage      |
|=============================================================================|
|  No running processes found                                                 |
+-----------------------------------------------------------------------------+
```

### CUDA 설치 (선택)

ubuntu 22 설치 시 기본 gcc 11 버전이 설치되어 cuda 11.3 설치 진행이 안된다.
gcc 10 버전을 설치해준다.

```
sudo apt install gcc-10 g++-10
sudo update-alternatives --install /usr/bin/gcc gcc /usr/bin/gcc-10 30
sudo update-alternatives --install /usr/bin/g++ g++ /usr/bin/g++-10 30
```

CUDA 설치

```
wget https://developer.download.nvidia.com/compute/cuda/11.3.1/local_installers/cuda_11.3.1_465.19.01_linux.run
sudo sh cuda_11.3.1_465.19.01_linux.run
```

CUDA 설치 진행 시 이미 드라이버가 설치되어있다고 경고가 나오는데, 드라이버 설치만 체크해제 하고 진행한다.

![Image]({{ site.url }}{{ site.baseurl }}/assets/images/proxmox-gpu-ubuntu/cuda1.png){: width="100%"}

continue 선택

![Image]({{ site.url }}{{ site.baseurl }}/assets/images/proxmox-gpu-ubuntu/cuda2.png){: width="100%"}

accpet 입력

![Image]({{ site.url }}{{ site.baseurl }}/assets/images/proxmox-gpu-ubuntu/cuda3.png){: width="100%"}

driver 체크 해제 후 install 선택

