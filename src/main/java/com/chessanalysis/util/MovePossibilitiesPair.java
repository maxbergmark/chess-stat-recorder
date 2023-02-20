package com.chessanalysis.util;

public class MovePossibilitiesPair {

    private final int left;
    private final int right;

    public MovePossibilitiesPair(int left, int right) {
        this.left = left;
        this.right = right;
    }

    public int getLeft() {
        return left;
    }

    public int getRight() {
        return right;
    }

    public String toString() {
        return String.format("%d:%d", left, right);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        MovePossibilitiesPair other = (MovePossibilitiesPair) o;
        return this.left == other.getLeft() && this.right == other.getRight();
    }

    @Override
    public int hashCode() {
        return 997 * left + 163 * right;
    }
}
