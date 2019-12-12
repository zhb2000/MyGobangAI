export default {
    /**
     * 创建多维数组并初始化为0
     * 
     * @param {Number} l1 第1维长度
     * @param {Number} l2 第2维长度
     * @param {Number} l3 第3维长度
     * @param {Number} l4 第4维长度
     * 
     * @returns {Number[]|Number[][]|Number[][][]|Number[][][][]} 多维数组
     */
    create: function (l1, l2, l3, l4) {
        if (arguments.length === 1) {
            let arr = [];
            for (let i = 0; i < l1; i++) {
                arr.push(0);
            }
            return arr;
        } else if (arguments.length === 2) {
            let arr = [];
            for (let i = 0; i < l1; i++) {
                arr.push(this.create(l2));
            }
            return arr;
        } else if (arguments.length === 3) {
            let arr = [];
            for (let i = 0; i < l1; i++) {
                arr.push(this.create(l2, l3));
            }
            return arr;
        } else {
            let arr = [];
            for (let i = 0; i < l1; i++) {
                arr.push(this.create(l2, l3, l4));
            }
            return arr;
        }

    }
}