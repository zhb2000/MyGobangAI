<template>
  <div class="outer">
    <transition name="fade">
      <div class="msg_mask" v-show="msgMaskOn">
        <div>{{message}}</div>
      </div>
    </transition>
    <transition name="fade">
      <div class="mask" v-show="maskOn">
        <div class="text_box">
          <div class="calcu">正在计算……</div>
          <div class="limit">时限30秒</div>
        </div>
      </div>
    </transition>
    <div class="inner">
      <div class="img_box">
        <img class="bg_img" src="../assets/board.jpg" />
      </div>
      <div class="board_grid">
        <div
          v-for="cell in cellArray"
          :key="cell.id"
          class="cell"
          @click="handleClick(cell.x, cell.y)"
        >
          <div
            class="chess"
            :class="{empty_chess: uiBoard[cell.id].chess === 0,
                    black_chess: uiBoard[cell.id].chess === firstChess,
                    white_chess: uiBoard[cell.id].chess === secondChess,
                    just_chess: cell.id === justChess}"
          >
            <div class="chess_number">
              {{uiBoard[cell.id].number != -1
              ?uiBoard[cell.id].number
              :""}}
            </div>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script>
import { COM_CHESS, HUM_CHESS } from "../GameAlgo/ChessType.js";

export default {
  name: "GameBoard",
  data: function() {
    return {
      /**带有编号的二维数组 */
      cellArray: this.createArray(),
      msgMaskOn: false,
      message: "no message"
    };
  },
  computed: {
    secondChess() {
      return this.firstChess === COM_CHESS ? HUM_CHESS : COM_CHESS;
    }
  },
  props: {
    /**控制UI界面的二维数组 */
    uiBoard: Array,
    justChess: {
      type: Number,
      default: -1
    },
    maskOn: {
      type: Boolean,
      default: true
    },
    playing: {
      type: Boolean,
      default: false
    },
    firstChess: {
      type: Number,
      default: HUM_CHESS
    }
  },
  methods: {
    /**生成带有编号的二维数组 */
    createArray() {
      let array = [];
      for (let x = 0; x < 15; x++) {
        for (let y = 0; y < 15; y++) {
          array.push({ x: x, y: y, id: x * 15 + y });
        }
      }
      return array;
    },
    /**抛出`cell-click`事件 */
    handleClick(x, y) {
      if (this.playing) {
        this.$emit("cell-click", x, y);
      }
    },
    showMsg(msgString) {
      this.message = msgString;
      this.msgMaskOn = true;
      setTimeout(() => {
        this.msgMaskOn = false;
      }, 2000);
    }
  }
};
</script>

<style scoped>
.outer {
  padding-bottom: 100%;
  position: relative;
  overflow: hidden;
  border-radius: 10px;
  box-shadow: 0px 2px 20px 3px rgba(68, 84, 106, 0.227);
}
.mask {
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  z-index: 10;
  background-color: rgba(0, 0, 0, 0.4);
  display: grid;
  grid-template-columns: 100%;
  justify-items: center;
  align-items: center;
}
.msg_mask {
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  z-index: 20;
  background-color: rgba(0, 0, 0, 0.4);
  display: grid;
  grid-template-columns: 100%;
  justify-items: center;
  align-items: center;
  color: white;
  font-size: 40px;
  font-weight: bold;
}
.fade-enter-active,
.fade-leave-active {
  transition: opacity 0.3s;
}
.fade-enter,
.fade-leave-to {
  opacity: 0;
}
.text_box {
  color: white;
  display: grid;
  grid-template-columns: 100%;
  justify-items: center;
  align-items: center;
}
.calcu {
  font-size: 30px;
  font-weight: bold;
  text-align: center;
  padding: 10px;
}
.limit {
  font-size: 20px;
  text-align: center;
  padding: 5px;
}
.inner {
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  z-index: 5;
}
.img_box {
  position: absolute;
  width: 100%;
  height: 100%;
  z-index: 1;
  overflow: hidden;
}
.bg_img {
  position: relative;
  width: 102%;
  height: 102%;
  top: -1%;
  left: -1%;
  right: -1%;
  bottom: -1%;
}
.board_grid {
  display: grid;
  grid-template-columns: repeat(15, 1fr);
  grid-template-rows: repeat(15, 1fr);
  position: absolute;
  width: 100%;
  height: 100%;
  z-index: 10;
}
.cell {
  /* background-color: lightblue; */
  background-color: rgba(255, 255, 255, 0);
  /* border: 1px solid gray; */
  padding: 10%;
}
.chess {
  width: 100%;
  height: 100%;
  border-radius: 50%;
  display: grid;
  grid-template-columns: 100%;
  align-items: center;
  justify-items: center;
  cursor: pointer;
  overflow: hidden;
}
.chess_number {
  font-size: 12px;
  text-align: center;
  overflow: hidden;
}
.white_chess {
  background-color: white;
  color: black;
  border: 1px solid black;
}
.black_chess {
  background-color: black;
  color: white;
  border: 1px solid white;
}
.no_chess {
  background-color: rgba(255, 255, 255, 0);
}
.just_chess {
  box-shadow: 0px 0px 10px 2px lightcoral;
}
</style>