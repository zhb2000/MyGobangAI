//相关参数配置
export default {
    /** 棋盘边长 */
    BOARD_SIZE: 15,
    /** 最大搜索空格数 */
    MAX_EMPTY_NUM: 15,
    /** 第几层开始算杀 */
    START_KILLER: 5,
    /** 最大搜索深度 */
    MAX_DEPTH: 11,
    /** 最长搜索时间 */
    MAX_TIME: 60 * 1000,
    /** 己方近邻系数 */
    CLOSE_RADIUS: 0.1
};