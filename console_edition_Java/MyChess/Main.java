package MyChess;

import static MyChess.ChessType.*;
import java.util.Scanner;

/**
 * Main
 */
public class Main {

    final static int UNKNOWN_WIN = 0;
    final static int COM_WIN = 1;
    final static int HUM_WIN = 2;
    final static int COM_FIRST = 0;
    final static int HUM_FIRST = 1;

    public static void main(String[] args) {
        ChessBoard board = new ChessBoard();
        int winStatus = UNKNOWN_WIN;
        int whoFirst;
        System.out.println("你先走则输入1，电脑先走则输入0\nonly 1 is accepted");
        Scanner cin = new Scanner(System.in);
        whoFirst = cin.nextInt();
        if (whoFirst == HUM_FIRST) {
            System.out.println(board);
        }
        if (whoFirst == HUM_FIRST) {
            while (true) {
                int humX, humY;
                int comX = -1, comY = -1;
                if (board.isFull()) {
                    break;
                }
                if (comX != -1 && comY != -1 && board.isWin(comX, comY)) {
                    winStatus = COM_WIN;
                    break;
                }
                Coord humCoord = humInputAndPut(board, cin);
                humX = humCoord.x;
                humY = humCoord.y;
                System.out.println("你的落子(" + humX + "," + humY + ")");
                System.out.println(board);

                if (board.isFull()) {
                    break;
                }
                if (board.isWin(humX, humY)) {
                    winStatus = HUM_WIN;
                    break;
                }
                Coord comCoord = SearchAlgo.getBestPut(board);
                comX = comCoord.x;
                comY = comCoord.y;
                board.put(comX, comY, COM_CHESS);
                System.out.println("电脑落子(" + comX + "," + comY + ")");
                System.out.println(board);
            }
        }
        if (winStatus == COM_WIN) {
            System.out.println("电脑赢了");
        } else if (winStatus == HUM_WIN) {
            System.out.println("你赢了");
        } else {
            System.out.println("平局");
        }
        cin.close();
    }

    /**
     * 获取坐标输入，并在棋盘上放棋子
     */
    private static Coord humInputAndPut(ChessBoard board, Scanner cin) {
        Coord coord = new Coord();
        // Scanner cin = new Scanner(System.in);
        while (true) {
            System.out.println("输入 x 和 y");
            int x, y;
            x = cin.nextInt();
            y = cin.nextInt();
            if (board.put(x, y, HUM_CHESS)) {
                coord.x = x;
                coord.y = y;
                break;
            } else {
                System.out.println("非法的位置！请再次输入！！");
            }
        }
        // cin.close();
        return coord;
    }
}