/**无效的f值 */
const INVALID_F = 0;
/**准确的f值 */
const EXACT_F = 1;
/**f值是下界 */
const LOWER_F = 2;
/**f值是上界 */
const UPPER_F = 3;
/**
 * TableCell
 */
export default class TableCell {
    /**
     * 构造函数
     * @constructor
     */
    constructor() {
        /**表项是否有效 */
        this.isValid = false;
        /**表项对应的哈希值 */
        this.hashCode = 0;
        /**棋子个数 */
        this.chessNum = 0;

        /**记录的结点f值的类型 */
        this.fType = INVALID_F;
        /**结点回传f值 */
        this.fValue = 19;
        /**子树深度 */
        this.treeDepth = 0;

        /**叶节点评估值是否有效 */
        this.evaValid = false;
        /**叶节点评估值 */
        this.evaluate = 0;
    }

    /**
     * 该表项是否有效
     * @param {Number} hashCode 棋盘哈希值
     * @param {Number} chessNum 棋盘棋子个数
     */
    validCell(hashCode, chessNum) {
        if (this.isValid && this.hashCode === hashCode && this.chessNum != chessNum) {
            console.log("same code, not same num");
        }
        return (this.isValid
            && this.hashCode === hashCode
            && this.chessNum === chessNum);
    }
}
export { INVALID_F, EXACT_F, LOWER_F, UPPER_F };