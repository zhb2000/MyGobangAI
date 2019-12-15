import Config from "./Config.js";
import { COM_CHESS, HUM_CHESS } from "./ChessType.js";

function randomNum() {
    return Math.floor(Math.random() * Number.MAX_SAFE_INTEGER);
}

/**
 * Zobrist
 */
export default class Zobrist {
    /**
     * @constructor
     */
    constructor() {
        /**
         * 空位状态
         * @type Number[]
         */
        this.empty = [];
        /**
         * 人类棋子状态
         * @type Number[]
         */
        this.hum = [];
        /**
         * 电脑棋子状态
         * @type Number[]
         */
        this.com = [];
        /**
         * 哈希值
         * @type Number
         */
        this.zobristCode = 0;

        for (let i = 0; i < Config.BOARD_SIZE * Config.BOARD_SIZE; i++) {
            this.empty.push(randomNum());
            this.hum.push(randomNum());
            this.com.push(randomNum());
        }
        for (let i = 0; i < Config.BOARD_SIZE * Config.BOARD_SIZE; i++) {
            this.zobristCode ^= this.empty[i];
        }
    }
    /**
     * 落子或撤销后更新哈希值
     * 
     * @param {Number} x 行号
     * @param {Number} y 列号
     * @param {Number} oldChess 旧棋子类型
     * @param {Number} newChess 新棋子类型
     */
    goUpdate(x, y, oldChess, newChess) {
        let index = x * Config.BOARD_SIZE + y;
        let oldArray, newArray;
        if (oldChess === HUM_CHESS) {
            oldArray = this.hum;
        } else if (oldChess === COM_CHESS) {
            oldArray = this.com;
        } else {
            oldArray = this.empty;
        }
        if (newChess === HUM_CHESS) {
            newArray = this.hum;
        } else if (newChess === COM_CHESS) {
            newArray = this.com;
        } else {
            newArray = this.empty;
        }
        this.zobristCode ^= oldArray[index];
        this.zobristCode ^= newArray[index];
    }
}