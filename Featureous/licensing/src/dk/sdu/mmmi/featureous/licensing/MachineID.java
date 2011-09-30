/*
 * Featureous is distributed under the GPLv3 license.
 *
 * University of Southern Denmark, 2011
 */
package dk.sdu.mmmi.featureous.licensing;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.NetworkInterface;
import java.security.MessageDigest;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 *
 * @author jemr
 */
class MachineID {
	private static Set<String> cache;

	public static synchronized Set<String> getMachineIds() {
		if (cache == null) {
			try {
				String os = System.getProperty("os.name");
				if (os.contains("Windows"))
					cache = getWindowsMachineIds();
				else if (os.contains("Mac OS") || os.contains("Linux") || os.contains("BSD"))
					cache = getNixMachineIds();
				else
					cache = getDefaultMachineIds();
			}
			catch (Exception e) {
				cache = getDefaultMachineIds();
			}
		}
		
		if (cache == null || cache.isEmpty())
			cache = getDefaultMachineIds();

		if (cache == null || cache.isEmpty()) {
			cache = new HashSet<String>();					
			cache.add(createId(new byte[] {10, 20, 30, 40, 50, 60} ));
		}
		
		return cache;
	}
	
	public static String getMachineIdString() {
		StringBuilder sb = new StringBuilder();
		boolean first = true;
		for (String id: getMachineIds()) {
			if (!first)	sb.append(":");
			sb.append(id);
			first = false;
		}
		return sb.toString();
	}

	private static Set<String> getWindowsMachineIds() {
		List<String> lines = run("ipconfig.exe", "/all");
		if (lines.isEmpty())
			return getDefaultMachineIds();
		
		Set<String> result = new HashSet<String>();
//		System.err.println("Getting Windows Machine ID");

		Pattern macPattern = Pattern.compile(" (\\p{XDigit}{2})-(\\p{XDigit}{2})-(\\p{XDigit}{2})-(\\p{XDigit}{2})-(\\p{XDigit}{2})-(\\p{XDigit}{2})$");
		Pattern namePattern = Pattern.compile("^\\S");
		
		String name = "";
		for (String line: lines) {			
			if (namePattern.matcher(line).find())
				name = line.toLowerCase();
			
			// ignore anything with tunnel or virtual in the name
			//if (name.contains("virtual") || name.contains("tunnel"))
			//	continue;
			
			Matcher m = macPattern.matcher(line);
			if (m.find()) {
				byte mac2[] = new byte[6];
				for (int i = 0; i < 6; i++)
					mac2[i] = (byte)(Integer.parseInt(m.group(i + 1), 16) & 0xff);
				result.add(createId(mac2));
			}
		}
		
		return result;
	}

	private static Set<String> getNixMachineIds() {
		List<String> lines = run("ifconfig", "-a");
		if (lines.isEmpty())
			return getDefaultMachineIds();
		
		Set<String> result = new HashSet<String>();		
//		System.err.println("Getting *NIX Machine ID");

		Pattern macPattern = Pattern.compile("(ether|HWaddr)\\s+(\\p{XDigit}{2})[-:](\\p{XDigit}{2})[-:](\\p{XDigit}{2})[-:](\\p{XDigit}{2})[-:](\\p{XDigit}{2})[-:](\\p{XDigit}{2})");
		Pattern namePattern = Pattern.compile("^\\S");
		
		Matcher ether = null;
		for (String line: lines) {	
			if (macPattern.matcher(line).find()) {
				ether = macPattern.matcher(line);
				ether.find();
			}
			
			if (namePattern.matcher(line).find()) {
				if (ether != null) {
					byte mac2[] = new byte[6];
					for (int i = 0; i < 6; i++)
						mac2[i] = (byte)(Integer.parseInt(ether.group(i + 2), 16) & 0xff);
					result.add(createId(mac2));
				}
				ether = null;
			}
		}
			
		if (ether != null) {
			byte mac2[] = new byte[6];
			for (int i = 0; i < 6; i++)
				mac2[i] = (byte)(Integer.parseInt(ether.group(i + 2), 16) & 0xff);
			result.add(createId(mac2));
		}
		
		return result;
	}

	private static Set<String> getDefaultMachineIds() {
		Set<String> result = new HashSet<String>();
		try {
//			System.err.println("Getting Default Machine ID");
			Enumeration<NetworkInterface> interfaces = NetworkInterface.getNetworkInterfaces();
			while (interfaces.hasMoreElements()) {
				NetworkInterface nic = interfaces.nextElement();
				if (!nic.isLoopback() && !nic.isVirtual() && !nic.isPointToPoint() && nic.getHardwareAddress() != null && nic.getHardwareAddress().length == 6) {
					result.add(createId(nic.getHardwareAddress()));
				}
			}
		}
		catch (Exception e) {
		}
		return result;
	}

	private static String createId(byte[] mac) {
		try {
			MessageDigest md = MessageDigest.getInstance("SHA-1");
			md.update(mac);
			md.update(MachineID.class.getName().getBytes("utf8"));
			return convertToHex(md.digest());
		}
		catch (Exception e) {
			return "";
		}
	}
	
    private static String convertToHex(byte[] data) { 
        StringBuilder sb = new StringBuilder();
		for (byte b: data)
			sb.append(String.format("%02X", b));
        return sb.toString();
    } 	
	
	private static List<String> run(String... args) {
		try {
			ProcessBuilder builder = new ProcessBuilder(args);
			final Process p = builder.start();
			final ArrayList<String> lines = new ArrayList<String>();

			new Thread() {
				public void run() {
					try {
						BufferedReader input = new BufferedReader(new InputStreamReader(p.getInputStream()));
						String line = null;

						while ((line = input.readLine()) != null)
							lines.add(line);
					}
					catch (IOException ex) {
						//
					}
				}
			}.start();

			new Thread() {
				public void run() {
					try {
						BufferedReader input = new BufferedReader(new InputStreamReader(p.getErrorStream()));
						String line = null;

						while ((line = input.readLine()) != null) {
							//System.err.println(line);
						}
					}
					catch (IOException ex) {
						//
					}
				}
			}.start();
			p.waitFor();
			return lines;
		}
		catch (Exception ex) {
			//ex.printStackTrace();
		}
		return Collections.emptyList();
	}

	private static byte[] smallestMac(byte[] mac, byte[] mac2) {
		if (mac.length != mac2.length)
			return mac;
		
		for (int i = 0; i < mac.length; i++) {
			int a = (int)(mac[i] & 0xff);
			int b = (int)(mac2[i] & 0xff);			
			if (a < b)
				return mac;
			else if (b < a)
				return mac2;
		}
		return mac;		
	}
}
