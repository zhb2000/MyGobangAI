<template>
  <div id="app">
    <grid>
      <template #board>
        <game-boardObj
          ref="board"
          :ui-board="uiBoard"
          :just-chess="justChess"
          :mask-on="maskOn"
          :playing="playing"
          :first-chess="firstChess"
          @cell-click="handleClick"
        />
      </template>
      <template #card>
        <info-card ref="infoCard" v-show="playing" />
        <setting-card @game-start="handleStart" v-show="!playing" />
      </template>
    </grid>
  </div>
</template>

<script>
import GameBoard from "./components/GameBoard.vue";
import MyGrid from "./components/MyGrid.vue";
import InfoCard from "./components/InfoCard.vue";
import SettingCard from "./components/SettingCard.vue";

import { EMPTY_CHESS, COM_CHESS, HUM_CHESS } from "./GameAlgo/ChessType.js";
import {
  humanPlay,
  getComputerPlay,
  winStatus,
  HUM_WIN,
  COM_WIN,
  DRAW,
  isEnd,
  controlInit
} from "./Controller/GameControl.js";

export default {
  name: "app",
  components: {
    "game-boardObj": GameBoard,
    "info-card": InfoCard,
    "setting-card": SettingCard,
    grid: MyGrid
  },
  data: function() {
    return {
      /**控制UI界面的二维数组 */
      uiBoard: new Array(15 * 15),
      /**状态字符串 */
      StatusInfo: "no status info",
      /**棋盘上棋子的总数 */
      chessNumber: 0,
      /**刚下的棋子的坐标 */
      justChess: -1,
      maskOn: false,
      playing: false,
      firstChess: COM_CHESS
    };
  },
  methods: {
    init() {
      for (let i = 0; i < 15 * 15; i++) {
        let emptyObj = { chess: EMPTY_CHESS, number: -1 };
        this.uiBoard[i] = emptyObj;
        this.$set(this.uiBoard, i, emptyObj);
      }
      this.chessNumber = 0;
      this.justChess = -1;
      this.maskOn = false;
    },
    handleStart(firstChess) {
      this.init();
      controlInit();

      this.$refs.board.showMsg("开始");
      this.firstChess = firstChess;
      console.log(this.firstChess);
      this.playing = true;
      if (this.firstChess === COM_CHESS) {
        setTimeout(() => {
          this.handleComputerPlay();
        }, 2601);
      }
    },
    handleComputerPlay() {
      let c = getComputerPlay();
      let index = c.x * 15 + c.y;
      let comObj = { chess: COM_CHESS, number: ++this.chessNumber };
      this.uiBoard[index] = comObj;
      this.$set(this.uiBoard, index, comObj);
      this.justChess = index;
      this.maskOn = false;
      this.$refs.infoCard.update();
      if (isEnd) {
        this.playing = false;
        if (winStatus === COM_WIN) {
          this.$refs.board.showMsg("电脑赢了");
        } else if (winStatus === DRAW) {
          this.$refs.board.showMsg("平局");
        }
      }
    },
    handleClick(x, y) {
      let index = x * 15 + y;
      if (this.playing && this.uiBoard[index].chess === EMPTY_CHESS) {
        this.maskOn = true;
        let humObj = { chess: HUM_CHESS, number: ++this.chessNumber };
        this.uiBoard[index].chess = humObj;
        this.$set(this.uiBoard, index, humObj);
        this.justChess = index;
        setTimeout(() => {
          humanPlay(x, y);
          if (isEnd) {
            this.playing = false;
            this.maskOn = false;
            if (winStatus === HUM_WIN) {
              this.$refs.board.showMsg("你赢了");
            } else if (winStatus === DRAW) {
              this.$refs.board.showMsg("平局");
            }
          } else {
            this.handleComputerPlay();
          }
        }, 305);
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
