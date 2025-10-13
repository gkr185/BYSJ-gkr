<template>
  <div class="home-page">
    <el-card>
      <h2>首页功能</h2>
      <el-empty description="首页功能开发中" />
      <div style="text-align: center; margin-top: 20px;">
        <el-button type="primary" @click="router.push('/profile')">进入个人中心</el-button>
      </div>
    </el-card>
  </div>
</template>

<script setup>
import { useRouter } from 'vue-router'

const router = useRouter()
</script>

<style scoped>
.home-page {
  padding: 20px;
}

h2 {
  margin-bottom: 20px;
  color: #333;
}
</style>
