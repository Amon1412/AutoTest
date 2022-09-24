for /l %%i in (1,1,50) do (
        echo "屏幕下方随机点击"
        set /a x2=%random%%%1040
        set /a y2=%random%%%500+1420
        adb shell input tap %x2% %y2%
        echo "x2 = %x2% y2= %y2%"
    )