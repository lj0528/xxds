<template>
  <div class="matrix-editor-overlay" @click.self="close">
    <div class="matrix-editor-modal">
      <div class="modal-header">
        <span>矩阵编辑器</span>
        <button class="close-btn" @click="close">&times;</button>
      </div>
      <div class="modal-body">
        <div class="matrix-dimension">
          <label>行数: <input type="number" v-model.number="rows" min="1" max="6" /></label>
          <label>列数: <input type="number" v-model.number="cols" min="1" max="6" /></label>
          <button @click="generateMatrix">生成矩阵</button>
        </div>
        <div class="matrix-input-table" v-if="matrixData.length">
          <table>
            <tbody>
              <tr v-for="(row, i) in matrixData" :key="i">
                <td v-for="(cell, j) in row" :key="j">
                  <input type="text" v-model="matrixData[i][j]" placeholder="元素" />
                </td>
              </tr>
            </tbody>
          </table>
          <div class="preview">
            <p>预览 (LaTeX):</p>
            <code>{{ generatedLatex }}</code>
          </div>
        </div>
      </div>
      <div class="modal-footer">
        <button @click="insert">插入到答案</button>
        <button @click="close">取消</button>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, computed, watch, onMounted } from 'vue'

const emit = defineEmits(['close', 'insert'])

const rows = ref(3)
const cols = ref(3)
const matrixData = ref([])

const generatedLatex = computed(() => {
  if (!matrixData.value.length) return ''
  const rowsLatex = matrixData.value.map(row => row.join(' & '))
  return `\\begin{bmatrix} ${rowsLatex.join(' \\\\ ')} \\end{bmatrix}`
})

const generateMatrix = () => {
  const newData = []
  for (let i = 0; i < rows.value; i++) {
    const row = []
    for (let j = 0; j < cols.value; j++) {
      const oldVal = (matrixData.value[i] && matrixData.value[i][j]) || ''
      row.push(oldVal)
    }
    newData.push(row)
  }
  matrixData.value = newData
}

const insert = () => {
  emit('insert', generatedLatex.value)
  close()
}

const close = () => {
  emit('close')
}

watch([rows, cols], () => {
  generateMatrix()
})

onMounted(() => {
  generateMatrix()
})
</script>

<style scoped>
.matrix-editor-overlay {
  position: fixed; top: 0; left: 0; width: 100%; height: 100%;
  background: rgba(0,0,0,0.5); display: flex; align-items: center; justify-content: center;
  z-index: 1000;
}
.matrix-editor-modal {
  background: white; border-radius: 8px; width: 600px; max-width: 90%;
  max-height: 80%; overflow-y: auto; box-shadow: 0 4px 12px rgba(0,0,0,0.15);
}
.modal-header {
  display: flex; justify-content: space-between; padding: 10px 16px;
  border-bottom: 1px solid #eee; font-weight: bold;
}
.close-btn { background: none; border: none; font-size: 24px; cursor: pointer; }
.modal-body { padding: 16px; }
.matrix-dimension { margin-bottom: 16px; display: flex; gap: 12px; align-items: center; }
.matrix-dimension input { width: 60px; padding: 4px; }
.matrix-input-table table { border-collapse: collapse; margin: 10px 0; }
.matrix-input-table td { border: 1px solid #ccc; padding: 4px; }
.matrix-input-table input { width: 60px; padding: 6px; text-align: center; border: none; }
.preview { background: #f9f9f9; padding: 10px; border-radius: 4px; margin-top: 16px; }
.preview code { font-family: monospace; background: #eee; padding: 4px; display: inline-block; }
.modal-footer { padding: 12px 16px; border-top: 1px solid #eee; display: flex; justify-content: flex-end; gap: 8px; }
.modal-footer button { padding: 6px 12px; cursor: pointer; }
</style>