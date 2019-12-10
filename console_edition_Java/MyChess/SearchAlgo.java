package MyChess;

import static MyChess.ChessType.*;
import MyChess.Config;
import MyChess.TransTable;
import MyChess.TableCell;

import java.util.List;

/**
 * SearchAlgo
 */
public class SearchAlgo {

    final private static int MAX_FLOOR = 0;
    final private static int MIN_FLOOR = 1;

    /**
     * 极大极小搜索的递归函数，承诺在返回前撤销一切操作
     * 
     * @param board     棋盘
     * @param fatherF   父结点当前的f值
     * @param floorType 当前结点层的类型
     * @param putX      要落子的x坐标
     * @param putY      要落子的y坐标
     * @param putType   要落子的类型
     * @param depth     当前结点的深度
     * 
     * @return 该结点的倒推f值
     */
    public static int dfs(ChessBoard board, int fatherF, int floorType, int putX, int putY, int putType, int depth) {
        Status.nodeNum++;
        Status.goMaxDepth = Math.max(depth, Status.goMaxDepth);

        // 备份
        // boolean[][] oldNeighbor = board.backupNeighbor(putX, putY);
        int[][][][] oldHeuristic = board.backupHeuristic(putX, putY);

        board.put(putX, putY, putType);// 落子

        // 判断是否一方已赢
        if (board.isWin(putX, putY)) {
            if (putType == COM_CHESS) {
                board.undo(putX, putY, oldHeuristic);
                return Score.INF;
            } else {
                board.undo(putX, putY, oldHeuristic);
                return -Score.INF;
            }
        }

        // 获取缓存
        long boardHashCode = board.getCode();
        TableCell cell = TransTable.get(boardHashCode);

        // 叶子结点评估
        if (board.isFull() || depth == Config.MAX_DEPTH) {
            Status.leafNum++;
            int eval;
            if (cell.isValid && cell.chessNum == board.getNumber() && cell.evaValid) {
                Status.leafMatch++;
                eval = cell.evaluate;
            } else {
                int evaluateType = (floorType == MAX_FLOOR) ? COM_CHESS : HUM_CHESS;
                eval = board.evaluate(evaluateType);

                cell.isValid = true;
                cell.chessNum = board.getNumber();
                cell.evaValid = true;
                cell.evaluate = eval;
            }
            board.undo(putX, putY, oldHeuristic);
            return eval;
        }

        if (depth == Config.START_KILLER) {
            Status.killerMode = true;// 开启算杀模式
        }
        if (floorType == MAX_FLOOR) {
            List<Coord> emptyPosList;
            if (Status.killerMode) {
                emptyPosList = board.generatorKill(COM_CHESS);
            } else {
                emptyPosList = board.generator(COM_CHESS);
            }

            int f = -Score.INF;

            if (cell.isValid && cell.chessNum == board.getNumber() && cell.fType != TableCell.INVALID_F) {
                if (cell.treeDepth >= Config.MAX_DEPTH - depth) {
                    if (cell.fType == TableCell.EXACT_F) {
                        Status.completeMatch++;// 完全匹配
                        f = cell.fValue;
                        board.undo(putX, putY, oldHeuristic);
                        if (depth == Config.START_KILLER) {
                            Status.killerMode = false;// 关闭算杀模式
                        }
                        return f;
                    } else if (cell.fType == TableCell.MIN_F) {
                        Status.partialMatch++;// 部分匹配
                        f = cell.fValue;
                    }
                } else {
                    Status.partialMatch++;// 部分匹配
                    f = cell.fValue;
                }
            }
            for (int i = 0, cnt = 0; i < emptyPosList.size() && cnt <= Config.MAX_EMPTY_NUM; i++, cnt++) {
                Coord coord = emptyPosList.get(i);
                int x = coord.x;// 空位的x坐标
                int y = coord.y;// 空位的y坐标
                int childF = dfs(board, f, MIN_FLOOR, x, y, COM_CHESS, depth + 1);
                if (childF > f) {// 更新f值，即alpha往大走
                    f = childF;
                }
                if (f > fatherF) {// beta剪枝
                    Status.ABPruning++;

                    if (!cell.isValid || cell.chessNum != board.getNumber()
                            || cell.treeDepth < Config.MAX_DEPTH - depth) {
                        cell.isValid = true;
                        cell.chessNum = board.getNumber();
                        cell.fType = TableCell.MIN_F;
                        cell.treeDepth = Config.MAX_DEPTH - depth;
                        cell.fValue = f;
                    } else if (cell.treeDepth == Config.MAX_DEPTH - depth) {
                        if (cell.fType == TableCell.MIN_F && f < cell.fValue) {
                            cell.fValue = f;
                        }
                    }

                    board.undo(putX, putY, oldHeuristic);

                    if (depth == Config.START_KILLER) {
                        Status.killerMode = false;// 关闭算杀模式
                    }

                    return f;
                }
                if (System.currentTimeMillis() - Status.startTime > Config.MAX_TIME) {
                    Status.isOutTime = true;
                    break;
                }
            }

            // 更新置换表
            if (!cell.isValid || cell.chessNum != board.getNumber() || cell.treeDepth <= Config.MAX_DEPTH - depth) {
                cell.isValid = true;
                cell.chessNum = board.getNumber();
                cell.treeDepth = Config.MAX_DEPTH - depth;
                if (Status.isOutTime) {
                    if (cell.isValid && cell.chessNum == board.getNumber()) {
                        if (cell.treeDepth == Config.MAX_DEPTH - depth && cell.fType == TableCell.MIN_F
                                && f < cell.fValue) {
                            cell.fType = TableCell.MIN_F;
                            cell.fValue = f;
                        }
                    } else {
                        cell.fType = TableCell.MIN_F;
                        cell.fValue = f;
                    }
                } else {
                    cell.fType = TableCell.EXACT_F;
                    cell.fValue = f;
                }
            }

            board.undo(putX, putY, oldHeuristic);

            if (depth == Config.START_KILLER) {
                Status.killerMode = false;// 关闭算杀模式
            }

            return f;
        } else {
            List<Coord> emptyPosList;
            if (Status.killerMode) {
                emptyPosList = board.generatorKill(HUM_CHESS);
            } else {
                emptyPosList = board.generator(HUM_CHESS);
            }

            int f = Score.INF;

            if (cell.isValid && cell.chessNum == board.getNumber() && cell.fType != TableCell.INVALID_F) {
                if (cell.treeDepth >= Config.MAX_DEPTH - depth) {
                    if (cell.fType == TableCell.EXACT_F) {
                        Status.completeMatch++;
                        f = cell.fValue;
                        board.undo(putX, putY, oldHeuristic);
                        if (depth == Config.START_KILLER) {
                            Status.killerMode = false;// 关闭算杀模式
                        }
                        return f;
                    } else if (cell.fType == TableCell.MAX_F) {
                        Status.partialMatch++;
                        f = cell.fValue;
                    }
                } else {
                    Status.partialMatch++;
                    f = cell.fValue;
                }
            }

            for (int i = 0, cnt = 0; i < emptyPosList.size() && cnt <= Config.MAX_EMPTY_NUM; i++, cnt++) {
                Coord coord = emptyPosList.get(i);
                int x = coord.x;// 空位的x坐标
                int y = coord.y;// 空位的y坐标
                int childF = dfs(board, f, MAX_FLOOR, x, y, HUM_CHESS, depth + 1);
                if (childF < f) {
                    f = childF;
                }
                if (f < fatherF) {
                    Status.ABPruning++;

                    if (!cell.isValid || cell.chessNum != board.getNumber()
                            || cell.treeDepth < Config.MAX_DEPTH - depth) {
                        cell.isValid = true;
                        cell.chessNum = board.getNumber();
                        cell.fType = TableCell.MAX_F;
                        cell.treeDepth = Config.MAX_DEPTH - depth;
                        cell.fValue = f;
                    } else if (cell.treeDepth == Config.MAX_DEPTH - depth) {
                        if (cell.fType == TableCell.MAX_F && f > cell.fValue) {
                            cell.fValue = f;
                        }
                    }

                    board.undo(putX, putY, oldHeuristic);

                    if (depth == Config.START_KILLER) {
                        Status.killerMode = false;// 关闭算杀模式
                    }

                    return f;
                }
                if (System.currentTimeMillis() - Status.startTime > Config.MAX_TIME) {
                    Status.isOutTime = true;
                    break;
                }
            }

            if (!cell.isValid || cell.chessNum != board.getNumber() || cell.treeDepth <= Config.MAX_DEPTH - depth) {
                cell.isValid = true;
                cell.chessNum = board.getNumber();
                cell.treeDepth = Config.MAX_DEPTH - depth;
                if (Status.isOutTime) {
                    if (cell.isValid && cell.chessNum == board.getNumber()) {
                        if (cell.treeDepth == Config.MAX_DEPTH - depth && cell.fType == TableCell.MAX_F
                                && f > cell.fValue) {
                            cell.fType = TableCell.MAX_F;
                            cell.fValue = f;
                        }
                    } else {
                        cell.fType = TableCell.MAX_F;
                        cell.fValue = f;
                    }
                } else {
                    cell.fType = TableCell.EXACT_F;
                    cell.fValue = f;
                }
            }

            board.undo(putX, putY, oldHeuristic);

            if (depth == Config.START_KILLER) {
                Status.killerMode = false;// 关闭算杀模式
            }

            return f;
        }
    }

