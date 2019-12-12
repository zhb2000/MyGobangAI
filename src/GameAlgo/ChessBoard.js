import { EMPTY_CHESS, COM_CHESS, HUM_CHESS } from "./ChessType.js";
import { BOARD_SIZE } from "./Config.js";
import Config from "./Config.js";
import array from "./MyArray.js";
import Zobrist from "./Zobrist.js";
import Direction from "./Direction.js";
import Coord from "./Coord.js";
import MiBackup from "./MiBackup.js";
import scoreLine from "./ScoreCalculator.js";
import StdType from "./StandardType.js";
import Score from "./Score.js";

export default class ChessBoard {
    /**
     * 构造函数
     * @constructor
     */
    constructor() {
        /**
         * 棋盘矩阵 Number[15][15]
         * @type Number[][]
         */
        this.boardMatrix = array.create(BOARD_SIZE.BOARD_SIZE);
        /**
         * 棋子总数 
         * @type Number
         */
        this.chessNum = 0;
        /**
         * 人类的空位分值 Number[15][15][4]
         * @type Number[][][]
         */
        this.humHeuristic = array.create(BOARD_SIZE, BOARD_SIZE, 4);
        /**
         * 电脑的空位分值 Number[15][15][4]
         * @type Number[][][]
         */
        this.comHeuristic = array.create(BOARD_SIZE, BOARD_SIZE, 4);
        /**
         * 位置附近棋子的总数 Number[15][15] 
         * @type Number[][] 
         */
        this.neighborNum = array.create(BOARD_SIZE, BOARD_SIZE);
        /**
         * 位置近邻人类棋子数量 Number[15][15]
         * @type Number[][]
         */
        this.humCloseNeighbor = array.create(BOARD_SIZE, BOARD_SIZE);
        /**
         * 位置近邻电脑棋子数量 Number[15][15]
         * @type Number[][]
         */
        this.comCloseNeighbor = array.create(BOARD_SIZE, BOARD_SIZE);
        /**
         * Zobrist对象
         * @type Zobrist
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
     * @param {MiBackup} backup 米字位置启发函数备份
     * 
     * @returns {Boolean} 操作是否合法
     */
    undo(x, y, backup) {
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
        this.restoreHeuristic(backup);// 更新启发函数矩阵
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
        //更新2格范围内邻居矩阵
        for (let i = x - 2; i <= x + 2; i++) {
            for (let j = y - 2; j <= y + 2; j++) {
                if (!ChessBoard.outOfBound(i, j) && i != x && j != y) {
                    if (oldType == EMPTY_CHESS) {
                        // 在(x,y)落子，周围邻居数量加1
                        this.neighborNum[i][j]++;
                    } else {
                        // 在(x,y)撤销，周围邻居数量减1
                        this.neighborNum[i][j]--;
                    }
                }
            }
        }
        //更新己方邻居矩阵
        for (let i = x - 1; i <= x + 1; i++) {
            for (let j = y - 1; j <= y + 1; j++) {
                if (!ChessBoard.outOfBound(i, j) && i != x && j != y) {
                    // 在(x,y)落子
                    if (oldType == EMPTY_CHESS) {
                        if (this.boardMatrix[x][y] == HUM_CHESS) {
                            // (x,y)现在是人类棋子
                            this.humCloseNeighbor[i][j]++;
                        } else {
                            // (x,y)现在是电脑棋子
                            this.comCloseNeighbor[i][j]++;
                        }
                    } else {// 在(x,y)撤销了一个棋子
                        if (oldType == HUM_CHESS) {
                            // (x,y)原来是人类棋子
                            this.humCloseNeighbor[i][j]--;
                        } else {
                            // (x,y)原来是电脑棋子
                            this.comCloseNeighbor[i][j]--;
                        }
                    }
                }
            }
        }
    }

    /**
     * 备份启发函数矩阵
     * 
     * @param {Number} x 中心位置行号
     * @param {Number} y 中心位置列号
     * 
     * @returns {MiBackup} 启发函数的备份
     */
    backupHeuristic(x, y) {
        let verticals = ChessBoard.lineCoords(x, y, Direction.VERTICAL);
        let horizontals = ChessBoard.lineCoords(x, y, Direction.HORIZONTAL);
        let diagonals = ChessBoard.lineCoords(x, y, Direction.DIAGONAL);
        let antidiagonals = ChessBoard.lineCoords(x, y, Direction.ANTIDIAGONAL);
        let backup = new MiBackup();
        backup.coords = [].concat(verticals).concat(horizontals).concat(diagonals).concat(antidiagonals);
        for (let i = 0; i < backup.coords.length; i++) {
            let coord = backup.coords[i];
            let x = coord.x, y = coord.y;
            if (!ChessBoard.outOfBound(x, y)) {
                for (let dir = 0; dir < 4; dir++) {
                    backup.hum[i][dir] = this.humHeuristic[x][y][dir];
                    backup.com[i][dir] = this.comHeuristic[x][y][dir];
                }
            }
        }
    }

    /**
     * 恢复米字形的启发函数
     * 
     * @param {MiBackup} backup 米字位置的启发函数备份
     */
    restoreHeuristic(backup) {
        for (let i = 0; i < backup.coords.length; i++) {
            let coord = backup.coords[i];
            let x = coord.x, y = coord.y;
            if (!ChessBoard.outOfBound(x, y)) {
                for (let dir = 0; dir < 4; dir++) {
                    this.humHeuristic[x][y][dir] = backup.hum[i][dir];
                    this.comHeuristic[x][y][dir] = backup.com[i][dir];
                }
            }
        }
    }

    /**
     * 落子或撤销后对启发函数值矩阵进行更新
     * 
     * @param {Number} x 中心位置的行号
     * @param {Number} y 中心位置的列号
     */
    updateHeuristicAfterDo(x, y) {
        let verticals = ChessBoard.lineCoords(x, y.Direction.VERTICAL);
        let horizontals = ChessBoard.lineCoords(x, y, Direction.HORIZONTAL);
        let diagonals = ChessBoard.lineCoords(x, y, Direction.DIAGONAL);
        let antidiagonals = ChessBoard.lineCoords(x, y, Direction.ANTIDIAGONAL);
        this.calcuLineHeuristic(verticals, Direction.VERTICAL);
        this.calcuHeuristic(horizontals, Direction.HORIZONTAL);
        this.calcuHeuristic(diagonals, Direction.diagonals);
        this.calcuHeuristic(antidiagonals, Direction.ANTIDIAGONAL);
    }

    /**
     * 计算并更新直线上坐标的启发函数
     * 
     * @param {Coord[]} line 直线坐标
     * @param {Number} dir 方向
     */
    calcuLineHeuristic(line, dir) {
        for (let coord of line) {
            let x = coord.x, y = coord.y;
            if (!ChessBoard.outOfBound(x, y) && this.boardMatrix[x][y] == EMPTY_CHESS) {
                this.calcuHeuristic(x, y, dir);
            }
        }
    }

    /**
     * 计算(x,y)处的启发函数值，并给启发函数矩阵的相应位置赋值
     * 
     * 为了提高速度，只修改direction方向的启发函数值
     * 
     * @param {Number} x 空位的行号
     * @param {Number} y 空位的列号
     * @param {Number} direction 指定的方向
     */
    calcuHeuristic(x, y, direction) {
        let coords = ChessBoard.lineCoords(x, y, direction);// 生成所需的直线坐标
        this.boardMatrix[x][y] = COM_CHESS;// 空位放上电脑棋子
        this.comHeuristic[x][y][direction] = this.score(coords, COM_CHESS);
        this.boardMatrix[x][y] = HUM_CHESS;// 空位放上人类棋子
        this.humHeuristic[x][y][direction] = this.score(coords, HUM_CHESS);
        this.boardMatrix[x][y] = EMPTY_CHESS;// 把空位恢复成空的
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
        // 把coords所表示的横线标准化后再打分
        let standardLine = this.standardizeLine(coords, ChessType);
        return scoreLine(standardLine);
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
        let value;
        if (type === COM_CHESS) {
            // 电脑方的棋子要下到(x,y)位置的空格上
            // 把该空格位置四个方向的启发函数值加起来
            value = this.comHeuristic[x][y][Direction.VERTICAL] + this.comHeuristic[x][y][Direction.HORIZONTAL]
                + this.comHeuristic[x][y][Direction.DIAGONAL] + this.comHeuristic[x][y][Direction.ANTIDIAGONAL];
            // 每一个己方近邻额外多加10%的分
            value *= 1 + this.comCloseNeighbor[x][y] * Config.CLOSE_RADIUS;
        } else {
            value = this.humHeuristic[x][y][Direction.VERTICAL] + this.humHeuristic[x][y][Direction.HORIZONTAL]
                + this.humHeuristic[x][y][Direction.DIAGONAL] + this.humHeuristic[x][y][Direction.ANTIDIAGONAL];
            value *= 1 + this.humCloseNeighbor[x][y] * Config.CLOSE_RADIUS;
        }
        return Math.round(value);
    }

    /**
     * 获取一组“优良的”空格位置
     * 
     * @param {Number} type 哪一方需要落子
     * 
     * @returns {Coord[]} 一组“优良的”空格位置
     */
    generator(type) {
        let self, enermy;
        if (type === COM_CHESS) {
            self = COM_CHESS;
            enermy = HUM_CHESS;
        } else {
            self = HUM_CHESS;
            enermy = COM_CHESS;
        }

        let fives = [];
        let selfAliveFours = [];
        let enermyAliveFours = [];
        let selfBlockedFours = [];
        let enermyBlockedFours = [];
        let selfDoubleThrees = [];
        let enermyDoubleThrees = [];
        let selfAliveThrees = [];
        let otherPositions = [];

        for (let x = 0; x < BOARD_SIZE; x++) {
            for (let y = 0; y < BOARD_SIZE; y++) {
                // 这个位置必须是空的，而且我们规定必须有邻居才有资格进入候选
                if (this.boardMatrix[x][y] === EMPTY_CHESS && ChessBoard.hasNeighbor(x, y)) {
                    let selfPosScore = this.heuristic(x, y, self);
                    let enermyPosScore = this.heuristic(x, y, enermy);
                    if (selfPosScore >= Score.FIVE || enermyPosScore >= Score.FIVE) {
                        fives.push(new Coord(x, y, Score.FIVE));
                    } else if (selfPosScore >= Score.ALIVE_FOUR) {
                        selfAliveFours.push(new Coord(x, y, selfPosScore));
                    } else if (enermyPosScore >= Score.ALIVE_FOUR) {
                        enermyAliveFours.push(new Coord(x, y, enermyPosScore));
                    } else if (selfPosScore >= Score.BLOCKED_FOUR) {
                        selfBlockedFours.push(new Coord(x, y, selfPosScore));
                    } else if (enermyPosScore >= Score.BLOCKED_FOUR) {
                        enermyBlockedFours.push(new Coord(x, y, enermyPosScore));
                    }
                    else if (selfPosScore >= Score.ALIVE_THREE * 2) {
                        selfDoubleThrees.push(new Coord(x, y, selfPosScore));
                    } else if (enermyPosScore >= Score.ALIVE_THREE * 2) {
                        enermyDoubleThrees.push(new Coord(x, y, enermyPosScore));
                    } else if (selfPosScore >= Score.ALIVE_THREE) {
                        selfAliveThrees.push(new Coord(x, y, selfPosScore));
                    } else {
                        otherPositions.push(new Coord(x, y, Math.max(selfPosScore, enermyPosScore)));
                    }
                }
            }
        }

        let result = [];
        /**
         * 升序
         * @param {Coord} a 
         * @param {Coord} b 
         * @returns {Number}
         */
        function ascSortFunc(a, b) {
            return a.h - b.h;
        }

        if (fives.length > 0) {
            return fives;
        }

        if (selfAliveFours.length > 0) {
            return selfAliveFours;
        }

        if (enermyAliveFours.length > 0) {
            selfBlockedFours.sort(ascSortFunc);
            enermyAliveFours.sort(ascSortFunc);
            if (selfBlockedFours.length > 0) {
                result = result.concat(selfBlockedFours)
                    .concat(enermyAliveFours);
            } else {
                result = result.concat(enermyAliveFours);
            }
            //return result; //TODO
        }

        selfDoubleThrees.sort(ascSortFunc);
        selfBlockedFours.sort(ascSortFunc);
        selfAliveThrees.sort(ascSortFunc);
        enermyDoubleThrees.sort(ascSortFunc);
        enermyBlockedFours.sort(ascSortFunc);
        otherPositions.sort(ascSortFunc);

        result = result.concat(selfDoubleThrees)
            .concat(selfBlockedFours)
            .concat(selfAliveThrees)
            .concat(enermyDoubleThrees)
            .concat(enermyBlockedFours)
            .concat(otherPositions);
        return result;
    }

    /**
     * 算杀
     * 
     * @param {Number} type 哪一方需要落子
     * 
     * @returns {Coord[]} 一组空格位置
     */
    generatorKill(type) {
        let self, enermy;
        if (type === COM_CHESS) {
            self = COM_CHESS;
            enermy = HUM_CHESS;
        } else {
            self = HUM_CHESS;
            enermy = COM_CHESS;
        }

        let fives = [];
        let selfAliveFours = [];
        let enermyAliveFours = [];
        let selfBlockedFours = [];
        let selfDoubleThrees = [];
        let enermyDoubleThrees = [];
        let selfAliveThrees = [];

        for (let x = 0; x < BOARD_SIZE; x++) {
            for (let y = 0; y < BOARD_SIZE; y++) {
                // 这个位置必须是空的，而且我们规定必须有邻居才有资格进入候选
                if (this.boardMatrix[x][y] === EMPTY_CHESS && this.hasNeighbor(x, y)) {
                    let selfPosScore = this.heuristic(x, y, self);
                    let enermyPosScore = this.heuristic(x, y, enermy);
                    if (selfPosScore >= Score.FIVE || enermyPosScore >= Score.FIVE) {
                        fives.push(new Coord(x, y, Score.FIVE));
                    } else if (selfPosScore >= Score.ALIVE_FOUR) {
                        selfAliveFours.push(new Coord(x, y, selfPosScore));
                    } else if (enermyPosScore >= Score.ALIVE_FOUR) {
                        enermyAliveFours.push(new Coord(x, y, enermyPosScore));
                    } else if (selfPosScore >= Score.BLOCKED_FOUR) {
                        selfBlockedFours.push(new Coord(x, y, selfPosScore));
                    } else if (selfPosScore >= Score.ALIVE_THREE * 2) {
                        selfDoubleThrees.push(new Coord(x, y, selfPosScore));
                    } else if (enermyPosScore >= Score.ALIVE_THREE * 2) {
                        enermyDoubleThrees.push(new Coord(x, y, enermyPosScore));
                    } else if (selfPosScore >= Score.ALIVE_THREE) {
                        selfAliveThrees.push(new Coord(x, y, selfPosScore));
                    }
                }
            }
        }

        let result = [];
        /**
         * 升序
         * @param {Coord} a 
         * @param {Coord} b 
         * @returns {Number}
         */
        function ascSortFunc(a, b) {
            return a.h - b.h;
        }

        if (fives.length > 0) {
            return fives;
        }

        if (selfAliveFours.length > 0) {
            return selfAliveFours;
        }

        if (enermyAliveFours.length > 0) {
            selfBlockedFours.sort(ascSortFunc);
            enermyAliveFours.sort(ascSortFunc);
            if (selfBlockedFours.length > 0) {
                result = result.concat(selfBlockedFours)
                    .concat(enermyAliveFours);
            } else {
                result = result.concat(enermyAliveFours);
            }
            //return result; //TODO
        }

        selfDoubleThrees.sort(ascSortFunc);
        selfBlockedFours.sort(ascSortFunc);
        selfAliveThrees.sort(ascSortFunc);
        enermyDoubleThrees.sort(ascSortFunc);

        result = result.concat(selfDoubleThrees)
            .concat(selfBlockedFours)
            .concat(selfAliveThrees)
            .concat(enermyDoubleThrees);
        return result;
    }

    /**
     * 评估函数，对整个棋盘的局势进行评估
     * 
     * 越大电脑越有利，越小人类越有利
     * 
     * 胜负需要在这之前单独用`isWin()`判断
     * 
     * @param {Number} type 哪一方已经落子
     * 
     * @returns {Number} 评估值
     */
    evaluate(type) {
        let comScore = this.evaluateOneSide(COM_CHESS);
        let humScore = this.evaluateOneSide(HUM_CHESS);
        if (type === COM_CHESS) {
            if (comScore >= Score.FIVE) {
                return Score.INF;
            } else if (humScore >= Score.FIVE) {
                return -Score.INF;
            } else {
                return comScore - humScore;
            }
        } else {
            if (humScore >= Score.FIVE) {
                return -Score.INF;
            } else if (comScore >= Score.FIVE) {
                return Score.INF;
            } else {
                return comScore - humScore;
            }
        }
    }

    /**
     * 单方面评估函数，只针对某一方进行评估
     * 
     * @param {Number} type 对哪一方进行评估
     * 
     * @return {Number} 单方面评估值
     */
    evaluateOneSide(type) {
        let result = 0;
        for (let x = 0; x < BOARD_SIZE; x++) {
            for (let y = 0; y < BOARD_SIZE; y++) {
                if (this.boardMatrix[x][y] === EMPTY_CHESS) {
                    result += this.heuristic(x, y, type);
                }
            }
        }
        return result;
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
        let stdLine = [StdType.BLOCKED];
        for (let coord of line) {
            let x = coord.x, y = coord.y;
            if (ChessBoard.outOfBound(x, y)) {
                stdLine.push(StdType.BLOCKED);
            } else if (this.boardMatrix[x][y] === EMPTY_CHESS) {
                stdLine.push(StdType.EMPTY);
            } else if (this.boardMatrix[x][y] === type) {
                stdLine.push(StdType.SELF);
            } else {
                stdLine.push(StdType.BLOCKED);
            }
        }
        stdLine = stdLine.push(StdType.BLOCKED);
        return stdLine;
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