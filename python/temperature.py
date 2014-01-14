#!/usr/bin/env python
# -*- coding: utf-8 -*-

import time

HOST = "localhost"
PORT = 4223
UID = "dXC" # Change to your UID

from tinkerforge.ip_connection import IPConnection
from tinkerforge.bricklet_temperature import Temperature

if __name__ == "__main__":
    ipcon = IPConnection() # Create IP connection
    t = Temperature(UID, ipcon) # Create device object

    ipcon.connect(HOST, PORT) # Connect to brickd
    temperature = t.get_temperature()/100.0
    ts = int(time.time())
    
    with open('/home/pi/wetterstation/temperature.csv', 'a') as f:
        f.write('{}\t{}\n'.format(ts, temperature))

    ipcon.disconnect()
                                
