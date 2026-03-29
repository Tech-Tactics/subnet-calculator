package com.subnetcalc.model;

/**
 * OverlapResult holds the outcome of comparing two CIDR ranges.
 * Reports whether they overlap, whether one contains the other,
 * or whether they are completely separate.
 *
 * @author Joseph Black
 */
public class OverlapResult {

    public enum Relationship {
        DISJOINT,
        OVERLAPPING,
        A_CONTAINS_B,
        B_CONTAINS_A,
        IDENTICAL
    }

    private final String subnetA;
    private final String subnetB;
    private final Relationship relationship;
    private final String overlapRange;
    private final String description;

    public OverlapResult(String subnetA, String subnetB,
                         Relationship relationship, String overlapRange,
                         String description) {
        this.subnetA = subnetA;
        this.subnetB = subnetB;
        this.relationship = relationship;
        this.overlapRange = overlapRange;
        this.description = description;
    }

    public String getSubnetA()            { return subnetA; }
    public String getSubnetB()            { return subnetB; }
    public Relationship getRelationship() { return relationship; }
    public String getOverlapRange()       { return overlapRange; }
    public String getDescription()        { return description; }

    @Override
    public String toString() {
        return String.format("%s vs %s: %s\n%s",
            subnetA, subnetB, relationship, description);
    }
}
