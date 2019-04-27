
Generate SSL Certificate:
keytool -genkeypair -alias timecast -keyalg RSA -keysize 2048 -storetype PKCS12 -key
store timecast.p12 -validity 3650

(If you've been asked for Firstname and Lastname just enter your domain name, e.g. localhost)

Put it in /src/main/resources/keystore

Adjust application.properties if necessary
