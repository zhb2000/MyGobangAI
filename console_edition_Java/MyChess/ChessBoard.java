package MyChess;

import static MyChess.ChessType.*;
import static MyChess.Config.BOARD_SIZE;

import java.util.ArrayList;
import java.util.List;

public class ChessBoard {
    /** 棋盘矩阵 */
    private int[][] boardMatrix;
    /** 人类的空位启发函数值 */
    private int[][][] humHeuristic;
    /** 电脑的空位启发式函数值 */
    private int[][][] comHeuristic;
    /** 空位附近是否有棋子（不管什么类型） */
    private boolean[][] neighborMatrix;
    /** 棋子总数 */
    private int chessNum;

    /** 构造函数 */
    public ChessBoard() {
        chessNum = 0;
        boardMatrix = new int[BOARD_SIZE][BOARD_SIZE];
        for (int i = 0; i < BOARD_SIZE; i++) {
            for (int j = 0; j < BOARD_SIZE; j++) {
                boardMatrix[i][j] = EMPTY_CHESS;
            }
        }
        humHeuristic = new int[BOARD_SIZE][BOARD_SIZE][4];
        comHeuristic = new int[BOARD_SIZE][BOARD_SIZE][4];
        neighborMatrix = new boolean[BOARD_SIZE][BOARD_SIZE];
    }

    /**
     * 落子并更新相应的数据
     * 
     * @param x    落子的行号
     * @param y    落子的列号
     * @param type 落子的类型
     * 
     * @return 操作是否合法
     */
    public boolean put(int x, int y, int type) {
        if (outOfBound(x, y)) {
            return false;
        }
        if (boardMatrix[x][y] != EMPTY_CHESS) {
            return false;
        }
        boardMatrix[x][y] = type;
        chessNum++;// 更新棋子总数
        updateHeuristic(x, y);// 更新启发函数矩阵
        updateNeighbor(x, y);// 更新邻居矩阵
        return true;
    }

    /**
     * 撤销棋子并更新相应的数据
     * 
     * @param x 落子的行号
     * @param y 落子的列号
     * 
     * @return 操作是否合法
     */
    public boolean undo(int x, int y) {
        if (outOfBound(x, y)) {
            return false;
        }
        if (boardMatrix[x][y] == EMPTY_CHESS) {
            return false;
        }
        boardMatrix[x][y] = EMPTY_CHESS;
        chessNum--;// 更新棋子总数
        updateHeuristic(x, y);// 更新启发函数矩阵
        updateNeighbor(x, y);// 更新邻居矩阵
        return true;
    }

    /**
     * @retrun 棋盘是否已满
     */
    public boolean isFull() {
        return chessNum == BOARD_SIZE * BOARD_SIZE;
    }

    /**
     * 落子或撤销后对neighborMatrix进行更新
     * 
     * @param x 中心位置的行号
     * @param y 中心位置的列号
     */
    private void updateNeighbor(int x, int y) {
        for (int i = x - 2; i <= x + 2; i++) {
            for (int j = y - 2; j <= y + 2; j++) {
                if (!outOfBound(i, j) && boardMatrix[i][j] == EMPTY_CHESS) {
                    calcuNeighbor(i, j);// 没有越界且是个空位
                }
            }
        }
    }

    /**
     * 计算(x,y)附近是否有棋子
     * 
     * @param x 中心位置的行号
     * @param y 中心位置的列号
     */
    private void calcuNeighbor(int x, int y) {
        boolean hasNeighbor = false;
        for (int i = x - 2; i <= x + 2; i++) {
            for (int j = y - 2; j <= y + 2; j++) {
                if (!outOfBound(i, j) && boardMatrix[i][j] != EMPTY_CHESS) {
                    hasNeighbor = true;// 没有越界且有棋子占据
                    break;
                }
            }
        }
        neighborMatrix[x][y] = hasNeighbor;
    }

