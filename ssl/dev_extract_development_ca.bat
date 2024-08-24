@echo off
setlocal enabledelayedexpansion

set "ALIAS=localhostKey"
set "KEYSTORE_PATH=.\keystore\keystore.jks"
set "CA_FILENAME=.\keystore\ca.pem"

keytool -exportcert ^
        -alias "!ALIAS!" ^
        -keystore "!KEYSTORE_PATH!" ^
        -file "!CA_FILENAME!"