adb root
adb remount

set /a looptimes=0
set /a errortimes=0
echo "su > temp.txt
echo "cd /sys/devices/platform/vendor/vendor:extcon_usb1" >> temp.txt
echo "echo 1 > otgcontrol" >> temp.txt
echo "cat otgcontrol" >> temp.txt
echo "echo 0 > otgcontrol " >> temp.txt
echo "cat otgcontrol" >> temp.txt
echo "ls /sys/bus/usb/devices" >> temp.txt
:loop
    set /a looptimes=%looptimes%+1
    adb shell < temp.txt
    del temp.txt
    goto loop
