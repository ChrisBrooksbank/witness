param(
    [Parameter(Mandatory = $true)]
    [string] $Domain,

    [Parameter(Mandatory = $true)]
    [string] $SshKeyName,

    [string] $DropletName = "witness-reference",
    [string] $Region = "nyc1",
    [string] $Size = "s-1vcpu-512mb-10gb",
    [string] $Image = "ubuntu-24-04-x64",
    [switch] $ManageDns
)

$ErrorActionPreference = "Stop"

function Require-Command($Name) {
    if (-not (Get-Command $Name -ErrorAction SilentlyContinue)) {
        throw "$Name is required. Install it first, then rerun this script."
    }
}

function Invoke-DoctlJson($Arguments) {
    $output = & doctl @Arguments --output json
    if ($LASTEXITCODE -ne 0) {
        throw "doctl failed: doctl $($Arguments -join ' ')"
    }
    if (-not $output) {
        return $null
    }
    return $output | ConvertFrom-Json
}

Require-Command "doctl"

$cloudInitPath = Join-Path (Get-Location) "deploy\digitalocean-cloud-init.yml"
if (-not (Test-Path $cloudInitPath)) {
    throw "Missing cloud-init file: $cloudInitPath"
}

$renderedCloudInit = Join-Path ([System.IO.Path]::GetTempPath()) "$DropletName-cloud-init.yml"
(Get-Content -Raw -Path $cloudInitPath).Replace("__WITNESS_SERVER_NAME__", $Domain) |
    Set-Content -Path $renderedCloudInit -Encoding utf8

$sshKeys = Invoke-DoctlJson @("compute", "ssh-key", "list")
$sshKey = $sshKeys | Where-Object { $_.name -eq $SshKeyName } | Select-Object -First 1
if (-not $sshKey) {
    throw "No DigitalOcean SSH key named '$SshKeyName' was found. Run 'doctl compute ssh-key list' to see available names."
}

$existing = Invoke-DoctlJson @("compute", "droplet", "list")
if ($existing | Where-Object { $_.name -eq $DropletName }) {
    throw "A Droplet named '$DropletName' already exists. Use -DropletName to choose another name or delete the existing Droplet first."
}

Write-Host "Creating DigitalOcean Droplet '$DropletName' in $Region using size $Size..."
& doctl compute droplet create $DropletName `
    --region $Region `
    --image $Image `
    --size $Size `
    --ssh-keys $sshKey.id `
    --user-data-file $renderedCloudInit `
    --wait
if ($LASTEXITCODE -ne 0) {
    throw "Droplet creation failed."
}

$droplet = Invoke-DoctlJson @("compute", "droplet", "get", $DropletName)
$ip = ($droplet.networks.v4 | Where-Object { $_.type -eq "public" } | Select-Object -First 1).ip_address
if (-not $ip) {
    throw "Droplet was created, but no public IPv4 address was found."
}

Write-Host "Creating firewall for SSH, HTTP, and HTTPS..."
$firewallName = "$DropletName-firewall"
$firewalls = Invoke-DoctlJson @("compute", "firewall", "list")
if (-not ($firewalls | Where-Object { $_.name -eq $firewallName })) {
    & doctl compute firewall create `
        --name $firewallName `
        --droplet-ids $droplet.id `
        --inbound-rules "protocol:tcp,ports:22,address:0.0.0.0/0,address:::/0 protocol:tcp,ports:80,address:0.0.0.0/0,address:::/0 protocol:tcp,ports:443,address:0.0.0.0/0,address:::/0" `
        --outbound-rules "protocol:tcp,ports:all,address:0.0.0.0/0,address:::/0 protocol:udp,ports:all,address:0.0.0.0/0,address:::/0"
    if ($LASTEXITCODE -ne 0) {
        throw "Firewall creation failed."
    }
}

if ($ManageDns) {
    $parts = $Domain.Split(".")
    if ($parts.Length -lt 3) {
        throw "-ManageDns expects a subdomain such as reference.example.org."
    }
    $rootDomain = ($parts | Select-Object -Skip ($parts.Length - 2)) -join "."
    $recordName = ($parts | Select-Object -First ($parts.Length - 2)) -join "."

    Write-Host "Creating DigitalOcean DNS A record $Domain -> $ip..."
    & doctl compute domain records create $rootDomain --record-type A --record-name $recordName --record-data $ip --record-ttl 300
    if ($LASTEXITCODE -ne 0) {
        throw "DNS record creation failed. Create an A record manually: $Domain -> $ip"
    }
}

Write-Host ""
Write-Host "Droplet created."
Write-Host "IP address: $ip"
Write-Host "Domain:     $Domain"
Write-Host ""
Write-Host "If DNS is not managed by DigitalOcean, create this A record now:"
Write-Host "$Domain -> $ip"
Write-Host ""
Write-Host "Then watch bootstrap logs:"
Write-Host "ssh root@$ip"
Write-Host "tail -f /var/log/cloud-init-output.log"
Write-Host ""
Write-Host "When DNS has propagated:"
Write-Host "curl https://$Domain/health"
