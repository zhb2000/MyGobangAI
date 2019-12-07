package MyChess;

import static MyChess.ChessType.*;
import static MyChess.Config.BOARD_SIZE;
import MyChess.Zobrist;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class ChessBoard {
    /** 棋盘矩阵 */
    private int[][] boardMatrix;
    /** 人类的空位启发函数值 */
    private int[][][] humHeuristic;
    /** 电脑的空位启发式函数值 */
    private int[][][] comHeuristic;
    /** 空位附近是否有棋子（不管什么类型） */
    // private boolean[][] neighborMatrix;
    /** 棋子总数 */
    private int chessNum;
    /** 位置附近棋子的总数 */
    private int[][] neighborNum;
    /** 位置近邻人类棋子数量 */
    private int[][] humCloseNeighbor;
    /** 位置近邻电脑棋子数量 */
    private int[][] comCloseNeighbor;
    /** Zobrist对象 */
    private Zobrist zobrist;

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
        neighborNum = new int[BOARD_SIZE][BOARD_SIZE];
        humCloseNeighbor = new int[BOARD_SIZE][BOARD_SIZE];
        comCloseNeighbor = new int[BOARD_SIZE][BOARD_SIZE];
        zobrist = new Zobrist();
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
        updateNeighborAfterDo(x, y, EMPTY_CHESS);// 更新邻居矩阵
        updateHeuristicAfterDo(x, y);// 更新启发函数矩阵
        zobrist.goUpdate(x, y, type);// 更新哈希值
        return true;
    }

    /**
     * 撤销棋子并更新相应的数据
     * 
     * @param x            落子的行号
     * @param y            落子的列号
     * @param oldHeuristic [hum[][][], com[][][]]
     * 
     * @return 操作是否合法
     */
    public boolean undo(int x, int y, int[][][][] oldHeuristic) {
        if (outOfBound(x, y)) {
            return false;
        }
        if (boardMatrix[x][y] == EMPTY_CHESS) {
            return false;
        }
        int oldType = boardMatrix[x][y];// 撤销前原来的棋子类型
        boardMatrix[x][y] = EMPTY_CHESS;
        chessNum--;// 更新棋子总数
        updateNeighborAfterDo(x, y, oldType);// 更新邻居矩阵
        restoreHeuristic(x, y, oldHeuristic[0], oldHeuristic[1]);// 更新启发函数矩阵
        zobrist.goUpdate(x, y, oldType);
        return true;
    }

    public long getCode() {
        return zobrist.code();
    }

    /**
     * 判断棋盘是否已满
     * 
     * @retrun 棋盘是否已满
     */
    public boolean isFull() {
        return chessNum == BOARD_SIZE * BOARD_SIZE;
    }

    /**
     * (x,y)两格内是否有邻居，无论什么类型
     * 
     * @param x 行号
     * @param y 列号
     * @return 两格内是否有邻居
     */
    private boolean hasNeighbor(int x, int y) {
        return neighborNum[x][y] > 0;
    }

    // /**
    // * 备份hasNeighbor矩阵
    // *
    // * @param x 落子的行号
    // * @param y 落子的列号
    // * @return 5*5数组
    // */
    // public boolean[][] backupNeighbor(int x, int y) {
    // boolean[][] oldNeighbor = new boolean[BOARD_SIZE][BOARD_SIZE];
    // for (int i = x - 2; i <= x + 2; i++) {
    // for (int j = y - 2; j <= y + 2; j++) {
    // if (!outOfBound(i, j)) {
    // oldNeighbor[i][j] = neighborMatrix[i][j];
    // }
    // }
    // }
    // return oldNeighbor;
    // }

    // private void restoreNeighbor(int x, int y, boolean[][] oldNeighbor) {
    // for (int i = x - 2; i <= x + 2; i++) {
    // for (int j = y - 2; j <= y + 2; j++) {
    // if (!outOfBound(i, j)) {
    // neighborMatrix[i][j] = oldNeighbor[i][j];
    // }
    // }
    // }
    // }

    // /**
    // * 对(x,y)位置落子或撤销后，更新邻居矩阵
    // *
    // * @param x 落子位置或撤销位置的行号
    // * @param y 落子位置或撤销位置的列号
    // */
    // private void updateNeighborAfterDo(int x, int y) {
    // if (boardMatrix[x][y] != EMPTY_CHESS) {
    // // 在(x,y)位置落子，直接把周围的空位修改成有邻居即可
    // for (int i = x - 2; i <= x + 2; i++) {
    // for (int j = y - 2; j <= y + 2; j++) {
    // // 没有越界且是个空位
    // if (!outOfBound(i, j) && boardMatrix[i][j] == EMPTY_CHESS) {
    // neighborMatrix[i][j] = true;
    // }
    // }
    // }
    // } else {
    // // 在(x,y)位置撤销棋子，周围空位的邻居情况需要具体计算
    // for (int i = x - 2; i <= x + 2; i++) {
    // for (int j = y - 2; j <= y + 2; j++) {
    // // 没有越界且是个空位
    // if (!outOfBound(i, j) && boardMatrix[i][j] == EMPTY_CHESS) {
    // calcuNeighbor(i, j);
    // }
    // }
    // }
    // }
    // }

    /**
     * 对(x,y)位置落子或撤销后，更新邻居矩阵
     * 
     * @param x       落子位置或撤销位置的行号
     * @param y       落子位置或撤销位置的列号
     * @param oldType 操作前(x,y)的棋子类型
     */
    private void updateNeighborAfterDo(int x, int y, int oldType) {
        // 更新neighborNum矩阵
        for (int i = x - 2; i <= x + 2; i++) {
            for (int j = y - 2; j <= y + 2; j++) {
                if (!outOfBound(i, j) && i != x && j != y) {
                    if (oldType == EMPTY_CHESS) {
                        // 在(x,y)落子，周围邻居数量加1
                        neighborNum[i][j]++;
                    } else {
                        //// 在(x,y)落子，周围邻居数量减1
                        neighborNum[i][j]--;
                    }
                }
            }
        }
        for (int i = x - 1; i <= x + 1; i++) {
            for (int j = y - 1; j <= y + 1; j++) {
                if (!outOfBound(i, j) && i != x && j != y) {
                    // 在(x,y)落子
                    if (oldType == EMPTY_CHESS) {
                        if (boardMatrix[x][y] == HUM_CHESS) {
                            // (x,y)现在是人类棋子
                            humCloseNeighbor[i][j]++;
                        } else {
                            // (x,y)现在是电脑棋子
                            comCloseNeighbor[i][j]++;
                        }
                    } else {// 在(x,y)撤销了一个棋子
                        if (oldType == HUM_CHESS) {
                            // (x,y)原来是人类棋子
                            humCloseNeighbor[i][j]--;
                        } else {
                            // (x,y)原来是电脑棋子
                            comCloseNeighbor[i][j]--;
                        }
                    }
                }
            }
        }
    }

    // /**
    // * 计算(x,y)附近是否有棋子，并修改邻居矩阵中相应的位置
    // *
    // * @param x 中心位置的行号
    // * @param y 中心位置的列号
    // */
    // private void calcuNeighbor(int x, int y) {
    // boolean hasNeighbor = false;
    // for (int i = x - 2; i <= x + 2; i++) {
    // for (int j = y - 2; j <= y + 2; j++) {
    // if (!outOfBound(i, j) && boardMatrix[i][j] != EMPTY_CHESS) {
    // hasNeighbor = true;// 没有越界且有棋子占据
    // break;
    // }
    // }
    // }
    // neighborMatrix[x][y] = hasNeighbor;
    // }

    /** [hum[][][], com[][][]] */
    public int[][][][] backupHeuristic(int x, int y) {
        int[][][] oldHum = new int[BOARD_SIZE][BOARD_SIZE][4];
        int[][][] oldCom = new int[BOARD_SIZE][BOARD_SIZE][4];
        // 垂直方向更新
        List<Coord> verticals = lineCoords(x, y, Direction.VERTICAL);
        for (Coord coord : verticals) {
            int ux = coord.x;
            int uy = coord.y;
            // 未越界
            if (!outOfBound(ux, uy)) {
                oldHum[ux][uy][Direction.VERTICAL] = humHeuristic[ux][uy][Direction.VERTICAL];
                oldHum[ux][uy][Direction.HORIZONTAL] = humHeuristic[ux][uy][Direction.HORIZONTAL];
                oldHum[ux][uy][Direction.DIAGONAL] = humHeuristic[ux][uy][Direction.DIAGONAL];
                oldHum[ux][uy][Direction.ANTIDIAGONAL] = humHeuristic[ux][uy][Direction.ANTIDIAGONAL];

                oldCom[ux][uy][Direction.VERTICAL] = comHeuristic[ux][uy][Direction.VERTICAL];
                oldCom[ux][uy][Direction.HORIZONTAL] = comHeuristic[ux][uy][Direction.HORIZONTAL];
                oldCom[ux][uy][Direction.DIAGONAL] = comHeuristic[ux][uy][Direction.DIAGONAL];
                oldCom[ux][uy][Direction.ANTIDIAGONAL] = comHeuristic[ux][uy][Direction.ANTIDIAGONAL];
            }
        }
        // 水平方向更新
        List<Coord> horizontals = lineCoords(x, y, Direction.HORIZONTAL);
        for (Coord coord : horizontals) {
            int ux = coord.x;
            int uy = coord.y;
            if (!outOfBound(ux, uy)) {
                oldHum[ux][uy][Direction.VERTICAL] = humHeuristic[ux][uy][Direction.VERTICAL];
                oldHum[ux][uy][Direction.HORIZONTAL] = humHeuristic[ux][uy][Direction.HORIZONTAL];
                oldHum[ux][uy][Direction.DIAGONAL] = humHeuristic[ux][uy][Direction.DIAGONAL];
                oldHum[ux][uy][Direction.ANTIDIAGONAL] = humHeuristic[ux][uy][Direction.ANTIDIAGONAL];

                oldCom[ux][uy][Direction.VERTICAL] = comHeuristic[ux][uy][Direction.VERTICAL];
                oldCom[ux][uy][Direction.HORIZONTAL] = comHeuristic[ux][uy][Direction.HORIZONTAL];
                oldCom[ux][uy][Direction.DIAGONAL] = comHeuristic[ux][uy][Direction.DIAGONAL];
                oldCom[ux][uy][Direction.ANTIDIAGONAL] = comHeuristic[ux][uy][Direction.ANTIDIAGONAL];
            }
        }
        // 对角线方向
        List<Coord> diagonals = lineCoords(x, y, Direction.DIAGONAL);
        for (Coord coord : diagonals) {
            int ux = coord.x;
            int uy = coord.y;
            if (!outOfBound(ux, uy)) {
                oldHum[ux][uy][Direction.VERTICAL] = humHeuristic[ux][uy][Direction.VERTICAL];
                oldHum[ux][uy][Direction.HORIZONTAL] = humHeuristic[ux][uy][Direction.HORIZONTAL];
                oldHum[ux][uy][Direction.DIAGONAL] = humHeuristic[ux][uy][Direction.DIAGONAL];
                oldHum[ux][uy][Direction.ANTIDIAGONAL] = humHeuristic[ux][uy][Direction.ANTIDIAGONAL];

                oldCom[ux][uy][Direction.VERTICAL] = comHeuristic[ux][uy][Direction.VERTICAL];
                oldCom[ux][uy][Direction.HORIZONTAL] = comHeuristic[ux][uy][Direction.HORIZONTAL];
                oldCom[ux][uy][Direction.DIAGONAL] = comHeuristic[ux][uy][Direction.DIAGONAL];
                oldCom[ux][uy][Direction.ANTIDIAGONAL] = comHeuristic[ux][uy][Direction.ANTIDIAGONAL];
            }
        }
        // 反对角线方向
        List<Coord> antidiagonals = lineCoords(x, y, Direction.ANTIDIAGONAL);
        for (Coord coord : antidiagonals) {
            int ux = coord.x;
            int uy = coord.y;
            if (!outOfBound(ux, uy)) {
                oldHum[ux][uy][Direction.VERTICAL] = humHeuristic[ux][uy][Direction.VERTICAL];
                oldHum[ux][uy][Direction.HORIZONTAL] = humHeuristic[ux][uy][Direction.HORIZONTAL];
                oldHum[ux][uy][Direction.DIAGONAL] = humHeuristic[ux][uy][Direction.DIAGONAL];
                oldHum[ux][uy][Direction.ANTIDIAGONAL] = humHeuristic[ux][uy][Direction.ANTIDIAGONAL];

                oldCom[ux][uy][Direction.VERTICAL] = comHeuristic[ux][uy][Direction.VERTICAL];
                oldCom[ux][uy][Direction.HORIZONTAL] = comHeuristic[ux][uy][Direction.HORIZONTAL];
                oldCom[ux][uy][Direction.DIAGONAL] = comHeuristic[ux][uy][Direction.DIAGONAL];
                oldCom[ux][uy][Direction.ANTIDIAGONAL] = comHeuristic[ux][uy][Direction.ANTIDIAGONAL];
            }
        }
        return new int[][][][] { oldHum, oldCom };
    }

    private void restoreHeuristic(int x, int y, int[][][] oldHum, int[][][] oldCom) {
        // 垂直方向恢复
        List<Coord> verticals = lineCoords(x, y, Direction.VERTICAL);
        for (Coord coord : verticals) {
            int ux = coord.x;
            int uy = coord.y;
            // 未越界
            if (!outOfBound(ux, uy)) {
                humHeuristic[ux][uy][Direction.VERTICAL] = oldHum[ux][uy][Direction.VERTICAL];
                humHeuristic[ux][uy][Direction.HORIZONTAL] = oldHum[ux][uy][Direction.HORIZONTAL];
                humHeuristic[ux][uy][Direction.DIAGONAL] = oldHum[ux][uy][Direction.DIAGONAL];
                humHeuristic[ux][uy][Direction.ANTIDIAGONAL] = oldHum[ux][uy][Direction.ANTIDIAGONAL];

                comHeuristic[ux][uy][Direction.VERTICAL] = oldCom[ux][uy][Direction.VERTICAL];
                comHeuristic[ux][uy][Direction.HORIZONTAL] = oldCom[ux][uy][Direction.HORIZONTAL];
                comHeuristic[ux][uy][Direction.DIAGONAL] = oldCom[ux][uy][Direction.DIAGONAL];
                comHeuristic[ux][uy][Direction.ANTIDIAGONAL] = oldCom[ux][uy][Direction.ANTIDIAGONAL];
            }
        }
        // 水平方向恢复
        List<Coord> horizontals = lineCoords(x, y, Direction.HORIZONTAL);
        for (Coord coord : horizontals) {
            int ux = coord.x;
            int uy = coord.y;
            if (!outOfBound(ux, uy)) {
                humHeuristic[ux][uy][Direction.VERTICAL] = oldHum[ux][uy][Direction.VERTICAL];
                humHeuristic[ux][uy][Direction.HORIZONTAL] = oldHum[ux][uy][Direction.HORIZONTAL];
                humHeuristic[ux][uy][Direction.DIAGONAL] = oldHum[ux][uy][Direction.DIAGONAL];
                humHeuristic[ux][uy][Direction.ANTIDIAGONAL] = oldHum[ux][uy][Direction.ANTIDIAGONAL];

                comHeuristic[ux][uy][Direction.VERTICAL] = oldCom[ux][uy][Direction.VERTICAL];
                comHeuristic[ux][uy][Direction.HORIZONTAL] = oldCom[ux][uy][Direction.HORIZONTAL];
                comHeuristic[ux][uy][Direction.DIAGONAL] = oldCom[ux][uy][Direction.DIAGONAL];
                comHeuristic[ux][uy][Direction.ANTIDIAGONAL] = oldCom[ux][uy][Direction.ANTIDIAGONAL];
            }
        }
        // 对角线方向
        List<Coord> diagonals = lineCoords(x, y, Direction.DIAGONAL);
        for (Coord coord : diagonals) {
            int ux = coord.x;
            int uy = coord.y;
            if (!outOfBound(ux, uy)) {
                humHeuristic[ux][uy][Direction.VERTICAL] = oldHum[ux][uy][Direction.VERTICAL];
                humHeuristic[ux][uy][Direction.HORIZONTAL] = oldHum[ux][uy][Direction.HORIZONTAL];
                humHeuristic[ux][uy][Direction.DIAGONAL] = oldHum[ux][uy][Direction.DIAGONAL];
                humHeuristic[ux][uy][Direction.ANTIDIAGONAL] = oldHum[ux][uy][Direction.ANTIDIAGONAL];

                comHeuristic[ux][uy][Direction.VERTICAL] = oldCom[ux][uy][Direction.VERTICAL];
                comHeuristic[ux][uy][Direction.HORIZONTAL] = oldCom[ux][uy][Direction.HORIZONTAL];
                comHeuristic[ux][uy][Direction.DIAGONAL] = oldCom[ux][uy][Direction.DIAGONAL];
                comHeuristic[ux][uy][Direction.ANTIDIAGONAL] = oldCom[ux][uy][Direction.ANTIDIAGONAL];
            }
        }
        // 反对角线方向
        List<Coord> antidiagonals = lineCoords(x, y, Direction.ANTIDIAGONAL);
        for (Coord coord : antidiagonals) {
            int ux = coord.x;
            int uy = coord.y;
            if (!outOfBound(ux, uy)) {
                humHeuristic[ux][uy][Direction.VERTICAL] = oldHum[ux][uy][Direction.VERTICAL];
                humHeuristic[ux][uy][Direction.HORIZONTAL] = oldHum[ux][uy][Direction.HORIZONTAL];
                humHeuristic[ux][uy][Direction.DIAGONAL] = oldHum[ux][uy][Direction.DIAGONAL];
                humHeuristic[ux][uy][Direction.ANTIDIAGONAL] = oldHum[ux][uy][Direction.ANTIDIAGONAL];

                comHeuristic[ux][uy][Direction.VERTICAL] = oldCom[ux][uy][Direction.VERTICAL];
                comHeuristic[ux][uy][Direction.HORIZONTAL] = oldCom[ux][uy][Direction.HORIZONTAL];
                comHeuristic[ux][uy][Direction.DIAGONAL] = oldCom[ux][uy][Direction.DIAGONAL];
                comHeuristic[ux][uy][Direction.ANTIDIAGONAL] = oldCom[ux][uy][Direction.ANTIDIAGONAL];
            }
        }
    }

    /**
     * 落子或撤销后对启发函数值矩阵进行更新
     * 
     * @param x 中心位置的行号
     * @param y 中心位置的列号
     */
    private void updateHeuristicAfterDo(int x, int y) {
        // 更新附近的空格的启发函数值，米字范围内的空格都要更新
        // 垂直方向更新
        List<Coord> verticals = lineCoords(x, y, Direction.VERTICAL);
        for (Coord coord : verticals) {
            int ux = coord.x;
            int uy = coord.y;
            // 未越界且是空格
            if (!outOfBound(ux, uy) && boardMatrix[ux][uy] == EMPTY_CHESS) {
                calcuHeuristic(ux, uy, Direction.VERTICAL);
            }
        }
        // 水平方向更新
        List<Coord> horizontals = lineCoords(x, y, Direction.HORIZONTAL);
        for (Coord coord : horizontals) {
            int ux = coord.x;
            int uy = coord.y;
            if (!outOfBound(ux, uy) && boardMatrix[ux][uy] == EMPTY_CHESS) {
                calcuHeuristic(ux, uy, Direction.HORIZONTAL);
            }
        }
        // 对角线方向更新
        List<Coord> diagonals = lineCoords(x, y, Direction.DIAGONAL);
        for (Coord coord : diagonals) {
            int ux = coord.x;
            int uy = coord.y;
            if (!outOfBound(ux, uy) && boardMatrix[ux][uy] == EMPTY_CHESS) {
                calcuHeuristic(ux, uy, Direction.DIAGONAL);
            }
        }
        // 反对角线方向更新
        List<Coord> antidiagonals = lineCoords(x, y, Direction.ANTIDIAGONAL);
        for (Coord coord : antidiagonals) {
            int ux = coord.x;
            int uy = coord.y;
            if (!outOfBound(ux, uy) && boardMatrix[ux][uy] == EMPTY_CHESS) {
                calcuHeuristic(ux, uy, Direction.ANTIDIAGONAL);
            }
        }
        // 若自己这个位置变成了空格，则需要更新自己这里的启发函数值
        if (boardMatrix[x][y] == EMPTY_CHESS) {
            calcuHeuristic(x, y, Direction.VERTICAL);
            calcuHeuristic(x, y, Direction.HORIZONTAL);
            calcuHeuristic(x, y, Direction.DIAGONAL);
            calcuHeuristic(x, y, Direction.ANTIDIAGONAL);
        }
    }

    /**
     * <p>
     * 计算(x,y)处的启发函数值，并给启发函数矩阵的相应位置赋值
     * </p>
     * <p>
     * 为了提高速度，只修改direction方向的启发函数值
     * </p>
     * 
     * @param x         空位的行号
     * @param y         空位的列号
     * @param direction 指定的方向
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
        List<Coord> coords = lineCoords(x, y, direction);// 生成所需的坐标
        boardMatrix[x][y] = COM_CHESS;// 空位放上电脑棋子
        comHeuristic[x][y][dir] = score(coords, COM_CHESS);
        boardMatrix[x][y] = HUM_CHESS;// 空位放上人类棋子
        humHeuristic[x][y][dir] = score(coords, HUM_CHESS);
        boardMatrix[x][y] = EMPTY_CHESS;// 把空位恢复成空的
    }

    /**
     * 根据直线中棋形的种类和数量，对直线coords打分
     * 
     * @param coords    直线，坐标允许越界
     * @param chessType 对哪一方打分
     * 
     * @return 打出的分数
     */
    private int score(List<Coord> coords, int chessType) {
        // 把coords所表示的横线标准化后再打分
        List<Integer> standardLine = standardizeLine(coords, chessType);
        return ScoreCalculator.scoreLine(standardLine);
    }

    /**
     * 获取某一方挑选空格时的启发式函数值
     * 
     * @param x    空格的行号
     * @param y    空格的列号
     * @param type 哪一方的棋子要下到这个空格上
     * 
     * @return 该空格对于该方的启发函数值
     */
    private int heuristic(int x, int y, int type) {
        // TODO 已修改启发函数，加入近邻奖励
        if (type == COM_CHESS) {
            // 电脑方的棋子要下到(x,y)位置的空格上
            // 把该空格位置四个方向的启发函数值加起来
            int value = comHeuristic[x][y][Direction.VERTICAL] + comHeuristic[x][y][Direction.HORIZONTAL]
                    + comHeuristic[x][y][Direction.DIAGONAL] + comHeuristic[x][y][Direction.ANTIDIAGONAL];
            // 每一个己方近邻额外多加10%的分
            return (int) (value * (1 + comCloseNeighbor[x][y] * Config.CLOSE_RADIUS));
        } else {
            int value = humHeuristic[x][y][Direction.VERTICAL] + humHeuristic[x][y][Direction.HORIZONTAL]
                    + humHeuristic[x][y][Direction.DIAGONAL] + humHeuristic[x][y][Direction.ANTIDIAGONAL];
            return (int) (value * (1 + humCloseNeighbor[x][y] * Config.CLOSE_RADIUS));
        }
    }

    /**
     * 获取一组“优良的”空格位置
     * 
     * @param type 哪一方需要落子
     * 
     * @return 一组“优良的”空格位置
     */
    public List<Coord> generator(int type) {
        int self, enermy;
        if (type == COM_CHESS) {
            self = COM_CHESS;
            enermy = HUM_CHESS;
        } else {
            self = HUM_CHESS;
            enermy = COM_CHESS;
        }
        // 己方或敌方放在该空位能连五
        List<Coord> fives = new ArrayList<>();
        // 己方放在该空位能活四
        List<Coord> selfAliveFours = new ArrayList<>();
        // 敌方放在该空位能活四
        List<Coord> enermyAliveFours = new ArrayList<>();
        // 己方放在该空位能死四
        List<CoordWithHeuristic> selfBlockedFours = new ArrayList<>();
        // 敌方放在该空位能死四
        List<CoordWithHeuristic> enermyBlockedFours = new ArrayList<>();
        // 己方放在该空位能双活三
        List<CoordWithHeuristic> selfDoubleThrees = new ArrayList<>();
        // 敌方放在该空位能双活三
        List<CoordWithHeuristic> enermyDoubleThrees = new ArrayList<>();
        // 其他
        List<CoordWithHeuristic> otherPositions = new ArrayList<>();

        for (int x = 0; x < BOARD_SIZE; x++) {
            for (int y = 0; y < BOARD_SIZE; y++) {
                // 这个位置必须是空的，而且我们规定必须有邻居才有资格进入候选
                if (boardMatrix[x][y] == EMPTY_CHESS && hasNeighbor(x, y)) {
                    Coord coord = new Coord(x, y);// 空位坐标
                    int selfPosScore = heuristic(x, y, self);// 己方空位得分
                    int enermyPosScore = heuristic(x, y, enermy);// 地方空位得分
                    if (selfPosScore >= Score.FIVE || enermyPosScore >= Score.FIVE) {
                        fives.add(coord);
                    } else if (selfPosScore >= Score.ALIVE_FOUR) {
                        selfAliveFours.add(coord);
                    } else if (enermyPosScore >= Score.ALIVE_FOUR) {
                        enermyAliveFours.add(coord);
                    } else if (selfPosScore >= Score.BLOCKED_FOUR) {
                        selfBlockedFours.add(new CoordWithHeuristic(coord, selfPosScore));
                    } else if (enermyPosScore >= Score.BLOCKED_FOUR) {
                        enermyBlockedFours.add(new CoordWithHeuristic(coord, enermyPosScore));
                    } else if (selfPosScore >= Score.ALIVE_THREE * 2) {
                        selfDoubleThrees.add(new CoordWithHeuristic(coord, selfPosScore));
                    } else if (enermyPosScore >= Score.ALIVE_THREE * 2) {
                        enermyDoubleThrees.add(new CoordWithHeuristic(coord, selfPosScore));
                    } else {
                        otherPositions.add(new CoordWithHeuristic(coord, Math.max(selfPosScore, enermyPosScore)));
                    }
                }
            }
        }

        // 己方能连五的位置，必杀，直接返回
        // 敌方下一手能连五的位置，己方要把这里堵住，直接返回
        if (fives.size() > 0) {
            return fives;
        }

        // 己方能活四的位置，必杀，直接返回
        if (selfAliveFours.size() > 0) {
            return selfAliveFours;
        }

        // 敌方下一手能活四的位置
        if (enermyAliveFours.size() > 0) {
            if (selfBlockedFours.size() > 0) {
                // 若己方这一手有能死四的位置
                // 优先考虑己方成死四，以攻代守
                List<Coord> retArray = new ArrayList<>();
                for (CoordWithHeuristic c : selfBlockedFours) {
                    retArray.add(c.coord);
                }
                // 其次考虑去堵敌方的活四，消极防守
                retArray.addAll(enermyAliveFours);
                return retArray;
            } else {
                // 己方这一手不能成死四
                // 堵敌方的活四，消极防守
                return enermyAliveFours;
            }
        }

        // 己方这一手和敌方下一手都不能连五或者活四
        List<Coord> result = new ArrayList<>();
        // 排序
        Collections.sort(selfDoubleThrees);
        Collections.sort(selfBlockedFours);
        Collections.sort(enermyDoubleThrees);
        Collections.sort(enermyBlockedFours);
        Collections.sort(otherPositions);
        // 己方成双活三
        for (CoordWithHeuristic coo : selfDoubleThrees) {
            result.add(coo.coord);
        }
        // 己方成死四
        for (CoordWithHeuristic coo : selfBlockedFours) {
            result.add(coo.coord);
        }
        // 堵敌方双活三
        for (CoordWithHeuristic coo : enermyDoubleThrees) {
            result.add(coo.coord);
        }
        // 堵敌方死四
        for (CoordWithHeuristic coo : enermyBlockedFours) {
            result.add(coo.coord);
        }
        // 其他棋形
        for (CoordWithHeuristic coo : otherPositions) {
            result.add(coo.coord);
        }
        return result;
    }

    /** 算杀 */
    public List<Coord> generatorKill(int type) {
        int self, enermy;
        if (type == COM_CHESS) {
            self = COM_CHESS;
            enermy = HUM_CHESS;
        } else {
            self = HUM_CHESS;
            enermy = COM_CHESS;
        }
        // 己方或敌方放在该空位能连五、活四、死四、活三
        List<CoordWithHeuristic> sortArray = new ArrayList<>();
        for (int x = 0; x < BOARD_SIZE; x++) {
            for (int y = 0; y < BOARD_SIZE; y++) {
                // 这个位置必须是空的，而且我们规定必须有邻居才有资格进入候选
                if (boardMatrix[x][y] == EMPTY_CHESS && hasNeighbor(x, y)) {
                    Coord coord = new Coord(x, y);
                    int selfPosScore = heuristic(x, y, self);
                    int enermyPosScore = heuristic(x, y, enermy);
                    if (selfPosScore >= Score.ALIVE_THREE || enermyPosScore >= Score.ALIVE_THREE) {
                        sortArray.add(new CoordWithHeuristic(coord, Math.max(selfPosScore, enermyPosScore)));
                    }
                }
            }
        }
        Collections.sort(sortArray);
        List<Coord> result = new ArrayList<>();
        for (CoordWithHeuristic c : sortArray) {
            result.add(c.coord);
        }
        return result;
    }

    /**
     * 带有启发式函数值的坐标类，用于对空位进行排序
     */
    class CoordWithHeuristic implements Comparable<CoordWithHeuristic> {
        /** 坐标 */
        Coord coord;
        /** 该位置的启发函数值 */
        int h;

        /**
         * 构造函数
         * 
         * @param coord 坐标
         * @param h     启发函数值
         */
        public CoordWithHeuristic(Coord coord, int h) {
            this.coord = coord;
            this.h = h;
        }

        @Override
        public int compareTo(CoordWithHeuristic c2) {
            return this.h - c2.h;
        }
    }

    /**
     * 评估函数，对整个棋盘的局势进行评估 越大电脑越有利，越小人类越有利
     * 
     * @return 评估值
     */
    public int evaluate(int type) {
        int comScore = evaluateOneSide(COM_CHESS);
        int humScore = evaluateOneSide(HUM_CHESS);
        if (type == COM_CHESS) {
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
     * @param type 对哪一方进行评估
     * 
     * @return 单方面评估值
     */
    private int evaluateOneSide(int type) {
        int result = 0;
        for (int x = 0; x < BOARD_SIZE; x++) {
            for (int y = 0; y < BOARD_SIZE; y++) {
                if (boardMatrix[x][y] == EMPTY_CHESS) {
                    result += heuristic(x, y, type);
                }
            }
        }
        return result;
    }

    /**
     * 在(x,y)被落子后，落子的那一方是否赢了
     */
    public boolean isWin(int x, int y) {
        // 因为落子之前肯定是个空格，直接看这个地方是空格时的启发函数值即可
        return heuristic(x, y, boardMatrix[x][y]) >= Score.FIVE;
    }

    /**
     * 把直线坐标变成标准化直线 坐标允许越界 两头添加阻塞
     */
    private List<Integer> standardizeLine(List<Coord> line, int type) {
        List<Integer> standardLine = new ArrayList<>();
        standardLine.add(StandardType.BLOCKED);
        for (Coord coord : line) {
            int x = coord.x;
            int y = coord.y;
            if (outOfBound(x, y)) {
                standardLine.add(StandardType.BLOCKED);
            } else if (boardMatrix[x][y] == EMPTY_CHESS) {
                standardLine.add(StandardType.EMPTY);
            } else if (boardMatrix[x][y] == type) {
                standardLine.add(StandardType.SELF);
            } else {
                standardLine.add(StandardType.BLOCKED);
            }
        }
        standardLine.add(StandardType.BLOCKED);
        return standardLine;
    }

    @Override
    public String toString() {
        StringBuilder str = new StringBuilder();
        str.append("  ");
        for (int i = 0; i < BOARD_SIZE; i++) {
            str.append(i % 10 + " ");
        }
        str.append("\n");
        for (int x = 0; x < BOARD_SIZE; x++) {
            str.append(x % 10 + " ");
            for (int y = 0; y < BOARD_SIZE; y++) {
                if (boardMatrix[x][y] == EMPTY_CHESS) {
                    // str.append(" ");
                    str.append("＋");// □
                } else if (boardMatrix[x][y] == COM_CHESS) {
                    // str.append("O ");
                    str.append("〇");// ○
                } else {
                    // str.append("X ");
                    str.append("●");
                }
            }
            str.append("\n");
        }
        return str.toString();
    }

    /**
     * 棋盘上棋子的总数
     */
    public int getNumber() {
        return chessNum;
    }

    // 各种工具static方法

    /**
     * 生成棋盘所有的直线的坐标
     * 
     * @retrun 所有的直线的坐标
     */
    /*
     * private static List<List<Coord>> allLines() { List<Coord> line = null;// 直线坐标
     * List<List<Coord>> lines = new ArrayList<>();// 装着所有的直线 // 水平直线 for (int x =
     * 0; x < BOARD_SIZE; x++) { line = new ArrayList<>(); for (int y = 0; y <
     * BOARD_SIZE; y++) { line.add(new Coord(x, y)); } lines.add(line); } // 竖直直线
     * for (int y = 0; y < BOARD_SIZE; y++) { line = new ArrayList<>(); for (int x =
     * 0; x < BOARD_SIZE; x++) { line.add(new Coord(x, y)); } lines.add(line); }
     * 
     * int startX, startY; // 对角线 for (startX = BOARD_SIZE - 1, startY = 0; startX
     * >= 0; startX--) { line = new ArrayList<>(); for (int x = startX, y = startY;
     * x < BOARD_SIZE && y < BOARD_SIZE; x++, y++) { line.add(new Coord(x, y)); } if
     * (line.size() >= 5) { lines.add(line); } } for (startX = 0, startY = 1; startY
     * < BOARD_SIZE; startY++) { line = new ArrayList<>(); for (int x = startX, y =
     * startY; x < BOARD_SIZE && y < BOARD_SIZE; x++, y++) { line.add(new Coord(x,
     * y)); } if (line.size() >= 5) { lines.add(line); } } // 反对角线 for (startX =
     * BOARD_SIZE - 1, startY = BOARD_SIZE - 1; startX >= 0; startX--) { line = new
     * ArrayList<>(); for (int x = startX, y = startY; x < BOARD_SIZE && y >= 0;
     * x++, y--) { line.add(new Coord(x, y)); } if (line.size() >= 5) {
     * lines.add(line); } } for (startX = 0, startY = BOARD_SIZE - 2; startY >= 0;
     * startY--) { line = new ArrayList<>(); for (int x = startX, y = startY; x <
     * BOARD_SIZE && y >= 0; x++, y--) { line.add(new Coord(x, y)); } if
     * (line.size() >= 5) { lines.add(line); } } return lines; }
     */

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
        if (direction == Direction.VERTICAL) {// 竖直方向
            for (int i = x - 4; i <= x + 4; i++) {
                coords.add(new Coord(i, y));
            }
        } else if (direction == Direction.HORIZONTAL) {// 水平方向
            for (int j = y - 4; j <= y + 4; j++) {
                coords.add(new Coord(x, j));
            }
        } else if (direction == Direction.DIAGONAL) {// 对角线
            int i = x - 4, j = y - 4;
            for (int k = 0; k <= 8; k++) {
                coords.add(new Coord(i + k, j + k));
            }
        } else {// 反对角线
            int i = x + 4, j = y - 4;
            for (int k = 0; k <= 8; k++) {
                coords.add(new Coord(i - k, j + k));
            }
        }
        return coords;
    }

}