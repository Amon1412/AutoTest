@echo off
chcp 65001

setlocal enabledelayedexpansion

:loop
    echo "返回桌面"
    adb shell input keyevent 3

    timeout /T 10 /NOBREAK

	echo "启动抖音直播--------------------------------------------------"
	echo "启动抖音"
	adb shell am start "com.ss.android.ugc.aweme/.splash.SplashActivity"
    echo adb shell input tap 105 440

	timeout /T 10 /NOBREAK
	
	echo "点击+号"
	adb shell input tap 400 1410
	
	set /a sleeptime=%random%%%3+3
	timeout /T %sleeptime% /NOBREAK

	adb shell input tap 460 688

	set /a sleeptime=%random%%%3+3
    timeout /T %sleeptime% /NOBREAK
	
	echo "点击开直播"
	adb shell input tap 522 1416
	
	set /a sleeptime=%random%%%3+3
    timeout /T %sleeptime% /NOBREAK
	
	echo "点击开启视频直播"
	adb shell input tap 410 1340
	
	set /a sleeptime=%random%%%3+3
    timeout /T 20 /NOBREAK
	
	echo "相机设置--------------------------------------------------"
	
	
	echo "点击相机设置按钮"
	adb shell input tap 190 1470
	
	set /a sleeptime=%random%%%3+3
	timeout /T %sleeptime% /NOBREAK
	
	echo "全景镜头设置--------------------------------------------------"
	
	
	echo "点击全景镜头按钮"
	adb shell input tap 70 1530
	
	set /a sleeptime=%random%%%3+3
	timeout /T %sleeptime% /NOBREAK
	
	echo "开启绿幕抠图"
	adb shell input tap 894 1590
	
	set /a sleeptime=%random%%%3+3
	timeout /T %sleeptime% /NOBREAK
	
	echo "开启免扣区"
	adb shell input tap 816 1790
	
	set /a sleeptime=%random%%%3+3
	timeout /T %sleeptime% /NOBREAK
	
	echo "确定开启免扣区"
	adb shell input tap 916 1785
	
	set /a sleeptime=%random%%%3+3
	timeout /T %sleeptime% /NOBREAK
	
	echo "保存开启免扣区"
	adb shell input tap 978 1824
	
	timeout /T 10 /NOBREAK
	
	echo "特写镜头设置--------------------------------------------------"
	
	
	echo "点击特写镜头按钮"
	adb shell input tap 183 1535
	
	set /a sleeptime=%random%%%3+3
	timeout /T %sleeptime% /NOBREAK
	
	echo "点击镜像按钮"
	adb shell input tap 231 1577
	
	set /a sleeptime=%random%%%3+3
	timeout /T %sleeptime% /NOBREAK
	
	echo "点击旋转按钮"
	adb shell input tap 316 1577
	
	set /a sleeptime=%random%%%3+3
	timeout /T %sleeptime% /NOBREAK
	
	echo "点击默认按钮"
	adb shell input tap 134 1577
	
	set /a sleeptime=%random%%%3+3
	timeout /T %sleeptime% /NOBREAK
	
	echo "点击特写机位1编辑按钮"
	adb shell input tap 1021 1594
	
	set /a sleeptime=%random%%%3+3
	timeout /T %sleeptime% /NOBREAK
	
	echo "长按角度左按钮 1"
	adb shell input swipe 970 1617 970 1617 5000
	
	set /a sleeptime=%random%%%3+3
	timeout /T %sleeptime% /NOBREAK
	
	echo "长按角度右按钮 1"
	adb shell input swipe 1032 1617 1032 1617 5000
	
	set /a sleeptime=%random%%%3+3
	timeout /T %sleeptime% /NOBREAK
	
	echo "长按角度左按钮 2"
	adb shell input swipe 970 1617 970 1617 5000
	
	set /a sleeptime=%random%%%3+3
	timeout /T %sleeptime% /NOBREAK
	
	echo "长按角度右按钮 2"
	adb shell input swipe 1032 1617 1032 1617 5000
	
	set /a sleeptime=%random%%%3+3
	timeout /T %sleeptime% /NOBREAK
	
	echo "长按变焦+按钮 1"
	adb shell input swipe 970 1680 970 1680 8000
	
	set /a sleeptime=%random%%%3+3
	timeout /T %sleeptime% /NOBREAK
	
	echo "长按变焦-按钮 1"
	adb shell input swipe 1032 1680 1032 1680 8000
	
	set /a sleeptime=%random%%%3+3
	timeout /T %sleeptime% /NOBREAK
	
	echo "长按变焦+按钮 2"
	adb shell input swipe 970 1680 970 1680 8000
	
	set /a sleeptime=%random%%%3+3
	timeout /T %sleeptime% /NOBREAK
	
	echo "长按变焦-按钮 2"
	adb shell input swipe 1032 1680 1032 1680 8000
	
	set /a sleeptime=%random%%%3+3
	timeout /T %sleeptime% /NOBREAK
	
	
	echo "点击机位设置按钮"
	adb shell input tap 1009 1823
	
	set /a sleeptime=%random%%%3+3
	timeout /T %sleeptime% /NOBREAK
	
	
	echo "机位设置--------------------------------------------------"
	
	echo "点击机位设置按钮"
	adb shell input tap 60 1468
	
	set /a sleeptime=%random%%%3+3
	timeout /T %sleeptime% /NOBREAK
	
	echo "点击编辑按钮"
	adb shell input tap 551 1523
	
	set /a sleeptime=%random%%%3+3
	timeout /T %sleeptime% /NOBREAK
	
	echo "点击保存按钮"
	adb shell input tap 383 1752
	
	timeout /T 10 /NOBREAK
	
	
	echo "美化设置--------------------------------------------------"
	
	echo "点击特效按钮"
	adb shell input tap 692 1411
	
	set /a sleeptime=%random%%%3+3
	timeout /T %sleeptime% /NOBREAK
	
	echo "点击美化按钮"
	adb shell input tap 102 1280
	
	set /a sleeptime=%random%%%3+3
	timeout /T %sleeptime% /NOBREAK
	
	echo "点击风格妆按钮"
	adb shell input tap 112 1310
	
	set /a sleeptime=%random%%%3+3
	timeout /T %sleeptime% /NOBREAK
	
	echo "选择风格妆"
	adb shell input tap 157 1377
	
	set /a sleeptime=%random%%%3+3
	timeout /T %sleeptime% /NOBREAK
	
	echo "点击滤镜按钮"
	adb shell input tap 175 1310
	
	set /a sleeptime=%random%%%3+3
	timeout /T %sleeptime% /NOBREAK
	
	echo "选择滤镜"
	adb shell input tap 157 1377
	
	set /a sleeptime=%random%%%3+3
	timeout /T %sleeptime% /NOBREAK
	
	echo "执行返回操作"
	adb shell input keyevent 4
	
	timeout /T 10 /NOBREAK
	
	echo "道具设置--------------------------------------------------"
	
	echo "点击特效按钮"
	adb shell input tap 692 1411
	
	set /a sleeptime=%random%%%3+3
	timeout /T %sleeptime% /NOBREAK
	
	echo "点击道具按钮"
	adb shell input tap 300 1277
	
	set /a sleeptime=%random%%%3+3
	timeout /T %sleeptime% /NOBREAK
	
	echo "点击氛围按钮"
	adb shell input tap 575 1200
	
	set /a sleeptime=%random%%%3+3
	timeout /T %sleeptime% /NOBREAK
	
	echo "选择特效"
	adb shell input tap 404 1260
	
	set /a sleeptime=%random%%%3+3
	timeout /T %sleeptime% /NOBREAK
	
	echo "执行返回操作"
	adb shell input keyevent 4
	
	timeout /T 10 /NOBREAK
	
	echo "点歌测试--------------------------------------------------"
	
	
	echo "点击功能按钮"
	adb shell input tap 645 1411
	
	set /a sleeptime=%random%%%3+3
	timeout /T %sleeptime% /NOBREAK
	
	echo "点击K歌"
	adb shell input tap 83 1038
	
	set /a sleeptime=%random%%%3+3
	timeout /T %sleeptime% /NOBREAK
	
	echo "点歌第一首"
	adb shell input tap 760 1104
	
	set /a sleeptime=%random%%%3+3
	timeout /T %sleeptime% /NOBREAK
	
	echo "点歌第二首"
	adb shell input tap 760 1184
	
	set /a sleeptime=%random%%%3+3
	timeout /T %sleeptime% /NOBREAK
	
	echo "点歌第三首"
	adb shell input tap 760 1264
	
	set /a sleeptime=%random%%%3+3
	timeout /T %sleeptime% /NOBREAK
	
	echo "点歌第四首"
	adb shell input tap 760 1334
	
	set /a sleeptime=%random%%%3+3
	timeout /T %sleeptime% /NOBREAK
	
	echo "点歌第五首"
	adb shell input tap 760 1424
	
	set /a sleeptime=%random%%%1200+1200
	echo "播放10-20分钟"
	timeout /T %sleeptime% /NOBREAK
	
	
	echo "退出直播--------------------------------------------------"
	
	echo "执行返回操作"
	adb shell input keyevent 4
	
	set /a sleeptime=%random%%%3+3
	timeout /T %sleeptime% /NOBREAK
	
	echo "执行第二次返回操作退出直播"
	adb shell input keyevent 4
	
	set /a sleeptime=%random%%%3+3
	timeout /T %sleeptime% /NOBREAK
	
	echo "点击确定"
	adb shell input tap 473 787
	
	set /a sleeptime=%random%%%3+3
	timeout /T %sleeptime% /NOBREAK
	
	echo "执行第二次返回操作退出直播"
	adb shell input keyevent 4
	
	set /a sleeptime=%random%%%3+3
	timeout /T %sleeptime% /NOBREAK
	
	echo "点击确定"
	adb shell input tap 473 787
	
	timeout /T 5 /NOBREAK
	
	echo "退出抖音--------------------------------------------------"
	
	echo "执行返回操作"
	adb shell input keyevent 4
	echo "执行返回操作"
	adb shell input keyevent 4
	echo "执行返回操作"
	adb shell input keyevent 4
	echo "执行返回操作"
	adb shell input keyevent 4
	echo "执行返回操作"
	adb shell input keyevent 4
	goto loop

pause