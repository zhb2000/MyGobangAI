/**
 * 坐标类
 */
export default class Coord {
    /**
     * @constructor
     * @param {Number} [x=0] 行号
     * @param {Number} [y=0] 列号
     * @param {Number} [h] 分数
     */
    constructor(x = 0, y = 0, h) {
        /**坐标行号 */
        this.x = x;
        /**坐标列号 */
        this.y = y;
        /**分数 */
        this.h = h;
    }
}