package MyChess;

/**
 * 记录搜索状态
 */
public class Status {

    /**是否开启算杀模式 */
    public static boolean killerMode = false;
    /**搜索开始的时间 */
    public static long startTime = 0;
    /**本轮搜索是否超时 */
    public static boolean isOutTime = false;
    /**本轮搜索去到的最大深度 */
    public static int goMaxDepth = 0;
    /**本轮搜索考察过的结点总数 */
    public static int nodeNum = 0;
}