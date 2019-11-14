package MyChess;

/**
 * 标准化后的棋子类型
 */
public class StandardType {

    /** 空位 */
    final public static int EMPTY = 0;
    /** 己方棋子 */
    final public static int SELF = 1;
    /** 非法位置或被敌方阻塞 */
    final public static int BLOCKED = -1;
}