<template>
  <div class="main-layout">
    <!-- 统一顶部导航栏 -->
    <Header />
    
    <!-- 主内容区 -->
    <main class="main-content">
      <slot />
    </main>
    
    <!-- 底部 -->
    <footer class="main-footer">
     </footer>
  </div>
</template>

<script setup>
import Header from './Header.vue'
</script>

<style scoped>
.main-layout {
  min-height: 100vh;
  display: flex;
  flex-direction: column;
}

/* 主内容区 */
.main-content {
  flex: 1;
  padding-top: 140px; /* Header顶部栏(~76px) + Nav菜单(~48px) + 间距(16px) */
  min-height: calc(100vh - 140px);
  background-color: #f5f5f5;
}

/* 底部 */
.main-footer {
  background-color: #2c3e50;
  color: #ecf0f1;
  padding: 40px 0 20px;
  margin-top: auto;
}

.footer-container {
  max-width: 1400px;
  margin: 0 auto;
  padding: 0 20px;
}

.footer-content {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(250px, 1fr));
  gap: 40px;
  margin-bottom: 30px;
}

.footer-section h4 {
  font-size: 16px;
  margin-bottom: 15px;
  color: #fff;
}

.footer-section p {
  margin-bottom: 8px;
  font-size: 14px;
  color: #bdc3c7;
}

.footer-section a {
  color: #bdc3c7;
  text-decoration: none;
  transition: color 0.3s;
}

.footer-section a:hover {
  color: #409EFF;
}

.footer-copyright {
  text-align: center;
  padding-top: 20px;
  border-top: 1px solid #34495e;
  font-size: 14px;
  color: #95a5a6;
}
</style>

