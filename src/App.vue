<template>
  <div id="app">
    <grid style="width:90%">
      <template #board>
        <game-boardObj :ui-board="uiBoard" :just-chess="justChess" @cell-click="handleClick" />
      </template>
      <template #info>
        <info-card />
      </template>
      <template #setting>
        <info-card />
      </template>
    </grid>
  </div>
</template>

<script>
import GameBoard from "./components/GameBoard.vue";
import MyGrid from "./components/MyGrid.vue";
import InfoCard from "./components/InfoCard.vue";

import { EMPTY_CHESS, COM_CHESS, HUM_CHESS } from "./GameAlgo/ChessType.js";
import { humanPlay, getComputerPlay } from "./Controller/GameControl.js";

export default {
  name: "app",
  components: {
    "game-boardObj": GameBoard,
    "info-card": InfoCard,
    grid: MyGrid
  },
  data: function() {
    return {
      /**控制UI界面的二维数组 */
      uiBoard: [],
      /**状态字符串 */
      StatusInfo: "no status info",
      /**棋盘上棋子的总数 */
      chessNumber: 0,
      /**刚下的棋子的坐标 */
      justChess: -1
    };
  },
  methods: {
    init() {
      for (let i = 0; i < 15 * 15; i++) {
        this.uiBoard.push({
          chess: EMPTY_CHESS,
          number: -1
        });
      }
      this.chessNumber = 0;
      this.justChess = -1;
    },
    handleClick(x, y) {
      let index = x * 15 + y;
      if (this.uiBoard[index].chess === EMPTY_CHESS) {
        let humObj = { chess: HUM_CHESS, number: ++this.chessNumber };
        this.uiBoard[index].chess = humObj;
        this.$set(this.uiBoard, index, humObj);
        this.justChess = index;

        setTimeout(() => {
          humanPlay(x, y);
          let c = getComputerPlay();
          index = c.x * 15 + c.y;
          let comObj = { chess: COM_CHESS, number: ++this.chessNumber };
          this.uiBoard[index] = comObj;
          this.$set(this.uiBoard, index, comObj);
          this.justChess = index;
        }, 100);
      }
    }
  },
  created() {
    this.init();
  }
};
</script>

<style>
/**把网页的边界去掉 */
body {
  margin: 0 0;
  background-color: #ecf0f3;
  font-size: 16px;
}

/**改变盒子模型 */
* {
  box-sizing: border-box;
}

#app {
  font-family: sans-serif;
  -webkit-font-smoothing: antialiased;
  -moz-osx-font-smoothing: grayscale;
}
</style>
