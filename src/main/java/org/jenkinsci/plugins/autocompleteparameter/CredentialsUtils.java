package org.jenkinsci.plugins.autocompleteparameter;

import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;

import org.apache.commons.codec.binary.Base64;

import com.cloudbees.plugins.credentials.CredentialsMatchers;
import com.cloudbees.plugins.credentials.CredentialsProvider;
import com.cloudbees.plugins.credentials.common.StandardUsernamePasswordCredentials;
import com.cloudbees.plugins.credentials.domains.URIRequirementBuilder;

import hudson.model.Item;
import hudson.security.ACL;

public class CredentialsUtils {
	static void addAuth(String uri, String credentialsId, HttpURLConnection conn) {
		StandardUsernamePasswordCredentials credentials = lookupUsernamePasswordCredentials(uri, credentialsId);
		if (credentials != null) {
			String auth;
			try {
				auth = Base64.encodeBase64String((credentials.getUsername() + ":" + credentials.getPassword()).getBytes("UTF-8"));
				conn.addRequestProperty("Authorization", "Basic " + auth);
			} catch (UnsupportedEncodingException e) {
				e.printStackTrace();
			}
		}
	}
	
	private static StandardUsernamePasswordCredentials lookupUsernamePasswordCredentials(String uri, String credentialsId) {
		if (credentialsId == null) 
			return null;
		return CredentialsMatchers.firstOrNull(
				CredentialsProvider.lookupCredentials(
						StandardUsernamePasswordCredentials.class,
						(Item)null,
						ACL.SYSTEM,
						URIRequirementBuilder.fromUri(uri).build()),
					CredentialsMatchers.withId(credentialsId)
				);
	}
}
