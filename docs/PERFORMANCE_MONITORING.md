# Руководство по мониторингу производительности

## Шаг 1: Micrometer и Prometheus

### Настройка

1. **Зависимости** уже добавлены в `pom.xml`:
   - `spring-boot-starter-actuator`
   - `micrometer-registry-prometheus`

2. **Конфигурация** в `application.properties`:
   ```properties
   management.endpoints.web.exposure.include=health,info,prometheus,metrics
   management.endpoint.prometheus.enabled=true
   ```

### Доступные метрики

После запуска приложения метрики доступны по адресам:

- **Prometheus метрики**: `http://localhost:8080/actuator/prometheus`
- **Все метрики**: `http://localhost:8080/actuator/metrics`

### Собранные метрики

1. **`crawler.parsing.duration`** - время выполнения парсинга (таймер)
2. **`crawler.parsing.success`** - количество успешных парсингов (счетчик)
3. **`crawler.parsing.errors`** - количество ошибочных парсингов (счетчик)
4. **`crawler.database.records.inserted`** - количество записей в БД (счетчик)
5. **`crawler.pages.crawled`** - количество обработанных страниц (счетчик)
6. **`crawler.urls.visited`** - количество посещенных URL (счетчик)
7. **`crawler.database.save.duration`** - время сохранения в БД (таймер)
8. **`crawler.html.fetch.duration`** - время получения HTML (таймер)

### Настройка Prometheus и Grafana

1. **Установка Prometheus**:
   ```yaml
   # prometheus.yml
   global:
     scrape_interval: 15s
   
   scrape_configs:
     - job_name: 'crawler'
       metrics_path: '/actuator/prometheus'
       static_configs:
         - targets: ['localhost:8080']
   ```

2. **Запуск Prometheus**:
   ```bash
   prometheus --config.file=prometheus.yml
   ```

3. **Импорт дашборда Grafana**:
   - Импортируйте готовый дашборд или создайте свой
   - Используйте метрики из списка выше

## Шаг 2: VisualVM и Java Flight Recorder (JFR)

### Настройка VisualVM

1. **Скачайте VisualVM**: https://visualvm.github.io/

2. **Запуск с JVM параметрами**:
   ```bash
   java -Dcom.sun.management.jmxremote \
        -Dcom.sun.management.jmxremote.port=9999 \
        -Dcom.sun.management.jmxremote.authenticate=false \
        -Dcom.sun.management.jmxremote.ssl=false \
        -jar target/company-crawler-1.0.0.jar
   ```

3. **Подключение в VisualVM**:
   - Откройте VisualVM
   - Добавьте JMX соединение: `localhost:9999`
   - Или выберите локальный процесс из списка

### Использование VisualVM

#### Профилирование CPU
- Вкладка **Sampler** → **CPU**
- Показывает методы, которые потребляют больше всего CPU
- Используйте для поиска узких мест

#### Профилирование памяти
- Вкладка **Sampler** → **Memory**
- Показывает объекты, занимающие память
- Используйте для поиска утечек памяти

#### Thread Dumps
- Вкладка **Threads** → **Thread Dump**
- Сохраняет состояние всех потоков
- Используйте для анализа deadlocks и блокировок

#### Heap Dumps
- Вкладка **Monitor** → **Heap Dump**
- Сохраняет полный снимок памяти
- Используйте для анализа утечек памяти

### Java Flight Recorder (JFR)

1. **Запуск с JFR**:
   ```bash
   java -XX:+FlightRecorder \
        -XX:StartFlightRecording=duration=60s,filename=recording.jfr \
        -jar target/company-crawler-1.0.0.jar
   ```

2. **Анализ записи**:
   - Откройте `recording.jfr` в VisualVM или JDK Mission Control
   - Анализируйте события GC, методы, потоки

3. **Непрерывная запись**:
   ```bash
   java -XX:+FlightRecorder \
        -XX:StartFlightRecording=continuous=true \
        -jar target/company-crawler-1.0.0.jar
   ```

### Поиск узких мест

1. **Медленные методы**:
   - Смотрите вкладку **Hot Spots** в VisualVM
   - Методы с высоким % времени выполнения

2. **Частые GC**:
   - Мониторьте вкладку **Visual GC**
   - Высокая частота GC указывает на проблемы с памятью

3. **Высокая нагрузка на CPU**:
   - Анализируйте **CPU Sampler**
   - Ищите методы в горячих точках

## Шаг 3: Анализ управления памятью и GC

### Настройка логирования GC

Добавьте JVM параметры при запуске:

