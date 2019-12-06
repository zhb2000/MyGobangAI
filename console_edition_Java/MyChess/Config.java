package MyChess;

/**
 * 相关参数的配置
 */
public class Config {
    /** 棋盘边长 */
    final public static int BOARD_SIZE = 15;
    /** 最大搜索空格数 */
    final public static int MAX_EMPTY_NUM = 10;// debug 20
    /** 第几层开始算杀 */
    final public static int START_KILLER = 4;
    /** 最大搜索深度 */
    final public static int MAX_DEPTH = 6;// debug
    /** 最长搜索时间 */
    final public static long MAX_TIME = 15 * 1000;
}