//各棋形的分数
let Score = {
    /** 连五 */
    FIVE: 1e7,
    /** 活四 */
    ALIVE_FOUR: 1e5,
    /** 死四 */
    BLOCKED_FOUR: 1e4,
    /** 活三 */
    ALIVE_THREE: 1e3,
    /** 死三 */
    BLOCKED_THREE: 1e2,
    /** 活二 */
    ALIVE_TWO: 20,
    /** 死二 */
    BLOCKED_TWO: 2,
    /** 无穷值表示胜利 */
    INF: 1e9
}
export default Score;