# QMS-AI Local Healthcheck (Windows PowerShell)
param(
  [int[]]$Ports = @(8085,3003,3004,8084,8081)
)

function Test-Port($port){
  try{ (Invoke-WebRequest -UseBasicParsing -Uri "http://localhost:$port/health" -TimeoutSec 3).StatusCode }catch{ -1 }
}

Write-Host "=== QMS-AI Local Health ===" -ForegroundColor Cyan

$map = @{
  8085 = 'Gateway';
  3003 = 'Config';
  3004 = 'Chat';
  8084 = 'Auth';
  8081 = 'Frontend';
}

foreach($p in $Ports){
  $role = $map[$p]
  $result = Test-Port $p
  if($result -eq 200){
    Write-Host ("OK  {0,-8} http://localhost:{1}/health" -f $role,$p) -ForegroundColor Green
  } else {
    Write-Host ("BAD {0,-8} http://localhost:{1}/health (code={2})" -f $role,$p,$result) -ForegroundColor Red
  }
}

Write-Host "Tip: run scripts/local-start.bat to start all; scripts/local-stop.bat to stop." -ForegroundColor Yellow

