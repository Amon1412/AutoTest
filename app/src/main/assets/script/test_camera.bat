@echo off
chcp 65001

setlocal enabledelayedexpansion



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

set /a sleeptime=%random%%%2+3
timeout /T %sleeptime% /NOBREAK

echo "点击开直播"
adb shell input tap 522 1416

timeout /T 20 /NOBREAK

echo "点击开启视频直播"
adb shell input tap 410 1340

timeout /T 20 /NOBREAK
:loop	
	echo "相机设置--------------------------------------------------"
	
	
	echo "点击相机设置按钮"
	adb shell input tap 190 1470
	
	set /a sleeptime=%random%%%2+3
	timeout /T %sleeptime% /NOBREAK
	
	
	echo "特写镜头设置--------------------------------------------------"
	
	
	echo "点击特写镜头按钮"
	adb shell input tap 183 1535
	
	set /a sleeptime=%random%%%2+3
	timeout /T %sleeptime% /NOBREAK
	
	echo "点击镜像按钮"
	adb shell input tap 231 1577
	
	set /a sleeptime=%random%%%2+3
	timeout /T %sleeptime% /NOBREAK
	
	echo "点击旋转按钮"
	adb shell input tap 316 1577
	
	set /a sleeptime=%random%%%2+3
	timeout /T %sleeptime% /NOBREAK
	
	echo "点击默认按钮"
	adb shell input tap 134 1577
	
	set /a sleeptime=%random%%%2+3
	timeout /T %sleeptime% /NOBREAK
	
	echo "点击特写机位1编辑按钮"
	adb shell input tap 1021 1594
	
	set /a sleeptime=%random%%%2+3
	timeout /T %sleeptime% /NOBREAK
	
	echo "长按角度左按钮 1"
	adb shell input swipe 970 1617 970 1617 5000
	
	set /a sleeptime=%random%%%2+3
	timeout /T %sleeptime% /NOBREAK
	
	echo "点击右旋转"
	adb shell input tap 1035 1616
	timeout /T 1 /NOBREAK
	echo "点击右旋转"
	adb shell input tap 1035 1616
	timeout /T 1 /NOBREAK
	echo "点击右旋转"
	adb shell input tap 1035 1616
	timeout /T 1 /NOBREAK
	echo "点击右旋转"
	adb shell input tap 1035 1616
	timeout /T 1 /NOBREAK
	echo "点击右旋转"
	adb shell input tap 1035 1616
	timeout /T 1 /NOBREAK
	echo "点击右旋转"
	adb shell input tap 1035 1616
	timeout /T 1 /NOBREAK
	echo "点击右旋转"
	adb shell input tap 1035 1616
	timeout /T 1 /NOBREAK
	echo "点击右旋转"
	adb shell input tap 1035 1616
	timeout /T 1 /NOBREAK
	echo "点击右旋转"
	adb shell input tap 1035 1616
	timeout /T 1 /NOBREAK
	echo "点击右旋转"
	adb shell input tap 1035 1616
	timeout /T 1 /NOBREAK
	
	echo "点击左旋转"
	adb shell input tap  975 1614
	timeout /T 1 /NOBREAK
	echo "点击左旋转"
	adb shell input tap  975 1614
	timeout /T 1 /NOBREAK
	echo "点击左旋转"
	adb shell input tap  975 1614
	timeout /T 1 /NOBREAK
	echo "点击左旋转"
	adb shell input tap  975 1614
	timeout /T 1 /NOBREAK
	echo "点击左旋转"
	adb shell input tap  975 1614
	timeout /T 1 /NOBREAK
	echo "点击左旋转"
	adb shell input tap  975 1614
	timeout /T 1 /NOBREAK
	echo "点击左旋转"
	adb shell input tap  975 1614
	timeout /T 1 /NOBREAK
	echo "点击左旋转"
	adb shell input tap  975 1614
	timeout /T 1 /NOBREAK
	echo "点击左旋转"
	adb shell input tap  975 1614
	timeout /T 1 /NOBREAK
	echo "点击左旋转"
	adb shell input tap  975 1614
	timeout /T 3 /NOBREAK
	
	echo "长按角度右按钮 1"
	adb shell input swipe 1032 1617 1032 1617 5000
	
	set /a sleeptime=%random%%%2+3
	timeout /T %sleeptime% /NOBREAK
	
	echo "长按角度左按钮 2"
	adb shell input swipe 970 1617 970 1617 5000
	
	set /a sleeptime=%random%%%2+3
	timeout /T %sleeptime% /NOBREAK
	
	echo "点击右旋转"
	adb shell input tap 1035 1616
	timeout /T 1 /NOBREAK
	echo "点击右旋转"
	adb shell input tap 1035 1616
	timeout /T 1 /NOBREAK
	echo "点击右旋转"
	adb shell input tap 1035 1616
	timeout /T 1 /NOBREAK
	echo "点击右旋转"
	adb shell input tap 1035 1616
	timeout /T 1 /NOBREAK
	echo "点击右旋转"
	adb shell input tap 1035 1616
	timeout /T 1 /NOBREAK
	echo "点击右旋转"
	adb shell input tap 1035 1616
	timeout /T 1 /NOBREAK
	echo "点击右旋转"
	adb shell input tap 1035 1616
	timeout /T 1 /NOBREAK
	echo "点击右旋转"
	adb shell input tap 1035 1616
	timeout /T 1 /NOBREAK
	echo "点击右旋转"
	adb shell input tap 1035 1616
	timeout /T 1 /NOBREAK
	echo "点击右旋转"
	adb shell input tap 1035 1616
	timeout /T 1 /NOBREAK
	
	echo "点击左旋转"
	adb shell input tap  975 1614
	timeout /T 1 /NOBREAK
	echo "点击左旋转"
	adb shell input tap  975 1614
	timeout /T 1 /NOBREAK
	echo "点击左旋转"
	adb shell input tap  975 1614
	timeout /T 1 /NOBREAK
	echo "点击左旋转"
	adb shell input tap  975 1614
	timeout /T 1 /NOBREAK
	echo "点击左旋转"
	adb shell input tap  975 1614
	timeout /T 1 /NOBREAK
	echo "点击左旋转"
	adb shell input tap  975 1614
	timeout /T 1 /NOBREAK
	echo "点击左旋转"
	adb shell input tap  975 1614
	timeout /T 1 /NOBREAK
	echo "点击左旋转"
	adb shell input tap  975 1614
	timeout /T 1 /NOBREAK
	echo "点击左旋转"
	adb shell input tap  975 1614
	timeout /T 1 /NOBREAK
	echo "点击左旋转"
	adb shell input tap  975 1614
	timeout /T 3 /NOBREAK
	
	echo "长按角度右按钮 2"
	adb shell input swipe 1032 1617 1032 1617 5000
	
	set /a sleeptime=%random%%%2+3
	timeout /T %sleeptime% /NOBREAK
	
	echo "长按变焦+按钮 1"
	adb shell input swipe 970 1680 970 1680 8000
	
	set /a sleeptime=%random%%%2+3
	timeout /T %sleeptime% /NOBREAK
	
	echo "长按变焦-按钮 1"
	adb shell input swipe 1032 1680 1032 1680 8000
	
	set /a sleeptime=%random%%%2+3
	timeout /T %sleeptime% /NOBREAK
	
	echo "长按变焦+按钮 2"
	adb shell input swipe 970 1680 970 1680 8000
	
	set /a sleeptime=%random%%%2+3
	timeout /T %sleeptime% /NOBREAK
	
	echo "长按变焦-按钮 2"
	adb shell input swipe 1032 1680 1032 1680 8000
	
	set /a sleeptime=%random%%%2+3
	timeout /T %sleeptime% /NOBREAK
	
	echo "点击+变焦倍数"
	adb shell input tap 977 1678
	timeout /T 1 /NOBREAK
	echo "点击+变焦倍数"
	adb shell input tap 977 1678
	timeout /T 1 /NOBREAK
	echo "点击+变焦倍数"
	adb shell input tap 977 1678
	timeout /T 1 /NOBREAK
	echo "点击+变焦倍数"
	adb shell input tap 977 1678
	timeout /T 1 /NOBREAK
	echo "点击+变焦倍数"
	adb shell input tap 977 1678
	timeout /T 1 /NOBREAK
	echo "点击+变焦倍数"
	adb shell input tap 977 1678
	timeout /T 1 /NOBREAK
	echo "点击+变焦倍数"
	adb shell input tap 977 1678
	timeout /T 1 /NOBREAK
	echo "点击+变焦倍数"
	adb shell input tap 977 1678
	timeout /T 1 /NOBREAK
	echo "点击+变焦倍数"
	adb shell input tap 977 1678
	timeout /T 1 /NOBREAK
	echo "点击+变焦倍数"
	adb shell input tap 977 1678
	timeout /T 1 /NOBREAK
	
	echo "点击-变焦倍数"
	adb shell input tap 1035 1681
	timeout /T 1 /NOBREAK
	echo "点击-变焦倍数"
	adb shell input tap 1035 1681
	timeout /T 1 /NOBREAK
	echo "点击-变焦倍数"
	adb shell input tap 1035 1681
	timeout /T 1 /NOBREAK
	echo "点击-变焦倍数"
	adb shell input tap 1035 1681
	timeout /T 1 /NOBREAK
	echo "点击-变焦倍数"
	adb shell input tap 1035 1681
	timeout /T 1 /NOBREAK
	echo "点击-变焦倍数"
	adb shell input tap 1035 1681
	timeout /T 1 /NOBREAK
	echo "点击-变焦倍数"
	adb shell input tap 1035 1681
	timeout /T 1 /NOBREAK
	echo "点击-变焦倍数"
	adb shell input tap 1035 1681
	timeout /T 1 /NOBREAK
	echo "点击-变焦倍数"
	adb shell input tap 1035 1681
	timeout /T 1 /NOBREAK
	echo "点击-变焦倍数"
	adb shell input tap 1035 1681
	timeout /T 1 /NOBREAK
	
	echo "点击取消按钮"
	adb shell input tap 884 1826
	
	set /a sleeptime=%random%%%2+3
	timeout /T %sleeptime% /NOBREAK
	
	goto loop
pause