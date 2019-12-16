import { COM_CHESS, HUM_CHESS } from "./ChessType.js";
import Config from "./Config.js";
//import TableCell from "./TableCell.js";
import TableCell, { INVALID_F, EXACT_F, LOWER_F, UPPER_F } from "./TableCell.js";
import { getCell, setCell } from "./TransTable.js";
import Status from "./Status.js";
//import ChessBoard from "./ChessBoard.js";
import Score from "./Score.js";
import Coord from "./Coord.js";

const MAX_FLOOR = 0;
const MIN_FLOOR = 1;

/**
 * 极大极小搜索的递归函数，承诺在返回前撤销一切操作
 * 
 * @param {ChessBoard} board 棋盘
 * @param {Number} fatherF 父结点当前的f值
 * @param {Number} floorType 当前结点层的类型
 * @param {Number} depth 当前结点的深度
 * @param {Number} hasX 已放置的棋子的行号
 * @param {Number} hasY 已放置的棋子的列号
 * @param {Number} hasType 已放置的棋子的类型
 * 
 * @returns {Number} 该结点的倒推f值
 */
function dfs(board, fatherF, floorType, depth, hasX, hasY, hasType) {
    Status.nodeNum++;
    Status.goMaxDepth = Math.max(depth, Status.goMaxDepth);

    // 判断是否一方已赢
    if (board.isWin(hasX, hasY)) {
        Status.winNum++;//TODO
        return (hasType === COM_CHESS) ? Score.INF : -Score.INF;
    }

    /**置换表表项*/
    let cell = getCell(board.getCode());
    if (cell === null) {
        cell = new TableCell();
        setCell(board.getCode(), cell);
    }
    /**当前结点子树深度 */
    let treeDepth = Config.MAX_DEPTH - depth;

    // 叶子结点评估
    if (board.isFull() || depth === Config.MAX_DEPTH) {
        Status.leafNum++;
        /**评估值 */
        let evaValue;
        if (Config.useTransTable
            && cell.validCell(board.getCode(), board.getNumber())
            && cell.evaValid) {
            Status.leafMatch++;
            evaValue = cell.evaluate;
        } else {
            //哪一方已经落子
            let type = (floorType === MAX_FLOOR) ? HUM_CHESS : COM_CHESS;
            evaValue = board.evaluate(type);

            cell.isValid = true;
            cell.hashCode = board.getCode();
            cell.chessNum = board.getNumber();
            cell.evaValid = true;
            cell.evaluate = evaValue;
        }
        return evaValue;
    }


    if (floorType === MAX_FLOOR) {
        let f = -Score.INF;
        if (Config.useTransTable
            && cell.validCell(board.getCode(), board.getNumber())
            && cell.fType != INVALID_F
            && cell.treeDepth >= treeDepth) {
            if (cell.fType === EXACT_F) {
                Status.completeMatch++;//完全命中
                return cell.fValue;//直接把f值返回
            } else if (cell.fType === LOWER_F) {
                Status.partialMatch++;//部分命中
                f = cell.fValue;//缩小f的范围
            }
        }

        let emptyPosList = (depth >= Config.START_KILLER)
            ? board.generatorKill(COM_CHESS)
            : board.generator(COM_CHESS);
        let hasPurning = false;//是否发生了剪枝
        for (let i = 0, cnt = 0;
            i < emptyPosList.length && cnt < Config.MAX_EMPTY_NUM;
            i++ , cnt++) {
            let x = emptyPosList[i].x, y = emptyPosList[i].y;
            let backup = board.backupHeuristic(x, y);//备份

            board.put(x, y, COM_CHESS);//落子
            let childF = dfs(board, f, MIN_FLOOR, depth + 1, x, y, COM_CHESS);
            board.undo(x, y, backup);//撤销

            // 更新f值，即alpha往大走
            if (childF > f) {
                f = childF;
            }
            // beta剪枝
            if (f > fatherF) {
                Status.ABPruning++;
                hasPurning = true;
                break;
            }
            // 超时强制剪枝
            if (new Date().getTime() - Status.startTime > Config.MAX_TIME) {
                Status.isOutTime = true;
                hasPurning = true;
                break;
            }
        }

        if (Config.useTransTable) {
            // 更新置换表
            if (!cell.validCell(board.getCode(), board.getNumber())
                || board.fType === INVALID_F) {
                cell.isValid = true;
                cell.hashCode = board.getCode();
                cell.chessNum = board.getNumber();
                cell.treeDepth = treeDepth;
                cell.f = f;
                cell.fType = hasPurning ? LOWER_F : EXACT_F;
            } else if (treeDepth >= cell.treeDepth) {
                if (hasPurning) {
                    if (treeDepth > cell.treeDepth
                        || (treeDepth === cell.treeDepth && f > cell.f)) {
                        cell.f = f;
                        cell.fType = LOWER_F;
                        cell.treeDepth = treeDepth;
                    }
                } else {
                    cell.f = f;
                    cell.fType = EXACT_F;
                    cell.treeDepth = treeDepth;
                }
            }
        }

        return f;
    } else {
        let f = Score.INF;
        if (Config.useTransTable
            && cell.validCell(board.getCode(), board.getNumber())
            && cell.fType != INVALID_F
            && cell.treeDepth >= treeDepth) {
            if (cell.fType === EXACT_F) {
                Status.completeMatch++;//完全命中
                return cell.fValue;//直接把f值返回
            } else if (cell.fType === UPPER_F) {
                Status.partialMatch++;//部分命中
                f = cell.fValue;//缩小f的范围
            }
        }

        let emptyPosList = (depth >= Config.START_KILLER)
            ? board.generatorKill(HUM_CHESS)
            : board.generator(HUM_CHESS);
        let hasPurning = false;//是否发生了剪枝
        for (let i = 0, cnt = 0;
            i < emptyPosList.length && cnt < Config.MAX_EMPTY_NUM;
            i++ , cnt++) {
            let x = emptyPosList[i].x, y = emptyPosList[i].y;
            let backup = board.backupHeuristic(x, y);//备份

            board.put(x, y, HUM_CHESS);//落子
            let childF = dfs(board, f, MAX_FLOOR, depth + 1, x, y, HUM_CHESS);
            board.undo(x, y, backup);//撤销

            // 更新f值，即beta往小走
            if (childF < f) {
                f = childF;
            }
            // alpha剪枝
            if (f < fatherF) {
                Status.ABPruning++;
                hasPurning = true;
                break;
            }
            // 超时强制剪枝
            if (new Date().getTime() - Status.startTime > Config.MAX_TIME) {
                Status.isOutTime = true;
                hasPurning = true;
                break;
            }
        }

        if (Config.useTransTable) {
            // 更新置换表
            if (!cell.validCell(board.getCode(), board.getNumber())
                || board.fType === INVALID_F) {
                cell.isValid = true;
                cell.hashCode = board.getCode();
                cell.chessNum = board.getNumber();
                cell.treeDepth = treeDepth;
                cell.f = f;
                cell.fType = hasPurning ? UPPER_F : EXACT_F;
            } else if (treeDepth >= cell.treeDepth) {
                if (hasPurning) {
                    if (treeDepth > cell.treeDepth
                        || (treeDepth === cell.treeDepth && f < cell.f)) {
                        cell.f = f;
                        cell.fType = UPPER_F;
                        cell.treeDepth = treeDepth;
                    }
                } else {
                    cell.f = f;
                    cell.fType = EXACT_F;
                    cell.treeDepth = treeDepth;
                }
            }
        }

        return f;
    }
}

