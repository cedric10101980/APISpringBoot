#!/bin/sh


echo "[req]
default_bits  = 2048
distinguished_name = req_distinguished_name
req_extensions = req_ext
x509_extensions = v3_req
prompt = no

[req_distinguished_name]
countryName = IN
stateOrProvinceName = Maharashtra
localityName = Pune
organizationName = Outbound
organizationalUnitName = Outbound
commonName =  AI Certificate

[req_ext]
subjectAltName = @alt_names

[v3_req]
subjectAltName = @alt_names

[alt_names]
IP.1 = 127.0.0.1
IP.2 = 10.133.73.91
IP.3 = 10.169.64.11
IP.4 = 52.253.73.200
DNS.1 = localhost
DNS.2 = hackathon.baas.com
" > san.cnf

openssl req -x509 -nodes -days 730 -newkey rsa:2048 -keyout key.pem -out cert.pem -config san.cnf -sha256
rm -rf san.cnf
openssl pkcs12 -export -in cert.pem -inkey key.pem -out server.p12 -name pomaikey -passin pass:changeit -passout pass:changeit
keytool -importkeystore -deststorepass changeit -destkeypass changeit -destkeystore keystore.jks -srckeystore server.p12 -srcstoretype PKCS12 -srcstorepass changeit -alias pomaikey
rm -rf cert.pem key.pem server.p12
cp -R keystore.jks src/main/resources/
rm -rf keystore.jks


