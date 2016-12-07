package com.caffinc.grex.common.util;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;

/**
 * Provides utility methods that deal with IP addresses and ports
 *
 * @author Sriram
 */
public class IPUtil {
    /**
     * Returns the public IP if one isn't specified in the <i>machine.ip</i> System property
     *
     * @return IP address if found, null otherwise
     * @throws IOException thrown if connection to third party service failed
     */
    public static String getIp() throws IOException {
        if (System.getProperty("machine.ip") != null) {
            return System.getProperty("machine.ip");
        }
        try (BufferedReader br = new BufferedReader(
                new InputStreamReader(new URL("http://caffinc.com/myip/").openConnection().getInputStream()))) {
            String ip;
            if ((ip = br.readLine()) != null) {
                return ip;
            }
        }
        return null;
    }
}
