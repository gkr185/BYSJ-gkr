// 商品Mock数据 - 严格遵守数据库设计文档字段标准

// 模拟商品数据
export const mockProducts = [
  {
    product_id: 1,
    category_id: 1,
    product_name: '新鲜草莓',
    cover_img: 'https://via.placeholder.com/300x300/FF6B6B/FFFFFF?text=新鲜草莓',
    detail: '新鲜采摘的草莓，香甜可口，富含维生素C',
    price: 25.00,
    group_price: 19.90,
    stock: 100,
    sales: 523,
    status: 1,
    create_time: '2025-10-15 10:00:00',
    update_time: '2025-10-21 09:30:00'
  },
  {
    product_id: 2,
    category_id: 1,
    product_name: '香蕉',
    cover_img: 'https://via.placeholder.com/300x300/FFD93D/FFFFFF?text=香蕉',
    detail: '进口香蕉，肉质细腻，口感香甜',
    price: 8.00,
    group_price: 6.50,
    stock: 200,
    sales: 856,
    status: 1,
    create_time: '2025-10-15 10:05:00',
    update_time: '2025-10-21 09:30:00'
  },
  {
    product_id: 3,
    category_id: 1,
    product_name: '苹果',
    cover_img: 'https://via.placeholder.com/300x300/C23B22/FFFFFF?text=苹果',
    detail: '烟台红富士苹果，脆甜多汁',
    price: 12.00,
    group_price: 9.90,
    stock: 150,
    sales: 672,
    status: 1,
    create_time: '2025-10-15 10:10:00',
    update_time: '2025-10-21 09:30:00'
  },
  {
    product_id: 4,
    category_id: 2,
    product_name: '新鲜生菜',
    cover_img: 'https://via.placeholder.com/300x300/6BCB77/FFFFFF?text=新鲜生菜',
    detail: '本地种植生菜，绿色健康，无农药残留',
    price: 5.00,
    group_price: 3.90,
    stock: 80,
    sales: 234,
    status: 1,
    create_time: '2025-10-15 10:15:00',
    update_time: '2025-10-21 09:30:00'
  },
  {
    product_id: 5,
    category_id: 2,
    product_name: '有机西红柿',
    cover_img: 'https://via.placeholder.com/300x300/FF6B6B/FFFFFF?text=有机西红柿',
    detail: '有机种植西红柿，酸甜可口，营养丰富',
    price: 8.00,
    group_price: null,
    stock: 120,
    sales: 445,
    status: 1,
    create_time: '2025-10-15 10:20:00',
    update_time: '2025-10-21 09:30:00'
  },
  {
    product_id: 6,
    category_id: 3,
    product_name: '鲜牛奶',
    cover_img: 'https://via.placeholder.com/300x300/4D96FF/FFFFFF?text=鲜牛奶',
    detail: '本地牧场直供，100%纯牛奶，每日新鲜配送',
    price: 15.00,
    group_price: 12.90,
    stock: 50,
    sales: 1203,
    status: 1,
    create_time: '2025-10-15 10:25:00',
    update_time: '2025-10-21 09:30:00'
  },
  {
    product_id: 7,
    category_id: 3,
    product_name: '酸奶',
    cover_img: 'https://via.placeholder.com/300x300/FFD93D/FFFFFF?text=酸奶',
    detail: '益生菌酸奶，促进消化，健康美味',
    price: 18.00,
    group_price: 15.90,
    stock: 60,
    sales: 789,
    status: 1,
    create_time: '2025-10-15 10:30:00',
    update_time: '2025-10-21 09:30:00'
  },
  {
    product_id: 8,
    category_id: 4,
    product_name: '猪肉',
    cover_img: 'https://via.placeholder.com/300x300/FF6B9D/FFFFFF?text=猪肉',
    detail: '新鲜猪肉，肉质鲜嫩，精选后腿肉',
    price: 35.00,
    group_price: 32.00,
    stock: 40,
    sales: 345,
    status: 1,
    create_time: '2025-10-15 10:35:00',
    update_time: '2025-10-21 09:30:00'
  },
  {
    product_id: 9,
    category_id: 4,
    product_name: '鸡胸肉',
    cover_img: 'https://via.placeholder.com/300x300/FFEAA7/FFFFFF?text=鸡胸肉',
    detail: '新鲜鸡胸肉，低脂高蛋白，健身必备',
    price: 28.00,
    group_price: null,
    stock: 30,
    sales: 234,
    status: 1,
    create_time: '2025-10-15 10:40:00',
    update_time: '2025-10-21 09:30:00'
  },
  {
    product_id: 10,
    category_id: 5,
    product_name: '面包',
    cover_img: 'https://via.placeholder.com/300x300/DFE4EA/333333?text=面包',
    detail: '新鲜烘焙面包，松软可口，早餐首选',
    price: 12.00,
    group_price: 9.90,
    stock: 100,
    sales: 567,
    status: 1,
    create_time: '2025-10-15 10:45:00',
    update_time: '2025-10-21 09:30:00'
  },
  {
    product_id: 11,
    category_id: 5,
    product_name: '饼干礼盒',
    cover_img: 'https://via.placeholder.com/300x300/A29BFE/FFFFFF?text=饼干礼盒',
    detail: '进口饼干礼盒，多种口味，送礼佳品',
    price: 58.00,
    group_price: 49.90,
    stock: 25,
    sales: 123,
    status: 1,
    create_time: '2025-10-15 10:50:00',
    update_time: '2025-10-21 09:30:00'
  },
  {
    product_id: 12,
    category_id: 6,
    product_name: '纸巾',
    cover_img: 'https://via.placeholder.com/300x300/74B9FF/FFFFFF?text=纸巾',
    detail: '柔软亲肤纸巾，家庭装，经济实惠',
    price: 25.00,
    group_price: 19.90,
    stock: 200,
    sales: 890,
    status: 1,
    create_time: '2025-10-15 10:55:00',
    update_time: '2025-10-21 09:30:00'
  }
]

