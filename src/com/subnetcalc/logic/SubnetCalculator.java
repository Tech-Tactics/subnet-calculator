package com.subnetcalc.logic;

import com.subnetcalc.model.SubnetResult;

/**
 * SubnetCalculator performs all IPv4 subnet calculations using
 * bitwise operations. This class has zero GUI dependencies so
 * the same logic can be ported to a web or mobile application.
 *
 * All IP addresses are handled internally as 32-bit integers
 * (stored in long to avoid sign issues) and converted to
 * dotted-decimal strings for display.
 *
 * @author Joseph Black
 */
public class SubnetCalculator {

    /**
     * Calculate all subnet details from an IP address and CIDR prefix.
     *
     * @param ipAddress dotted-decimal IPv4 address (e.g. "192.168.1.50")
     * @param cidr      prefix length between 0 and 32
     * @return SubnetResult with all computed values
     * @throws IllegalArgumentException if the input is invalid
     */
    public static SubnetResult calculate(String ipAddress, int cidr) {
        validateIpAddress(ipAddress);
        validateCidr(cidr);

        long ip = ipToLong(ipAddress);
        long mask = cidrToMask(cidr);
        long wildcard = mask ^ 0xFFFFFFFFL;
        long network = ip & mask;
        long broadcast = network | wildcard;

        long totalHosts = (long) Math.pow(2, 32 - cidr);
        long usableHosts = (cidr >= 31) ? 0 : totalHosts - 2;

        String firstHost;
        String lastHost;
        if (cidr >= 31) {
            firstHost = "N/A";
            lastHost = "N/A";
        } else {
            firstHost = longToIp(network + 1);
            lastHost = longToIp(broadcast - 1);
        }

        String ipClass = getIpClass(ip);
        String binaryMask = toBinaryString(mask);

        return new SubnetResult(
            ipAddress, cidr,
            longToIp(network),
            longToIp(broadcast),
            longToIp(mask),
            longToIp(wildcard),
            firstHost, lastHost,
            totalHosts, usableHosts,
            ipClass, binaryMask
        );
    }

    /**
     * Parse a CIDR notation string like "192.168.1.0/24" and calculate.
     *
     * @param cidrNotation string in the format "x.x.x.x/n"
     * @return SubnetResult with all computed values
     * @throws IllegalArgumentException if the format is invalid
     */
    public static SubnetResult calculateFromCidr(String cidrNotation) {
        if (cidrNotation == null || !cidrNotation.contains("/")) {
            throw new IllegalArgumentException(
                "Expected format: x.x.x.x/n (e.g. 192.168.1.0/24)");
        }

        String[] parts = cidrNotation.trim().split("/");
        if (parts.length != 2) {
            throw new IllegalArgumentException(
                "Expected format: x.x.x.x/n (e.g. 192.168.1.0/24)");
        }

        int cidr;
        try {
            cidr = Integer.parseInt(parts[1].trim());
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException(
                "CIDR prefix must be a number between 0 and 32");
        }

        return calculate(parts[0].trim(), cidr);
    }

    /**
     * Convert a dotted-decimal IP string to a 32-bit long value.
     * Uses long instead of int to avoid sign issues with addresses
     * above 127.255.255.255.
     */
    public static long ipToLong(String ip) {
        validateIpAddress(ip);
        String[] octets = ip.trim().split("\\.");
        long result = 0;
        for (int i = 0; i < 4; i++) {
            result = (result << 8) | Integer.parseInt(octets[i].trim());
        }
        return result;
    }

    /**
     * Convert a 32-bit long value back to a dotted-decimal string.
     */
    public static String longToIp(long ip) {
        return String.format("%d.%d.%d.%d",
            (ip >> 24) & 0xFF,
            (ip >> 16) & 0xFF,
            (ip >> 8) & 0xFF,
            ip & 0xFF);
    }

    /**
     * Create a subnet mask from a CIDR prefix length.
     * For example, /24 produces 255.255.255.0 (0xFFFFFF00).
     */
    public static long cidrToMask(int cidr) {
        if (cidr == 0) return 0L;
        return (0xFFFFFFFFL << (32 - cidr)) & 0xFFFFFFFFL;
    }

    /**
     * Determine the minimum CIDR prefix that can hold a given
     * number of hosts. Used by the VLSM planner.
     *
     * @param hosts number of usable hosts needed
     * @return CIDR prefix (e.g. 26 for up to 62 hosts)
     */
    public static int hostsToMinCidr(int hosts) {
        int needed = hosts + 2; // network + broadcast addresses
        int bits = 0;
        int size = 1;
        while (size < needed) {
            bits++;
            size *= 2;
        }
        return 32 - bits;
    }

    /**
     * Calculate how many total addresses a CIDR prefix provides.
     */
    public static long cidrToTotalHosts(int cidr) {
        return (long) Math.pow(2, 32 - cidr);
    }

    /**
     * Determine the traditional IP class based on the first octet.
     */
    public static String getIpClass(long ip) {
        int firstOctet = (int) ((ip >> 24) & 0xFF);
        if (firstOctet <= 127) return "Class A";
        if (firstOctet <= 191) return "Class B";
        if (firstOctet <= 223) return "Class C";
        if (firstOctet <= 239) return "Class D (Multicast)";
        return "Class E (Reserved)";
    }

    /**
     * Convert a 32-bit value to a binary string with dots between octets.
     * Example: 255.255.255.0 becomes "11111111.11111111.11111111.00000000"
     */
    public static String toBinaryString(long value) {
        StringBuilder sb = new StringBuilder();
        for (int i = 31; i >= 0; i--) {
            sb.append((value >> i) & 1);
            if (i > 0 && i % 8 == 0) {
                sb.append('.');
            }
        }
        return sb.toString();
    }

    /**
     * Validate that an IP address string is a properly formatted IPv4 address.
     */
    public static void validateIpAddress(String ip) {
        if (ip == null || ip.trim().isEmpty()) {
            throw new IllegalArgumentException("IP address cannot be empty");
        }

        String[] octets = ip.trim().split("\\.");
        if (octets.length != 4) {
            throw new IllegalArgumentException(
                "IP address must have 4 octets separated by dots");
        }

        for (int i = 0; i < 4; i++) {
            try {
                int value = Integer.parseInt(octets[i].trim());
                if (value < 0 || value > 255) {
                    throw new IllegalArgumentException(
                        "Each octet must be between 0 and 255. " +
                        "Octet " + (i + 1) + " is: " + value);
                }
            } catch (NumberFormatException e) {
                throw new IllegalArgumentException(
                    "Octet " + (i + 1) + " is not a valid number: " +
                    octets[i]);
            }
        }
    }

    /**
     * Validate that a CIDR prefix is between 0 and 32.
     */
    public static void validateCidr(int cidr) {
        if (cidr < 0 || cidr > 32) {
            throw new IllegalArgumentException(
                "CIDR prefix must be between 0 and 32. Got: " + cidr);
        }
    }
}
