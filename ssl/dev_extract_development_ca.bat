@echo off
setlocal enabledelayedexpansion

set "ALIAS=localhostKey"
set "KEYSTORE_PATH=.\generated\keystore.jks"
set "CA_FILENAME=.\generated\ca.pem"

keytool -exportcert ^
        -alias "!ALIAS!" ^
        -keystore "!KEYSTORE_PATH!" ^
        -file "!CA_FILENAME!"