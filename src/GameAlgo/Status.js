let Status = {
    /** 搜索开始的时间 */
    startTime: 0,
    /** 本轮搜索是否超时 */
    isOutTime: false,
    /** 本轮搜索去到的最大深度 */
    goMaxDepth: 0,
    /**根节点搜索某个空位去到的最大深度 */
    posDepth: 0,
    /** 本轮搜索考察过的结点总数 */
    nodeNum: 0,
    /** 叶子结点个数 */
    leafNum: 0,
    /**输赢局面个数 */
    winNum: 0,
    /** 完全命中 */
    completeMatch: 0,
    /** 部分命中 */
    partialMatch: 0,
    /** 叶子结点命中 */
    leafMatch: 0,
    /** ab剪枝次数 */
    ABPruning: 0,
    /**回传f值 */
    f: 0
};
export default Status;