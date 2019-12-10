package MyChess;

/**
 * 记录搜索状态
 */
public class Status {

    /** 是否开启算杀模式 */
    public static boolean killerMode = false;
    /** 搜索开始的时间 */
    public static long startTime = 0;
    /** 本轮搜索是否超时 */
    public static boolean isOutTime = false;
    /** 本轮搜索去到的最大深度 */
    public static int goMaxDepth = 0;
    /** 本轮搜索考察过的结点总数 */
    public static int nodeNum = 0;
    /** 叶子结点个数 */
    public static int leafNum = 0;
    /** 完全命中 */
    public static int completeMatch = 0;
    /** 部分命中 */
    public static int partialMatch = 0;
    /** 叶子结点命中 */
    public static int leafMatch = 0;
    /** ab剪枝次数 */
    public static int ABPruning = 0;

}