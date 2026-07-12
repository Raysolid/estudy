<template>
  <div
    class="scroll-container"
    ref="scrollContainer"
    @wheel.prevent="handleScroll"
  >
    <div
      class="scroll-wrapper"
      ref="scrollWrapper"
      :style="{ left: left + 'px' }"
    >
      <slot></slot>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'

const scrollContainer = ref(null)
const scrollWrapper = ref(null)

const left = ref(0)

const handleScroll = (e) => {
  const eventDelta = e.wheelDelta || -e.deltaY * 40
  const $container = scrollContainer.value
  const $containerWidth = $container.offsetWidth
  const $wrapper = scrollWrapper.value
  const $wrapperWidth = $wrapper.offsetWidth

  if (eventDelta > 0) {
    left.value = Math.min(0, left.value + eventDelta)
  } else {
    if ($containerWidth - left.value < $wrapperWidth) {
      if (left.value < -($wrapperWidth - $containerWidth)) {
        left.value = left.value
      } else {
        left.value = Math.max(
          left.value + eventDelta,
          $containerWidth - $wrapperWidth
        )
      }
    } else {
      left.value = 0
    }
  }
}

const moveToTarget = ($target) => {
  const $container = scrollContainer.value
  const $containerWidth = $container.offsetWidth
  const $targetLeft = $target.offsetLeft
  const $targetWidth = $target.offsetWidth

  if ($targetLeft < -left.value) {
    // 标签在可视区域左侧
    left.value = -$targetLeft + 20
  } else if (
    $targetLeft + 20 > -left.value &&
    $targetLeft + $targetWidth < -left.value + $containerWidth
  ) {
    // 标签在可视区域
  } else {
    // 标签在可视区域右侧
    left.value = -($targetLeft - ($containerWidth - $targetWidth) + 20)
  }
}

defineExpose({
  moveToTarget
})
</script>

<style lang="scss" scoped>
.scroll-container {
  white-space: nowrap;
  position: relative;
  overflow: hidden;
  width: 100%;

  .scroll-wrapper {
    position: absolute;
  }
}
</style>
