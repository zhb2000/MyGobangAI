package MyChess;

import static MyChess.StandardType.*;
import MyChess.Score;

import java.util.List;

/**
 * ScoreCalculator2
 */
public class ScoreCalculator {
    private static int selfCnt[] = new int[11];
    private static int emptyCnt[] = new int[11];
    private static int blockedCnt[] = new int[11];

    /**
     * 计算长度为9+2的单条标准直线的得分
     * 
     * @param line 标准的直线，长度为9+2
     * 
     * @return 这条直线的得分
     */
    public static int scoreLine(List<Integer> line) {
        calcuArray(line);

        if (Config.CALCU_TYPE == 1) {
            int sum = 0;
            int fiveScore = fiveNum(line) * Score.FIVE;
            sum += fiveScore;
            if (fiveScore > 0) {
                return sum;
            }
            int aliveFourScore = aliveFourNum(line) * Score.ALIVE_FOUR;
            sum += aliveFourScore;
            if (aliveFourScore == 0) {
                sum += blockedFourNum(line) * Score.BLOCKED_FOUR;
            }
            int aliveThreeScore = aliveThreeNum(line) * Score.ALIVE_THREE;
            sum += aliveThreeScore;
            if (aliveThreeScore == 0) {
                sum += blockedThreeNum(line) * Score.BLOCKED_THREE;
            }
            int aliveTwoScore = aliveTwoNum(line) * Score.ALIVE_TWO;
            sum += aliveTwoScore;
            if (aliveTwoScore == 0) {
                sum += blockedTwoNum(line) * Score.BLOCKED_TWO;
            }
            return sum;
        } else {
            if (hasFive(line)) {
                return Score.FIVE;
            } else if (hasAliveFour(line)) {
                return Score.ALIVE_FOUR;
            } else if (hasBlockedFour(line)) {
                return Score.BLOCKED_FOUR;
            } else if (hasAliveThree(line)) {
                return Score.ALIVE_THREE;
            } else if (hasBlockedThree(line)) {
                return Score.BLOCKED_THREE;
            } else if (hasAliveTwo(line)) {
                return Score.ALIVE_TWO;
            } else if (hasBlockedTwo(line)) {
                return Score.BLOCKED_TWO;
            } else {
                return 0;
            }
        }
    }

    /**
     * 计算出现次数的前缀和
     * 
     * @param line 标准化直线
     */
    private static void calcuArray(List<Integer> line) {
        selfCnt[0] = emptyCnt[0] = blockedCnt[0] = 0;
        if (line.get(0) == SELF) {
            selfCnt[0] = 1;
        } else if (line.get(0) == EMPTY) {
            emptyCnt[0] = 1;
        } else {
            blockedCnt[0] = 1;
        }
        for (int i = 1; i < line.size(); i++) {
            if (line.get(i) == SELF) {
                selfCnt[i] = selfCnt[i - 1] + 1;
                emptyCnt[i] = emptyCnt[i - 1];
                blockedCnt[i] = blockedCnt[i - 1];
            } else if (line.get(i) == EMPTY) {
                emptyCnt[i] = emptyCnt[i - 1] + 1;
                selfCnt[i] = selfCnt[i - 1];
                blockedCnt[i] = blockedCnt[i - 1];
            } else {
                blockedCnt[i] = blockedCnt[i - 1] + 1;
                selfCnt[i] = selfCnt[i - 1];
                emptyCnt[i] = emptyCnt[i - 1];
            }
        }
    }

    /**
     * 闭区间[left, right]内type的出现次数
     * 
     * @param left  区间左端点
     * @param right 区间右端点
     * @param type  标准棋子类型
     * @return 出现次数
     */
    private static int cnt(int left, int right, int type) {
        int[] cntArray;
        if (type == SELF) {
            cntArray = selfCnt;
        } else if (type == EMPTY) {
            cntArray = emptyCnt;
        } else {
            cntArray = blockedCnt;
        }
        if (left == 0) {
            return cntArray[right];
        } else {
            return cntArray[right] - cntArray[left - 1];
        }
    }

    /** 是否有连五出现 */
    private static boolean hasFive(List<Integer> line) {
        // 5个一组，5个1
        int left, right;
        for (left = 0; left + 5 - 1 < line.size(); left++) {
            right = left + 5 - 1;
            if (cnt(left, right, SELF) == 5) {
                return true;
            }
        }
        return false;
    }

    private static int fiveNum(List<Integer> line) {
        int num = 0;
        int left, right;
        for (left = 0; left + 5 - 1 < line.size(); left++) {
            right = left + 5 - 1;
            if (cnt(left, right, SELF) == 5) {
                num++;
            }
        }
        return num;
    }

    /** 是否有活四出现 */
    private static boolean hasAliveFour(List<Integer> line) {
        // 6个一组，两头0，4个1，2个0
        int left, right;
        for (left = 0; left + 6 - 1 < line.size(); left++) {
            right = left + 6 - 1;
            if (line.get(left) == 0 && line.get(right) == 0 && cnt(left, right, SELF) == 4
                    && cnt(left, right, EMPTY) == 2) {
                return true;
            }
        }
        return false;
    }

