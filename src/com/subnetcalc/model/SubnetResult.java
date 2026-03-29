package com.subnetcalc.model;

/**
 * SubnetResult holds all the values computed from an IP address
 * and CIDR prefix. This class has no dependency on JavaFX so
 * the same logic can be reused in a web or mobile version later.
 *
 * @author Joseph Black
 */
public class SubnetResult {

    private final String ipAddress;
    private final int cidr;
    private final String networkAddress;
    private final String broadcastAddress;
    private final String subnetMask;
    private final String wildcardMask;
    private final String firstHost;
    private final String lastHost;
    private final long totalHosts;
    private final long usableHosts;
    private final String ipClass;
    private final String binarySubnetMask;

    public SubnetResult(String ipAddress, int cidr, String networkAddress,
                        String broadcastAddress, String subnetMask,
                        String wildcardMask, String firstHost,
                        String lastHost, long totalHosts, long usableHosts,
                        String ipClass, String binarySubnetMask) {
        this.ipAddress = ipAddress;
        this.cidr = cidr;
        this.networkAddress = networkAddress;
        this.broadcastAddress = broadcastAddress;
        this.subnetMask = subnetMask;
        this.wildcardMask = wildcardMask;
        this.firstHost = firstHost;
        this.lastHost = lastHost;
        this.totalHosts = totalHosts;
        this.usableHosts = usableHosts;
        this.ipClass = ipClass;
        this.binarySubnetMask = binarySubnetMask;
    }

    public String getIpAddress()        { return ipAddress; }
    public int getCidr()                { return cidr; }
    public String getNetworkAddress()   { return networkAddress; }
    public String getBroadcastAddress() { return broadcastAddress; }
    public String getSubnetMask()       { return subnetMask; }
    public String getWildcardMask()     { return wildcardMask; }
    public String getFirstHost()        { return firstHost; }
    public String getLastHost()         { return lastHost; }
    public long getTotalHosts()         { return totalHosts; }
    public long getUsableHosts()        { return usableHosts; }
    public String getIpClass()          { return ipClass; }
    public String getBinarySubnetMask() { return binarySubnetMask; }

    @Override
    public String toString() {
        return String.format(
            "IP: %s/%d\nNetwork: %s\nBroadcast: %s\n" +
            "Mask: %s\nWildcard: %s\nHost Range: %s - %s\n" +
            "Total: %d  Usable: %d\nClass: %s",
            ipAddress, cidr, networkAddress, broadcastAddress,
            subnetMask, wildcardMask, firstHost, lastHost,
            totalHosts, usableHosts, ipClass);
    }
}
