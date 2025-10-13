<template>
  <div class="placeholder-page">
    <h2>{{ title }}</h2>
    
    <el-card>
      <el-empty :description="description">
        <el-button type="primary" @click="router.push('/profile')">
          返回个人中心
        </el-button>
      </el-empty>
    </el-card>
  </div>
</template>

<script setup>
import { computed } from 'vue'
import { useRouter, useRoute } from 'vue-router'

const router = useRouter()
const route = useRoute()

const title = computed(() => route.meta.title || '功能开发中')
const description = computed(() => 
  route.meta.description || '该功能正在开发中，敬请期待...'
)
</script>

<style scoped>
.placeholder-page {
  padding: 20px;
}

h2 {
  margin-bottom: 20px;
  color: #333;
}
</style>
