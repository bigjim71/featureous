/*
 * Featureous is distributed under the GPLv3 license.
 *
 * University of Southern Denmark, 2011
 */
package dk.sdu.mmmi.featureous.licensing;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.Signature;
import java.security.SignatureException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Properties;
import java.util.Set;
import sun.misc.BASE64Decoder;
import sun.misc.BASE64Encoder;
import sun.security.jca.ProviderList;
import sun.security.jca.Providers;

/**
 *
 * @author jemr
 */
public class SignatureVerifyer {
	public static boolean verifySignature(Properties p, PublicKey publicKey) {
		try {
			// Some of the providers do not like being loaded premain, and will cause
			// an exception when iterating over them to find the correct provider.
			// It depends on the internal order the providers are listed in, so normally
			// Windows doesn't show any signs of problems, but Mac has issues with
			// the Apple provider.
			// The below is a "hack" to limit the provider list to only contain
			// the needed provider (which doesn't cause problems), and then restoring
			// the complete list afterwards, so user applications can use all
			// available providers.
			// NOTE: the SunRsaSign provider is instantiated directly, since
			//       list.getProvider() causes the above issue.
			ProviderList org = Providers.getProviderList();
			ProviderList newList = ProviderList.newList(new sun.security.rsa.SunRsaSign());
			Providers.setProviderList(newList);
			Signature sig = Signature.getInstance("SHA1withRSA");
			Providers.setProviderList(org);

			sig.initVerify(publicKey);
			iterateProperties(sig, p);
			return sig.verify(decodeByteArray(p.getProperty("signature")));
		}
		catch (Exception e) {
			e.printStackTrace();
		}
		return false;
	}

	public static void buildSignature(Properties p, PrivateKey privateKey) {
		try {
			Signature sig = Signature.getInstance("SHA1withRSA", "SunRsaSign");
			sig.initSign(privateKey);
			iterateProperties(sig, p);
			p.setProperty("signature", encodeByteArray(sig.sign()));
		}
		catch (Exception e) {
			e.printStackTrace();
		}
	}

	private static void iterateProperties(Signature sig, Properties p) throws SignatureException, UnsupportedEncodingException {
		Set<Object> keySet = p.keySet();
		ArrayList<String> keys = new ArrayList<String>();
		for (Object o: keySet)
			keys.add(o.toString());

		Collections.sort(keys);
		for (String key: keys) {
			if (!key.equals("signature")) {
				sig.update(key.getBytes("utf8"));
				sig.update(p.getProperty(key).getBytes("utf8"));
			}
		}
	}
	
	public static String encodeByteArray(byte[] byteArray) {
		return new BASE64Encoder().encode(byteArray).replaceAll("[\r\n]", "");
	}
	
	public static byte[] decodeByteArray(String encodedBytes) {
		try {
			return new BASE64Decoder().decodeBuffer(encodedBytes);
		}
		catch (IOException e) {
			e.printStackTrace();
			return new byte[0];
		}
	}
	
}
