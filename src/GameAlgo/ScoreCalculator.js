import array from "./MyArray.js";
import { EMPTY, SELF } from "./StandardType.js";
import Score from "./Score.js";

/**
 * 己方棋子个数前缀和
 * @type Number[]
 */
let selfCnt = array.create(11);
/**
 * 空格个数前缀和
 * @type Number[]
 */
let emptyCnt = array.create(11);
/**
 * 阻塞个数前缀和
 * @type Number[]
 */
let blockedCnt = array.create(11);
/**
 * 计算长度为9+2的单条标准直线的得分
 * 
 * @param {Number[]} line 标准的直线，长度为9+2
 * 
 * @returns {Number} 这条直线的得分
 */
function scoreLine(line) {
    calcuArray(line);

    let sum = 0;
    let fiveScore = fiveNum(line) * Score.FIVE;
    sum += fiveScore;
    if (fiveScore > 0) {
        return sum;
    }
    let aliveFourScore = aliveFourNum(line) * Score.ALIVE_FOUR;
    sum += aliveFourScore;
    if (aliveFourScore === 0) {
        sum += blockedFourNum(line) * Score.BLOCKED_FOUR;
    }
    let aliveThreeScore = aliveThreeNum(line) * Score.ALIVE_THREE;
    sum += aliveThreeScore;
    if (aliveThreeScore === 0) {
        sum += blockedThreeNum(line) * Score.BLOCKED_THREE;
    }
    let aliveTwoScore = aliveTwoNum(line) * Score.ALIVE_TWO;
    sum += aliveTwoScore;
    if (aliveTwoScore === 0) {
        sum += blockedTwoNum(line) * Score.BLOCKED_TWO;
    }
    return sum;
}

/**
 * 计算出现次数的前缀和
 * 
 * @param {Number[]} line 标准化直线
 */
function calcuArray(line) {
    selfCnt[0] = emptyCnt[0] = blockedCnt[0] = 0;
    if (line[0] === SELF) {
        selfCnt[0] = 1;
    } else if (line[0] === EMPTY) {
        emptyCnt[0] = 1;
    } else {
        blockedCnt[0] = 1;
    }
    for (let i = 1; i < line.length; i++) {
        if (line[i] === SELF) {
            selfCnt[i] = selfCnt[i - 1] + 1;
            emptyCnt[i] = emptyCnt[i - 1];
            blockedCnt[i] = blockedCnt[i - 1];
        } else if (line[i] === EMPTY) {
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
 * @param {Number} left 区间左端点
 * @param {Number} right 区间右端点
 * @param {Number} type 标准棋子类型
 * 
 * @returns {Number} 出现次数
 */
function cnt(left, right, type) {
    let cntArray;
    if (type === SELF) {
        cntArray = selfCnt;
    } else if (type === EMPTY) {
        cntArray = emptyCnt;
    } else {
        cntArray = blockedCnt;
    }
    if (left === 0) {
        return cntArray[right];
    } else {
        return cntArray[right] - cntArray[left - 1];
    }
}

/**
 * 计算连五的个数
 * @param {Number[]} line 标准化直线
 * @returns {Number} 连五的个数
 */
function fiveNum(line) {
    let num = 0;
    let left, right;
    for (left = 0; left + 5 - 1 < line.length; left++) {
        right = left + 5 - 1;
        if (cnt(left, right, SELF) === 5) {
            num++;
        }
    }
    return num;
}

/**
 * 计算活四的个数
 * @param {Number[]} line 标准化直线
 * @returns {Number} 活四的个数
 */
function aliveFourNum(line) {
    let num = 0;
    // 6个一组，两头0，4个1，2个0
    let left, right;
    for (left = 0; left + 6 - 1 < line.length; left++) {
        right = left + 6 - 1;
        if (line[left] === 0 && line[right] === 0 && cnt(left, right, SELF) === 4
            && cnt(left, right, EMPTY) === 2) {
            num++;
        }
    }
    return num;
}

/**
 * 计算死四的个数
 * @param {Number[]} line 标准化直线
 * @returns {Number} 死四的个数
 */
function blockedFourNum(line) {
    let num = 0;
    // 5个一组，4个1，1个0
    let left, right;
    for (left = 0; left + 5 - 1 < line.length; left++) {
        right = left + 5 - 1;
        if (cnt(left, right, SELF) === 4 && cnt(left, right, EMPTY) === 1) {
            num++;
        }
    }
    return num;
}

/**
 * 计算活三的个数
 * @param {Number[]} line 标准化直线
 * @returns {Number} 活三的个数
 */
function aliveThreeNum(line) {
    let num = 0;
    // 6个一组，两头0，3个1，3个0
    let left, right;
    for (left = 0; left + 6 - 1 < line.length; left++) {
        right = left + 6 - 1;
        if (line[left] === 0 && line[right] === 0 && cnt(left, right, SELF) === 3
            && cnt(left, right, EMPTY) === 3) {
            num++;
        }
    }
    return num;
}

/**
 * 计算死三的个数
 * @param {Number[]} line 标准化直线
 * @returns {Number} 死三的个数
 */
function blockedThreeNum(line) {
    let num = 0;
    // 5个一组，3个1，2个0
    let left, right;
    for (left = 0; left + 5 - 1 < line.length; left++) {
        right = left + 5 - 1;
        if (cnt(left, right, SELF) === 3 && cnt(left, right, EMPTY) === 2) {
            num++;
        }
    }
    return num;
}

/**
 * 计算活二的个数
 * @param {Number[]} line 标准化直线
 * @returns {Number} 活二的个数
 */
function aliveTwoNum(line) {
    let num = 0;
    // 6个一组，两头0，2个1，4个0
    let left, right;
    for (left = 0; left + 6 - 1 < line.length; left++) {
        right = left + 6 - 1;
        if (line[left] === 0 && line[right] === 0 && cnt(left, right, SELF) === 2
            && cnt(left, right, EMPTY) === 4) {
            num++;
        }
    }
    return num;
}

/**
 * 计算死二的个数
 * @param {Number[]} line 标准化直线
 * @returns {Number} 死二的个数
 */
function blockedTwoNum(line) {
    let num = 0;
    // 5个一组，2个1，3个0
    let left, right;
    for (left = 0; left + 5 - 1 < line.length; left++) {
        right = left + 5 - 1;
        if (cnt(left, right, SELF) === 2 && cnt(left, right, EMPTY) === 3) {
            num++;
        }
    }
    return num;
}

export default scoreLine;