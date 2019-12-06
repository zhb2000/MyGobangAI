package MyChess;

/**
 * 坐标类
 */
public class Coord {

    /** 行号 */
    public int x;
    /** 列号 */
    public int y;

    public Coord() {
        x = 0;
        y = 0;
    }

    /**
     * @param x 行号
     * @param y 列号
     */
    public Coord(int x, int y) {
        this.x = x;
        this.y = y;
    }

    @Override
    public String toString() {
        return "(" + x + ", " + y + ")";
    }
}