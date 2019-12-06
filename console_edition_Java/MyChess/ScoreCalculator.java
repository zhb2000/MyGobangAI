package MyChess;

import static MyChess.StandardType.*;

import java.util.List;

import MyChess.Score;

/**
 * 根据棋形计算横线的分数 横线是经过标准化之后的
 */
public class ScoreCalculator {

    /** 连五棋形 */
    final private static int[][] fives = { { SELF, SELF, SELF, SELF, SELF } };
    /** 活四棋形 */
    final private static int[][] aliveFours = { { EMPTY, SELF, SELF, SELF, SELF, EMPTY } };
    /** 活三棋形 */
    final private static int[][] aliveThrees = { { EMPTY, SELF, SELF, SELF, EMPTY, EMPTY },
            { EMPTY, EMPTY, SELF, SELF, SELF, EMPTY }, { EMPTY, SELF, EMPTY, SELF, SELF, EMPTY },
            { EMPTY, SELF, SELF, EMPTY, SELF, EMPTY } };
    /** 活二棋形 */
    final private static int[][] aliveTwos = { { EMPTY, SELF, SELF, EMPTY, EMPTY, EMPTY },
            { EMPTY, EMPTY, SELF, SELF, EMPTY, EMPTY }, { EMPTY, EMPTY, EMPTY, SELF, SELF, EMPTY },
            { EMPTY, SELF, EMPTY, SELF, EMPTY, EMPTY }, { EMPTY, EMPTY, SELF, EMPTY, SELF, EMPTY },
            { EMPTY, SELF, EMPTY, EMPTY, SELF, EMPTY } };
    /** 死四棋形 */
    final private static int[][] blockedFours = { { EMPTY, SELF, SELF, SELF, SELF, BLOCKED },
            { BLOCKED, SELF, SELF, SELF, SELF, EMPTY }, { SELF, SELF, SELF, EMPTY, SELF },
            { SELF, EMPTY, SELF, SELF, SELF }, { SELF, SELF, EMPTY, SELF, SELF } };
    /** 死三棋形 */
    final private static int[][] blockedThrees = { { SELF, EMPTY, SELF, EMPTY, SELF },
            { SELF, SELF, EMPTY, EMPTY, SELF }, { BLOCKED, SELF, SELF, SELF, EMPTY, EMPTY },
            { EMPTY, EMPTY, SELF, SELF, SELF, BLOCKED }, { BLOCKED, SELF, SELF, EMPTY, SELF, EMPTY },
            { EMPTY, SELF, EMPTY, SELF, SELF, BLOCKED } };

    /**
     * 计算单条直线的得分
     * 
     * @param line 标准化后的单条直线
     * 
     * @return 这条直线的得分
     */
    public static int scoreLine(List<Integer> line) {
        int result = 0;
        result += scoreFive(line);// 连五
        result += scoreAliveFour(line);// 活四
        result += scoreBlockedFour(line);// 死四
        result += scoreAliveThree(line);// 活三
        result += scoreBlockedThrees(line);// 死三
        result += scoreAliveTwo(line);// 活二
        return result;
    }

    /**
     * 计算若干条直线的得分之和
     * 
     * @param lines 标准化后的若干直线
     * 
     * @return 这些直线的得分之和
     */
    public static int scoreLines(List<List<Integer>> lines) {
        int result = 0;
        for (List<Integer> line : lines) {
            result += scoreLine(line);
        }
        return result;
    }

    /** 对连五棋形打分的分数 */
    public static int scoreFive(List<Integer> line) {
        int result = 0;
        for (int i = 0; i < fives.length; i++) {
            result += occurredTimes(line, fives[i]) * Score.FIVE;
        }
        // debug
        // if (result > 0) {
        // System.out.println("连五个数 " + result);
        // }
        return result;
    }

    /** 对活四棋形打分的分数 */
    private static int scoreAliveFour(List<Integer> line) {
        int result = 0;
        for (int i = 0; i < aliveFours.length; i++) {
            result += occurredTimes(line, aliveFours[i]) * Score.ALIVE_FOUR;
        }
        return result;
    }

    /** 对死四棋形打分的分数 */
    private static int scoreBlockedFour(List<Integer> line) {
        int result = 0;
        for (int i = 0; i < blockedFours.length; i++) {
            result += occurredTimes(line, blockedFours[i]) * Score.BLOCKED_FOUR;
        }
        return result;
    }

    /** 对活三棋形打分的分数 */
    private static int scoreAliveThree(List<Integer> line) {
        int result = 0;
        for (int i = 0; i < aliveThrees.length; i++) {
            result += occurredTimes(line, aliveThrees[i]) * Score.ALIVE_THREE;
        }
        return result;
    }

    /** 对死三棋形打分的分数 */
    private static int scoreBlockedThrees(List<Integer> line) {
        int result = 0;
        for (int i = 0; i < blockedThrees.length; i++) {
            result += occurredTimes(line, blockedThrees[i]) * Score.BLOCKED_THREE;
        }
        return result;
    }

    /** 对活二棋形打分的分数 */
    private static int scoreAliveTwo(List<Integer> line) {
        int result = 0;
        for (int i = 0; i < aliveTwos.length; i++) {
            result += occurredTimes(line, aliveTwos[i]) * Score.ALIVE_TWO;
        }
        return result;
    }

    /**
     * 用KMP算法统计模式串出现的次数，可以部分重叠
     * 
     * @param text    文本串
     * @param pattern 模式串
     * 
     * @return 模式串出现次数
     */
    private static int occurredTimes(List<Integer> text, int[] pattern) {
        int cnt = 0;
        int[] pnext = getNextVal(pattern);
        int tlen = text.size();
        int plen = pattern.length;
        if (tlen < plen) {
            return 0;
        }
        int i = 0;
        int j = 0;
        while (true) {
            while (i < tlen && j < plen) {
                if (j == -1 || text.get(i) == pattern[j]) {
                    i++;
                    j++;
                } else {
                    j = pnext[j];
                }
            }
            if (j == plen) {
                cnt++;
                j = 0;
                // TODO 重叠匹配
                // i = i - plen + 1;
                // 可以部分重叠
                continue;
            } else {
                // i == tlen
                break;
            }
        }
        return cnt;
    }

    /**
     * 计算模式串的nextval数组
     * 
     * @param pattern 模式串
     * 
     * @return nextval数组
     */
    private static int[] getNextVal(int[] pattern) {
        int len = pattern.length;
        int[] next = new int[len];
        next[0] = -1;
        int k = -1;
        int j = 0;
        while (j + 1 < len) {
            if (k == -1 || pattern[k] == pattern[j]) {
                k++;
                j++;
                if (pattern[j] != pattern[k]) {
                    next[j] = k;
                } else {
                    next[j] = next[k];
                }
            } else {
                k = next[k];
            }
        }
        return next;
    }
}
