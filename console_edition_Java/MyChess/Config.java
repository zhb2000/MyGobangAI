package MyChess;

/**
 * 相关参数的配置
 */
public class Config {
    /** 棋盘边长 */
    final public static int BOARD_SIZE = 15;
    /** 最大搜索空格数 */
    /*final*/ public static int MAX_EMPTY_NUM = 15;
    /** 第几层开始算杀 */
    final public static int START_KILLER = 5;
    /** 最大搜索深度 */
    /*final*/ public static int MAX_DEPTH = 11;
    /** 最长搜索时间 */
    final public static long MAX_TIME = 60 * 1000;
    /** 己方近邻系数 */
    final public static double CLOSE_RADIUS = 0.1;
    public static int CALCU_TYPE = 1;
}