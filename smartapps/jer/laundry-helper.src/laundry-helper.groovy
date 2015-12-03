/**
 *  Laundry Helper
 *
 *  Copyright 2015 jer
 *
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
definition(
    name: "Laundry Helper",
    namespace: "jer",
    author: "jer",
    description: "A helper for laundry that tries to stick to actionable notification.",
    category: "My Apps",
    iconUrl: "https://s3.amazonaws.com/smartapp-icons/Convenience/Cat-Convenience.png",
    iconX2Url: "https://s3.amazonaws.com/smartapp-icons/Convenience/Cat-Convenience@2x.png",
    iconX3Url: "https://s3.amazonaws.com/smartapp-icons/Convenience/Cat-Convenience@2x.png")


preferences {
    section ("Devices") {
        input "washer", "capability.powerMeter", title: "Washer", multiple: false, required: true
        input "dryer", "capability.powerMeter", title: "Dryer", multiple: false, required: true
    }

    section ("Power Settings") {
        input "washer_start_threshold", "number", title: "Washer considered started above", description: "Watts", required: true, default: 10
        input "washer_stop_threshold", "number", title: "Washer considered stopped below", description: "Watts", required: true, default: 4
        input "dryer_start_threshold", "number", title: "Dryer considered started above", description: "Watts", required: true, default: 10
        input "dryer_stop_threshold", "number", title: "Dryer considered stopped below", description: "Watts", required: true, default: 4
    }

    section ("Notifications") {
        input "sendPushMessage", "bool", title: "Send a push notification?"
        input "phone", "phone", title: "Send a text message to:", required: false
        input "switches", "capability.switch", title: "Turn on this switch", required:false, multiple:true
    }
}

def installed() {
    log.debug "Installed with settings: ${settings}"

    initialize()
}

def updated() {
    log.debug "Updated with settings: ${settings}"

    unsubscribe()
    initialize()
}

def initialize() {
    subscribe(washer, "power", washerHandler)
    subscribe(dryer, "power", dryerHandler)
}

def washer_running() {
    def power = washer.currentValue("power")
    log.debug "Washer: ${power}W"
    power >= washer_start_threshold
}

def washer_idle() {
    def power = washer.currentValue("power")
    log.debug "Washer: ${power}W"
    power <= washer_stop_threshold
}

def transition_start(device) {
    setState(device, 'is_running', true)
}

def transition_stop(device) {
    setState(device, 'is_running', false)
}

def setState(device, key, value) {
    if (!state[device.id]) {
        state[device.id] = [:]
    }
    state[device.id][key] = value
}

def getState(device, key) {
    if (!state[device.id]) {
        state[device.id] = [:]
    }
    state[device.id][key]
}

def washerHandler(evt) {
  log.debug "Washer Handler"
    log.debug "State: ${state}"

    log.debug "washer_running: ${washer_running()}"
    if (getState(washer, 'is_running')) {
        if (washer_idle()) {
            transition_stop(washer)
            log.debug "Washer stopped"
        }
    } else if (!getState(washer, 'is_running')) {
        if (washer_running()) {
            transition_start(washer)
            log.debug "Washer started"
        }
    }
    log.debug "New state: ${state}"
}

def dryerHandler(evt) {
  log.debug "Dryer Handler"
}
