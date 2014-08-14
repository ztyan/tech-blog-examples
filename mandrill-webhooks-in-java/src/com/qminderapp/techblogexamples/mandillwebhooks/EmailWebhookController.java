package com.qminderapp.techblogexamples.mandillwebhooks;

import static org.springframework.http.HttpStatus.BAD_REQUEST;
import static org.springframework.http.HttpStatus.OK;
import static org.springframework.http.HttpStatus.UNAUTHORIZED;
import static org.springframework.web.bind.annotation.RequestMethod.HEAD;
import static org.springframework.web.bind.annotation.RequestMethod.POST;

import java.io.IOException;
import java.util.List;

import javax.servlet.http.HttpServletRequest;

import org.apache.log4j.Logger;
import org.codehaus.jackson.map.DeserializationConfig;
import org.codehaus.jackson.map.ObjectMapper;
import org.codehaus.jackson.type.TypeReference;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

/**
 * Controller for handling Mandrill webhook requests.
 * 
 * @see http://help.mandrill.com/entries/21738186-Introduction-to-Webhooks
 * @author Siim Raud
 * 
 */
@Controller
@RequestMapping(value = "api/email/webhook/")
public class EmailWebhookController {

	private static final Logger log = Logger.getLogger(EmailWebhookController.class);
	private static final String EVENTS_KEY = "mandrill_events";
	private static final String KEY = "KEY";

	@ResponseBody
	@RequestMapping(method = POST)
	public ResponseEntity<String> handleRequest(HttpServletRequest request) {

		String signature = request.getHeader("X-Mandrill-Signature");
		if (signature == null) {
			log.warn("Mandrill request without signature");
			return new ResponseEntity<String>(UNAUTHORIZED);
		}
		log.debug("Signature: " + signature);

		StringBuilder data = new StringBuilder();

		String rawEvents = (String) request.getParameter(EVENTS_KEY);
		if (rawEvents == null) {
			log.warn("No events provided");
			return new ResponseEntity<String>(BAD_REQUEST);
		}
		log.debug("Events: " + rawEvents);

		String url = ServletUtil.getRequestURL(request);
		log.debug("Calculating signature based on following parameters:");
		log.debug("URL: " + url);
		log.debug("Events: " + rawEvents);
		data.append(url);
		data.append(EVENTS_KEY);
		data.append(rawEvents);

		String calculatedSignature = EncryptionUtil.calculateRFC2104HMACInBase64(data.toString(), KEY);
		log.debug("Calculated signature: " + calculatedSignature);

		if (!signature.equals(calculatedSignature)) {
			log.warn("Invalid signature");
			return new ResponseEntity<String>(UNAUTHORIZED);
		}

		List<Event> events;
		ObjectMapper mapper = new ObjectMapper();
		mapper.configure(DeserializationConfig.Feature.FAIL_ON_UNKNOWN_PROPERTIES, false);
		try {
			events = mapper.readValue(rawEvents, new TypeReference<List<Event>>() {
			});
		} catch (IOException e) {
			log.error("Invalid events format", e);
			return new ResponseEntity<String>(BAD_REQUEST);
		}

		for (Event event : events) {
			log.debug("Event: " + event.event);
			log.debug("Email: " + event.msg.email);
			log.debug("Mandrill ID: " + event.msg._id);
			log.debug("User Agent: " + event.user_agent);
		}

		return new ResponseEntity<String>(OK);
	}

	/**
	 * Mandrill sends head request to verify the URL
	 * 
	 * @see http 
	 *      ://help.mandrill.com/entries/23704122-Authenticating-webhook-requests
	 * @returns empty body
	 */
	@ResponseBody
	@RequestMapping(method = HEAD)
	public String handleHead() {
		log.debug("Head request from Mandrill");
		return "";
	}

	private static class Event {
		public String event;
		public Message msg;
		public String user_agent;
	}

	private static class Message {
		public String email;
		public String _id;
	}
}
