import ChessBoard from "../GameAlgo/ChessBoard.js";
import getBestPut from "../GameAlgo/SearchAlgo.js";
import { HUM_CHESS, COM_CHESS } from "../GameAlgo/ChessType.js";

const board = new ChessBoard();
const UNKNOWN_WIN = 0, HUM_WIN = 1, COM_WIN = 2, DRAW = 3;
/**游戏结局状况 */
let winStatus = UNKNOWN_WIN;
/**游戏是否结束 */
let isEnd = false;

function controlInit() {
    winStatus = UNKNOWN_WIN;
    isEnd = false;
    board.reset();
}

function humanPlay(x, y) {
    board.put(x, y, HUM_CHESS);
    console.log("人类落子" + " (" + x + ", " + y + ")");
    if (board.isWin(x, y)) {
        isEnd = true;
        winStatus = HUM_WIN;
    } else if (board.isFull()) {
        isEnd = true;
        winStatus = DRAW;
    }
    //console.log(board.toString());
    console.log(board.heuristicToString(COM_CHESS));
    console.log(board.heuristicToString(HUM_CHESS));
    console.log(board.neighborToString());
    //console.log(board.closeToString(HUM_CHESS));
}
function getComputerPlay() {
    let coord = getBestPut(board);
    let x = coord.x, y = coord.y;
    board.put(x, y, COM_CHESS);
    console.log("电脑落子" + " (" + x + ", " + y + ")");
    if (board.isWin(x, y)) {
        isEnd = true;
        winStatus = COM_WIN;
    } else if (board.isFull()) {
        isEnd = true;
        winStatus = DRAW;
    }
    //console.log(board.toString());
    console.log(board.heuristicToString(COM_CHESS));
    console.log(board.heuristicToString(HUM_CHESS));
    console.log(board.neighborToString());
    //console.log(board.closeToString(COM_CHESS));
    return { x: coord.x, y: coord.y };
}

export {
    getComputerPlay, humanPlay,
    winStatus,
    UNKNOWN_WIN, HUM_WIN, COM_WIN, DRAW,
    isEnd,
    controlInit
};