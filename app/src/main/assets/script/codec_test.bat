set /a looptime=0
:loop0
for /l %%i in (1,1,3) do (
    for /l %%i in (1,1,4) do (
    set /a looptime=%looptime%+1
            echo "循环第%looptime%轮"
            timeout /T 1 /NOBREAK
    )
)
for /l %%i in (1,1,20) do (
        echo "循环第%%i轮"
        timeout /T 1 /NOBREAK
    )
    for /l %%i in (1,1,20) do (
        echo "循环2第%%i轮"
        timeout /T 1 /NOBREAK
    )

goto loop0