    /**
     * 获取电脑一方的最佳落子位置
     * 
     * @param board 当前棋盘
     * @return 电脑应该落子的坐标
     */
    public static Coord getBestPut(ChessBoard board) {
        initStatus();

        if (board.getNumber() <= 6) {
            //1~3回合
            Config.MAX_DEPTH = 4;
            Config.MAX_EMPTY_NUM = 20;
        } else if (board.getNumber() <= 10) {
            //4~5回合
            Config.MAX_DEPTH = 7;
            Config.MAX_EMPTY_NUM = 11;
        } else if (board.getNumber() <= 14) {
            //6~7回合
            Config.MAX_DEPTH = 8;
        } else {
            Config.MAX_DEPTH = 9;
            Config.MAX_EMPTY_NUM = 12;
        }

        int f = -Score.INF - 1;
        Coord bestPut = new Coord(7, 7);// 若generator生成的候选数组为空则默认放中间
        List<Coord> emptyPosList = board.generator(COM_CHESS);
        int cnt = 0;
        for (int i = 0; i < emptyPosList.size() && cnt <= Config.MAX_EMPTY_NUM; i++, cnt++) {
            Coord coord = emptyPosList.get(i);
            int x = coord.x;// 空位的x坐标
            int y = coord.y;// 空位的y坐标
            int childF = dfs(board, f, MIN_FLOOR, x, y, COM_CHESS, 1);
            if (childF > f) {
                f = childF;
                bestPut.x = x;
                bestPut.y = y;
            }
        }

        printStatus(f);
        return bestPut;
    }

