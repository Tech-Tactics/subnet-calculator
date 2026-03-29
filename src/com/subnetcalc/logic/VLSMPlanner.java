package com.subnetcalc.logic;

import com.subnetcalc.model.VLSMAllocation;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

/**
 * VLSMPlanner takes a parent network block and a list of host
 * requirements (each with a label like "Marketing" or "VoIP Phones")
 * and allocates optimally sized subnets for each one.
 *
 * The allocation follows the standard VLSM approach: sort by
 * largest requirement first, then assign the smallest possible
 * subnet that fits each need. This minimizes wasted address space.
 *
 * @author Joseph Black
 */
public class VLSMPlanner {

    /**
     * A single requirement before allocation.
     */
    public static class SubnetRequest {
        private final String label;
        private final int hostsNeeded;

        public SubnetRequest(String label, int hostsNeeded) {
            this.label = label;
            this.hostsNeeded = hostsNeeded;
        }

        public String getLabel()    { return label; }
        public int getHostsNeeded() { return hostsNeeded; }
    }

    /**
     * Plan VLSM allocations from a parent network block.
     *
     * @param parentNetwork dotted-decimal network address (e.g. "10.0.0.0")
     * @param parentCidr    parent block prefix length (e.g. 16)
     * @param requests      list of subnet requirements with labels and host counts
     * @return list of VLSMAllocation results in allocation order
     * @throws IllegalArgumentException if the parent block is too small
     */
    public static List<VLSMAllocation> plan(String parentNetwork,
                                            int parentCidr,
                                            List<SubnetRequest> requests) {
        SubnetCalculator.validateIpAddress(parentNetwork);
        SubnetCalculator.validateCidr(parentCidr);

        if (requests == null || requests.isEmpty()) {
            throw new IllegalArgumentException("At least one subnet request is required");
        }

        // Validate each request
        for (SubnetRequest req : requests) {
            if (req.getHostsNeeded() < 1) {
                throw new IllegalArgumentException(
                    "Host count must be at least 1 for: " + req.getLabel());
            }
        }

        // Sort by largest requirement first for optimal allocation
        List<SubnetRequest> sorted = new ArrayList<>(requests);
        sorted.sort(Comparator.comparingInt(SubnetRequest::getHostsNeeded).reversed());

        long parentStart = SubnetCalculator.ipToLong(parentNetwork);
        long parentMask = SubnetCalculator.cidrToMask(parentCidr);
        long parentEnd = parentStart | (parentMask ^ 0xFFFFFFFFL);

        // Make sure the parent network is correctly aligned
        parentStart = parentStart & parentMask;

        long currentPosition = parentStart;
        List<VLSMAllocation> allocations = new ArrayList<>();

        for (SubnetRequest req : sorted) {
            int subnetCidr = SubnetCalculator.hostsToMinCidr(req.getHostsNeeded());
            long subnetSize = SubnetCalculator.cidrToTotalHosts(subnetCidr);
            long subnetMask = SubnetCalculator.cidrToMask(subnetCidr);

            // Align the current position to the subnet boundary
            if ((currentPosition % subnetSize) != 0) {
                currentPosition = ((currentPosition / subnetSize) + 1) * subnetSize;
            }

            long subnetNetwork = currentPosition;
            long subnetBroadcast = subnetNetwork + subnetSize - 1;

            // Check if this allocation fits in the parent block
            if (subnetBroadcast > parentEnd) {
                throw new IllegalArgumentException(String.format(
                    "Not enough space in /%d block for '%s' (%d hosts). " +
                    "Try a larger parent block or reduce host requirements.",
                    parentCidr, req.getLabel(), req.getHostsNeeded()));
            }

            String firstHost = (subnetCidr >= 31) ? "N/A" :
                SubnetCalculator.longToIp(subnetNetwork + 1);
            String lastHost = (subnetCidr >= 31) ? "N/A" :
                SubnetCalculator.longToIp(subnetBroadcast - 1);

            int usable = (subnetCidr >= 31) ? 0 : (int)(subnetSize - 2);
            int wasted = usable - req.getHostsNeeded();

            VLSMAllocation allocation = new VLSMAllocation(
                req.getLabel(),
                req.getHostsNeeded(),
                (int) subnetSize,
                subnetCidr,
                SubnetCalculator.longToIp(subnetNetwork),
                SubnetCalculator.longToIp(subnetBroadcast),
                SubnetCalculator.longToIp(subnetMask),
                firstHost,
                lastHost,
                wasted
            );

            allocations.add(allocation);
            currentPosition = subnetBroadcast + 1;
        }

        return allocations;
    }

    /**
     * Calculate total address space used and wasted across all allocations.
     *
     * @return int array: [0] = total allocated, [1] = total wasted
     */
    public static int[] getSummary(List<VLSMAllocation> allocations) {
        int totalAllocated = 0;
        int totalWasted = 0;
        for (VLSMAllocation a : allocations) {
            totalAllocated += a.getAllocatedSize();
            totalWasted += a.getWastedAddresses();
        }
        return new int[]{ totalAllocated, totalWasted };
    }
}
