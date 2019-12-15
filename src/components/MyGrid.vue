<template>
  <div>
    <slot name="board"></slot>
    <div class="right_grid">
      <slot name="info"></slot>
      <slot name="setting"></slot>
    </div>
  </div>
</template>

<script>
export default {
  name: "MyGrid"
};
</script>

<style scoped>
.my_grid {
  display: grid;
  gap: 20px;
  grid-template-columns: 1fr 1fr;
}
.right_grid {
  display: grid;
  gap: 20;
  grid-template-columns: 1fr;
  grid-template-rows: 1fr 1fr;
}
</style>