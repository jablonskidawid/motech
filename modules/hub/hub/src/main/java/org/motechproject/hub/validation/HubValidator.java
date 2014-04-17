package org.motechproject.hub.validation;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang.StringUtils;
import org.motechproject.hub.model.Modes;
import org.springframework.stereotype.Service;

@Service
public class HubValidator {
	public List<String> validateSubscription(String callbackUrl, String mode, String topic, String leaseSeconds) {
		List<String> errors = new ArrayList<String>();
		if (callbackUrl == null || ("").equals(callbackUrl)) {
			errors.add("hub.callback must be provided");
		}
		if (mode == null || ("").equals(mode)) {
			errors.add("hub.mode must be provided");
		} else {
			try {
				Modes modes = Modes.valueOf(mode.toUpperCase()); 
				if (!modes.equals(Modes.SUBSCRIBE) && !modes.equals(Modes.UNSUBSCRIBE)) {
					errors.add("Invalid mode type [" + mode + "]. Supported modes: 'subscribe' or 'unsubscribe'");
				}
			} catch (IllegalArgumentException e) {
				errors.add("hub.mode provided is not valid. Provided value is [" + mode + "]");
			}
		}
		if (topic == null || ("").equals(topic)) {
			errors.add("hub.topic must be provided");
		}
		if (leaseSeconds != null) {
			if (!StringUtils.isNumeric(leaseSeconds)) {
				errors.add("hub.lease_seconds must be numeric. Provided value is [" + leaseSeconds + "]");
			}
		}
		return errors;
	}

	public List<String> validatePing(String mode, String url) {
		List<String> errors = new ArrayList<String>();
		if (mode == null || ("").equals(mode)) {
			errors.add("hub.mode must be provided");
		} else {
			try {
				Modes.valueOf(mode.toUpperCase()); 
			} catch (IllegalArgumentException e) {
				errors.add("hub.mode provided is not valid. Provided value is [" + mode + "]");
			}
		}
		if (url == null || ("").equals(url)) {
			errors.add("hub.url must be provided");
		}
		return errors;
	}
	
}
