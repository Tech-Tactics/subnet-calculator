package com.subnetcalc.logic;

import com.subnetcalc.model.OverlapResult;
import com.subnetcalc.model.OverlapResult.Relationship;

/**
 * OverlapDetector compares two CIDR ranges and determines their
 * relationship: identical, one contains the other, overlapping,
 * or completely disjoint.
 *
 * The comparison works by converting both ranges to their start
 * and end addresses (as longs) and checking for intersection.
 *
 * @author Joseph Black
 */
public class OverlapDetector {

    /**
     * Compare two subnets given in CIDR notation.
     *
     * @param cidrA first subnet (e.g. "192.168.1.0/24")
     * @param cidrB second subnet (e.g. "192.168.1.128/25")
     * @return OverlapResult describing the relationship
     * @throws IllegalArgumentException if either input is invalid
     */
    public static OverlapResult compare(String cidrA, String cidrB) {
        if (cidrA == null || cidrB == null) {
            throw new IllegalArgumentException("Both subnets are required");
        }

        // Parse subnet A
        String[] partsA = cidrA.trim().split("/");
        if (partsA.length != 2) {
            throw new IllegalArgumentException(
                "Subnet A format must be x.x.x.x/n");
        }
        String ipA = partsA[0].trim();
        int prefixA = Integer.parseInt(partsA[1].trim());
        SubnetCalculator.validateIpAddress(ipA);
        SubnetCalculator.validateCidr(prefixA);

        // Parse subnet B
        String[] partsB = cidrB.trim().split("/");
        if (partsB.length != 2) {
            throw new IllegalArgumentException(
                "Subnet B format must be x.x.x.x/n");
        }
        String ipB = partsB[0].trim();
        int prefixB = Integer.parseInt(partsB[1].trim());
        SubnetCalculator.validateIpAddress(ipB);
        SubnetCalculator.validateCidr(prefixB);

        // Calculate ranges for A
        long maskA = SubnetCalculator.cidrToMask(prefixA);
        long startA = SubnetCalculator.ipToLong(ipA) & maskA;
        long endA = startA | (maskA ^ 0xFFFFFFFFL);

        // Calculate ranges for B
        long maskB = SubnetCalculator.cidrToMask(prefixB);
        long startB = SubnetCalculator.ipToLong(ipB) & maskB;
        long endB = startB | (maskB ^ 0xFFFFFFFFL);

        // Normalize the CIDR strings to network addresses
        String normalA = SubnetCalculator.longToIp(startA) + "/" + prefixA;
        String normalB = SubnetCalculator.longToIp(startB) + "/" + prefixB;

        // Determine relationship
        if (startA == startB && endA == endB) {
            return new OverlapResult(normalA, normalB,
                Relationship.IDENTICAL,
                normalA,
                "Both subnets cover the exact same range: " +
                SubnetCalculator.longToIp(startA) + " to " +
                SubnetCalculator.longToIp(endA));

        } else if (startA <= startB && endA >= endB) {
            return new OverlapResult(normalA, normalB,
                Relationship.A_CONTAINS_B,
                normalB,
                "Subnet A (" + normalA + ") fully contains Subnet B (" +
                normalB + "). All addresses in B are within A.");

        } else if (startB <= startA && endB >= endA) {
            return new OverlapResult(normalA, normalB,
                Relationship.B_CONTAINS_A,
                normalA,
                "Subnet B (" + normalB + ") fully contains Subnet A (" +
                normalA + "). All addresses in A are within B.");

        } else if (startA <= endB && endA >= startB) {
            long overlapStart = Math.max(startA, startB);
            long overlapEnd = Math.min(endA, endB);
            String overlapRange = SubnetCalculator.longToIp(overlapStart) +
                                  " to " +
                                  SubnetCalculator.longToIp(overlapEnd);
            long overlapSize = overlapEnd - overlapStart + 1;

            return new OverlapResult(normalA, normalB,
                Relationship.OVERLAPPING,
                overlapRange,
                "Subnets partially overlap. " + overlapSize +
                " addresses are shared: " + overlapRange);

        } else {
            return new OverlapResult(normalA, normalB,
                Relationship.DISJOINT,
                "None",
                "Subnets are completely separate with no shared addresses. " +
                "A: " + SubnetCalculator.longToIp(startA) + " to " +
                SubnetCalculator.longToIp(endA) + ", B: " +
                SubnetCalculator.longToIp(startB) + " to " +
                SubnetCalculator.longToIp(endB));
        }
    }
}
