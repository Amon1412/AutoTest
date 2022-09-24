@echo off
chcp 65001

setlocal enabledelayedexpansion

set device=%1
echo %device%




	echo "返回桌面"
    adb shell input keyevent 3

    timeout /T 10 /NOBREAK

	echo "启动抖音直播--------------------------------------------------"
	echo "启动抖音"
	adb shell am start "com.ss.android.ugc.aweme/.splash.SplashActivity"
    echo adb shell input tap 105 440

	timeout /T 10 /NOBREAK
:loop
	echo "点击+号"
	adb shell input tap 405 1413

	timeout /T 3 /NOBREAK

	echo "点击开直播"
	adb shell input tap 531 1403

	timeout /T 20 /NOBREAK

	echo "点击开启视频直播"
	adb shell input tap 396 1335

	timeout /T 20 /NOBREAK

	echo "上特效"
	adb shell input tap 692 1413
	timeout /T 2 /NOBREAK
	adb shell input tap 302 1274
	timeout /T 2 /NOBREAK
	adb shell input tap 239 1340
	timeout /T 2 /NOBREAK
	adb shell input keyevent 4
	timeout /T 2 /NOBREAK

	echo "开始循环3次"
@echo "bluetooth test"

@for /l %%i in (1,1,3) do @(
 @echo "%%i"

 echo "点歌测试--------------------------------------------------"


	echo "点击功能按钮"
	adb shell input tap 645 1411
	timeout /T 3 /NOBREAK


	echo "点击K歌"
	adb shell input tap 83 1038

	timeout /T 3 /NOBREAK


	echo "点歌第一首"
	adb shell input tap 760 1104

	timeout /T 3 /NOBREAK


	echo "点歌第二首"
	adb shell input tap 760 1184

	timeout /T 3 /NOBREAK


	echo "点歌第三首"
	adb shell input tap 760 1264

	timeout /T 3 /NOBREAK


	echo "点歌第四首"
	adb shell input tap 760 1334

	timeout /T 3 /NOBREAK


	echo "点歌第五首"
	adb shell input tap 760 1424

	timeout /T 3 /NOBREAK

	echo "播放15分钟"
	timeout /T 900 /NOBREAK

	echo "暂停播放"
	adb shell input tap 405 1407
	timeout /T 3 /NOBREAK


	echo "执行返回操作"
	adb shell input keyevent 4
	timeout /T 3 /NOBREAK



	echo "场景设置"
	adb shell input 63 1473
	timeout /T 3 /NOBREAK
	echo "编辑场景"
	adb shell input 168 1521
	timeout /T 3 /NOBREAK


	echo "取消"
	adb shell input tap 121 1711
	timeout /T 3 /NOBREAK

	echo "相机设置"
	adb shell input tap 207 1476
	timeout /T 3 /NOBREAK

	echo "内置麦克风"
	adb shell input tap 306 1471
	timeout /T 3 /NOBREAK

	echo "显示设置"
	adb shell input tap 455 1482
	timeout /T 3 /NOBREAK

	echo "进入脚本"
	adb shell input tap 932 241
	timeout /T 3 /NOBREAK

	echo "导播台"
	adb shell input tap 857 1470
	timeout /T 3 /NOBREAK

	echo "换背景"
	adb shell input tap 665 1514
	timeout /T 3 /NOBREAK


	echo "背景1"
	adb shell input tap 672 1620
	timeout /T 3 /NOBREAK

	echo "背景2"
	adb shell input tap 775 1625
	timeout /T 3 /NOBREAK

	echo "背景3"
	adb shell input tap 848 1604
	timeout /T 3 /NOBREAK

	echo "背景1"
	adb shell input tap 672 1620
	timeout /T 3 /NOBREAK

	echo "切场景"
	adb shell input tap 747 1514
	timeout /T 3 /NOBREAK

	echo "场景1"
	adb shell input tap 677 1618
	timeout /T 3 /NOBREAK

	echo "场景2"
	adb shell input tap 801 1654
	timeout /T 3 /NOBREAK

	echo "场景3"
	adb shell input tap 928 1648
	timeout /T 3 /NOBREAK

	echo "场景4"
	adb shell input tap 1000 1627
	timeout /T 3 /NOBREAK

	echo "场景1"
	adb shell input tap 677 1618
	timeout /T 3 /NOBREAK

	echo "贴水印"
	adb shell input tap 839 1511
	timeout /T 3 /NOBREAK

	echo "水印1"
	adb shell input tap 794 1620
	timeout /T 3 /NOBREAK

	echo "水印2"
	adb shell input tap 905 1609
	timeout /T 3 /NOBREAK

	echo "水印3"
	adb shell input tap 1030 1632
	timeout /T 3 /NOBREAK

	echo "保存"
	adb shell input tap 542 1403
	timeout /T 3 /NOBREAK

	echo "取消水印1"
	adb shell input tap 794 1620
	timeout /T 3 /NOBREAK

	echo "取消水印2"
	adb shell input tap 905 1609
	timeout /T 3 /NOBREAK

	echo "取消水印3"
	adb shell input tap 1030 1632
	timeout /T 3 /NOBREAK

	echo "换背景"
	adb shell input tap 665 1514
	timeout /T 3 /NOBREAK

	echo "关闭脚本"
	adb shell input tap 1013 1472
	timeout /T 5 /NOBREAK


	)

	echo "退出抖音"
	adb shell input keyevent 4
	timeout /T 3 /NOBREAK

	echo "确定退出"
	adb shell input tap 485 789
	timeout /T 5 /NOBREAK

	echo "执行返回操作"
	adb shell input keyevent 4
	timeout /T 3 /NOBREAK









	goto loop
pause