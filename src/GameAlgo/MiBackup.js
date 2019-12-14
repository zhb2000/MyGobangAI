import array from "./MyArray.js";
//import Coord from "./Coord.js";

/**
 * 米字形备份
 */
export default class MiBackup {
    /**
     * 构造函数，创建出数组
     * @constructor
     */
    constructor() {
        /**
         * 坐标数组，4 * 9 = 36
         * 
         * @type Coord[]
         */
        this.coords = [];
        /**
         * 人类启发函数，每个位置有4个维度
         * 
         * hum[36][4]
         * 
         * @type Number[][]
         */
        this.hum = array.create(36, 4);
        /**
         * 电脑启发函数，每个位置有4个维度
         * 
         * com[36][4]
         * 
         * @type Number[][]
         */
        this.com = array.create(36,4);
    }
}