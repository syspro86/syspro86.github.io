---
layout: post
title: "LVM - thinpool 쓰기 불가 해결하기"
date: 2022-05-25 00:00:00 +0900
categories: home-server
---

### 오류 증상 확인

thinpool이 읽기만 가능하며, 쓰기 시도 시 오류가 발상하여 불가한 상태

```
root@pve:~# lvs
  LV                  VG     Attr       LSize   Pool          Origin Data%  Meta%
  raid_thinpool       hdd_vg twi-cotzM-  21.00t                      47.70  0.37                            
  vm-121-disk-0       hdd_vg Vwi-aotz--  20.00t raid_thinpool        50.09                                  
  data                pve    twi-aotz--   1.00t                      28.62  1.64                            
  root                pve    -wi-ao----  96.00g                                                             
  swap                pve    -wi-ao----   8.00g                                                             
  vm-121-disk-0       pve    Vwi-aotz--  20.00g data                 43.96                                  
  vm-122-disk-0       pve    Vwi-aotz--  32.00g data                 100.00                                 
```

thinpool의 attr 정보에 c (thinpool check needed), M (metadata read only) 값이 확인된다. 
즉 metadata data only로 인해 신규 블록 할당이 불가한 상태이다.

---
### lvs 의 결과에서 thinpool의 Meta% 사용도가 100%에 가깝다면 메타데이터 크기를 키워서 해결할 수 있다.

단 메타데이터 사이즈는 약 15.8G 를 초과할 수 없다. 증가 시키기전에 현재 사이즈를 확인해야 한다.
```
root@pve:~# lvs -a
  LV                    VG     Attr       LSize   Pool          Origin Data%  Meta%
  raid_thinpool         hdd_vg twi-cotzM-  21.00t                      47.70  100
  [raid_thinpool_tdata] hdd_vg Twi-ao----  21.00t                                                             
  [raid_thinpool_tmeta] hdd_vg ewi-ao----  15.50g                                  
```

raid_thinpool_tmeta 크기가 15.5g 이기 때문에 크기를 키울 수 없지만, 보통 더 작은 크기이며 아래 명령어를 키울 수 있다.

```
lvextend --poolmetadatasize +2G hdd_vg/raid_thinpool
```

실수로 메타데이터 크기를 16g 이상 키운 경우 thinpool 을 사용할 수 없게 된다.
신규 메타데이터 볼륨을 만들고 교환하여 해결할 수 있다.

실행 전 해당 thinpool의 볼륨을 활용하는 모든 vm 을 중지하고, pve 화면의 스토리지에서도 비활성화해야 한다.

```
# 기존 메타데이터의 블록매핑정보를 저장한다.
thin_dump /dev/mapper/hdd_vg-raid_thinpool_tmeta > meta  
# 원하는 크기의 교체용 메타데이터를 생성한다.
lvcreate -n raid_thinpool_meta1 -L 15G hdd_vg
# 신규 메타데이터 볼륨에 매핑 정보를 복원한다.
thin_restore -i meta -o /dev/mapper/hdd_vg-raid_thinpool_meta1
# 신규 메타데이터 볼륨으로 교체한다. (기존 메타데이터와 신규 볼륨이 서로 교체된다.)
lvconvert --thinpool hdd_vg/raid_thinpool --poolmetadata hdd_vg/raid_thinpool_meta1
# thinpool 을 활성화 한다.
lvchange -ay hdd_vg/raid_thinpool
# 이상이 없다면 교체된 이전 메타데이터 볼륨을 삭제한다.
lvremove hdd_vg/raid_thinpool_meta1
```

---
### lvconvert --repair 통한 복구 시도

LVM 기본 복구 기능을 통해 복구를 시도한다.

```
lvconvert --repair hdd_vg/raid_thinpool
```

실행 후에도 정상화 되지 않고 <code>lvs -a</code> 의 결과에서 tmeta 볼륨이 16G 이상으로 증가 되었다면 상단의 thin_dump~lvremove 명령어를 통해 16G 이하 볼륨을 생성하여 교체해준다.

---
### 오류 플래그 강제 수정하기

이 방법은 더 이상 시도해 볼 방법이 검색되지 않아 시도한 방법으로, 데이터 손실의 위험을 감수하고 수행해야 한다.

상단의 thin_dump 를 수행하여 생성된 meta 파일을 직접 수정 후 복원하여 정상 인식을 기대해 본다.

```
<superblock uuid="" time="0" transaction="1" flags="1" version="2" data_block_size="16384" nr_data_blocks="2621440">
```

flags="1" 로 되어있다면 1을 0으로 수정 후에 저장한다. 해당 플래그 값으로 인해 c (thinpool check needed) 로 인식되는 것으로 추정된다.

그 후 lvcreate, thin_restore ~~ lvremove 까지의 단계를 동일하게 수행해 준다.

---
### 정상으로 돌아온 경우

```
root@pve:~# lvs
  LV                  VG     Attr       LSize   Pool          Origin Data%  Meta%
  raid_thinpool       hdd_vg twi-aotz--  21.00t                      48.45  0.37                            
  raid_thinpool_meta0 hdd_vg -wi-a----- 160.00m                                                             
  raid_thinpool_meta1 hdd_vg -wi-a-----  15.00g                                                             
  vm-121-disk-0       hdd_vg Vwi-aotz--  20.00t raid_thinpool        50.09                                  
  data                pve    twi-aotz--   1.00t                      28.62  1.64                            
  root                pve    -wi-ao----  96.00g                                                             
  swap                pve    -wi-ao----   8.00g                                                             
  vm-121-disk-0       pve    Vwi-aotz--  20.00g data                 43.96                                  
  vm-122-disk-0       pve    Vwi-aotz--  32.00g data                 100.00                                 
root@pve:~# 
```

thinpool의 attr 정보에 c (thinpool check needed), M (metadata read only) 값이 사라졌으며, a (active) 상태가 되어 정상적으로 쓰기가 가능해졌다.
