package MyChess;

import MyChess.TableCell;

/**
 * TransTable
 */
public class TransTable {
    // 1<<20=1,048,576
    private static final int TABLE_SIZE = 1 << 25;// 33,554,432
    public static TableCell[] cells;

    static {
        cells = new TableCell[TABLE_SIZE];
        for (int i = 0; i < TABLE_SIZE; i++) {
            cells[i] = new TableCell();
        }
        // System.out.println("ok");
    }

    public static TableCell get(long hashCode) {
        int index = (int) (((hashCode % TABLE_SIZE) + TABLE_SIZE) % TABLE_SIZE);
        return cells[index];
    }
}