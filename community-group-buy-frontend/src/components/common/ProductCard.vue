<template>
  <el-card class="product-card" :body-style="{ padding: '0' }" shadow="hover">
    <!-- 商品图片 -->
    <div class="product-image" @click="goToDetail">
      <el-image
        :src="productImageUrl"
        fit="cover"
        lazy
      >
        <template #error>
          <div class="image-error">
            <el-icon :size="50"><Picture /></el-icon>
            <div>暂无图片</div>
          </div>
        </template>
      </el-image>
      
      <!-- 拼团标签 -->
      <el-tag v-if="product.hasGroupBuy" class="group-tag" type="danger" effect="dark">
        拼团中
      </el-tag>
      
      <!-- 库存不足标签 -->
      <el-tag v-if="product.stock === 0" class="stock-tag" type="info">
        已售罄
      </el-tag>
    </div>

    <!-- 商品信息 -->
    <div class="product-info">
      <!-- 商品名称 -->
      <div class="product-name" @click="goToDetail" :title="product.productName">
        {{ product.productName }}
      </div>

      <!-- 价格区域 -->
      <div class="product-price">
        <!-- 拼团价 -->
        <span v-if="product.groupPrice" class="group-price">
          ¥{{ product.groupPrice }}
        </span>
        <!-- 原价 -->
        <span class="original-price" :class="{ 'has-group': product.groupPrice }">
          ¥{{ product.price }}
        </span>
      </div>

      <!-- 底部信息 -->
      <div class="product-footer">
        <span class="sales">已售 {{ product.sales || 0 }}</span>
        <el-button
          size="small"
          :icon="ShoppingCart"
          circle
          @click.stop="handleAddToCart"
          :disabled="product.stock === 0"
        />
      </div>
    </div>
  </el-card>
</template>

<script setup>
import { computed } from 'vue'
import { useRouter } from 'vue-router'
import { ShoppingCart, Picture } from '@element-plus/icons-vue'
import { ElMessage } from 'element-plus'
import { useCartStore } from '@/stores/cart'
import { getProductImageUrl } from '@/utils/image'

const props = defineProps({
  product: {
    type: Object,
    required: true
  }
})

const router = useRouter()
const cartStore = useCartStore()

// 计算商品图片URL
const productImageUrl = computed(() => {
  return getProductImageUrl(props.product)
})

const goToDetail = () => {
  router.push(`/products/${props.product.productId}`)
}

const handleAddToCart = () => {
  if (props.product.stock === 0) {
    ElMessage.warning('商品已售罄')
    return
  }
  
  cartStore.addItem(props.product, 1)
  ElMessage.success('已添加到购物车')
}
</script>

<style scoped>
.product-card {
  cursor: pointer;
  transition: transform 0.3s, box-shadow 0.3s;
  height: 100%;
  border-radius: 8px;
  overflow: hidden;
}

.product-card:hover {
  transform: translateY(-8px);
  box-shadow: 0 8px 24px rgba(0, 0, 0, 0.15);
}

.product-image {
  position: relative;
  width: 100%;
  height: 240px;
  overflow: hidden;
  background-color: #f5f5f5;
}

.product-image :deep(.el-image) {
  width: 100%;
  height: 100%;
}

.product-image :deep(.el-image img) {
  width: 100%;
  height: 100%;
  object-fit: cover;
  transition: transform 0.3s;
}

.product-card:hover .product-image :deep(.el-image img) {
  transform: scale(1.1);
}

.image-error {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  height: 100%;
  color: #ccc;
  font-size: 14px;
}

.group-tag {
  position: absolute;
  top: 12px;
  left: 12px;
  z-index: 1;
}

.stock-tag {
  position: absolute;
  top: 12px;
  right: 12px;
  z-index: 1;
}

.product-info {
  padding: 16px;
}

.product-name {
  font-size: 15px;
  color: #333;
  margin-bottom: 12px;
  line-height: 1.5;
  height: 45px;
  overflow: hidden;
  text-overflow: ellipsis;
  display: -webkit-box;
  -webkit-line-clamp: 2;
  line-clamp: 2;
  -webkit-box-orient: vertical;
  cursor: pointer;
  transition: color 0.3s;
}

.product-name:hover {
  color: #409EFF;
}

.product-price {
  margin-bottom: 16px;
  display: flex;
  align-items: baseline;
  gap: 8px;
}

.group-price {
  font-size: 20px;
  color: #F56C6C;
  font-weight: bold;
}

.group-price::before {
  content: '拼';
  font-size: 12px;
  padding: 2px 4px;
  background: #F56C6C;
  color: #fff;
  border-radius: 2px;
  margin-right: 4px;
  font-weight: normal;
}

.original-price {
  font-size: 18px;
  color: #333;
  font-weight: bold;
}

.original-price.has-group {
  font-size: 14px;
  color: #999;
  text-decoration: line-through;
  font-weight: normal;
}

.product-footer {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.sales {
  font-size: 13px;
  color: #999;
}
</style>

