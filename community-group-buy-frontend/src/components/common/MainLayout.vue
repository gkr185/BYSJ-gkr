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
      <div class="footer-container">
        <div class="footer-content">
          <div class="footer-section">
            <h4>关于我们</h4>
            <p>社区团购系统 - 便捷、优惠的社区购物体验</p>
          </div>
          <div class="footer-section">
            <h4>联系方式</h4>
            <p>客服电话：400-888-8888</p>
            <p>工作时间：9:00 - 18:00</p>
          </div>
          <div class="footer-section">
            <h4>帮助中心</h4>
            <p><a href="#">常见问题</a></p>
            <p><a href="#">配送说明</a></p>
          </div>
        </div>
        <div class="footer-copyright">
          © 2025 社区团购系统. All rights reserved.
        </div>
      </div>
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
  padding-top: 120px; /* Header高度(60px) + Nav高度(60px) */
  min-height: calc(100vh - 120px - 200px); /* 减去Header和Footer高度 */
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

