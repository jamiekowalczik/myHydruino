/**
 *  Raspberry Pi
 *
 *  Copyright 2014 Nicholas Wilde
 *
 *  Monitor your Raspberry Pi using SmartThings and WebIOPi <https://code.google.com/p/webiopi/>
 *
 *  Companion WebIOPi python script can be found here:
 *  <https://github.com/nicholaswilde/smartthings/blob/master/device-types/raspberry-pi/raspberrypi.py>
 *
 *  Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except
 *  in compliance with the License. You may obtain a copy of the License at:
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software distributed under the License is distributed
 *  on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License
 *  for the specific language governing permissions and limitations under the License.
 *
 */
 
import groovy.json.JsonSlurper

preferences {
        input("ip", "string", title:"IP Address", description: "192.168.2.135", required: true, displayDuringSetup: true)
        input("port", "string", title:"Port", description: "8000", defaultValue: 8000 , required: true, displayDuringSetup: true)
        input("username", "string", title:"Username", description: "webiopi", required: false, displayDuringSetup: true)
        input("password", "password", title:"Password", description: "Password", required: false, displayDuringSetup: true)
}

metadata {
	definition (name: "RaspberryPi", namespace: "jamiekowalczik", author: "Jamie Kowalczik") {
		capability "Polling"
		capability "Refresh"
		capability "Temperature Measurement"
        capability "Switch"
        capability "Sensor"
        capability "Actuator"
        
        attribute "depth", "string"
        attribute "wtemp", "string"
        attribute "atemp", "string"
        attribute "ahumid", "string"
        attribute "relay1", "string"
        attribute "relay2", "string"
        attribute "relay3", "string"
        attribute "relay4", "string"
        
        command "restart"
        command "NF24ON" 
        command "NF24OFF"
        command "NF24STATUS"
        
        command "NF24Action", ["number","number"]
        command "NF24Status", ["number"]
        command "NF24Name", ["number"]
	}

	simulator {
		// TODO: define status and reply messages here
	}

	tiles {
        valueTile("Label1", "device.Label1", decoration: "flat") {state "default", label:'${currentValue}'}
        standardTile("NF241", "device.NF241", width: 1, height: 1) {
            state "off", action: "NF24ON", label: 'Off', backgroundColor: "#ffffff", nextState: "on"
            state "on" , action: "NF24OFF", label: 'On', backgroundColor: "#79b821", nextState: "off"
        }
        standardTile("Cycle1", "device.Cycle1", width: 1, height: 1) {
            state "off", action: "NF24CL", label: 'Off', backgroundColor: "#ffffff", nextState: "on", icon: "st.secondary.refresh"
            state "on" , label: 'On', backgroundColor: "#79b821", nextState: "off", icon: "st.secondary.refresh"
        }

        standardTile("switch", "device.switch", width: 1, height: 1, canChangeIcon: true) {
			state "off", label: 'Off', action: "switch.on", icon: "st.Lighting.light3", backgroundColor: "#ffffff", nextState: "on"
			state "on", label: 'On', action: "switch.off", icon: "st.Lighting.light3", backgroundColor: "#79b821", nextState: "off"
		}	
        standardTile("button", "device.pi", width: 1, height: 1, canChangeIcon: true) {
			state "off", label: 'Off', icon: "st.Electronics.electronics18", backgroundColor: "#ffffff", nextState: "on"
			state "on", label: 'On', icon: "st.Electronics.electronics18", backgroundColor: "#79b821", nextState: "off"
		}
        valueTile("depth", "device.depth", inactiveLabel: false) {
        	state "default", label:'Depth \n${currentValue}', unit:"cm",
            backgroundColors:[
                [value: 31, color: "#153591"],
                [value: 44, color: "#1e9cbb"],
                [value: 59, color: "#90d2a7"],
                [value: 74, color: "#44b621"],
                [value: 84, color: "#f1d801"],
                [value: 95, color: "#d04e00"],
                [value: 96, color: "#bc2323"]
            ]
        }
        valueTile("wtemp", "device.wtemp", width: 1, height: 1) {
        	state "default", label:'Water \n${currentValue}° F', unit:"F",
            backgroundColors:[
                [value: 353, color: "#153591"],
                [value: 287, color: "#1e9cbb"],
                [value: 210, color: "#90d2a7"],
                [value: 133, color: "#44b621"],
                [value: 82, color: "#f1d801"],
                [value: 26, color: "#d04e00"],
                [value: 20, color: "#bc2323"]
            ]
        }
        valueTile("atemp", "device.atemp", width: 1, height: 1) {
        	state "default", label:'Air \n${currentValue}° F', unit:"F",
            backgroundColors:[
                [value: 12, color: "#153591"],
                [value: 10, color: "#1e9cbb"],
                [value: 8, color: "#44b621"],
                [value: 6, color: "#f1d801"],
                [value: 4, color: "#d04e00"],
                [value: 0, color: "#153591"]
            ]
        }
        
        valueTile("ahumid", "device.ahumid", width: 1, height: 1) {
        	state "default", label:'Humid \n${currentValue}%', unit:"Percentage",
            backgroundColors:[
                [value: 12, color: "#153591"],
                [value: 10, color: "#1e9cbb"],
                [value: 8, color: "#44b621"],
                [value: 6, color: "#f1d801"],
                [value: 4, color: "#d04e00"],
                [value: 0, color: "#153591"]
            ]
        }
        
        standardTile("restart", "device.restart", inactiveLabel: false, decoration: "flat") {
        	state "default", action:"restart", label: "Restart", displayName: "Restart"
        }
        standardTile("refresh", "device.refresh", inactiveLabel: false, decoration: "flat") {
        	state "default", action:"refresh.refresh", icon: "st.secondary.refresh"
        }
        main "switch"
        details(["depth",  "wtemp", "atemp", "ahumid", "refresh", "restart"])
    }
}

