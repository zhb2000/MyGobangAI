import TableCell from "./TableCell";

const TABLE_SIZE = 1 << 25;// 33,554,432
let cells = [];
for (let i = 0; i < TABLE_SIZE; i++) {
    cells[i] = new TableCell();
}
/**
 * 获取对应的置换表表项
 * @param {Number} hashCode 棋盘哈希值
 * @returns {TableCell} 置换表表项
 */
function getCell(hashCode) {
    let index = hashCode % TABLE_SIZE;
    return cells[index];
}

export default getCell;