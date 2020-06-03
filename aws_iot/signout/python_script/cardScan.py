from __future__ import absolute_import
from __future__ import print_function
from awscrt import io, mqtt, auth, http
from awsiot import mqtt_connection_builder
from inky import InkyWHAT #NOTE can only have this line on a rasberry pi
from PIL import Image, ImageDraw
import datetime
import evdev
import subprocess
import argparse
import sys
import threading
import time
import qrcode
import json
import subprocess
import json

# Note: We are using File I/O here. This is likely unncessary. This can be updated
# in the future to just have the file stored in a variable. However, for testing purposes
# we kept it as a file so we can input custom values easily.

# Reads card scan events until program is stopped
while(True):
    print("<<Ready for new scan>>\n")

    # Clean our input file before every scan
    f = open("scanID", "w")
    f.write("")
    f.close()

    # Using python-evdev to interface with the card scanner attached to the pi. 
    # It should always be event0 unless more devices are plugged in.
    device = evdev.InputDevice('/dev/input/event0')

    # Get input events (keystrokes) and append each input to the file.
    # The card scanner emulates a keyboard and therefore we need to parse
    # the input as keystrokes here.
    count = 0
    for event in device.read_loop():
        # IDs should not be longer than 7 digits.
        if count == 7:
            break
        f = open("scanID", "a")
        # This tests for keystrokes between 0-9 and writes it to the file.
        # See the python-evdev docs for more information.
        if (event.type == evdev.ecodes.EV_KEY and event.value == 1):
            if (event.code == 11):
                if (count >= 1):
                    f.write("0")
                count += 1
            elif(event.code >= 2 and event.code <= 10):
                f.write(str(event.code - 1))
                count += 1
        f.close()
    # End of cardscanning

    ############################################################################################
    #                                                                                          #
    #        BELOW IS CODE TO CONNECT TO THE IOT TOPIC AND SEND A MESSAGE TO THE TOPIC         #
    #                                                                                          #
    ############################################################################################
    
    # The code below here will NOT run if you do not scan a card. If you don't have a cardscanner
    # attatched to the pi then you will have to edit the code above for the cardscanning to 
    # test out the code below. If you scan a card then the code below should execute.

    # Get current timestamp to act as a signin or signout times
    timestamp = datetime.datetime.now()

    # Premade code from sample aws starter code.
    # ***DO NOT TOUCH unless you know what you are doing***
    event_loop_group = io.EventLoopGroup(1)
    host_resolver = io.DefaultHostResolver(event_loop_group)
    client_bootstrap = io.ClientBootstrap(event_loop_group, host_resolver)

    parser = argparse.ArgumentParser(description="Send and receive messages through and MQTT connection.")

    # If you need to add more arguments to this function, add them here.
    parser.add_argument('--endpoint', required=True, help="Your AWS IoT custom endpoint, not including a port. " +
                                                        "Ex: \"abcd123456wxyz-ats.iot.us-east-1.amazonaws.com\"")
    parser.add_argument('--cert', help="File path to your client certificate, in PEM format.")
    parser.add_argument('--key', help="File path to your private key, in PEM format.")
    parser.add_argument('--root-ca', help="File path to root certificate authority, in PEM format. " +
                                        "Necessary if MQTT server uses a certificate that's not already in " +
                                        "your trust store.")
    parser.add_argument('--client-id', default='samples-client-id', help="Client ID for MQTT connection.")
    parser.add_argument('--topic', default="samples/test", help="Topic to subscribe to, and publish messages to.")
    parser.add_argument('--location', default = "Default Location")
    parser.add_argument('--event', default="Sign In")
    args = parser.parse_args()

    # Establish an mqtt connection to publish and subscribe to topics on
    mqtt_connection = mqtt_connection_builder.mtls_from_path(
        endpoint=args.endpoint,
        cert_filepath=args.cert,
        pri_key_filepath=args.key,
        client_bootstrap=client_bootstrap, # Don't know what this is; don't touch it
        ca_filepath=args.root_ca,
        #on_connection_interrupted=on_connection_interrupted, # Don't know what this is; don't touch it
        #on_connection_resumed=on_connection_resumed, # Don't know what this is; don't touch it
        client_id=args.client_id,
        clean_session=False, # Don't know what this is; don't touch it
        keep_alive_secs=6) # Don't know what this is; don't touch it

    print("> Connecting to '{}' with client ID '{}'".format(
    args.endpoint, args.client_id))

    connect_future = mqtt_connection.connect()

    # Future.result() waits until a result is available
    connect_future.result()
    print("> Connected!")

    # Subscribe to the topic passed in from the arguments.
    print("> Subscribing to topic '{}'".format(args.topic))
    subscribe_future, packet_id = mqtt_connection.subscribe(
        topic=args.topic,
        qos=mqtt.QoS.AT_LEAST_ONCE)

    # 
    subscribe_result = subscribe_future.result()
    print("> Subscribed with {}".format(str(subscribe_result['qos'])))
    

    ############################################################################################
    #                                                                                          #
    #        BELOW IS WHAT SHOULD BE EDITED TO CHANGE WHAT MESSAGE IS SENT TO THE TOPIC        #
    #                                                                                          #
    ############################################################################################

    # Get card id from the file we scanned earlier. Should probably change this so we don't need
    # to open the file again up earlier.
    f = open("scanID", "r")
    cardID = f.read()
    f.close()

    # Message JSON <<THIS IS WHAT GETS SENT TO THE TOPIC>>

    msg = {
    "ID": cardID,
    "Location": args.location,
    "Event": args.event,
    "DateTime": timestamp.strftime("%Y-%m-%d %H:%M:%S")
    }

    message = json.dumps(msg)
    print("> Publishing message to topic '{}': {}".format(args.topic, message))
    mqtt_connection.publish(
        topic=args.topic,
        payload=message,
        qos=mqtt.QoS.AT_LEAST_ONCE)
    time.sleep(1)

    disconnect_future = mqtt_connection.disconnect()

    # Anything below is not needed for signout at the moment.
    # ######## Pi Hat Setup ########

    # # We are using the inkywHAT Red/Black/White https://shop.pimoroni.com/products/inky-what?variant=13590497624147 
    # # Documentation: https://github.com/pimoroni/inky 
    # inkywhat = InkyWHAT('black')
    
    # # Establish a new connection for checking if the card is in the DB.
    # # Not sure if this is required, but this is the easiest way I could find to do it.
    # # Copy-pasted from the earlier mqtt_connection variable.
    # pi_hat_connection = mqtt_connection_builder.mtls_from_path(
    #     endpoint=args.endpoint,
    #     cert_filepath=args.cert,
    #     pri_key_filepath=args.key,
    #     client_bootstrap=client_bootstrap,
    #     ca_filepath=args.root_ca,
    #     client_id=args.client_id,
    #     clean_session=False, 
    #     keep_alive_secs=6)


    # # Getting the message from the topic, copy-pasted from above, topic is slightly changed
    # subscribe_cardInDB, packet_id2 = pi_hat_connection.subscribe(
    # topic=args.topic + "/checkCard",
    # qos=mqtt.QoS.AT_LEAST_ONCE)
    # cardInDBResult = subscribe_cardInDB.result()

    # # If the card is in the database, set the screen to be shown to the "Welcome" screen
    # if (str(cardInDBResult['response']) == "True"):
    #     imgURL = "../../../qrcode/displayScreens/welcome_bw.png"

    #     # Resizing the image so it fits to the screen, adding RGB support
    #     size = (400,300)
    #     out = im.resize(size)
    #     out.save('../../../qrcode/resize-output.png')
    #     img = Image.open('../../../qrcode/resize-output.png')
    #     pal_img = Image.new("P", (1, 1))
    #     pal_img.putpalette((255, 255, 255, 0, 0, 0, 255, 0, 0) + (0, 0, 0) * 252)
    #     img = img.convert("RGB").quantize(palette=pal_img)
    # # If the card is not in the database, make a QR code and show the "QR Code" screen
    # elif (str(cardInDBResult['response']) == "False"):
    #     imgURL = "../../../qrcode//displayScreens/qrCodeScreenMerged.png"

    #     # Generate a QR Code Object
    #     qr = qrcode.QRCode(
    #         version=None,
    #         error_correction=qrcode.constants.ERROR_CORRECT_L,
    #         box_size=4,
    #         border=2,
    #     )

    #     # Generate the URL the code points to and add it to the QR Code
    #     wURL = "https://clemson.edu/"
    #     qr.add_data(wURL)
    #     qr.make(fit=True)
    #     img = qr.make_image(fill_color="black", back_color="white")

    #     # Save the image to the proper folder
    #     img.save('../../../qrcode/qr_image.png')
    #     im = Image.open(imgURL)

    #     # Resizing the image so it fits to the screen, adding RGB support
    #     size = (400,300)
    #     out = im.resize(size)
    #     out.save('../../../qrcode/resize-output.png')

    #     # We are using ImageMagick installed on the Raspberry Pi to 
    #     # composite our generated QR Code onto the premade image
    #     # with this subprocess call. This could be changed to a 
    #     # bash script called in the same way.
    #     subprocess.call('composite -blend 100 -gravity center ../../../qrcode/qr_image.png ../../../qrcode/resize-output.png ../../../qrcode/qrCodeScreenMerged.png', shell=True)
    #     img = Image.open(imgURL)

    #     # Continue resizing, compiler complains otherwise.
    #     pal_img = Image.new("P", (1, 1))
    #     pal_img.putpalette((255, 255, 255, 0, 0, 0, 255, 0, 0) + (0, 0, 0) * 252) 
    #     img = img.convert("RGB").quantize(palette=pal_img)

    # # Update the image on the screen and show it.
    # inkywhat.set_image(img)
    # inkywhat.set_border('white')
    # inkywhat.show()

    # Disconnect and return to the start of the while loop.
    print("Disconnecting...")
    disconnect_future.result()