    /**
     * 落子或撤销后对启发函数值矩阵进行更新
     * 
     * @param x 中心位置的行号
     * @param y 中心位置的列号
     */
    private void updateHeuristic(int x, int y) {
        // 更新附近的空格的启发函数值，米字范围内的空格都要更新
        // 垂直方向
        List<Coord> verticals = lineCoords(x, y, Direction.VERTICAL);
        for (Coord coord : verticals) {
            int ux = coord.x;
            int uy = coord.y;
            // 未越界且是空格
            if (!outOfBound(ux, uy) && boardMatrix[ux][uy] == EMPTY_CHESS) {
                calcuHeuristic(ux, uy, Direction.VERTICAL);
            }
        }
        // 水平方向
        List<Coord> horizontals = lineCoords(x, y, Direction.HORIZONTAL);
        for (Coord coord : horizontals) {
            int ux = coord.x;
            int uy = coord.y;
            if (!outOfBound(ux, uy) && boardMatrix[ux][uy] == EMPTY_CHESS) {
                calcuHeuristic(ux, uy, Direction.HORIZONTAL);
            }
        }
        // 对角线方向
        List<Coord> diagonals = lineCoords(x, y, Direction.DIAGONAL);
        for (Coord coord : diagonals) {
            int ux = coord.x;
            int uy = coord.y;
            if (!outOfBound(ux, uy) && boardMatrix[ux][uy] == EMPTY_CHESS) {
                calcuHeuristic(ux, uy, Direction.DIAGONAL);
            }
        }
        // 反对角线方向
        List<Coord> antidiagonals = lineCoords(x, y, Direction.ANTIDIAGONAL);
        for (Coord coord : antidiagonals) {
            int ux = coord.x;
            int uy = coord.y;
            if (!outOfBound(ux, uy) && boardMatrix[ux][uy] == EMPTY_CHESS) {
                calcuHeuristic(ux, uy, Direction.ANTIDIAGONAL);
            }
        }
        // 若自己这个位置是空格，则更新自己的启发函数值
        if (boardMatrix[x][y] == EMPTY_CHESS) {
            calcuHeuristic(x, y, Direction.VERTICAL);
            calcuHeuristic(x, y, Direction.HORIZONTAL);
            calcuHeuristic(x, y, Direction.DIAGONAL);
            calcuHeuristic(x, y, Direction.ANTIDIAGONAL);
        }
    }

    /**
     * 计算(x,y)处的启发函数值并赋值，只对direction方向进行更新
     * 
     * @param x         行号
     * @param y         列号
     * @param direction 更新的方向
     */
    private void calcuHeuristic(int x, int y, int direction) {
        int dir;
        if (direction == Direction.VERTICAL) {
            dir = 0;
        } else if (direction == Direction.HORIZONTAL) {
            dir = 1;
        } else if (direction == Direction.DIAGONAL) {
            dir = 2;
        } else {
            dir = 3;
        }
        List<Coord> coords = lineCoords(x, y, direction);
        comHeuristic[x][y][dir] = score(coords, COM_CHESS);
        humHeuristic[x][y][dir] = score(coords, HUM_CHESS);
        //TODO 重复计算估计函数值了吗？
    }

    /**
     * 根据指向中棋形的种类和数量，对直线coords打分
     * 
     * @param coords    直线，坐标允许越界
     * @param chessType 对哪一方打分
     * 
     * @return 打出的分数
     */
    private int score(List<Coord> coords, int chessType) {
        // 把coords所表示的横线标准化后再打分
        List<Integer> standardLine = new ArrayList<>();
        for (Coord coord : coords) {
            int x = coord.x;
            int y = coord.y;
            if (!outOfBound(x, y)) {
                standardLine.add(StandardType.BLOCKED);
            } else {
                if (boardMatrix[x][y] == EMPTY_CHESS) {
                    standardLine.add(StandardType.EMPTY);
                } else if (boardMatrix[x][y] == chessType) {
                    standardLine.add(StandardType.SELF);
                } else {
                    standardLine.add(StandardType.BLOCKED);
                }
            }
        }
        return scoreStandardLine(standardLine);
    }

    // 各种工具static方法

    /**
     * @return (x,y)是否越界
     */
    private static boolean outOfBound(int x, int y) {
        return (x < 0 || x >= BOARD_SIZE || y < 0 || y >= BOARD_SIZE);
    }

    /**
     * 返回以(x,y)为中心，长度为9的直线上的所有坐标
     */
    private static List<Coord> lineCoords(int x, int y, int direction) {
        List<Coord> coords = new ArrayList<>();
        if (direction == Direction.VERTICAL) {
            for (int j = y - 4; j <= y + 4; j++) {
                coords.add(new Coord(x, j));
            }
        } else if (direction == Direction.HORIZONTAL) {
            for (int i = x - 4; i <= x + 4; i++) {
                coords.add(new Coord(i, y));
            }
        } else if (direction == Direction.DIAGONAL) {
            int i = x - 4, j = y - 4;
            for (int k = 0; k <= 8; k++) {
                coords.add(new Coord(i + k, j + k));
            }
        } else {
            int i = x + 4, j = y - 4;
            for (int k = 0; k <= 8; k++) {
                coords.add(new Coord(i - k, j + k));
            }
        }
        return coords;
    }

    /**
     * 对一条标准化的横线按照棋形进行打分
     * 
     * @param line 标准化后的横线
     * @return 该横线的分数
     */
    private static int scoreStandardLine(List<Integer> line) {
        // TODO
        return 0;
    }
}