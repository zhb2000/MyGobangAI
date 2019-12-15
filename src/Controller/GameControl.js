import ChessBoard from "../GameAlgo/ChessBoard.js";
import getBestPut from "../GameAlgo/SearchAlgo.js";
import { HUM_CHESS, COM_CHESS } from "../GameAlgo/ChessType.js";

const board = new ChessBoard();
const UNKNOWN_WIN = 0, HUM_WIN = 1, COM_WIN = 2;
let winStatus = UNKNOWN_WIN;

function humanPlay(x, y) {
    board.put(x, y, HUM_CHESS);
    console.log("人类落子" + " (" + x + ", " + y + ")");
    //console.log(board.toString());
    //console.log(board.heuristicToString(COM_CHESS));
    //console.log(board.heuristicToString(HUM_CHESS));
    //console.log(board.neighborToString());
    //console.log(board.closeToString(HUM_CHESS));
}
function getComputerPlay() {
    let coord = getBestPut(board);
    board.put(coord.x, coord.y, COM_CHESS);
    console.log("电脑落子" + " (" + coord.x + ", " + coord.y + ")");
    //console.log(board.toString());
    //console.log(board.heuristicToString(COM_CHESS));
    //console.log(board.heuristicToString(HUM_CHESS));
    //console.log(board.neighborToString());
    //console.log(board.closeToString(COM_CHESS));
    return { x: coord.x, y: coord.y };
}

export {
    getComputerPlay, humanPlay,
    winStatus,
    UNKNOWN_WIN, HUM_WIN, COM_WIN
};