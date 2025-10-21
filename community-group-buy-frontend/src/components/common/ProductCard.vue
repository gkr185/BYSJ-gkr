<template>
  <el-card :body-style="{ padding: '0px' }" class="product-card" shadow="hover">
    <div class="product-image" @click="goToDetail">
      <img :src="product.cover_img" :alt="product.product_name" />
      <div v-if="product.group_price" class="group-tag">
        <el-tag type="danger" size="small">拼团</el-tag>
      </div>
    </div>
    <div class="product-info">
      <div class="product-name" @click="goToDetail">{{ product.product_name }}</div>
      <div class="product-price">
        <span v-if="product.group_price" class="group-price">¥{{ product.group_price }}</span>
        <span :class="['original-price', product.group_price ? 'has-group' : '']">
          ¥{{ product.price }}
        </span>
      </div>
      <div class="product-footer">
        <span class="sales">已售{{ product.sales || 0 }}</span>
        <el-button 
          type="primary" 
          size="small" 
          :icon="ShoppingCart"
          @click="handleAddToCart"
          :disabled="product.stock === 0"
        >
          {{ product.stock === 0 ? '已售罄' : '加购' }}
        </el-button>
      </div>
    </div>
  </el-card>
</template>

<script setup>
import { useRouter } from 'vue-router'
import { ShoppingCart } from '@element-plus/icons-vue'
import { ElMessage } from 'element-plus'
import { addToCart } from '@/utils/cart'

const props = defineProps({
  product: {
    type: Object,
    required: true
  }
})

const router = useRouter()

const goToDetail = () => {
  router.push(`/products/${props.product.product_id}`)
}

const handleAddToCart = () => {
  if (props.product.stock === 0) {
    ElMessage.warning('商品已售罄')
    return
  }
  
  addToCart(props.product, 1)
  ElMessage.success('已添加到购物车')
}
</script>

<style scoped>
.product-card {
  cursor: pointer;
  transition: transform 0.3s;
  height: 100%;
}

.product-card:hover {
  transform: translateY(-4px);
}

.product-image {
  position: relative;
  width: 100%;
  height: 200px;
  overflow: hidden;
  background-color: #f5f5f5;
}

.product-image img {
  width: 100%;
  height: 100%;
  object-fit: cover;
  transition: transform 0.3s;
}

.product-card:hover .product-image img {
  transform: scale(1.05);
}

.group-tag {
  position: absolute;
  top: 10px;
  left: 10px;
}

.product-info {
  padding: 12px;
}

.product-name {
  font-size: 14px;
  color: #333;
  margin-bottom: 8px;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
  cursor: pointer;
}

.product-name:hover {
  color: #409EFF;
}

.product-price {
  margin-bottom: 12px;
  display: flex;
  align-items: center;
  gap: 8px;
}

.group-price {
  font-size: 18px;
  color: #F56C6C;
  font-weight: bold;
}

.original-price {
  font-size: 16px;
  color: #333;
  font-weight: bold;
}

.original-price.has-group {
  font-size: 12px;
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
  font-size: 12px;
  color: #999;
}
</style>

