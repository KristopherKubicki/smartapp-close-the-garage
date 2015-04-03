/**
 *  Close the Garage
 *
 */
definition(
    name: "Close the Garage",
    namespace: "KristopherKubicki",
    author: "Kristopher Kubicki",
    description: "Closes the garage door if no motion detected",
    category: "My Apps",
    iconUrl: "https://s3.amazonaws.com/smartapp-icons/Convenience/Cat-Convenience.png",
    iconX2Url: "https://s3.amazonaws.com/smartapp-icons/Convenience/Cat-Convenience@2x.png")



preferences {
	section("Close the garage door if there's no motion..."){
		input "motions", "capability.motionSensor", title: "Where?", multiple: true
	}
	section("For how many minutes..."){
		input "minutes", "number", title: "Minutes?"
	}
	section("Close this door..."){
		input "doors", "capability.doorControl"
	}
}


def installed() {
   initialized()
}

def updated() {
	unsubscribe()
    initialized()
}

def initialized() {
    subscribe(motions, "motion", motionHandler)
}

def motionHandler(evt) {
	log.debug "$evt.name: $evt.value"

	if (evt.value == "inactive") {
		if (!state.inactiveAt) {
			state.inactiveAt = now()
            runIn(minutes * 60, "scheduleCheck", [overwrite: false])
		}
	}
}

def scheduleCheck() {
	log.debug "schedule check, ts = ${state.inactiveAt}"
	if (state.inactiveAt) {
    	def success = 1
        for (sensor in settings.motions) { 
			if(sensor.latestValue("motion") == "active") { 
				success = 0
			}
		}
        
		if (success) {
			log.debug "closing door"
			doors.close()
			state.inactiveAt = null
		}
	}
}