```bash
java -Xlog:gc*:file=gc.log:time,level,tags \
     -XX:+PrintGCDetails \
     -XX:+PrintGCDateStamps \
     -XX:+PrintGCTimeStamps \
     -jar target/company-crawler-1.0.0.jar
```

### Анализ GC логов

1. **Частота GC**:
   - Ищите строки `GC` в логах
   - Высокая частота (>1 раз в секунду) - проблема

2. **Время GC**:
   - Смотрите на `real=` время
   - Должно быть < 100ms для большинства GC

3. **Типы объектов**:
   - Используйте VisualVM Heap Dump
   - Анализируйте какие объекты занимают память

### Рекомендуемые JVM параметры

```bash
java -Xms512m \
     -Xmx2g \
     -XX:+UseG1GC \
     -XX:MaxGCPauseMillis=200 \
     -XX:+HeapDumpOnOutOfMemoryError \
     -XX:HeapDumpPath=./logs/heap-dump.hprof \
     -jar target/company-crawler-1.0.0.jar
```

### Анализ влияния парсинга на GC

1. Запустите приложение с GC логированием
2. Запустите краулинг
3. Анализируйте логи:
   - Увеличивается ли частота GC?
   - Увеличивается ли время GC?
   - Какие объекты создаются чаще всего?

## Шаг 4: Оптимизация производительности

### Исправленные проблемы

1. **N+1 запросы**:
   - Использованы `JOIN FETCH` в запросах
   - Коллекции загружаются вместе с сущностями

2. **Индексы**:
   - Добавлены индексы на `website`, `name`, `crawledAt`
   - Индексы на `phones` и `emails` в коллекционных таблицах

3. **Аллокации объектов**:
   - Использованы `LinkedHashSet` вместо `ArrayList.contains()`
   - Оптимизированы регулярные выражения (статичные Pattern)

4. **Синхронизация потоков**:
   - Использованы потокобезопасные структуры (`ConcurrentHashMap`)
   - Минимизированы блокировки

## Шаг 5: OpenTelemetry и Jaeger

### Настройка Jaeger

1. **Запуск Jaeger** (через Docker):
   ```bash
   docker run -d --name jaeger \
     -e COLLECTOR_ZIPKIN_HOST_PORT=:9411 \
     -p 5775:5775/udp \
     -p 6831:6831/udp \
     -p 6832:6832/udp \
     -p 5778:5778 \
     -p 16686:16686 \
     -p 14250:14250 \
     -p 14268:14268 \
     -p 14269:14269 \
     -p 9411:9411 \
     jaegertracing/all-in-one:latest
   ```

2. **Конфигурация** в `application.properties`:
   ```properties
   otel.exporter.jaeger.endpoint=http://localhost:14250
   otel.service.name=company-crawler
   ```

3. **Доступ к UI**: http://localhost:16686

### Трейсинг этапов парсинга

Приложение автоматически создает трейсы для:
- `fetch_html` - получение HTML контента
- `parse_contacts` - парсинг контактов
- `save_company` - сохранение компании
- `extract_links` - извлечение ссылок

### Просмотр трейсов

1. Откройте Jaeger UI: http://localhost:16686
2. Выберите сервис `company-crawler`
3. Нажмите "Find Traces"
4. Просмотрите временные диаграммы выполнения

## JMH Бенчмарки

### Запуск бенчмарков

```bash
mvn clean package
java -jar target/benchmarks.jar
```

### Доступные бенчмарки

1. **`extractEmailsWithForLoop`** - классический цикл for
2. **`extractEmailsWithStream`** - Stream API
3. **`extractEmailsWithParallelStream`** - Parallel Stream
4. **`extractEmailsWithStreamOptimized`** - Оптимизированный Stream

### Анализ результатов

Сравните производительность разных реализаций:
- Время выполнения
- Пропускная способность
- Использование CPU

## Выводы и рекомендации

После оптимизации ожидаются следующие улучшения:

1. **Снижение времени парсинга** на 20-30% за счет оптимизации алгоритмов
2. **Уменьшение частоты GC** за счет оптимизации аллокаций
3. **Ускорение запросов к БД** на 40-50% за счет индексов и устранения N+1
4. **Улучшение масштабируемости** за счет оптимизации синхронизации

### Метрики для мониторинга

Регулярно проверяйте:
- `crawler.parsing.duration` - должно снижаться
- `crawler.parsing.errors` - должно быть минимальным
- Частоту GC - должна быть стабильной
- Время ответа API - должно быть < 100ms