/**
 * 获取电脑一方的最佳落子位置
 * @param {ChessBoard} board 当前棋盘
 * @returns {Coord} 电脑应该落子的坐标
 */
function getBestPut(board) {
    initStatus();

    if (!Config.useHeuristic) {
        Config.MAX_DEPTH = 3;
        Config.MAX_EMPTY_NUM = 15 * 15;
    } else {
        if (board.getNumber() < 4) {
            //1~2回合
            Config.MAX_DEPTH = 3;
            Config.MAX_EMPTY_NUM = 20;
        } else if (board.getNumber() < 6) {
            //3回合
            Config.MAX_DEPTH = 4;
            Config.MAX_EMPTY_NUM = 18;
        } else if (board.getNumber() < 8) {
            //4回合
            Config.MAX_DEPTH = 5;
            Config.MAX_EMPTY_NUM = 16;
        } else if (board.getNumber() < 10) {
            //5回合
            Config.MAX_DEPTH = 6;
            Config.MAX_EMPTY_NUM = 15;
        } else if (board.getNumber() < 12) {
            //6回合
            Config.MAX_DEPTH = 7;
            Config.MAX_EMPTY_NUM = 15;
        } else {
            Config.MAX_DEPTH = 8;
            Config.MAX_EMPTY_NUM = 14;
        }
    }

    let f = -Score.INF - 1;
    let bestPut = new Coord(7, 7);//若generator生成的候选数组为空则默认放中间
    let emptyPosList = board.generator(COM_CHESS);
    for (let i = 0, cnt = 0;
        i < emptyPosList.length && cnt < Config.MAX_EMPTY_NUM;
        i++ , cnt++) {
        let x = emptyPosList[i].x, y = emptyPosList[i].y;
        let backup = board.backupHeuristic(x, y);//备份

        board.put(x, y, COM_CHESS);//落子
        let childF = dfs(board, f, MIN_FLOOR, 1, x, y, COM_CHESS);
        board.undo(x, y, backup);//撤销

        if (childF > f) {
            f = childF;
            bestPut.x = x;
            bestPut.y = y;
        }
        if (f >= Score.INF) {
            break;
        }
    }
    Status.f = f;
    console.log(getStatusStr(f));
    return bestPut;
}

/**
 * 每轮搜索开始前初始化Status对象的状态
 */
function initStatus() {
    Status.startTime = new Date().getTime();
    Status.isOutTime = false;
    Status.goMaxDepth = 0;
    Status.nodeNum = 1;
    Status.leafNum = 0;
    Status.winNum = 0;
    Status.completeMatch = 0;
    Status.partialMatch = 0;
    Status.leafMatch = 0;
    Status.ABPruning = 0;
    Status.f = 0;
}

/**
 * 获取表示本轮搜索状态的字符串
 * @param {Number} f 本轮搜索的回传f值
 * @returns {String} 表示本轮搜索状态的字符串
 */
function getStatusStr() {
    let str = "";
    str += "最大搜索深度：" + Status.goMaxDepth + "\n";
    str += "考察结点个数：" + Status.nodeNum + " "
        + "叶子结点个数：" + Status.leafNum + " "
        + "输赢局面个数：" + Status.winNum + "\n";
    str += "置换表命中总次数：" + (Status.completeMatch + Status.partialMatch + Status.leafMatch) + " "
        + "完全命中：" + Status.completeMatch + " "
        + "部分命中：" + Status.partialMatch + " "
        + "叶子结点命中：" + Status.leafMatch + "\n";
    str += "用时：" + Math.round((new Date().getTime() - Status.startTime) / 1000.0) + "秒" + " "
        + "超时：" + Status.isOutTime + "\n";
    str += "ab剪枝次数：" + Status.ABPruning + "\n";
    str += "回传f值：" + Status.f + "\n";
    return str;
}

export default getBestPut;