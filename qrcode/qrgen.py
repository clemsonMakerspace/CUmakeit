import qrcode
import json
import subprocess
from inky import InkyWHAT #NOTE can only have this line on a rasberry pi
from PIL import Image, ImageDraw

#DOCUMENTATIONN
#to run on a ras pi,
#IF for some reason pip isnt installed $ sudo apt-get install python-pip
#otherwise:
#           pip install qrcode[pil]
#           NOTE: json is a native library

#sudo raspi-config
#set SPI to enabled

#For the pi hat
#sudo pip install inky

#we happen to be using a wHat
inkywhat = InkyWHAT('black') #NOTE can only have this line on a rasberry pi

#defining
#version = 1-40 int that specifies size
#error_correction = error correction in code (L is %)
#fill and back change background of image
#box_size is number of pixels each qr box has
#border is how many boxes thick the boder should be

qr = qrcode.QRCode(
    version=None,
    error_correction=qrcode.constants.ERROR_CORRECT_L,
    box_size=4,
    border=2,
)

#testing JSON
testJ = '{ "key": "ScannedID|Location|Event|Timestamp|"}'
#parse to python
test = json.loads(testJ)
test["key"] = test["key"].split('|')
#print the ScannedID
print(test["key"][0])


#Website Signin URL
#wURL = "makerspace_com"
wURL = "testURL.com"

#add embedded data TEST
qr.add_data('data')

#add embedded data 
qr.add_data(wURL)# + "/" + test["key"][0])

#auto choses best qr size IE version
qr.make(fit=True)

#make the image - commented for LED
#img = qr.make_image(fill_color="orange", back_color="purple")
img = qr.make_image(fill_color="black", back_color="white")

#save as a file
img.save('qr_image.png')

imgURL = "./displayScreens/qrscan_bw.png"
im = Image.open(imgURL)
#resizing the image so it fits to the screen adding RGB support
size = (400,300)
out = im.resize(size)
out.save('resize-output.png')

#combine the images, note: must have ImageMagick installed on pi
subprocess.call('composite -blend 100 -gravity center qr_image.png resize-output.png displayImage.png', shell=True)
print("Merged images")

img = Image.open('displayImage.png')
pal_img = Image.new("P", (1, 1))
pal_img.putpalette((255, 255, 255, 0, 0, 0, 255, 0, 0) + (0, 0, 0) * 252)

img = img.convert("RGB").quantize(palette=pal_img)


inkywhat.set_image(img) #NOTE can only have this line on a rasberry pi
inkywhat.set_border('white')
inkywhat.show()
