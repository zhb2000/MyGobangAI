<template>
  <div class="card">
    <div class="row">
      <input type="radio" name="who_first" id="com_first" value="com_first" v-model="firstSide" />
      <label for="com_first">电脑先走</label>
    </div>
    <div class="row">
      <input type="radio" name="who_first" id="hum_first" value="hum_first" v-model="firstSide" />
      <label for="hum_first">你先走</label>
    </div>
    <div class="line"></div>
    <div class="row">
      <input type="checkbox" id="use_heuristic" v-model="useHeuristic" />
      <label for="use_heuristic">使用启发式搜索</label>
    </div>
    <div class="row">
      <input type="checkbox" id="use_transtable" v-model="useTransTable" />
      <label for="use_transtable">使用置换表</label>
    </div>
    <div style="width:100%">
    <button class="button" @click="startGame">开始</button>
    </div>
  </div>
</template>

<script>
import Config from "../GameAlgo/Config.js";
import { COM_CHESS, HUM_CHESS } from "../GameAlgo/ChessType.js";

export default {
  name: "SettingCard",
  data: function() {
    return {
      firstSide: "com_first",
      useHeuristic: true,
      useTransTable: true
    };
  },
  methods: {
    startGame() {
      Config.useHeuristic = this.useHeuristic;
      Config.useTransTable = this.useTransTable;
      let firstChess = this.firstSide === "com_first" ? COM_CHESS : HUM_CHESS;
      this.$emit("game-start", firstChess);
    }
  }
};
</script>

<style scoped>
.card {
  background-color: antiquewhite;
  box-shadow: 0px 2px 20px 3px rgba(68, 84, 106, 0.227);
  padding: 20px 20px;
  border-radius: 10px;
}
.row {
  margin-bottom: 10px;
}
.line{
  width: 100%;
  height: 1.5px;
  background-color: grey;
  margin: 10px 0px;
}
.button {
  display: inline-block;
  color: white;
  background-color: #ff8f00;
  box-shadow: 0px 1px 5px 1px rgba(0, 0, 0, 0.2);
  border-radius: 5px;
  border: 0px solid;
  outline: none;
  padding: 10px 10px;
  margin-right: 10px;
  cursor: pointer;
  width: 100px;
  margin: 10px 0 0 0;
  transition: 0.3s;
}
.button:hover{
  background-color: #fab965;
}
</style>