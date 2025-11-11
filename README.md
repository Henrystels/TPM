# Company Contact Crawler

Многопоточное MVC-приложение на Spring Boot для сбора контактной информации компаний (телефоны, email, адреса) с веб‑страниц и бизнес‑директорий.

Используются: кастомный ExecutorService, ForkJoinPool, @Scheduled и ScheduledExecutorService, WebFlux (WebClient), RestTemplate, OpenFeign, H2 + JPA, потокобезопасные структуры данных и parallelStream API.

## 1) Предварительные требования
- Java 11+ (JDK)
- Maven 3.8+
- Windows PowerShell (для примеров ниже)

Путь проекта: `C:\Users\henry\Documents\GitHub\TPM`

## 2) Запуск приложения

Откройте PowerShell в корне проекта и выберите один из способов.

- Вариант A (через Maven плагин):
```powershell
mvn spring-boot:run
```

- Вариант B (сборка jar и запуск):
```powershell
mvn -U -DskipTests clean package
java -jar target/company-crawler-1.0.0.jar
```

После старта приложение доступно на `http://localhost:8080`.

Остановить сервер: в окне, где он запущен, нажмите Ctrl+C.

## 3) Минимальная проверка (API)

Откройте ВТОРОЕ окно PowerShell (сервер должен продолжать работать в первом).

1) Запуск краулинга
- Эндпоинт: `POST /api/crawler/start`
- Тело: JSON‑массив стартовых URL
```powershell
$urls = @("https://2gis.ru","https://yandex.ru/maps","https://example.org")
$payload = $urls | ConvertTo-Json -Compress
Invoke-RestMethod -Method Post -Uri "http://localhost:8080/api/crawler/start" -Body $payload -ContentType "application/json"
```
Ответ:
```json
{"taskId":"<uuid>","message":"Crawling started"}
```

2) Проверка статуса задания
- Эндпоинт: `GET /api/crawler/status/{taskId}`
```powershell
Invoke-RestMethod -Uri "http://localhost:8080/api/crawler/status/<taskId>"
```
Важно: подставляйте `taskId` без угловых скобок. После перезапуска приложения старые taskId недействительны.

3) Получение результатов
- Эндпоинт: `GET /api/data/answer?page=0&size=10&search=it`
  - `page`, `size` — пагинация
  - `search` — фильтр по названию/сайту/адресу/контактам (опционально)
```powershell
Invoke-RestMethod -Uri "http://localhost:8080/api/data/answer?page=0&size=10&search=it"
```

Дополнительные варианты:
- Сортировка и общий список:
  - `GET /api/data/companies?search=&sortBy=name&ascending=true`
- По телефону:
  - `GET /api/data/companies/phone/81234567890`
- По email:
  - `GET /api/data/companies/email/info@example.org`

Служебные эндпоинты:
- `GET /api/crawler/stats`
- `GET /api/crawler/active-tasks`

## 4) Консоль H2
- Откройте: `http://localhost:8080/h2-console`
- JDBC URL: `jdbc:h2:file:./data/crawlerdb`
- User: `sa`
- Password: `password`

Таблица: `companies`

## 5) Планировщики
- `@Scheduled` — ежедневный автозапуск в 02:00.
- `ScheduledExecutorService` — запуск каждые 30 минут.

Для мгновенной проверки используйте ручной POST (раздел 3).

## 6) Конфигурация (src/main/resources/application.properties)
- Порт/контекст: `server.port=8080`, `server.servlet.context-path=/`
- H2 и JPA уже настроены
- Логи: `./logs/crawler.log`
- Feign (демо‑прокси для HTML): `feign.htmlFetch.baseUrl=https://r.jina.ai/http`

## 7) Подсказки для PowerShell и кодировки
- Если кириллица в ответах отображается квадратиками:
```powershell
[Console]::OutputEncoding = [System.Text.Encoding]::UTF8
```
- Для многострочных команд используйте бэктик `` ` `` в конце строки (не ^, он для cmd.exe).
- Чтобы увидеть «сырой» JSON:
```powershell
Invoke-RestMethod -Uri "http://localhost:8080/api/data/companies?search=&sortBy=name&ascending=true" | ConvertTo-Json -Depth 6
```

## 8) Частые ошибки и решения
- 400 при POST /start — тело запроса не JSON‑массив. Проверьте `$payload` печатью в консоль.
- 404 для `/` — это нормально, корневого контроллера нет. Используйте `/api/...`.
- Проблемы с `spring-boot:run` (префикс плагина):
```powershell
mvn org.springframework.boot:spring-boot-maven-plugin:2.7.0:run
```
или соберите jar по варианту B.
- Нет данных — попробуйте реальные корпоративные сайты. Сети/прокси могут блокировать HTTP‑запросы.

## 9) Что реализовано
- Краулер ссылок с извлечением контактов
- Потокобезопасные структуры данных и кастомный ExecutorService
- Параллельная обработка через ForkJoinPool и parallelStream
- Два планировщика: @Scheduled и ScheduledExecutorService
- Хранение в H2 через Spring Data JPA
- WebFlux + RestTemplate + OpenFeign (с фолбэками) для сетевых запросов
- Эндпоинты запуска/статуса/получения результатов


