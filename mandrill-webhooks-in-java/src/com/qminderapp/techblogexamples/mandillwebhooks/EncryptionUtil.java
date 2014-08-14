package com.qminderapp.techblogexamples.mandillwebhooks;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;

import org.apache.commons.codec.binary.Base64;

class EncryptionUtil {
	private static final String HMAC_SHA1_ALGORITHM = "HmacSHA1";

	private EncryptionUtil() {

	}

	public static String calculateRFC2104HMACInBase64(String data, String key) {
		SecretKeySpec signingKey = new SecretKeySpec(key.getBytes(), HMAC_SHA1_ALGORITHM);
		try {
			Mac mac = Mac.getInstance(HMAC_SHA1_ALGORITHM);
			mac.init(signingKey);

			byte[] rawHmac = mac.doFinal(data.getBytes());
			byte[] baseEncoded = Base64.encodeBase64(rawHmac);

			return new String(baseEncoded, "UTF-8");

		} catch (Exception e) {
			throw new RuntimeException("Failed to generate HMAC" + e.getMessage());
		}
	}
}
