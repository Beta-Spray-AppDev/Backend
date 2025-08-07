# Upload backend.jar to Raspberry Pi

$jarPath    = "C:\Users\marku\Documents\GitHub\Backend\boulder-backend\target\backend.jar"
$sshKey     = "C:\Users\marku\.ssh\id_rsa_rpi"
$piHost     = "pi@212.17.121.41"
$remotePath = "~/sprayconnect/"
$port       = 2222

scp -i $sshKey -P $port $jarPath "${piHost}:${remotePath}"
