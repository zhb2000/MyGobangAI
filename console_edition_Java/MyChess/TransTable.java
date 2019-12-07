package MyChess;

import MyChess.TableCell;

/**
 * TransTable
 */
public class TransTable {
    private static final int TABLE_SIZE = 1 << 20;// 1,048,576
    public static TableCell[] cells;

    static {
        cells = new TableCell[TABLE_SIZE];
        for (int i = 0; i < TABLE_SIZE; i++) {
            cells[i] = new TableCell();
        }
        System.out.println("ok");
    }

    public static boolean isExist(long hashCode) {
        int index = (int) (((hashCode % TABLE_SIZE) + TABLE_SIZE) % TABLE_SIZE);
        return cells[index].isValid;
    }

    public static TableCell get(long hashCode) {
        int index = (int) (((hashCode % TABLE_SIZE) + TABLE_SIZE) % TABLE_SIZE);
        return cells[index];
    }
}