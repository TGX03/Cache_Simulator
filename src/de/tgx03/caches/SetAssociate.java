package de.tgx03.caches;

import java.util.Arrays;
import java.util.BitSet;

public class SetAssociate implements Cache {

    private final int bytesPerLine;
    private final long[][] sets;
    private final boolean[][] dirty;
    private final int[][] lastUsed;

    public SetAssociate(int sets, int linesPerSet, int bytesPerLine) {
        this.bytesPerLine = bytesPerLine;
        this.sets = new long[sets][linesPerSet];
        this.dirty = new boolean[sets][linesPerSet];
        this.lastUsed = new int[sets][linesPerSet];
        for (long[] set : this.sets) {
            Arrays.fill(set, -1L);
        }
    }

    @Override
    public void readAddress(long address) {
        cycle();
        int index = index(address);
        int position = findInSet(address, index);
        if (position >= 0) {
            System.out.println("A2 cache hit");
            lastUsed[index][position] = 0;
        } else {
            int leastUsed = leastUsed(index);
            if (dirty[index][leastUsed]) {
                System.out.println("A2 cache miss with write-back");
                dirty[index][leastUsed] = false;
            } else {
                System.out.println("A2 cache miss");
            }
            sets[index][leastUsed] = firstAddress(address);
            lastUsed[index][leastUsed] = 0;
        }
    }

    @Override
    public void writeAddress(long address) {
        cycle();
        int index = index(address);
        int position = findInSet(address, index);
        if (position >= 0) {
            System.out.println("A2 cache hit");
            lastUsed[index][position] = 0;
            dirty[index][position] = true;
        } else {
            int leastUsed = leastUsed(index);
            if (dirty[index][leastUsed]) {
                System.out.println("A2 cache miss with write-back");
            } else {
                System.out.println("A2 cache miss");
                dirty[index][leastUsed] = true;
            }
            sets[index][leastUsed] = firstAddress(address);
            lastUsed[index][leastUsed] = 0;
        }
    }

    @Override
    public boolean holdsAddress(long address) {
        int index = index(address);
        long firstAddress = firstAddress(address);
        for (long cur : sets[index]) {
            if (cur == firstAddress) {
                return true;
            }
        }
        return false;
    }

    private int findInSet(long address, int index) {
        long firstAddress = firstAddress(address);
        for (int i = 0; i < sets[index].length; i++) {
            if (sets[index][i] == firstAddress) {
                return i;
            }
        }
        return -1;
    }

    private int index(long address) {
        int startOfIndex = (int) Math.round(Math.sqrt(bytesPerLine));
        int endOfIndex = startOfIndex + (int) Math.round(Math.sqrt(sets.length));
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

    private int leastUsed(int set) {
        int result = 0;
        int highest = 0;
        for (int i = 0; i < lastUsed[set].length; i++) {
            if (lastUsed[set][i] > highest) {
                result = i;
                highest = lastUsed[set][i];
            }
        }
        return result;
    }

    private void cycle() {
        for (int[] set : lastUsed) {
            for (int i = 0; i < set.length; i++) {
                set[i]++;
            }
        }
    }

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder(Long.toHexString(Long.MAX_VALUE).length() * sets.length * sets[0].length * 2);
        for (int i = 0; i < sets.length; i++) {
            builder.append("Set ").append(i).append(":").append("\t");
        }
        for (int inSet = 0; inSet < sets[0].length; inSet++) {
            builder.append(System.getProperty("line.separator")).append("[ ");
            for (int set = 0; set < sets.length; set++) {
                long start = sets[set][inSet];
                long end = start + (bytesPerLine - 1);
                builder.append(Long.toHexString(start)).append("-").append(Long.toHexString(end)).append(" ");
                if (dirty[set][inSet]) {
                    builder.append("D ");
                }
                if (set != sets.length - 1) {
                    builder.append("| ");
                }
            }
            builder.append("]");
        }
        return builder.toString();
    }
}
