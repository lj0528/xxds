<template>
  <div class="theorem-video" v-if="videoUrl">
    <video controls width="100%" :poster="posterUrl">
      <source :src="videoUrl" type="video/mp4">
      您的浏览器不支持 video 标签。
    </video>
    <p class="video-caption">{{ caption }}</p>
  </div>
  <div v-else class="no-video">
    <p>📹 暂无动画演示，敬请期待</p>
  </div>
</template>

<script setup>
import { computed } from 'vue';

const props = defineProps({
  theoremId: {
    type: String,
    required: true
  }
});

// 根据定理 ID 映射视频文件
const videoMap = {
  'thm_1_1': '/videos/Thm_1_1.mp4',
  'thm_1_2': '/videos/Thm_1_2.mp4',
  'thm_1_3': '/videos/Thm_1_3.mp4',   // 行列式交换变号
  'thm_1_4': '/videos/Thm_1_4.mp4',
  'thm_1_5': '/videos/Thm_1_5.mp4',
  'thm_2_1': '/videos/Thm_2_1.mp4',
  'thm_2_2': '/videos/Thm_2_2.mp4',
  'thm_2_3': '/videos/Thm_2_3.mp4',
  'thm_2_4': '/videos/Thm_2_4.mp4',
  'thm_2_5': '/videos/Thm_2_5.mp4',
  'thm_3_1': '/videos/Thm_3_1.mp4',
  'thm_3_2': '/videos/Thm_3_2.mp4',
  'thm_3_3': '/videos/Thm_3_3.mp4',
  'thm_3_4': '/videos/Thm_3_4.mp4',
  'thm_4_1': '/videos/Thm_4_1.mp4',
  'thm_4_2': '/videos/Thm_4_2.mp4',
  'thm_4_3': '/videos/Thm_4_3.mp4',
  'thm_4_4': '/videos/Thm_4_4.mp4',
  'thm_5_1': '/videos/Thm_5_1.mp4',
  'thm_5_2': '/videos/Thm_5_2.mp4',
  'thm_5_3': '/videos/Thm_5_3.mp4',
  'thm_5_4': '/videos/Thm_5_4.mp4',
  'thm_6_1': '/videos/Thm_6_1.mp4',
  'thm_6_2': '/videos/Thm_6_2.mp4',
  'thm_6_3': '/videos/Thm_6_3.mp4',
  'thm_6_4': '/videos/Thm_6_4.mp4',
  // 可继续添加其他定理的视频
};

const videoUrl = computed(() => videoMap[props.theoremId] || null);
const caption = computed(() => {
  if (props.theoremId === 'thm_1_3') return '动画演示：行列式交换两行，行列式变号';
  return '线性代数动画演示';
});
const posterUrl = computed(() => videoUrl.value ? videoUrl.value.replace('.mp4', '_poster.jpg') : '');
</script>

<style scoped>
.theorem-video {
  margin: 1.5rem 0;
  border-radius: 12px;
  overflow: hidden;
  background: #000;
  box-shadow: 0 4px 12px rgba(0,0,0,0.1);
}
.video-caption {
  text-align: center;
  font-size: 0.85rem;
  color: #666;
  margin-top: 0.5rem;
}
.no-video {
  text-align: center;
  padding: 2rem;
  background: #f8fafc;
  border-radius: 20px;
  color: #94a3b8;
}
</style>