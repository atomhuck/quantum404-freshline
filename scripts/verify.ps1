[CmdletBinding()]
param(
    [ValidateSet('full', 'backend')]
    [string]$Scope = 'full'
)

$ErrorActionPreference = 'Stop'
$root = (Resolve-Path (Join-Path $PSScriptRoot '..')).Path
Push-Location $root

function Invoke-CheckedCommand {
    param(
        [Parameter(Mandatory)] [string]$Name,
        [Parameter(Mandatory)] [string[]]$Arguments
    )

    Write-Host "RUN: $Name $($Arguments -join ' ')"
    & $Name @Arguments
    if ($LASTEXITCODE -ne 0) {
        throw "FAIL: $Name exited with code $LASTEXITCODE"
    }
    Write-Host "PASS: $Name"
}

try {
    if (-not (Test-Path 'backend/pom.xml')) {
        throw 'FAIL: backend/pom.xml is missing; Maven verification cannot run.'
    }
    Invoke-CheckedCommand 'mvn' @('-B', '-f', 'backend/pom.xml', 'verify')

    if (-not (Test-Path 'contracts/openapi.yaml') -or -not (Test-Path 'redocly.yaml')) {
        throw 'FAIL: OpenAPI source or Redocly configuration is missing.'
    }
    if (-not (Get-Command npx -ErrorAction SilentlyContinue)) {
        throw 'FAIL: npx is required for pinned OpenAPI lint.'
    }
    Invoke-CheckedCommand 'npx' @('--yes', '@redocly/cli@2.63.0', 'lint', '--config=redocly.yaml', 'contracts/openapi.yaml')

    if (-not (Get-Command docker -ErrorAction SilentlyContinue)) {
        throw 'FAIL: Docker CLI is required for Compose config validation.'
    }
    $oldDbPassword = [Environment]::GetEnvironmentVariable('POSTGRES_PASSWORD', 'Process')
    $oldDemoCode = [Environment]::GetEnvironmentVariable('FRESHLINE_DEMO_ACCESS_CODE', 'Process')
    try {
        $env:POSTGRES_PASSWORD = 'validation-only'
        $env:FRESHLINE_DEMO_ACCESS_CODE = 'validation-only'
        Invoke-CheckedCommand 'docker' @('compose', 'config', '--quiet')
        Write-Host 'RUN: docker compose config --format json (assert PostgreSQL has no published ports)'
        $composeJson = & docker compose config --format json
        if ($LASTEXITCODE -ne 0) {
            throw "FAIL: docker compose config --format json exited with code $LASTEXITCODE"
        }
        $composeConfig = ($composeJson -join "`n") | ConvertFrom-Json
        if ($null -eq $composeConfig.services.postgres) {
            throw 'FAIL: Compose service "postgres" is missing.'
        }
        if ($null -ne $composeConfig.services.postgres.ports -and $composeConfig.services.postgres.ports.Count -gt 0) {
            throw 'FAIL: PostgreSQL must not publish host ports.'
        }
        Write-Host 'PASS: PostgreSQL has no published host ports.'
    }
    finally {
        if ($null -eq $oldDbPassword) { Remove-Item Env:POSTGRES_PASSWORD -ErrorAction SilentlyContinue }
        else { $env:POSTGRES_PASSWORD = $oldDbPassword }
        if ($null -eq $oldDemoCode) { Remove-Item Env:FRESHLINE_DEMO_ACCESS_CODE -ErrorAction SilentlyContinue }
        else { $env:FRESHLINE_DEMO_ACCESS_CODE = $oldDemoCode }
    }

    if ($Scope -eq 'full') {
        foreach ($path in @('frontend/package.json', 'frontend/package-lock.json', 'frontend/Dockerfile', 'simulator/Dockerfile')) {
            if (-not (Test-Path $path)) {
                throw "NOT RUN / FAIL: required full-stack file '$path' is missing; its owner has not published this Compose dependency."
            }
        }
        Invoke-CheckedCommand 'npm' @('--prefix', 'frontend', 'ci')
        Invoke-CheckedCommand 'npm' @('--prefix', 'frontend', 'run', 'build')
    }
    else {
        Write-Host 'NOT RUN: frontend build excluded by Scope=backend; this is not a full-stack PASS.'
    }

    Write-Host "PASS: requested '$Scope' verification scope completed."
}
finally {
    Pop-Location
}
