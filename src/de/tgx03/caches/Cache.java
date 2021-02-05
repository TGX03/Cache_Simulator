package de.tgx03.caches;

public interface Cache {

    void readAddress(long address);

    void writeAddress(long address);

    boolean holdsAddress(long address);
}
