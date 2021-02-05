package de.tgx03.caches;

import de.tgx03.ConsoleReader;

public class UI {

    private static final Cache[] caches = new Cache[3];

    /**
     * @param args Number of lines, byte per lines and number of sets for SetAssociate
     */
    public static void main(String[] args) {
        initialize(args);
        boolean exit = false;
        while (!exit) {
            String input = ConsoleReader.readLine();
            if (input.equals("exit")) {
                exit = true;
            } else {
                long address = Long.decode(input);
                for (Cache cache : caches) {
                    cache.readAddress(address);
                }
            }
        }
    }

    private static void initialize(String[] args) {
        int lines = Integer.parseInt(args[0]);
        int bytesPerLine = Integer.parseInt(args[1]);
        int sets = Integer.parseInt(args[2]);
        caches[0] = new DirectMapping(lines, bytesPerLine);
        caches[1] = new FullAssociate(lines, bytesPerLine);
        caches[2] = new SetAssociate(sets, lines/sets, bytesPerLine);
        if (lines % sets != 0) {
            throw new IllegalArgumentException("Number of lines must be divisable by number of sets");
        }
    }
}
