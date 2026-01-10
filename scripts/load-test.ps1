# Скрипт для нагрузочного тестирования API

$baseUrl = "http://localhost:8080"
$iterations = 100
$concurrentRequests = 10

Write-Host "Нагрузочное тестирование API" -ForegroundColor Green
Write-Host "Base URL: $baseUrl" -ForegroundColor Yellow
Write-Host "Итераций: $iterations" -ForegroundColor Yellow
Write-Host "Параллельных запросов: $concurrentRequests" -ForegroundColor Yellow
Write-Host ""

# Тест 1: Health endpoint
Write-Host "Тест 1: Health endpoint" -ForegroundColor Cyan
$healthResults = @()
for ($i = 1; $i -le $iterations; $i++) {
    $start = Get-Date
    try {
        $response = Invoke-RestMethod -Uri "$baseUrl/actuator/health" -Method Get -ErrorAction Stop
        $end = Get-Date
        $duration = ($end - $start).TotalMilliseconds
        $healthResults += $duration
    } catch {
        Write-Host "Ошибка на итерации $i : $_" -ForegroundColor Red
    }
}

$avgHealth = ($healthResults | Measure-Object -Average).Average
$minHealth = ($healthResults | Measure-Object -Minimum).Minimum
$maxHealth = ($healthResults | Measure-Object -Maximum).Maximum
Write-Host "  Среднее время: $([math]::Round($avgHealth, 2)) мс" -ForegroundColor Green
Write-Host "  Минимальное: $([math]::Round($minHealth, 2)) мс" -ForegroundColor Green
Write-Host "  Максимальное: $([math]::Round($maxHealth, 2)) мс" -ForegroundColor Green
Write-Host ""

# Тест 2: Metrics endpoint
Write-Host "Тест 2: Metrics endpoint" -ForegroundColor Cyan
$metricsResults = @()
for ($i = 1; $i -le $iterations; $i++) {
    $start = Get-Date
    try {
        $response = Invoke-RestMethod -Uri "$baseUrl/actuator/metrics" -Method Get -ErrorAction Stop
        $end = Get-Date
        $duration = ($end - $start).TotalMilliseconds
        $metricsResults += $duration
    } catch {
        Write-Host "Ошибка на итерации $i : $_" -ForegroundColor Red
    }
}

$avgMetrics = ($metricsResults | Measure-Object -Average).Average
$minMetrics = ($metricsResults | Measure-Object -Minimum).Minimum
$maxMetrics = ($metricsResults | Measure-Object -Maximum).Maximum
Write-Host "  Среднее время: $([math]::Round($avgMetrics, 2)) мс" -ForegroundColor Green
Write-Host "  Минимальное: $([math]::Round($minMetrics, 2)) мс" -ForegroundColor Green
Write-Host "  Максимальное: $([math]::Round($maxMetrics, 2)) мс" -ForegroundColor Green
Write-Host ""

# Тест 3: Prometheus endpoint
Write-Host "Тест 3: Prometheus endpoint" -ForegroundColor Cyan
$prometheusResults = @()
for ($i = 1; $i -le $iterations; $i++) {
    $start = Get-Date
    try {
        $response = Invoke-WebRequest -Uri "$baseUrl/actuator/prometheus" -Method Get -ErrorAction Stop
        $end = Get-Date
        $duration = ($end - $start).TotalMilliseconds
        $prometheusResults += $duration
    } catch {
        Write-Host "Ошибка на итерации $i : $_" -ForegroundColor Red
    }
}

$avgPrometheus = ($prometheusResults | Measure-Object -Average).Average
$minPrometheus = ($prometheusResults | Measure-Object -Minimum).Minimum
$maxPrometheus = ($prometheusResults | Measure-Object -Maximum).Maximum
Write-Host "  Среднее время: $([math]::Round($avgPrometheus, 2)) мс" -ForegroundColor Green
Write-Host "  Минимальное: $([math]::Round($minPrometheus, 2)) мс" -ForegroundColor Green
Write-Host "  Максимальное: $([math]::Round($maxPrometheus, 2)) мс" -ForegroundColor Green
Write-Host ""

# Итоговая статистика
Write-Host "Итоговая статистика:" -ForegroundColor Magenta
Write-Host "  Health: $([math]::Round($avgHealth, 2)) мс (avg)" -ForegroundColor White
Write-Host "  Metrics: $([math]::Round($avgMetrics, 2)) мс (avg)" -ForegroundColor White
Write-Host "  Prometheus: $([math]::Round($avgPrometheus, 2)) мс (avg)" -ForegroundColor White

# Расчет пропускной способности
$totalTime = ($healthResults + $metricsResults + $prometheusResults | Measure-Object -Sum).Sum / 1000
$totalRequests = $iterations * 3
$throughput = [math]::Round($totalRequests / $totalTime, 2)
Write-Host ""
Write-Host "Пропускная способность: $throughput запросов/сек" -ForegroundColor Green
