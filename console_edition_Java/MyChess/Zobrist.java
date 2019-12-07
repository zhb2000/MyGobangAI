package MyChess;

import static MyChess.Config.BOARD_SIZE;

import java.util.Random;

import static MyChess.ChessType.*;

/**
 * Zobrist
 */
public class Zobrist {

    private long hum[][];
    private long com[][];
    private long zobristCode;
    private Random rnd;

    public Zobrist() {
        rnd = new Random();
        zobristCode = rnd.nextLong();
        hum = new long[BOARD_SIZE][BOARD_SIZE];
        com = new long[BOARD_SIZE][BOARD_SIZE];
        for (int x = 0; x < BOARD_SIZE; x++) {
            for (int y = 0; y < BOARD_SIZE; y++) {
                hum[x][y] = rnd.nextLong();
                com[x][y] = rnd.nextLong();
            }
        }
    }

    /**
     * 落子或撤销后更新哈希值
     * 
     * @param x      行号
     * @param y      列号
     * @param doType 棋子类型，只能是HUM_CHESS或COM_CHESS
     */
    public void goUpdate(int x, int y, int doType) {
        if (doType == HUM_CHESS) {
            zobristCode ^= hum[x][y];
        } else {
            zobristCode ^= com[x][y];
        }
    }

    /**
     * 获取棋盘的哈希值
     * 
     * @return 棋盘哈希值
     */
    public long code() {
        return zobristCode;
    }
}