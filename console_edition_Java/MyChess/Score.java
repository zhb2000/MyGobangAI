package MyChess;

/**
 * 各棋形的分数
 */
public class Score {

    /** 连五 */
    final public static int FIVE = 10000000;// 1e7
    /** 活四 */
    final public static int ALIVE_FOUR = 100000;// 1e5
    /** 活三 */
    final public static int ALIVE_THREE = 1000;// 1e3
    /** 活二 */
    final public static int ALIVE_TWO = 100;// 1e2
    /** 活一 */
    final public static int ALIVE_ONE = 10;// 1e1
    /** 死四 */
    final public static int BLOCKED_FOUR = 10000;// 1e4
    /** 死三 */
    final public static int BLOCKED_THREE = 100;// 1e2
    /** 死二 */
    final public static int BLOCKED_TWO = 10;// 1e1
    /** 死一 */
    final public static int BLOCKED_ONE = 1;
    /** 无穷值表示胜利 */
    final public static int INF = FIVE;
}