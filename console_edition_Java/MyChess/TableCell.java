package MyChess;

//import static MyChess.Config.BOARD_SIZE;

/**
 * TableCell
 */
public class TableCell {

    public boolean isValid = false;
    public int chessNum;

    public static final int INVALID_F = 0;
    public static final int EXACT_F = 1;
    public static final int MIN_F = 2;
    public static final int MAX_F = 3;

    public int fType = INVALID_F;
    public int fValue;
    public int treeDepth;

    public boolean evaValid = false;
    public int evaluate;
}