    private static int aliveFourNum(List<Integer> line) {
        int num = 0;
        // 6个一组，两头0，4个1，2个0
        int left, right;
        for (left = 0; left + 6 - 1 < line.size(); left++) {
            right = left + 6 - 1;
            if (line.get(left) == 0 && line.get(right) == 0 && cnt(left, right, SELF) == 4
                    && cnt(left, right, EMPTY) == 2) {
                num++;
            }
        }
        return num;
    }

    /** 是否有死四出现 */
    private static boolean hasBlockedFour(List<Integer> line) {
        // 5个一组，4个1，1个0
        int left, right;
        for (left = 0; left + 5 - 1 < line.size(); left++) {
            right = left + 5 - 1;
            if (cnt(left, right, SELF) == 4 && cnt(left, right, EMPTY) == 1) {
                return true;
            }
        }
        return false;
    }

    private static int blockedFourNum(List<Integer> line) {
        int num = 0;
        // 5个一组，4个1，1个0
        int left, right;
        for (left = 0; left + 5 - 1 < line.size(); left++) {
            right = left + 5 - 1;
            if (cnt(left, right, SELF) == 4 && cnt(left, right, EMPTY) == 1) {
                num++;
            }
        }
        return num;
    }

    /** 是否有活三出现 */
    private static boolean hasAliveThree(List<Integer> line) {
        // 6个一组，两头0，3个1，3个0
        int left, right;
        for (left = 0; left + 6 - 1 < line.size(); left++) {
            right = left + 6 - 1;
            if (line.get(left) == 0 && line.get(right) == 0 && cnt(left, right, SELF) == 3
                    && cnt(left, right, EMPTY) == 3) {
                return true;
            }
        }
        return false;
    }

    private static int aliveThreeNum(List<Integer> line) {
        int num = 0;
        // 6个一组，两头0，3个1，3个0
        int left, right;
        for (left = 0; left + 6 - 1 < line.size(); left++) {
            right = left + 6 - 1;
            if (line.get(left) == 0 && line.get(right) == 0 && cnt(left, right, SELF) == 3
                    && cnt(left, right, EMPTY) == 3) {
                num++;
            }
        }
        return num;
    }

    /** 是否有死三出现 */
    private static boolean hasBlockedThree(List<Integer> line) {
        // 5个1组，3个1，2个0
        int left, right;
        for (left = 0; left + 5 - 1 < line.size(); left++) {
            right = left + 5 - 1;
            if (cnt(left, right, SELF) == 3 && cnt(left, right, EMPTY) == 2) {
                return true;
            }
        }
        return false;
    }

    private static int blockedThreeNum(List<Integer> line) {
        int num = 0;
        // 5个1组，3个1，2个0
        int left, right;
        for (left = 0; left + 5 - 1 < line.size(); left++) {
            right = left + 5 - 1;
            if (cnt(left, right, SELF) == 3 && cnt(left, right, EMPTY) == 2) {
                num++;
            }
        }
        return num;
    }

    /** 是否有活二出现 */
    private static boolean hasAliveTwo(List<Integer> line) {
        // 6个一组，两头0，2个1，4个0
        int left, right;
        for (left = 0; left + 6 - 1 < line.size(); left++) {
            right = left + 6 - 1;
            if (line.get(left) == 0 && line.get(right) == 0 && cnt(left, right, SELF) == 2
                    && cnt(left, right, EMPTY) == 4) {
                return true;
            }
        }
        return false;
    }

    private static int aliveTwoNum(List<Integer> line) {
        int num = 0;
        // 6个一组，两头0，2个1，4个0
        int left, right;
        for (left = 0; left + 6 - 1 < line.size(); left++) {
            right = left + 6 - 1;
            if (line.get(left) == 0 && line.get(right) == 0 && cnt(left, right, SELF) == 2
                    && cnt(left, right, EMPTY) == 4) {
                num++;
            }
        }
        return num;
    }

    /** 是否有死二出现 */
    private static boolean hasBlockedTwo(List<Integer> line) {
        // 5个一组，2个1，3个0
        int left, right;
        for (left = 0; left + 5 - 1 < line.size(); left++) {
            right = left + 5 - 1;
            if (cnt(left, right, SELF) == 2 && cnt(left, right, EMPTY) == 3) {
                return true;
            }
        }
        return false;
    }

    private static int blockedTwoNum(List<Integer> line) {
        int num = 0;
        // 5个一组，2个1，3个0
        int left, right;
        for (left = 0; left + 5 - 1 < line.size(); left++) {
            right = left + 5 - 1;
            if (cnt(left, right, SELF) == 2 && cnt(left, right, EMPTY) == 3) {
                num++;
            }
        }
        return num;
    }

}