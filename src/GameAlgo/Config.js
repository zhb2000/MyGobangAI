//相关参数配置
let Config = {
    /** 棋盘边长 */
    BOARD_SIZE: 15,
    /** 最大搜索空格数 */
    MAX_EMPTY_NUM: 10,
    /** 第几层开始算杀 */
    START_KILLER: 4,
    /** 最大搜索深度 */
    MAX_DEPTH: 8,
    /** 最长搜索时间 */
    MAX_TIME: 30 * 1000,
    /** 己方近邻系数 */
    CLOSE_RADIUS: 0,
    /**开启置换表优化 */
    useTransTable: true,
    /**使用启发函数优化 */
    useHuristic: true
};
export default Config;