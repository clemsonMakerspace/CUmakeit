#!/bin/sh

PRIVATE_KEY="../certs/private.pem.key"
DEVICE_CERT="../certs/device.pem.crt"
DEVICE_NAME="makerspacePi_1"
CA_CERT="../certs/AmazonRootCA1.pem"
THING_ENDPOINT=$(awk -F "=" '/piEndpoint1/ {print $2}' ../../../config.ini)
TOPIC="thing/makerspace_pi/signin"
PORT=8883
THING_NAME="makerspace_pi"
LOCATION="Watt" #cannot have spaces
EVENT="SignIn"
HAT_CONNECTED="True"

python cardScan.py --endpoint $THING_ENDPOINT --root-ca $CA_CERT --cert $DEVICE_CERT --key $PRIVATE_KEY --location $LOCATION --event $EVENT --topic $TOPIC --hat-connected $HAT_CONNECTED

