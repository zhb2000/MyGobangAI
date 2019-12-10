import { EMPTY_CHESS, COM_CHESS, HUM_CHESS } from "./ChessType.js";
import { BOARD_SIZE } from "./Config.js";
import array from "./MyArray.js";
import Zobrist from "./Zobrist.js";
import Direction from "./Direction.js";
import Coord from "./Coord.js";

export default class ChessBoard {
    /**
     * 构造函数
     * @constructor
     */
    constructor() {
        /**
         * 棋盘矩阵
         * @type Number[15][15]
         */
        this.boardMatrix = array.create(BOARD_SIZE.BOARD_SIZE);
        /**
         * 棋子总数 
         */
        this.chessNum = 0;
        /**
         * 人类的空位分值
         * @type Number[15][15][4]
         */
        this.humHeuristic = array.create(BOARD_SIZE, BOARD_SIZE, 4);
        /**
         * 电脑的空位分值
         * @type Number[15][15][4]
         */
        this.comHeuristic = array.create(BOARD_SIZE, BOARD_SIZE, 4);
        /**
         * 位置附近棋子的总数 
         * @type Number[15][15] 
         */
        this.neighborNum = array.create(BOARD_SIZE, BOARD_SIZE);
        /**
         * 位置近邻人类棋子数量
         * @type Number[15][15]
         */
        this.humCloseNeighbor = array.create(BOARD_SIZE, BOARD_SIZE);
        /**
         * 位置近邻电脑棋子数量
         * @type Number[15][15]
         */
        this.comCloseNeighbor = array.create(BOARD_SIZE, BOARD_SIZE);
        /**
         * Zobrist对象
         */
        this.zobris = new Zobrist();
    }

    /**
     * 落子并更新相应的数据
     * 
     * @param {Number} x 落子的行号
     * @param {Number} y 落子的列号
     * @param {Number} type 落子的类型
     * 
     * @returns {Boolean} 操作是否合法
     */
    put(x, y, type) {
        if (ChessBoard.outOfBound(x, y)) {
            return false;
        }
        if (this.boardMatrix[x][y] != EMPTY_CHESS) {
            return false;
        }
        this.boardMatrix[x][y] = type;
        this.chessNum++;// 更新棋子总数
        this.updateNeighborAfterDo(x, y, EMPTY_CHESS); // 更新邻居矩阵
        this.updateHeuristicAfterDo(x, y); // 更新启发函数矩阵
        this.zobris.goUpdate(x, y, type);// 更新哈希值
        return true;
    }

    /**
     * 撤销棋子并更新相应的数据
     * 
     * @param {Number} x 行号
     * @param {Number} y 列号
     * @param {Object} oldHum 人类启发函数备份
     * @param {Object} oldCom 电脑启发函数备份
     * 
     * @returns {Boolean} 操作是否合法
     */
    undo(x, y, oldHum, oldCom) {
        if (ChessBoard.outOfBound(x, y)) {
            return false;
        }
        if (this.boardMatrix[x][y] === EMPTY_CHESS) {
            return false;
        }
        let oldType = this.boardMatrix[x][y];// 撤销前原来的棋子类型
        this.boardMatrix[x][y] = EMPTY_CHESS;
        this.chessNum--;// 更新棋子总数
        this.updateNeighborAfterDo(x, y, oldType);// 更新邻居矩阵
        this.restoreHeuristic(x, y, oldHum, oldCom);// 更新启发函数矩阵
        this.zobrist.goUpdate(x, y, oldType);
        return true;
    }

    /**
     * 判断棋盘是否已满
     * 
     * @return {Boolean} 棋盘是否已满
     */
    isFull() {
        return this.chessNum === BOARD_SIZE * BOARD_SIZE;
    }

    /**
     * (x,y)两格内是否有邻居，无论什么类型
     * 
     * @param {Number} x 行号
     * @param {Number} y 列号
     * 
     * @returns {Boolean} 两格内是否有邻居
     */
    hasNeighbor(x, y) {
        return this.neighborNum[x][y] > 0;
    }

    /**
     * 对(x,y)位置落子或撤销后，更新邻居矩阵
     * 
     * @param {Number} x 落子位置或撤销位置的行号
     * @param {Number} y 落子位置或撤销位置的列号
     * @param {Number} oldType 操作前(x,y)的棋子类型
     */
    updateNeighborAfterDo(x, y, oldType) {
        //TODO
    }

    /**
     * 备份启发函数矩阵
     * 
     * @param {Number} x 中心位置行号
     * @param {Number} y 中心位置列号
     * 
     * @returns {Object} {hum:Number[][][], com:Number[][][]}
     */
    backupHeuristic(x, y) {
        //TODO
    }

