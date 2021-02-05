package de.tgx03.caches;

import java.util.Arrays;

public class FullAssociate implements Cache{

    private final int bytesPerLine;
    private final long[] lines;
    private final boolean[] dirty;
    private final int[] lastUsed;

    public FullAssociate(int lines, int bytesPerLine) {
        this.bytesPerLine = bytesPerLine;
        this.lines = new long[lines];
        this.dirty = new boolean[lines];
        this.lastUsed = new int[lines];
        Arrays.fill(this.lines, -1L);
    }

    @Override
    public void readAddress(long address) {
        cycle();
        int position = position(address);
        if (position >= 0) {
            System.out.println("AV cache hit");
            lastUsed[position] = 0;
        } else {
            int leastUsed = leastUsed();
            if (dirty[leastUsed]) {
                System.out.println("AV cache miss with write-back");
                dirty[leastUsed] = false;
            } else {
                System.out.println("AV cache miss");
            }
            lines[leastUsed] = firstAddress(address);
            lastUsed[leastUsed] = 0;
        }
    }

    @Override
    public void writeAddress(long address) {
        cycle();
        int position = position(address);
        if (position >= 0) {
            System.out.println("AV cache hit");
            lastUsed[position] = 0;
            dirty[position] = true;
        } else {
            int leastUsed = leastUsed();
            if (dirty[leastUsed]) {
                System.out.println("AV cache miss with write-back");
            } else {
                System.out.println("AV cache miss");
                dirty[leastUsed] = true;
            }
            lines[leastUsed] = firstAddress(address);
            lastUsed[leastUsed] = 0;
        }
    }

    @Override
    public boolean holdsAddress(long address) {
        return position(address) != -1;
    }

    private long firstAddress(long address) {
        return address - (address % bytesPerLine);
    }

    private int position(long address) {
        long firstAddress = firstAddress(address);
        for (int i = 0; i < lines.length; i++) {
            if ((lines[i]) == firstAddress) {
                return i;
            }
        }
        return -1;
    }

    private int leastUsed() {
        int position = 0;
        int highest = 0;
        for (int i = 0; i < lastUsed.length; i++) {
            if (lastUsed[i] > highest) {
                position = i;
                highest = lastUsed[i];
            }
        }
        return position;
    }

    private void cycle() {
        for (int i = 0; i < lastUsed.length; i++) {
            lastUsed[i]++;
        }
    }

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        builder.append("[ ");
        for (int i = 0; i < lines.length; i++) {
            long start = lines[i];
            long end = start + (bytesPerLine - 1);
            builder.append(Long.toHexString(start)).append("-").append(Long.toHexString(end)).append(" ");
            if (dirty[i]) {
                builder.append("D ");
            }
            if (i != lines.length - 1) {
                builder.append("| ");
            }
        }
        builder.append("]");
        return builder.toString();
    }
}
