<template>
  <div class="my_grid">
    <slot name="board"></slot>
    <transition name="fade">
      <div>
        <slot name="card"></slot>
      </div>
    </transition>
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
  gap: 30px;
  transition: 0.5s;
}
@media screen and (min-width: 801px) {
  .my_grid {
    grid-template-columns: 1fr 1fr;
    margin: 50px;
  }
}
@media screen and (min-width: 501px) and (max-width: 800px) {
  .my_grid {
    grid-template-columns: 100%;
    grid-template-rows: auto auto;
    gap: 20px;
    margin: 20px;
  }
}
@media screen and (max-width: 500px){
  .my_grid{
    grid-template-columns: 100%;
    grid-template-rows: auto auto;
    gap: 10px;
    margin: 10px;
  }
}

.fade-enter-active,
.fade-leave-active {
  transition: opacity 0.5s;
}
.fade-enter,
.fade-leave-to {
  opacity: 0;
}
</style>