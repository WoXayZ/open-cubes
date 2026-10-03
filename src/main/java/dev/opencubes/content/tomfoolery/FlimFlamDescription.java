package dev.opencubes.content.tomfoolery;

public final class FlimFlamDescription {

    private final String name;
    private final int cost;
    private final int weight;
    private final FlimFlamAction action;
    private final boolean safe;
    private final boolean silent;
    private final int lowerBound;
    private final int upperBound;

    public FlimFlamDescription(String name, int cost, int weight, FlimFlamAction action,
                               boolean safe, boolean silent, int lowerBound, int upperBound) {
        this.name = name;
        this.cost = cost;
        this.weight = weight;
        this.action = action;
        this.safe = safe;
        this.silent = silent;
        this.lowerBound = lowerBound;
        this.upperBound = upperBound;
    }

    public static FlimFlamDescription create(String name, int cost, int weight, FlimFlamAction action) {
        int lower = Integer.MIN_VALUE;
        int upper = Integer.MAX_VALUE;
        if (cost < 0) {
            upper = cost;
        } else {
            lower = cost;
        }
        return new FlimFlamDescription(name, cost, weight, action, true, false, lower, upper);
    }

    public FlimFlamDescription markUnsafe() {
        return new FlimFlamDescription(name, cost, weight, action, false, silent, lowerBound, upperBound);
    }

    public FlimFlamDescription markSilent() {
        return new FlimFlamDescription(name, cost, weight, action, safe, true, lowerBound, upperBound);
    }

    public FlimFlamDescription setRange(int lower, int upper) {
        return new FlimFlamDescription(name, cost, weight, action, safe, silent, lower, upper);
    }

    public String name() {
        return name;
    }

    public int cost() {
        return cost;
    }

    public int weight() {
        return weight;
    }

    public FlimFlamAction action() {
        return action;
    }

    public boolean safe() {
        return safe;
    }

    public boolean silent() {
        return silent;
    }

    public boolean canApply(int luck) {
        return luck >= lowerBound && luck <= upperBound;
    }
}
