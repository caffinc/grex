package com.caffinc.grex.common.util;

import org.junit.Assert;
import org.junit.Test;

/**
 * Tests the IPUtil class
 *
 * @author Sriram
 */
public class IPUtilTest {
    @Test
    public void testGetIp() throws Exception {
        Assert.assertNotNull("IP Address must not be null", IPUtil.getIp());
    }

    @Test
    public void testGetIpWithPresetMachineIp() throws Exception {
        System.setProperty("machine.ip", "localhost");
        Assert.assertEquals("IP Address must be localhost", "localhost", IPUtil.getIp());
        System.clearProperty("machine.ip");
    }
}