    /**
     * 恢复以(x,y)为中心的米字形的启发函数
     * @param {Number} x 中心位置行号
     * @param {Number} y 中心位置列号
     * @param {Object} oldHum 人类
     * @param {Object} oldCom 电脑
     */
    restoreHeuristic(x, y, oldHum, oldCom) {
        //TODO
    }

    /**
     * 落子或撤销后对启发函数值矩阵进行更新
     * 
     * @param {Number} x 中心位置的行号
     * @param {Number} y 中心位置的列号
     */
    updateHeuristicAfterDo(x, y) {
        //TODO
    }

    /**
     * 计算(x,y)处的启发函数值，并给启发函数矩阵的相应位置赋值
     * 为了提高速度，只修改direction方向的启发函数值
     * 
     * @param {Number} x 空位的行号
     * @param {Number} y 空位的列号
     * @param {Number} direction 指定的方向
     */
    calcuHeuristic(x, y, direction) {
        //TODO
    }

    /**
     * 根据直线中棋形的种类和数量，对直线coords打分
     * 
     * @param {Coord[]} coords 
     * @param {Number} ChessType 
     * 
     * @returns {Number} 打出的分数
     */
    score(coords, ChessType) {
        //TODO
    }

    /**
     * 获取某一方挑选空格时的启发式函数值
     * 
     * @param {Number} x 空格的行号
     * @param {Number} y 空格的列号
     * @param {Number} type 哪一方的棋子要下到这个空格上
     * 
     * @returns {Number} (x,y)空格对于该方的启发函数值
     */
    heuristic(x, y, type) {
        //TODO
    }

    /**
     * 获取一组“优良的”空格位置
     * 
     * @param {Number} type 哪一方需要落子
     * 
     * @returns {Coord[]} 一组“优良的”空格位置
     */
    generator(type) {
        //TODO
    }

    /**
     * 算杀
     * 
     * @param {Number} type 哪一方需要落子
     * 
     * @returns {Coord[]} 一组空格位置
     */
    generatorKill(type) {
        //TODO
    }

    /**
     * 评估函数，对整个棋盘的局势进行评估 
     * 越大电脑越有利，越小人类越有利
     * 
     * @param {Number} type 哪一方已经落子
     * 
     * @returns {Number} 评估值
     */
    evaluate(type) {
        //TODO
    }

    /**
     * 单方面评估函数，只针对某一方进行评估
     * 
     * @param {Number} type 对哪一方进行评估
     * 
     * @return {Number} 单方面评估值
     */
    evaluateOneSide(type) {
        //TODO
    }

    /**
     * 在(x,y)被落子后，落子的那一方是否赢了
     * 
     * @param {Number} x 落子行号
     * @param {Number} y 落子列号
     * 
     * @returns {Boolean} 落子方是否赢了
     */
    isWin(x, y) {
        return this.heuristic(x, y, this.boardMatrix[x][y]);
    }

    /**
     * 把直线坐标变成标准化直线 坐标允许越界 两头添加阻塞
     * 
     * @param {Coord[]} line 坐标直线
     * @param {Number} type 己方
     * 
     * @returns {Number[]} 标准直线
     */
    standardizeLine(line, type) {
        //TODO
    }

    /**
     * 获取棋子的总数
     * 
     * @returns {Number} 棋子的总数
     */
    getNumber() {
        return this.chessNum;
    }

    /**
     * 获取棋盘哈希值
     * @returns {Number} 哈希值
     */
    getCode() {
        return this.zobris.code();
    }

    /**
     * 判断坐标是否越界
     * @param {Number} x 行号
     * @param {Number} y 列号
     * @returns {Boolean} (x,y)是否越界
     */
    static outOfBound(x, y) {
        return (x < 0 || x >= BOARD_SIZE || y < 0 || y >= BOARD_SIZE);
    }

    /**
     * 返回以(x,y)为中心，长度为9的直线上的所有坐标
     * 
     * @param {Number} x 坐标行号
     * @param {Number} y 坐标列号
     * @param {Number} direction 方向
     * 
     * @returns {Coord[]} 直线上的所有坐标
     */
    static lineCoords(x, y, direction) {
        let coords = [];
        if (direction === Direction.VERTICAL) {// 竖直方向
            for (let i = x - 4; i <= x + 4; i++) {
                coords.push(new Coord(i, y));
            }
        } else if (direction == Direction.HORIZONTAL) {// 水平方向
            for (let j = y - 4; j <= y + 4; j++) {
                coords.push(new Coord(x, j));
            }
        } else if (direction == Direction.DIAGONAL) {// 对角线
            let i = x - 4, j = y - 4;
            for (let k = 0; k <= 8; k++) {
                coords.push(new Coord(i + k, j + k));
            }
        } else {// 反对角线
            let i = x + 4, j = y - 4;
            for (let k = 0; k <= 8; k++) {
                coords.push(new Coord(i - k, j + k));
            }
        }
        return coords;
    }
}