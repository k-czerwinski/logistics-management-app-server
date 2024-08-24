@echo off
setlocal enabledelayedexpansion

@REM  ensure that certificate is saved in save directory
set "KEYSTORE=./keystore/keystore.jks"
set "ALIAS=localhostKey"
set "KEYALG=EC"
set "KEYSIZE=256"
set "SIGALG=SHA256withECDSA"
set "VALIDITY=365"
set "CN=10.0.2.2"
@REM SAN - Subject Alternative Name - used for hostname verification to avoid man in the middle attack
set "SAN=DNS:localhost,IP:10.0.2.2"
set "OU=Development Team"
set "O=Company Name"
set "L=Cracow"
set "ST=Lesser Poland Voivodeship"
set "C=PL"

keytool -keystore "!KEYSTORE!" ^
        -storetype PKCS12 ^
        -alias "!ALIAS!" ^
        -genkeypair ^
        -keyalg "!KEYALG!" ^
        -keysize "!KEYSIZE!" ^
        -sigalg "!SIGALG!" ^
        -validity "!VALIDITY!" ^
        -dname "CN=!CN!, OU=!OU!, O=!O!, L=!L!, ST=!ST!, C=!C!" ^
        -ext "SAN=!SAN!"


