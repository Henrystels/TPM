# Настройка JVM для мониторинга производительности

## Рекомендуемые JVM параметры

### Базовые параметры для разработки

```bash
java -Xms512m \
     -Xmx2g \
     -XX:+UseG1GC \
     -XX:MaxGCPauseMillis=200 \
     -jar target/company-crawler-1.0.0.jar
```

### Полная конфигурация с мониторингом

```bash
java -Xms512m \
     -Xmx2g \
     -XX:+UseG1GC \
     -XX:MaxGCPauseMillis=200 \
     -XX:+HeapDumpOnOutOfMemoryError \
     -XX:HeapDumpPath=./logs/heap-dump.hprof \
     -XX:+PrintGCDetails \
     -XX:+PrintGCDateStamps \
     -Xlog:gc*:file=./logs/gc.log:time,level,tags \
     -Dcom.sun.management.jmxremote \
     -Dcom.sun.management.jmxremote.port=9999 \
     -Dcom.sun.management.jmxremote.authenticate=false \
     -Dcom.sun.management.jmxremote.ssl=false \
     -XX:+FlightRecorder \
     -XX:StartFlightRecording=duration=60s,filename=./logs/recording.jfr \
     -jar target/company-crawler-1.0.0.jar
```

## Параметры по категориям

### Управление памятью

- `-Xms512m` - начальный размер heap
- `-Xmx2g` - максимальный размер heap
- `-XX:NewRatio=2` - соотношение young/old generation (опционально)

### Garbage Collection

- `-XX:+UseG1GC` - использовать G1 сборщик мусора
- `-XX:MaxGCPauseMillis=200` - целевая пауза GC
- `-XX:+UseStringDeduplication` - дедупликация строк (G1)

### Логирование GC

- `-Xlog:gc*:file=./logs/gc.log:time,level,tags` - логирование GC в файл
- `-XX:+PrintGCDetails` - детальная информация о GC
- `-XX:+PrintGCDateStamps` - временные метки в логах GC

### Heap Dumps

- `-XX:+HeapDumpOnOutOfMemoryError` - создавать heap dump при OOM
- `-XX:HeapDumpPath=./logs/heap-dump.hprof` - путь для heap dump

### JMX для VisualVM

- `-Dcom.sun.management.jmxremote` - включить JMX
- `-Dcom.sun.management.jmxremote.port=9999` - порт JMX
- `-Dcom.sun.management.jmxremote.authenticate=false` - без аутентификации (только для разработки!)
- `-Dcom.sun.management.jmxremote.ssl=false` - без SSL (только для разработки!)

### Java Flight Recorder

- `-XX:+FlightRecorder` - включить JFR
- `-XX:StartFlightRecording=duration=60s,filename=./logs/recording.jfr` - начать запись

## Создание скрипта запуска

### Windows (start-with-monitoring.bat)

```batch
@echo off
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
```

### Linux/Mac (start-with-monitoring.sh)

```bash
#!/bin/bash
java -Xms512m \
     -Xmx2g \
     -XX:+UseG1GC \
     -XX:MaxGCPauseMillis=200 \
     -XX:+HeapDumpOnOutOfMemoryError \
     -XX:HeapDumpPath=./logs/heap-dump.hprof \
     -Xlog:gc*:file=./logs/gc.log:time,level,tags \
     -Dcom.sun.management.jmxremote \
     -Dcom.sun.management.jmxremote.port=9999 \
     -Dcom.sun.management.jmxremote.authenticate=false \
     -Dcom.sun.management.jmxremote.ssl=false \
     -XX:+FlightRecorder \
     -XX:StartFlightRecording=duration=60s,filename=./logs/recording.jfr \
     -jar target/company-crawler-1.0.0.jar
```

## Анализ логов GC

### Формат лога GC

```
[2024-01-15T10:30:45.123+0000][info][gc] GC(123) Pause Young (Normal) (G1 Evacuation Pause) 512M->256M (1024M) 45.123ms
```

### Ключевые метрики

1. **Частота GC**: количество событий GC в секунду
2. **Время GC**: `real=` время в миллисекундах
3. **Освобожденная память**: разница до/после GC
4. **Паузы приложения**: время остановки приложения

### Инструменты для анализа

1. **GCViewer**: https://github.com/chewiebug/GCViewer
2. **GCPlot**: https://gcplot.com/
3. **VisualVM**: встроенный анализатор GC логов

## Работа с Heap Dumps

### Создание heap dump вручную

```bash
# Через jmap
jmap -dump:format=b,file=heap-dump.hprof <pid>

# Через VisualVM
# Monitor → Heap Dump
```

### Анализ heap dump

1. **VisualVM**: File → Load → heap-dump.hprof
2. **Eclipse MAT**: https://www.eclipse.org/mat/
3. **jhat**: `jhat heap-dump.hprof` (затем http://localhost:7000)

### Поиск утечек памяти

1. Откройте heap dump в VisualVM или MAT
2. Ищите объекты с неожиданно большим количеством экземпляров
3. Анализируйте цепочки ссылок (GC Roots)
4. Проверяйте коллекции, которые растут без очистки

## Работа с Thread Dumps

### Создание thread dump

```bash
# Через jstack
jstack <pid> > thread-dump.txt

# Через VisualVM
# Threads → Thread Dump
```

### Анализ thread dump

1. Ищите блокировки (locked/waiting)
2. Проверяйте deadlocks
3. Анализируйте состояние потоков
4. Ищите потоки в состоянии BLOCKED или WAITING

### Типичные проблемы

- **Deadlock**: два потока ждут друг друга
- **Блокировки**: потоки ждут мониторов
- **Голодание**: потоки не получают CPU время

## Мониторинг в реальном времени

### Использование jstat

```bash
# Статистика GC каждые 1 секунду
jstat -gc <pid> 1000

# Статистика компиляции
jstat -compiler <pid> 1000
```

### Использование jconsole

```bash
jconsole <pid>
```

Доступные вкладки:
- **Overview**: общая информация
- **Memory**: использование памяти
- **Threads**: состояние потоков
- **Classes**: загруженные классы
- **VM Summary**: информация о JVM
- **MBeans**: управляемые бины

## Рекомендации для продакшена

### Безопасность JMX

```bash
-Dcom.sun.management.jmxremote=true
-Dcom.sun.management.jmxremote.port=9999
-Dcom.sun.management.jmxremote.authenticate=true
-Dcom.sun.management.jmxremote.password.file=./jmxremote.password
-Dcom.sun.management.jmxremote.ssl=true
-Dcom.sun.management.jmxremote.ssl.need.client.auth=true
```

### Оптимизация для продакшена

```bash
-XX:+UseG1GC
-XX:MaxGCPauseMillis=200
-XX:G1HeapRegionSize=16m
-XX:InitiatingHeapOccupancyPercent=45
-XX:ConcGCThreads=4
```

## Чеклист настройки

- [ ] Настроены параметры памяти (-Xms, -Xmx)
- [ ] Выбран подходящий GC (G1 для большинства случаев)
- [ ] Включено логирование GC
- [ ] Настроен heap dump при OOM
- [ ] Включен JMX для мониторинга
- [ ] Настроен JFR для профилирования
- [ ] Созданы скрипты запуска
- [ ] Настроен мониторинг метрик
