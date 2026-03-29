package com.subnetcalc.model;

/**
 * VLSMAllocation represents one subnet carved from a parent block
 * during VLSM planning. Each allocation has a label (like a department
 * name or device type), the number of hosts requested, and the
 * resulting subnet details.
 *
 * @author Joseph Black
 */
public class VLSMAllocation {

    private final String label;
    private final int requestedHosts;
    private final int allocatedSize;
    private final int cidr;
    private final String networkAddress;
    private final String broadcastAddress;
    private final String subnetMask;
    private final String firstHost;
    private final String lastHost;
    private final int wastedAddresses;

    public VLSMAllocation(String label, int requestedHosts,
                          int allocatedSize, int cidr,
                          String networkAddress, String broadcastAddress,
                          String subnetMask, String firstHost,
                          String lastHost, int wastedAddresses) {
        this.label = label;
        this.requestedHosts = requestedHosts;
        this.allocatedSize = allocatedSize;
        this.cidr = cidr;
        this.networkAddress = networkAddress;
        this.broadcastAddress = broadcastAddress;
        this.subnetMask = subnetMask;
        this.firstHost = firstHost;
        this.lastHost = lastHost;
        this.wastedAddresses = wastedAddresses;
    }

    public String getLabel()            { return label; }
    public int getRequestedHosts()      { return requestedHosts; }
    public int getAllocatedSize()        { return allocatedSize; }
    public int getCidr()                { return cidr; }
    public String getNetworkAddress()   { return networkAddress; }
    public String getBroadcastAddress() { return broadcastAddress; }
    public String getSubnetMask()       { return subnetMask; }
    public String getFirstHost()        { return firstHost; }
    public String getLastHost()         { return lastHost; }
    public int getWastedAddresses()     { return wastedAddresses; }

    @Override
    public String toString() {
        return String.format(
            "%-20s  Need: %-5d  /%d  %s - %s  (wasted: %d)",
            label, requestedHosts, cidr, networkAddress,
            broadcastAddress, wastedAddresses);
    }
}
