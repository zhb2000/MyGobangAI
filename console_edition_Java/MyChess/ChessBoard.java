package MyChess;

import static MyChess.ChessType.*;
import static MyChess.Config.BOARD_SIZE;

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
        updateHeuristicAfterDo(x, y);// 更新启发函数矩阵
        updateNeighborAfterDo(x, y);// 更新邻居矩阵
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
        updateHeuristicAfterDo(x, y);// 更新启发函数矩阵
        updateNeighborAfterDo(x, y);// 更新邻居矩阵
        return true;
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
     * 对(x,y)位置落子或撤销后，更新邻居矩阵
     * 
     * @param x 落子位置或撤销位置的行号
     * @param y 落子位置或撤销位置的列号
     */
    private void updateNeighborAfterDo(int x, int y) {
        if (boardMatrix[x][y] != EMPTY_CHESS) {
            // 在(x,y)位置落子，直接把周围的空位修改成有邻居即可
            for (int i = x - 2; i <= x + 2; i++) {
                for (int j = y - 2; j <= y + 2; j++) {
                    // 没有越界且是个空位
                    if (!outOfBound(i, j) && boardMatrix[i][j] == EMPTY_CHESS) {
                        neighborMatrix[i][j] = true;
                    }
                }
            }
        } else {
            // 在(x,y)位置撤销棋子，周围空位的邻居情况需要具体计算
            for (int i = x - 2; i <= x + 2; i++) {
                for (int j = y - 2; j <= y + 2; j++) {
                    // 没有越界且是个空位
                    if (!outOfBound(i, j) && boardMatrix[i][j] == EMPTY_CHESS) {
                        calcuNeighbor(i, j);
                    }
                }
            }
        }
    }

    /**
     * 计算(x,y)附近是否有棋子，并修改邻居矩阵中相应的位置
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
    private void updateHeuristicAfterDo(int x, int y) {
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
            // 把该空格位置四个方向的启发函数值加起来返回
            return comHeuristic[x][y][Direction.VERTICAL] + comHeuristic[x][y][Direction.HORIZONTAL]
                    + comHeuristic[x][y][Direction.DIAGONAL] + comHeuristic[x][y][Direction.ANTIDIAGONAL];
        } else {
            return humHeuristic[x][y][Direction.VERTICAL] + humHeuristic[x][y][Direction.HORIZONTAL]
                    + humHeuristic[x][y][Direction.DIAGONAL] + humHeuristic[x][y][Direction.ANTIDIAGONAL];
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
        // 某一方放在该空位能连五
        List<Coord> fives = new ArrayList<>();
        // 己方放在该空位能活四
        List<Coord> selfAliveFours = new ArrayList<>();
        // 敌方放在该空位能活四
        List<Coord> enermyAliveFours = new ArrayList<>();
        // 己方放在该空位能死四
        List<Coord> selfBlockedFours = new ArrayList<>();
        // 敌方放在该空位能死四
        List<Coord> enermyBlockedFours = new ArrayList<>();
        // 己方放在该空位能双活三
        List<Coord> selfDoubleThrees = new ArrayList<>();
        // 敌方放在该空位能双活三
        List<Coord> enermyDoubleThrees = new ArrayList<>();
        // 其他
        List<CoordWithHeuristic> otherPositions = new ArrayList<>();

        for (int x = 0; x < BOARD_SIZE; x++) {
            for (int y = 0; y < BOARD_SIZE; y++) {
                // 这个位置必须是空的，而且我们规定必须有邻居才有资格进入候选
                if (boardMatrix[x][y] == EMPTY_CHESS && neighborMatrix[x][y]) {
                    Coord coord = new Coord(x, y);
                    int selfPosScore = heuristic(x, y, self);
                    int enermyPosScore = heuristic(x, y, enermy);
                    if (selfPosScore >= Score.FIVE || enermyPosScore >= Score.FIVE) {
                        fives.add(coord);
                    } else if (selfPosScore >= Score.ALIVE_FOUR) {
                        selfAliveFours.add(coord);
                    } else if (enermyPosScore >= Score.ALIVE_FOUR) {
                        enermyAliveFours.add(coord);
                    } else if (selfPosScore >= Score.BLOCKED_FOUR) {
                        selfBlockedFours.add(coord);
                    } else if (enermyPosScore >= Score.BLOCKED_FOUR) {
                        enermyBlockedFours.add(coord);
                    } else if (selfPosScore >= Score.ALIVE_THREE * 2) {
                        selfDoubleThrees.add(coord);
                    } else if (enermyPosScore >= Score.ALIVE_THREE * 2) {
                        enermyDoubleThrees.add(coord);
                    } else {
                        otherPositions.add(new CoordWithHeuristic(coord, Math.abs(selfPosScore - enermyPosScore)));
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
            // 若己方这一手有能死四的位置
            if (selfBlockedFours.size() > 0) {
                // 优先考虑下在己方死四的位置，以攻代守
                // 其次考虑去堵敌方的活四，消极防守，垂死挣扎
                selfBlockedFours.addAll(enermyAliveFours);
                return selfBlockedFours;
            } else {
                // 堵敌方的活四，消极防守
                return enermyAliveFours;
            }
        }

        List<Coord> result = new ArrayList<>();
        result.addAll(selfDoubleThrees);// 己方双活三
        result.addAll(selfBlockedFours);// 己方死四
        result.addAll(enermyDoubleThrees);// 敌方双活三
        result.addAll(enermyBlockedFours);// 己方死四
        Collections.sort(otherPositions);
        for (CoordWithHeuristic coo : otherPositions) {
            result.add(coo.coord);
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
     * 评估函数，对整个棋盘的局势进行评估
     * 
     * @param type 己方棋子的类型
     * 
     * @return 评估值
     */
    public int evaluate(int type) {
        int self, enermy;
        if (type == COM_CHESS) {
            self = COM_CHESS;
            enermy = HUM_CHESS;
        } else {
            self = HUM_CHESS;
            enermy = COM_CHESS;
        }
        int selfScore = evaluateOneSide(self);
        int enermyScore = evaluateOneSide(enermy);
        return selfScore - enermyScore;
    }

    /**
     * 单方面评估函数，只针对某一方进行评估
     * 
     * @param type 对哪一方进行评估
     * 
     * @return 评估值
     */
    private int evaluateOneSide(int type) {
        int self = type;
        List<Integer> standardLine = null;
        List<List<Integer>> standardLines = new ArrayList<>();
        List<List<Coord>> coordLines = allLines();
        for (List<Coord> coordLine : coordLines) {
            standardLine = new ArrayList<>();
            standardLine.add(StandardType.BLOCKED);// 首部加一个阻塞
            for (Coord coord : coordLine) {
                int x = coord.x;
                int y = coord.y;
                if (boardMatrix[x][y] == EMPTY_CHESS) {
                    standardLine.add(StandardType.EMPTY);
                } else if (boardMatrix[x][y] == self) {
                    standardLine.add(StandardType.SELF);
                } else {
                    standardLine.add(StandardType.BLOCKED);
                }
            }
            standardLine.add(StandardType.BLOCKED);// 尾部加一个阻塞
            standardLines.add(standardLine);
        }
        return ScoreCalculator.scoreLines(standardLines);
    }

    // 各种工具static方法

    /**
     * 生成棋盘所有的直线的坐标
     * 
     * @retrun 所有的直线的坐标
     */
    private static List<List<Coord>> allLines() {
        List<Coord> line = null;// 直线坐标
        List<List<Coord>> lines = new ArrayList<>();// 装着所有的直线
        // 水平直线
        for (int x = 0; x < BOARD_SIZE; x++) {
            line = new ArrayList<>();
            for (int y = 0; y < BOARD_SIZE; y++) {
                line.add(new Coord(x, y));
            }
            lines.add(line);
        }
        // 竖直直线
        for (int y = 0; y < BOARD_SIZE; y++) {
            line = new ArrayList<>();
            for (int x = 0; x < BOARD_SIZE; x++) {
                line.add(new Coord(x, y));
            }
            lines.add(line);
        }

        int startX, startY;
        // 对角线
        for (startX = BOARD_SIZE - 1, startY = 0; startX >= 0; startX--) {
            line = new ArrayList<>();
            for (int x = startX, y = startY; x < BOARD_SIZE && y < BOARD_SIZE; x++, y++) {
                line.add(new Coord(x, y));
            }
            if (line.size() >= 5) {
                lines.add(line);
            }
        }
        for (startX = 0, startY = 1; startY < BOARD_SIZE; startY++) {
            line = new ArrayList<>();
            for (int x = startX, y = startY; x < BOARD_SIZE && y < BOARD_SIZE; x++, y++) {
                line.add(new Coord(x, y));
            }
            if (line.size() >= 5) {
                lines.add(line);
            }
        }
        // 反对角线
        for (startX = BOARD_SIZE - 1, startY = BOARD_SIZE - 1; startX >= 0; startX--) {
            line = new ArrayList<>();
            for (int x = startX, y = startY; x < BOARD_SIZE && y >= 0; x++, y--) {
                line.add(new Coord(x, y));
            }
            if (line.size() >= 5) {
                lines.add(line);
            }
        }
        for (startX = 0, startY = BOARD_SIZE - 2; startY >= 0; startY--) {
            line = new ArrayList<>();
            for (int x = startX, y = startY; x < BOARD_SIZE && y >= 0; x++, y--) {
                line.add(new Coord(x, y));
            }
            if (line.size() >= 5) {
                lines.add(line);
            }
        }
        return lines;
    }

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
        return ScoreCalculator.scoreLine(line);
    }
}