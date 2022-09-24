:loop
    set /a randomTime0=%random%%%20
    set /a randomTime1=%random%%%200
    set /a randomTime2=%random%%%5

    for /l %%i in (1,1,%randomTime0%) do (
        echo "随机点击"
        set /a x1=%random%%%1080
        set /a y1=%random%%%1920
        adb shell input tap %x1% %y1%
    )

    for /l %%i in (1,1,5) do (
        echo "返回"
        adb shell input keyevent 4
    )

    for /l %%i in (1,1,1) do (
        echo "启动相机"
        adb shell am start "com.bomine.camera/.MainActivity"
    )

    for /l %%i in (1,1,%randomTime1%) do (
        echo "launcher控制界面随机点击"
        set /a x2=%random%%%1080
        set /a y2=%random%%%500+1420
        adb shell input tap %x2% %y2%
    )

    for /l %%i in (1,1,%randomTime2%) do (
        echo "返回"
        adb shell input keyevent 4
    )
    goto loop
