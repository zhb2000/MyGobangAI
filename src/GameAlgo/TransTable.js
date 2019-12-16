//import TableCell from "./TableCell";

const TABLE_SIZE = 1 << 21;
let cells = new Array(TABLE_SIZE);
cells.fill(null, 0, TABLE_SIZE);
//console.log("transtable size:" + TABLE_SIZE);

/**
 * 获取对应的置换表表项
 * @param {Number} hashCode 棋盘哈希值
 * @returns {TableCell} 置换表表项
 */
function getCell(hashCode) {
    let index = ((hashCode % TABLE_SIZE) + TABLE_SIZE) % TABLE_SIZE;
    return cells[index];
}

/**
 * 新建对应的置换表表项
 * @param {Number} hashCode 棋盘哈希值
 * @param {TableCell} cell 新建的置换表表项
 */
function setCell(hashCode, cell) {
    let index = ((hashCode % TABLE_SIZE) + TABLE_SIZE) % TABLE_SIZE;
    cells[index] = cell;
}

export { getCell, setCell };