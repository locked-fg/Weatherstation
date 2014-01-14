#!/usr/bin/env python
# -*- coding: utf-8 -*-

import time

HOST = "localhost"
PORT = 4223
UID = "hRd" # Change to your UID

from tinkerforge.ip_connection import IPConnection
from tinkerforge.bricklet_humidity import Humidity

if __name__ == "__main__":
    ipcon = IPConnection() # Create IP connection
    h = Humidity(UID, ipcon) # Create device object

    ipcon.connect(HOST, PORT) # Connect to brickd
    rh = h.get_humidity()/10.0
    ts = int(time.time())
    
    with open('/home/pi/wetterstation/humidity.csv', 'a') as f:
        f.write('{}\t{}\n'.format(ts, rh))

    ipcon.disconnect()
                                
