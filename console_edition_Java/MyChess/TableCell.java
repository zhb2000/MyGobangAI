package MyChess;

import static MyChess.Config.BOARD_SIZE;

/**
 * TableCell
 */
public class TableCell {

    public boolean isValid = false;
    public int chessNum;

    //public int[][][] humBackup = new int[BOARD_SIZE][BOARD_SIZE][4];
    //public int[][][] comBackup = new int[BOARD_SIZE][BOARD_SIZE][4];

    public static int INVALID_F = 0;
    public static int EXACT_F = 1;
    public static int MIN_F = 2;
    public static int MAX_F = 3;
    public int fType = INVALID_F;
    public int fValue;
    public int treeDepth;
}