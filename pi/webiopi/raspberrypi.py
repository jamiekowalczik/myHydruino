#!/usr/bin/env python

# To be used with WebIOPi <https://code.google.com/p/webiopi/>

import sys
import datetime
import time
import dlipower
import webiopi
import psutil
import os
import json
import subprocess

from threading import Timer
from time import sleep

GPIO = webiopi.GPIO

# setup function is automatically called at WebIOPi startup
def setup():
    # Do nothing
    pass

# loop function is repeatedly called by WebIOPi
def loop():
    # Do Nothing
    sleep(0.1) # Prevent the CPU from running at 100%
    pass

# destroy function is called at WebIOPi shutdown
def destroy():
    # do nothing
    pass

@webiopi.macro
def reboot(t=1):
    Timer(int(t)*60,os.system("sudo reboot")).start()
    return "The system is going DOWN for reboot in %d minute" % t

@webiopi.macro
def getData():
    #celtemp = round(int(open('/sys/class/thermal/thermal_zone0/temp').read()) / 1e3,1)
    #fartemp = round(celtemp * 9/5 + 32)
    #temp = fartemp
    temp = nf24command2(91)
    #perc = psutil.cpu_percent()
    perc = nf24command2(93);
    #memAvail = round(psutil.swap_memory()/1000000,1)
    #memAvail = round(psutil.avail_phymem()/1000000,1)
    #memAvail = round(psutil.virtual_memory().available/1000000,1)
    memAvail = nf24command2(92)
    #diskUsage =  psutil.disk_usage('/').percent
    diskUsage = nf24command2(95)
    switch = nf24command(90)
    j = {'cpu_temp': temp, 'cpu_perc': perc, 'mem_avail': memAvail, 'disk_usage': diskUsage, 'switch': switch}
    print(json.dumps(j,indent=4, separators=(',', ': ')))
    return json.dumps(j,indent=4, separators=(',', ': '))

@webiopi.macro
def nf24command(cmd):
   cmdString = '/usr/local/bin/remote -m %s' % cmd
   print(cmdString)
   proc = subprocess.Popen([cmdString], stdout=subprocess.PIPE, shell=True)
   (out, err) = proc.communicate()
   content = out.decode("utf8")
   if (content.strip() == "response: 1"):
      return "on"
   else:
      return "off"

@webiopi.macro
def nf24command1(cmd):
   cmdString = '/usr/local/bin/remote -m %s' % cmd
   print(cmdString)
   proc = subprocess.Popen([cmdString], stdout=subprocess.PIPE, shell=True)
   (out, err) = proc.communicate()
   content = out.decode("utf8")
   if (content.strip() == "response: 1"):
      return "High"
   else:
      return "Low"

@webiopi.macro
def nf24command2(cmd):
   cmdString = '/usr/local/bin/remote -m %s' % cmd
   print(cmdString)
   proc = subprocess.Popen([cmdString], stdout=subprocess.PIPE, shell=True)
   (out, err) = proc.communicate()
   content = out.decode("utf8")
   value = content.replace("response: ","")
   return value.strip()


