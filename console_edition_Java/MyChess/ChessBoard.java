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
        zobrist.goUpdate(x, y, oldType);// 更新哈希值
        return true;
    }

    public long getCode() {
        return zobrist.code();
    }

    /**
     * 判断棋盘是否已满
     * 
     * @return 棋盘是否已满
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
                        // 在(x,y)撤销，周围邻居数量减1
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
        List<CoordWithHeuristic> fives = new ArrayList<>();
        // 己方放在该空位能活四
        List<CoordWithHeuristic> selfAliveFours = new ArrayList<>();
        // 敌方放在该空位能活四
        List<CoordWithHeuristic> enermyAliveFours = new ArrayList<>();
        // 己方放在该空位能死四
        List<CoordWithHeuristic> selfBlockedFours = new ArrayList<>();
        // 敌方放在该空位能死四
        List<CoordWithHeuristic> enermyBlockedFours = new ArrayList<>();
        // 己方放在该空位能双活三
        List<CoordWithHeuristic> selfDoubleThrees = new ArrayList<>();
        // 敌方放在该空位能双活三
        List<CoordWithHeuristic> enermyDoubleThrees = new ArrayList<>();
        // 己方放在该空位能活三
        List<CoordWithHeuristic> selfAliveThrees = new ArrayList<>();
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
                        fives.add(new CoordWithHeuristic(coord, Score.FIVE));
                    } else if (selfPosScore >= Score.ALIVE_FOUR) {
                        selfAliveFours.add(new CoordWithHeuristic(coord, selfPosScore));
                    } else if (enermyPosScore >= Score.ALIVE_FOUR) {
                        enermyAliveFours.add(new CoordWithHeuristic(coord, enermyPosScore));
                    } else if (selfPosScore >= Score.BLOCKED_FOUR) {
                        selfBlockedFours.add(new CoordWithHeuristic(coord, selfPosScore));
                    } else if (enermyPosScore >= Score.BLOCKED_FOUR) {
                        enermyBlockedFours.add(new CoordWithHeuristic(coord, enermyPosScore));
                    } else if (selfPosScore >= Score.ALIVE_THREE * 2) {
                        selfDoubleThrees.add(new CoordWithHeuristic(coord, selfPosScore));
                    } else if (enermyPosScore >= Score.ALIVE_THREE * 2) {
                        enermyDoubleThrees.add(new CoordWithHeuristic(coord, enermyPosScore));
                    } else if (selfPosScore >= Score.ALIVE_THREE) {
                        selfAliveThrees.add(new CoordWithHeuristic(coord, selfPosScore));
                    } else {
                        otherPositions.add(new CoordWithHeuristic(coord, Math.max(selfPosScore, enermyPosScore)));
                    }
                }
            }
        }

        List<Coord> result = new ArrayList<>();
        // 己方能连五的位置，必杀，直接返回
        // 敌方下一手能连五的位置，己方要把这里堵住，直接返回
        if (fives.size() > 0) {
            result.addAll(fives);
            return result;
        }

        // 己方能活四的位置，必杀，直接返回
        if (selfAliveFours.size() > 0) {
            result.addAll(selfAliveFours);
            return result;
        }

        // 敌方下一手能活四的位置
        if (enermyAliveFours.size() > 0) {
            Collections.sort(selfBlockedFours);
            Collections.sort(enermyAliveFours);
            if (selfBlockedFours.size() > 0) {
                result.addAll(selfBlockedFours);// 优先考虑己方成死四，以攻代守
                result.addAll(enermyAliveFours);// 其次考虑去堵敌方的活四，消极防守
            } else {
                // 己方这一手不能成死四
                result.addAll(enermyAliveFours);// 堵敌方的活四，消极防守
            }
            return result;
        }

        // 己方这一手和敌方下一手都不能连五或者活四
        // 排序
        Collections.sort(selfDoubleThrees);
        Collections.sort(selfBlockedFours);
        Collections.sort(enermyDoubleThrees);
        Collections.sort(enermyBlockedFours);
        Collections.sort(selfAliveThrees);
        Collections.sort(otherPositions);

        result.addAll(selfDoubleThrees);// 己方成双活三
        result.addAll(selfBlockedFours);// 己方成死四
        result.addAll(selfAliveThrees); // 己方成活三
        result.addAll(enermyDoubleThrees);// 堵敌方双活三
        result.addAll(enermyBlockedFours);// 堵敌方死四
        result.addAll(otherPositions);

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

        List<CoordWithHeuristic> fives = new ArrayList<>();
        List<CoordWithHeuristic> selfAliveFours = new ArrayList<>();
        List<CoordWithHeuristic> enermyAliveFours = new ArrayList<>();
        List<CoordWithHeuristic> selfBlockedFours = new ArrayList<>();
        List<CoordWithHeuristic> selfDoubleThrees = new ArrayList<>();
        List<CoordWithHeuristic> enermyDoubleThrees = new ArrayList<>();
        List<CoordWithHeuristic> selfAliveThrees = new ArrayList<>();

        for (int x = 0; x < BOARD_SIZE; x++) {
            for (int y = 0; y < BOARD_SIZE; y++) {
                // 这个位置必须是空的，而且我们规定必须有邻居才有资格进入候选
                if (boardMatrix[x][y] == EMPTY_CHESS && hasNeighbor(x, y)) {
                    int selfPosScore = heuristic(x, y, self);
                    int enermyPosScore = heuristic(x, y, enermy);
                    if (selfPosScore >= Score.FIVE || enermyPosScore >= Score.FIVE) {
                        fives.add(new CoordWithHeuristic(x, y, Score.FIVE));
                    } else if (selfPosScore >= Score.ALIVE_FOUR) {
                        selfAliveFours.add(new CoordWithHeuristic(x, y, selfPosScore));
                    } else if (enermyPosScore >= Score.ALIVE_FOUR) {
                        enermyAliveFours.add(new CoordWithHeuristic(x, y, enermyPosScore));
                    } else if (selfPosScore >= Score.BLOCKED_FOUR) {
                        selfBlockedFours.add(new CoordWithHeuristic(x, y, selfPosScore));
                    } else if (selfPosScore >= Score.ALIVE_THREE * 2) {
                        selfDoubleThrees.add(new CoordWithHeuristic(x, y, selfPosScore));
                    } else if (enermyPosScore >= Score.ALIVE_THREE * 2) {
                        enermyDoubleThrees.add(new CoordWithHeuristic(x, y, enermyPosScore));
                    } else if (selfPosScore >= Score.ALIVE_THREE) {
                        selfAliveThrees.add(new CoordWithHeuristic(x, y, selfPosScore));
                    }
                }
            }
        }

        List<Coord> result = new ArrayList<>();

        // 己方能连五的位置，必杀，直接返回
        // 敌方下一手能连五的位置，己方要把这里堵住，直接返回
        if (fives.size() > 0) {
            result.addAll(fives);
            return result;
        }

        // 己方能活四的位置，必杀，直接返回
        if (selfAliveFours.size() > 0) {
            result.addAll(selfAliveFours);
            return result;
        }

        // 敌方下一手能活四的位置
        if (enermyAliveFours.size() > 0) {
            Collections.sort(selfBlockedFours);
            Collections.sort(enermyAliveFours);
            if (selfBlockedFours.size() > 0) {
                result.addAll(selfBlockedFours);// 优先考虑己方成死四，以攻代守
                result.addAll(enermyAliveFours);// 其次考虑去堵敌方的活四，消极防守
            } else {
                // 己方这一手不能成死四
                result.addAll(enermyAliveFours);// 堵敌方的活四，消极防守
            }
            return result;
        }

        // 己方这一手和敌方下一手都不能连五或者活四
        // 排序
        Collections.sort(selfDoubleThrees);
        Collections.sort(selfBlockedFours);
        Collections.sort(enermyDoubleThrees);
        Collections.sort(selfAliveThrees);

        result.addAll(selfDoubleThrees);// 己方成双活三
        result.addAll(selfBlockedFours);// 己方成死四
        result.addAll(selfAliveThrees); // 己方成活三
        result.addAll(enermyDoubleThrees);// 堵敌方双活三
        return result;
    }

    /**
     * 带有启发式函数值的坐标类，用于对空位进行排序
     */
    class CoordWithHeuristic extends Coord implements Comparable<CoordWithHeuristic> {
        /** 坐标 */
        // Coord coord;
        /** 该位置的启发函数值 */
        int h;

        /**
         * 构造函数
         * 
         * @param coord 坐标
         * @param h     启发函数值
         */
        public CoordWithHeuristic(Coord coord, int h) {
            this.x = coord.x;
            this.y = coord.y;
            this.h = h;
        }

        public CoordWithHeuristic(int x, int y, int h) {
            this.x = x;
            this.y = y;
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
                    str.append("＋");
                } else if (boardMatrix[x][y] == COM_CHESS) {
                    str.append("〇");
                } else {
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