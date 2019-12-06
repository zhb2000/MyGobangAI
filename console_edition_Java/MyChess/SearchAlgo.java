package MyChess;

import static MyChess.ChessType.*;
import MyChess.Config;

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
        boolean[][] oldNeighbor = board.backupNeighbor(putX, putY);
        int[][][][] oldHeuristic = board.backupHeuristic(putX, putY);
        board.put(putX, putY, putType);// 落子

        // 判断是否一方已赢
        if (board.isWin(putX, putY)) {
            if (putType == COM_CHESS) {
                board.undo(putX, putY, oldNeighbor, oldHeuristic);
                return Score.INF;
            } else {
                board.undo(putX, putY, oldNeighbor, oldHeuristic);
                return -Score.INF;
            }
        }
        if (board.isFull()) {
            board.undo(putX, putY, oldNeighbor, oldHeuristic);
            return board.evaluate();
        }
        if (depth == Config.MAX_DEPTH) {
            board.undo(putX, putY, oldNeighbor, oldHeuristic);
            return board.evaluate();
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
            for (int i = 0, cnt = 0; i < emptyPosList.size() && cnt <= Config.MAX_EMPTY_NUM; i++, cnt++) {
                Coord coord = emptyPosList.get(i);
                int x = coord.x;// 空位的x坐标
                int y = coord.y;// 空位的y坐标
                int childF = dfs(board, f, MIN_FLOOR, x, y, COM_CHESS, depth + 1);
                if (childF > f) {
                    f = childF;
                }
                if (f > fatherF) {
                    board.undo(putX, putY, oldNeighbor, oldHeuristic);
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
            board.undo(putX, putY, oldNeighbor, oldHeuristic);
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
            for (int i = 0, cnt = 0; i < emptyPosList.size() && cnt <= Config.MAX_EMPTY_NUM; i++, cnt++) {
                Coord coord = emptyPosList.get(i);
                int x = coord.x;// 空位的x坐标
                int y = coord.y;// 空位的y坐标
                int childF = dfs(board, f, MAX_FLOOR, x, y, HUM_CHESS, depth + 1);
                if (childF < f) {
                    f = childF;
                }
                if (f < fatherF) {
                    board.undo(putX, putY, oldNeighbor, oldHeuristic);
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
            board.undo(putX, putY, oldNeighbor, oldHeuristic);
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
        Status.startTime = System.currentTimeMillis();// 开始计时
        Status.isOutTime = false;
        Status.goMaxDepth = 0;
        Status.nodeNum = 1;

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
        System.out.println("最大搜索深度：" + Status.goMaxDepth);
        System.out.println("考察结点个数：" + Status.nodeNum);
        System.out.println("用时：" + (System.currentTimeMillis() - Status.startTime) / 1000.0 + "秒");
        System.out.println("超时：" + Status.isOutTime);
        System.out.println("分数：" + f);
        return bestPut;
    }
}