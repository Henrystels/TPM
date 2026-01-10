# Быстрый старт с мониторингом производительности

## 1. Сборка проекта

```bash
mvn clean package
```

## 2. Запуск инфраструктуры мониторинга (Docker)

```bash
docker-compose up -d
```

Это запустит:
- **Prometheus** на http://localhost:9090
- **Grafana** на http://localhost:3000 (логин: admin, пароль: admin)
- **Jaeger** на http://localhost:16686

## 3. Запуск приложения с мониторингом

### Windows:
```bash
.\scripts\start-with-monitoring.bat
```

### Linux/Mac:
```bash
chmod +x scripts/start-with-monitoring.sh
./scripts/start-with-monitoring.sh
```

## 4. Проверка метрик

### Prometheus метрики
Откройте: http://localhost:8080/actuator/prometheus

### Все метрики
Откройте: http://localhost:8080/actuator/metrics

### Health check
Откройте: http://localhost:8080/actuator/health

## 5. Настройка Grafana

1. Откройте http://localhost:3000
2. Войдите (admin/admin)
3. Добавьте источник данных:
   - Тип: Prometheus
   - URL: http://prometheus:9090
   - Нажмите "Save & Test"

4. Создайте дашборд с метриками:
   - `crawler.parsing.duration`
   - `crawler.parsing.success`
   - `crawler.parsing.errors`
   - `crawler.database.records.inserted`

## 6. Просмотр трейсов в Jaeger

1. Откройте http://localhost:16686
2. Выберите сервис `company-crawler`
3. Нажмите "Find Traces"
4. Запустите краулинг через API для генерации трейсов

## 7. Запуск краулинга

```powershell
$urls = @("https://example.org")
$payload = $urls | ConvertTo-Json -Compress
Invoke-RestMethod -Method Post -Uri "http://localhost:8080/api/crawler/start" -Body $payload -ContentType "application/json"
```

## 8. Подключение VisualVM

1. Скачайте VisualVM: https://visualvm.github.io/
2. Откройте VisualVM
3. Найдите процесс `company-crawler` в списке или добавьте JMX: `localhost:9999`

## 9. Запуск JMH бенчмарков

```bash
java -jar target/benchmarks.jar
```

## 10. Анализ GC логов

GC логи сохраняются в `./logs/gc.log`

Для анализа используйте:
- GCViewer: https://github.com/chewiebug/GCViewer
- VisualVM (вкладка Visual GC)

## Полезные ссылки

- **Prometheus UI**: http://localhost:9090
- **Grafana**: http://localhost:3000
- **Jaeger UI**: http://localhost:16686
- **Приложение**: http://localhost:8080
- **H2 Console**: http://localhost:8080/h2-console
- **Actuator**: http://localhost:8080/actuator

## Остановка

```bash
# Остановить приложение: Ctrl+C

# Остановить инфраструктуру
docker-compose down
```
