<template>
  <div class="outer">
    <div class="inner">
      <img class="bg_img" src="../assets/board.jpg" />
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
                    white_chess: uiBoard[cell.id].chess === 2,
                    black_chess: uiBoard[cell.id].chess === 1,
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
export default {
  name: "GameBoard",
  data: function() {
    return {
      /**带有编号的二维数组 */
      cellArray: this.createArray()
    };
  },
  props: {
    /**控制UI界面的二维数组 */
    uiBoard: Array,
    justChess: {
      type: Number,
      default: -1
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
      this.$emit("cell-click", x, y);
    }
  }
};
</script>

<style scoped>
.outer {
  /*width: 100%;*/
  padding-bottom: 100%;
  position: relative;
}
.inner {
  display: block;
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
}
.bg_img {
  position: absolute;
  width: 100%;
  height: 100%;
  z-index: 1;
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
  background-color: lightblue;
  border: 1px solid gray;
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
}
.chess_number {
  font-size: 12px;
  text-align: center;
}
.white_chess {
  background-color: white;
  color: black;
}
.black_chess {
  background-color: black;
  color: white;
}
.no_chess {
  background-color: rgba(255, 255, 255, 0);
}
.just_chess {
  box-shadow: 0px 0px 10px 2px lightcoral;
}
</style>