// ------------------------------------------------------------------

// parse events into attributes
def parse(String description) {
    log.debug("Parse Data: ${description}")
    def map = [:]
    def descMap = parseDescriptionAsMap(description)
    log.debug descMap
    def body = new String(descMap["body"].decodeBase64())
    log.debug "body: ${body}"
    def slurper = new JsonSlurper()
    def result = slurper.parseText(body)
    log.debug "result: ${result}"
	if (result){
    	log.debug "Computer is up"
   		sendEvent(name: "pi", value: "on")
    }
    
    if (result.containsKey("light_status")) {
        sendEvent(name: "switch", value: result.light_status)
    }
    
    if (result.containsKey("depth")) {
    	sendEvent(name: "depth", value: result.depth)
    }
    
    if (result.containsKey("wtemp")) {
        sendEvent(name: "wtemp", value: result.wtemp)
    }
    
    if (result.containsKey("atemp")) {
    	log.debug "atemp: ${result.atemp}"
        sendEvent(name: "atemp", value: result.atemp)
    }
    if (result.containsKey("ahumid")) {
    	log.debug "ahumid: ${result.ahumid}"
        sendEvent(name: "ahumid", value: result.ahumid)
    }
  
}

// handle commands
def poll() {
	log.debug "Executing 'poll'"
    getRPiData()
}

def refresh() {
	log.debug "Executing 'refresh'"
    getRPiData()
}

def restart(){
	log.debug "Restart was pressed"
    sendEvent(name: "pi", value: "off")
    def uri = "/macros/reboot"
    postAction(uri)
}

def off(){
    log.debug "ToggleIoT was pressed"
    NF24OFF()
}

def on(){
    log.debug "ToggleIoT was pressed"
    NF24ON()
}

// Get CPU percentage reading
private getRPiData() {
	def uri = "/macros/getAllData"
    postAction(uri)
}

private getRPiData2() {
   def uri = "/macros/getData2"
   postAction(uri)
}

def NF24ON() {NF24Action(10,1)}
def NF24OFF() {NF24Action(10,0)}

def NF24Action(devid,action){
	log.debug "ToggleIoT was pressed Device ID: ${devid} Action: ${action}"
    def uri = ""
    if(action == 0){
       sendEvent(name: "switch", value: "off")
       uri = "/macros/nf24command/${devid}${action}"
    }
    if(action == 1){
       sendEvent(name: "switch", value: "on")
       uri = "/macros/nf24command/${devid}${action}"
    }
    postAction(uri)
}

// ------------------------------------------------------------------

private postAction(uri){
  setDeviceNetworkId(ip,port)  
  
  def userpass = encodeCredentials(username, password)
  
  def headers = getHeader(userpass)
  
  def hubAction = new physicalgraph.device.HubAction(
    method: "POST",
    path: uri,
    headers: headers
  )//,delayAction(1000), refresh()]
  log.debug("Executing hubAction on " + getHostAddress())
  //log.debug hubAction
  hubAction    
}

// ------------------------------------------------------------------
// Helper methods
// ------------------------------------------------------------------

def parseDescriptionAsMap(description) {
	description.split(",").inject([:]) { map, param ->
		def nameAndValue = param.split(":")
		map += [(nameAndValue[0].trim()):nameAndValue[1].trim()]
	}
}

private encodeCredentials(username, password){
	log.debug "Encoding credentials"
	def userpassascii = "${username}:${password}"
    def userpass = "Basic " + userpassascii.encodeAsBase64().toString()
    //log.debug "ASCII credentials are ${userpassascii}"
    //log.debug "Credentials are ${userpass}"
    return userpass
}

private getHeader(userpass){
	log.debug "Getting headers"
    def headers = [:]
    headers.put("HOST", getHostAddress())
    headers.put("Authorization", userpass)
    //log.debug "Headers are ${headers}"
    return headers
}

private delayAction(long time) {
	new physicalgraph.device.HubAction("delay $time")
}

private setDeviceNetworkId(ip,port){
  	def iphex = convertIPtoHex(ip)
  	def porthex = convertPortToHex(port)
  	device.deviceNetworkId = "$iphex:$porthex"
  	log.debug "Device Network Id set to ${iphex}:${porthex}"
}

private getHostAddress() {
	return "${ip}:${port}"
}

private String convertIPtoHex(ipAddress) { 
    String hex = ipAddress.tokenize( '.' ).collect {  String.format( '%02x', it.toInteger() ) }.join()
    return hex

}

private String convertPortToHex(port) {
	String hexport = port.toString().format( '%04x', port.toInteger() )
    return hexport
}
