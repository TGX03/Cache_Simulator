package de.tgx03.caches;

import java.util.Arrays;
import java.util.BitSet;

public class DirectMapping implements Cache{

    private final int bytesPerLine;
    private final long[] lines;
    private final boolean[] dirty;

    public DirectMapping(int lines, int bytesPerLine) {
        this.bytesPerLine = bytesPerLine;
        this.lines = new long[lines];
        this.dirty = new boolean[lines];
        Arrays.fill(this.lines, -1L);
    }

    @Override
    public void readAddress(long address) {
        int index = index(address);
        if (lines[index(address)] == firstAddress(address)) {
            System.out.println("DM cache hit");
        } else {
            if (dirty[index]) {
                System.out.println("DM cache miss with write-back");
                dirty[index] = false;
            } else {
                System.out.println("DM cache miss");
            }
            lines[index] = firstAddress(address);
        }
    }

    @Override
    public void writeAddress(long address) {
        int index = index(address);
        if (lines[index(address)] == firstAddress(address)) {
            System.out.println("DM cache hit");
            dirty[index] = true;
        } else {
            if (dirty[index]) {
                System.out.println("DM cache miss with write-back");
            } else {
                System.out.println("DM cache miss");
                dirty[index] = true;
            }
            lines[index] = firstAddress(address);
        }
    }

    @Override
    public boolean holdsAddress(long address) {
        return lines[index(address)] == firstAddress(address);
    }

    private int index(long address) {
        int startOfIndex = (int) Math.round(Math.sqrt(bytesPerLine));
        int endOfIndex = startOfIndex + (int) Math.round(Math.sqrt(lines.length));
        BitSet bits = BitSet.valueOf(new long[]{address});
        bits.clear(0, startOfIndex);
        bits.clear(endOfIndex, 64);
        if (bits.toLongArray().length == 0) {
            return 0;
        } else {
            long cleared = bits.toLongArray()[0];
            cleared = cleared >>> startOfIndex;
            return (int) cleared;
        }
    }

    private long firstAddress(long address) {
        return address - (address % bytesPerLine);
    }

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder(Long.toHexString(Long.MAX_VALUE).length() * lines.length * 2);
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
