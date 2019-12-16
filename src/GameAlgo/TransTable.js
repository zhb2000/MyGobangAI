import TableCell from "./TableCell";

const TABLE_SIZE = 1 << 19;
let cells = new Array(TABLE_SIZE);
for (let i = 0; i < TABLE_SIZE; i++) {
    cells[i] = new TableCell();
}
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

export default getCell;