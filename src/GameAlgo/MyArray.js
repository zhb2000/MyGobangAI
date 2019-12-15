let array = {
    /**
     * 创建多维数组并初始化为0
     * 
     * @param {Number} l1 第1维长度
     * @param {Number} l2 第2维长度
     * @param {Number} l3 第3维长度
     * 
     * @returns {Number[]|Number[][]|Number[][][]} 多维数组
     */
    create: function (l1, l2, l3) {
        if (arguments.length === 1) {
            let arr = new Array(l1);
            arr.fill(0, 0, l1);
            return arr;
        } else if (arguments.length === 2) {
            let arr = new Array(l1);
            for (let i = 0; i < l1; i++) {
                arr[i] = this.create(l2);
            }
            return arr;
        } else {
            let arr = new Array(l1);
            for (let i = 0; i < l1; i++) {
                arr[i] = this.create(l2, l3);
            }
            return arr;
        }
    }
}
export default array;