// 模拟分类数据
export const mockCategories = [
  {
    category_id: 1,
    parent_id: 0,
    category_name: '新鲜水果',
    icon: 'Apple',
    sort: 1,
    status: 1
  },
  {
    category_id: 2,
    parent_id: 0,
    category_name: '新鲜蔬菜',
    icon: 'Orange',
    sort: 2,
    status: 1
  },
  {
    category_id: 3,
    parent_id: 0,
    category_name: '乳制品',
    icon: 'CoffeeCup',
    sort: 3,
    status: 1
  },
  {
    category_id: 4,
    parent_id: 0,
    category_name: '肉禽蛋',
    icon: 'Chicken',
    sort: 4,
    status: 1
  },
  {
    category_id: 5,
    parent_id: 0,
    category_name: '粮油副食',
    icon: 'Food',
    sort: 5,
    status: 1
  },
  {
    category_id: 6,
    parent_id: 0,
    category_name: '日用百货',
    icon: 'Goods',
    sort: 6,
    status: 1
  }
]

// 模拟轮播图数据
export const mockBanners = [
  {
    id: 1,
    image_url: 'https://via.placeholder.com/1200x400/FF6B6B/FFFFFF?text=社区团购+新鲜水果',
    link_url: '/products?category=1',
    sort: 1,
    status: 1
  },
  {
    id: 2,
    image_url: 'https://via.placeholder.com/1200x400/4ECDC4/FFFFFF?text=限时拼团+低至5折',
    link_url: '/group-buy',
    sort: 2,
    status: 1
  },
  {
    id: 3,
    image_url: 'https://via.placeholder.com/1200x400/95E1D3/FFFFFF?text=新人专享+优惠大礼包',
    link_url: '/home',
    sort: 3,
    status: 1
  }
]

// 获取商品详情（包含图片列表）
export function getProductDetail(productId) {
  const product = mockProducts.find(p => p.product_id === parseInt(productId))
  if (!product) return null
  
  return {
    ...product,
    images: [
      product.cover_img,
      product.cover_img.replace('300x300', '300x300&img=2'),
      product.cover_img.replace('300x300', '300x300&img=3')
    ],
    description: product.detail + '\n\n' + '【产品特色】\n- 新鲜直采，品质保证\n- 冷链配送，保鲜到家\n- 产地直供，价格实惠\n\n【储存方式】\n请放置于阴凉干燥处，避免阳光直射。',
    reviews: [
      {
        user_name: '张***',
        rating: 5,
        comment: '非常新鲜，配送也很快！',
        create_time: '2025-10-20 15:30:00'
      },
      {
        user_name: '李***',
        rating: 4,
        comment: '质量不错，下次还会购买',
        create_time: '2025-10-19 10:20:00'
      }
    ]
  }
}

