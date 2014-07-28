@ECHO OFF
SETLOCAL

FOR /F "tokens=*  delims="  %%A IN ('WMIC /NameSpace:"\\root\WMI" Path BatteryStatus              Get PowerOnline^,RemainingCapacity  /Format:list ^| FIND "="')     DO SET  Battery.%%A
FOR /F "tokens=*  delims="  %%A IN ('WMIC /NameSpace:"\\root\WMI" Path BatteryFullChargedCapacity Get FullChargedCapacity             /Format:list ^| FIND "="')     DO SET  Battery.%%A

:: Calculate runtime capacity
SET /A Battery.RemainingCapacity = ( %Battery.RemainingCapacity%00 + %Battery.FullChargedCapacity% / 2 ) / %Battery.FullChargedCapacity%

:: Display results
ECHO %Battery.RemainingCapacity%
GOTO:EOF

:: End localization
IF "%OS%"=="Windows_NT" ENDLOCAL
