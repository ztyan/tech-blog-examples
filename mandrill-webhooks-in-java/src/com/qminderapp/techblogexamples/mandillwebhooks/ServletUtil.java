package com.qminderapp.techblogexamples.mandillwebhooks;

import javax.servlet.http.HttpServletRequest;

class ServletUtil {
	private ServletUtil() {

	}

	public static String getRequestURL(HttpServletRequest request) {
		StringBuilder result = new StringBuilder();

		String protocol = request.getHeader("X-Forwarded-Proto");
		if (protocol == null) {
			throw new RuntimeException("X-Forwarded-Proto is missing. Configure ELB, Ngnix or Apache to forward original request protocol");
		}
		result.append(protocol);
		result.append("://");

		result.append(request.getServerName());

		String forwardedURI = request.getHeader("X-Forwarded-URI");
		if (forwardedURI == null) {
			throw new RuntimeException("X-Forwarded-URI is missing. Configure Ngnix or Apache to forward original request URI");
		}
		result.append(forwardedURI);

		return result.toString();
	}
}