    private static void initStatus() {
        Status.startTime = System.currentTimeMillis();// 开始计时
        Status.isOutTime = false;
        Status.goMaxDepth = 0;
        Status.nodeNum = 1;
        Status.leafNum = 0;
        Status.completeMatch = 0;
        Status.partialMatch = 0;
        Status.leafMatch = 0;
        Status.ABPruning = 0;
    }

    private static void printStatus(int f) {
        String depthStr = "最大搜索深度：" + Status.goMaxDepth;
        String nodeNumStr = "考察结点个数：" + Status.nodeNum + " " + "叶子结点个数：" + Status.leafNum;
        String tableStr = "置换表命中总次数：" + (Status.completeMatch + Status.partialMatch + Status.leafMatch) + " " + "完全命中："
                + Status.completeMatch + " " + "部分命中：" + Status.partialMatch + " " + "叶子结点命中：" + Status.leafMatch;
        String timeUseStr = "用时：" + (System.currentTimeMillis() - Status.startTime) / 1000.0 + "秒" + " " + "超时："
                + Status.isOutTime;
        String purnStr = "ab剪枝次数：" + Status.ABPruning;
        String fStr = "倒推f值：" + f;
        System.out.println(
                depthStr + "\n" + nodeNumStr + "\n" + tableStr + "\n" + timeUseStr + "\n" + purnStr + "\n" + fStr);
    }

}