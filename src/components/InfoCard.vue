<template>
  <div class="card">
    <p>最大搜索深度：{{goMaxDepth}}</p>
    <p>考察结点个数：{{nodeNum}}</p>
    <p>用时：{{timeUsed}}秒；超时：{{isOutTime}}</p>
    <p>ab剪枝次数：{{ABPruning}}</p>
    <p>回传f值：{{f}}</p>
    <p>置换表命中总次数：{{sumMatch}}</p>
    <p> 完全命中：{{completeMatch}}；部分命中：{{partialMatch}}；叶子结点命中：{{leafMatch}}</p>
  </div>
</template>

<script>
import Status from "../GameAlgo/Status.js";
export default {
  name: "InfoCard",
  data: function() {
    return {
      goMaxDepth: 0,
      nodeNum: 0,
      completeMatch: 0,
      partialMatch: 0,
      leafMatch: 0,
      sumMatch: 0,
      ABPruning: 0,
      timeUsed: 0,
      isOutTime: false,
      f: 0
    };
  },
  methods: {
    update() {
      this.goMaxDepth = Status.goMaxDepth;
      this.nodeNum = Status.nodeNum;
      this.completeMatch = Status.completeMatch;
      this.partialMatch = Status.partialMatch;
      this.leafMatch = Status.leafMatch;
      this.sumMatch = this.completeMatch + this.partialMatch + this.leafMatch;
      this.ABPruning = Status.ABPruning;
      this.timeUsed = Math.round(
        (new Date().getTime() - Status.startTime) / 1000
      );
      this.isOutTime = Status.isOutTime;
      this.f = Status.f;
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
</style>