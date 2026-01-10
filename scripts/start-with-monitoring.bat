@echo off
echo Starting Company Crawler with Performance Monitoring...
echo.

REM Create logs directory if it doesn't exist
if not exist "logs" mkdir logs

java -Xms512m ^
     -Xmx2g ^
     -XX:+UseG1GC ^
     -XX:MaxGCPauseMillis=200 ^
     -XX:+HeapDumpOnOutOfMemoryError ^
     -XX:HeapDumpPath=./logs/heap-dump.hprof ^
     -Xlog:gc*:file=./logs/gc.log:time,level,tags ^
     -Dcom.sun.management.jmxremote ^
     -Dcom.sun.management.jmxremote.port=9999 ^
     -Dcom.sun.management.jmxremote.authenticate=false ^
     -Dcom.sun.management.jmxremote.ssl=false ^
     -XX:+FlightRecorder ^
     -XX:StartFlightRecording=duration=60s,filename=./logs/recording.jfr ^
     -jar target/company-crawler-1.0.0.jar

pause
