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

    public static int dfs(ChessBoard board, int fatherF, int floorType, int putX, int putY, int putType, int depth,
            int maxd) {
        boolean[][] oldNeighbor = board.backupNeighbor(putX, putY);
        int[][][][] oldHeuristic = board.backupHeuristic(putX, putY);
        board.put(putX, putY, putType);

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
        if (depth == maxd) {
            board.undo(putX, putY, oldNeighbor, oldHeuristic);
            return board.evaluate();
        }

        if (floorType == MAX_FLOOR) {
            int f = -Score.INF;
            List<Coord> emptyPosList = board.generator(COM_CHESS);
            int cnt = 0;
            for (int i = 0; i < emptyPosList.size() && cnt <= Config.MAX_EMPTY_NUM; i++, cnt++) {
                Coord coord = emptyPosList.get(i);
                int x = coord.x;// 空位的x坐标
                int y = coord.y;// 空位的y坐标
                int childF = dfs(board, f, MIN_FLOOR, x, y, COM_CHESS, depth + 1, maxd);
                if (childF > f) {
                    f = childF;
                }
                if (f > fatherF) {
                    board.undo(putX, putY, oldNeighbor, oldHeuristic);
                    return f;
                }
            }
            board.undo(putX, putY, oldNeighbor, oldHeuristic);
            return f;
        } else {
            int f = Score.INF;
            List<Coord> emptyPosList = board.generator(COM_CHESS);
            int cnt = 0;
            for (int i = 0; i < emptyPosList.size() && cnt <= Config.MAX_EMPTY_NUM; i++, cnt++) {
                Coord coord = emptyPosList.get(i);
                int x = coord.x;// 空位的x坐标
                int y = coord.y;// 空位的y坐标
                int childF = dfs(board, f, MAX_FLOOR, x, y, HUM_CHESS, depth + 1, maxd);
                if (childF < f) {
                    f = childF;
                }
                if (f < fatherF) {
                    board.undo(putX, putY, oldNeighbor, oldHeuristic);
                    return f;
                }
            }
            board.undo(putX, putY, oldNeighbor, oldHeuristic);
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
        int f = -Score.INF - 1;
        Coord bestPut = new Coord(7, 7);
        List<Coord> emptyPosList = board.generator(COM_CHESS);
        int cnt = 0;
        int maxd;
        if (board.getNumber() <= 6) {
            maxd = 4;
        } else {
            maxd = Config.MAX_DEPTH;
        }
        for (int i = 0; i < emptyPosList.size() && cnt <= Config.MAX_EMPTY_NUM; i++, cnt++) {
            Coord coord = emptyPosList.get(i);
            int x = coord.x;// 空位的x坐标
            int y = coord.y;// 空位的y坐标
            int childF = dfs(board, f, MIN_FLOOR, x, y, COM_CHESS, 1, maxd);
            if (childF > f) {
                f = childF;
                bestPut.x = x;
                bestPut.y = y;
            }
        }
        // for (Coord coord : emptyPosList) {
        // int x = coord.x;// 空位的x坐标
        // int y = coord.y;// 空位的y坐标
        // for (int maxd = 2; maxd <= Config.MAX_DEPTH; maxd += 2) {
        // int childF = dfs(board, f, MIN_FLOOR, x, y, COM_CHESS, 1, maxd);
        // if (childF > f) {
        // f = childF;
        // bestPut.x = x;
        // bestPut.y = y;
        // }
        // }
        // }
        return bestPut;
    }
}