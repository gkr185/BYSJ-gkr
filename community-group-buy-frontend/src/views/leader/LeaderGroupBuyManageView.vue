<template>
  <MainLayout>
    <div class="leader-groupbuy-manage">
    <div class="page-header">
      <div class="header-content">
        <h1 class="page-title">
          <el-icon><Grid /></el-icon>
          拼团活动管理
        </h1>
        <p class="page-desc">管理拼团活动和发起的团队</p>
      </div>
      <el-button 
        v-if="activeTab === 'activities'" 
        type="primary" 
        size="large" 
        @click="showActivityDialog"
      >
        <el-icon><Plus /></el-icon>
        创建活动
      </el-button>
      <el-button 
        v-else 
        type="primary" 
        size="large" 
        @click="showCreateDialog"
      >
        <el-icon><Plus /></el-icon>
        发起新团
      </el-button>
    </div>

    <!-- Tab切换 -->
    <el-card class="tab-card" shadow="never">
      <el-tabs v-model="activeTab" @tab-change="handleMainTabChange">
        <el-tab-pane label="活动管理" name="activities">
          <template #label>
            <span class="tab-label">
              <el-icon><DocumentCopy /></el-icon>
              活动管理
            </span>
          </template>
        </el-tab-pane>
        <el-tab-pane label="我的团队" name="teams">
          <template #label>
            <span class="tab-label">
              <el-icon><UserFilled /></el-icon>
              团购管理
            </span>
          </template>
        </el-tab-pane>
      </el-tabs>
    </el-card>

    <!-- 活动管理区域 -->
    <div v-if="activeTab === 'activities'" class="activities-section">
      <!-- 活动统计 -->
      <div class="activity-stats">
        <el-card class="stat-item" shadow="hover">
          <div class="stat-content">
            <div class="stat-icon active">
              <el-icon><Promotion /></el-icon>
            </div>
            <div class="stat-info">
              <div class="stat-value">{{ activityStats.active }}</div>
              <div class="stat-label">进行中</div>
            </div>
          </div>
        </el-card>
        
        <el-card class="stat-item" shadow="hover">
          <div class="stat-content">
            <div class="stat-icon teams">
              <el-icon><UserFilled /></el-icon>
            </div>
            <div class="stat-info">
              <div class="stat-value">{{ activityStats.totalTeams }}</div>
              <div class="stat-label">总团数</div>
            </div>
          </div>
        </el-card>
        
        <el-card class="stat-item" shadow="hover">
          <div class="stat-content">
            <div class="stat-icon success">
              <el-icon><CircleCheck /></el-icon>
            </div>
            <div class="stat-info">
              <div class="stat-value">{{ activityStats.successTeams }}</div>
              <div class="stat-label">已成团</div>
            </div>
          </div>
        </el-card>
      </div>

      <!-- 活动列表 -->
      <div class="activities-list" v-loading="activitiesTableLoading">
        <el-empty 
          v-if="myActivities.length === 0 && !activitiesTableLoading" 
          description="暂无活动，快来创建第一个拼团活动吧！"
          :image-size="180"
        >
          <el-button type="primary" size="large" @click="showActivityDialog">
            <el-icon><Plus /></el-icon>
            立即创建活动
          </el-button>
        </el-empty>

        <!-- 卡片式布局 -->
        <div class="activities-grid" v-else>
          <el-card 
            v-for="activity in myActivities" 
            :key="activity.activityId"
            class="activity-card"
            shadow="hover"
          >
            <!-- 活动状态标签 -->
            <div class="activity-status-badge" :class="getActivityStatusClass(activity.status)">
              {{ getActivityStatusText(activity.status) }}
            </div>

            <!-- 活动内容 -->
            <div class="activity-content">
              <!-- 商品信息区域 -->
              <div class="product-section">
                <el-image 
                  :src="activity.product?.coverImg || '/placeholder-product.png'" 
                  fit="cover"
                  class="product-image"
                >
                  <template #error>
                    <div class="image-error">
                      <el-icon><Picture /></el-icon>
                    </div>
                  </template>
                </el-image>
                
                <div class="product-info">
                  <h3 class="product-name">{{ activity.product?.productName || '未知商品' }}</h3>
                  <div class="product-meta">
                    <span class="meta-item">
                      <el-icon><ShoppingBag /></el-icon>
                      商品ID: {{ activity.productId }}
                    </span>
                    <span class="meta-item">
                      <el-icon><Grid /></el-icon>
                      活动ID: {{ activity.activityId }}
                    </span>
                  </div>
                </div>
              </div>

              <!-- 价格对比 -->
              <div class="price-section">
                <div class="price-item">
                  <span class="price-label">拼团价</span>
                  <span class="group-price">¥{{ activity.groupPrice }}</span>
                </div>
                <div class="price-divider">
                  <el-icon><Right /></el-icon>
                </div>
                <div class="price-item">
                  <span class="price-label">原价</span>
                  <span class="original-price">¥{{ activity.product?.price }}</span>
                </div>
                <div class="discount-badge">
                  省¥{{ (activity.product?.price - activity.groupPrice).toFixed(2) }}
                </div>
              </div>

              <!-- 活动详情 -->
              <div class="activity-details">
                <div class="detail-row">
                  <div class="detail-item">
                    <el-icon><User /></el-icon>
                    <span>{{ activity.requiredNum }}人成团</span>
                  </div>
                  <div class="detail-item" v-if="activity.maxNum">
                    <el-icon><Warning /></el-icon>
                    <span>限{{ activity.maxNum }}人</span>
                  </div>
                  <div class="detail-item" v-else>
                    <el-icon><Checked /></el-icon>
                    <span>无人数限制</span>
                  </div>
                </div>

                <div class="detail-row time-info">
                  <div class="time-item">
                    <el-icon><Clock /></el-icon>
                    <span>{{ formatDateTime(activity.startTime) }}</span>
                  </div>
                  <span class="time-separator">~</span>
                  <div class="time-item">
                    <el-icon><Clock /></el-icon>
                    <span>{{ formatDateTime(activity.endTime) }}</span>
                  </div>
                </div>
              </div>

              <!-- 团队统计 -->
              <div class="team-stats" v-if="activity.teamStats">
                <div class="stat-badge ongoing">
                  <span class="badge-label">拼团中</span>
                  <span class="badge-value">{{ activity.teamStats.ongoing || 0 }}</span>
                </div>
                <div class="stat-badge success">
                  <span class="badge-label">已成团</span>
                  <span class="badge-value">{{ activity.teamStats.success || 0 }}</span>
                </div>
                <div class="stat-badge total">
                  <span class="badge-label">总计</span>
                  <span class="badge-value">{{ activity.teamStats.total || 0 }}</span>
                </div>
              </div>

              <!-- 操作按钮 -->
              <div class="activity-actions">
                <el-button 
                  type="success" 
                  @click="launchFromActivity(activity)"
                  :icon="Plus"
                >
                  发起拼团
                </el-button>
                <el-button 
                  type="primary" 
                  plain
                  @click="editActivity(activity)"
                  :icon="Edit"
                >
                  编辑
                </el-button>
                <el-button 
                  type="danger" 
                  plain
                  @click="deleteActivity(activity)"
                  :icon="Delete"
                >
                  删除
                </el-button>
                <el-button 
                  plain
                  @click="viewActivityTeams(activity)"
                  :icon="View"
                >
                  查看团队
                </el-button>
              </div>
            </div>
          </el-card>
        </div>
      </div>
    </div>

    <!-- 团购管理区域 -->
    <div v-if="activeTab === 'teams'" class="teams-section">
      <!-- 统计卡片 -->
      <div class="stats-cards">
        <el-card class="stat-card" shadow="hover">
          <div class="stat-content">
            <div class="stat-icon ongoing">
              <el-icon><Loading /></el-icon>
            </div>
            <div class="stat-info">
              <div class="stat-value">{{ stats.ongoing }}</div>
              <div class="stat-label">拼团中</div>
            </div>
          </div>
        </el-card>
        
        <el-card class="stat-card" shadow="hover">
          <div class="stat-content">
            <div class="stat-icon success">
              <el-icon><CircleCheck /></el-icon>
            </div>
            <div class="stat-info">
              <div class="stat-value">{{ stats.success }}</div>
              <div class="stat-label">已成团</div>
            </div>
          </div>
        </el-card>
        
        <el-card class="stat-card" shadow="hover">
          <div class="stat-content">
            <div class="stat-icon failed">
              <el-icon><CircleClose /></el-icon>
            </div>
            <div class="stat-info">
              <div class="stat-value">{{ stats.failed }}</div>
              <div class="stat-label">已失败</div>
            </div>
          </div>
        </el-card>
        
        <el-card class="stat-card" shadow="hover">
          <div class="stat-content">
            <div class="stat-icon total">
              <el-icon><DataLine /></el-icon>
            </div>
            <div class="stat-info">
              <div class="stat-value">{{ stats.total }}</div>
              <div class="stat-label">总计</div>
            </div>
          </div>
        </el-card>
      </div>

    <!-- TAB切换筛选 -->
    <el-card class="filter-tabs-card" shadow="never">
      <el-tabs v-model="activeStatusTab" @tab-change="handleStatusTabChange">
        <el-tab-pane label="拼团中" name="0">
          <template #label>
            <span class="tab-label">
              <el-icon><Loading /></el-icon>
              拼团中
              <el-badge v-if="stats.ongoing > 0" :value="stats.ongoing" type="danger" class="tab-badge" />
            </span>
          </template>
        </el-tab-pane>
        <el-tab-pane label="已成团" name="1">
          <template #label>
            <span class="tab-label">
              <el-icon><CircleCheck /></el-icon>
              已成团
              <el-badge v-if="stats.success > 0" :value="stats.success" type="success" class="tab-badge" />
            </span>
          </template>
        </el-tab-pane>
        <el-tab-pane label="已失败" name="2">
          <template #label>
            <span class="tab-label">
              <el-icon><CircleClose /></el-icon>
              已失败
              <el-badge v-if="stats.failed > 0" :value="stats.failed" type="warning" class="tab-badge" />
            </span>
          </template>
        </el-tab-pane>
        <el-tab-pane label="全部" name="all">
          <template #label>
            <span class="tab-label">
              <el-icon><DataLine /></el-icon>
              全部
              <el-badge v-if="stats.total > 0" :value="stats.total" class="tab-badge" />
            </span>
          </template>
        </el-tab-pane>
      </el-tabs>
    </el-card>

    <!-- 团列表 -->
    <div class="teams-list" v-loading="loading">
      <el-empty 
        v-if="teams.length === 0 && !loading" 
        description="暂无拼团记录，快来发起第一个团购吧！"
        :image-size="180"
      >
        <el-button type="primary" size="large" @click="showCreateDialog">
          <el-icon><Plus /></el-icon>
          立即发起新团
        </el-button>
      </el-empty>

      <div class="teams-grid" v-else>
        <el-card 
          v-for="team in teams" 
          :key="team.teamId" 
          class="team-card" 
          shadow="hover"
          @click="viewTeamDetail(team.teamId)"
        >
          <!-- 团状态标签 -->
          <div class="team-status-badge" :class="getStatusClass(team.teamStatus)">
            {{ getStatusText(team.teamStatus) }}
          </div>

          <!-- 团信息 -->
          <div class="team-info">
            <div class="team-header">
              <h3 class="team-no">{{ team.teamNo }}</h3>
              <div class="team-price">
                <span class="price-label">拼团价</span>
                <span class="price-value">¥{{ team.groupPrice }}</span>
              </div>
            </div>

            <div class="team-meta">
              <div class="meta-item">
                <el-icon><Location /></el-icon>
                <span>{{ team.communityName || '未知社区' }}</span>
              </div>
              <div class="meta-item">
                <el-icon><Clock /></el-icon>
                <span>{{ formatDateTime(team.createTime) }}</span>
              </div>
            </div>

            <!-- 进度条 -->
            <div class="team-progress">
              <div class="progress-info">
                <span class="progress-text">
                  已拼 <strong>{{ team.currentNum }}</strong> / {{ team.requiredNum }}人
                </span>
                <span class="remain-text" v-if="team.teamStatus === 0">
                  还差{{ team.remainNum }}人
                </span>
              </div>
              <el-progress 
                :percentage="getProgressPercentage(team)" 
                :status="getProgressStatus(team.teamStatus)"
                :stroke-width="8"
              />
            </div>

            <!-- 过期时间提示 -->
            <div class="expire-info" v-if="team.teamStatus === 0">
              <el-icon><Timer /></el-icon>
              <span>{{ formatExpireTime(team.expireTime) }}</span>
            </div>

            <!-- 成团时间 -->
            <div class="success-info" v-if="team.teamStatus === 1 && team.successTime">
              <el-icon><CircleCheck /></el-icon>
              <span>成团时间：{{ formatDateTime(team.successTime) }}</span>
            </div>
          </div>

          <!-- 操作按钮 -->
          <div class="team-actions">
            <el-button type="primary" link @click.stop="viewTeamDetail(team.teamId)">
              <el-icon><View /></el-icon>
              查看详情
            </el-button>
            <el-button type="success" link @click.stop="shareTeam(team)" v-if="team.teamStatus === 0">
              <el-icon><Share /></el-icon>
              分享拼团
            </el-button>
          </div>
        </el-card>
      </div>

      <!-- 分页 -->
      <div class="pagination-wrapper" v-if="total > 0">
        <el-pagination
          :current-page="queryForm.page + 1"
          :page-size="queryForm.limit"
          :page-sizes="[12, 24, 48]"
          :total="total"
          layout="total, sizes, prev, pager, next, jumper"
          @size-change="handleSizeChange"
          @current-change="handlePageChange"
        />
      </div>
    </div>

    <!-- 发起拼团对话框 -->
    <el-dialog
      v-model="createDialogVisible"
      title="发起新团"
      width="900px"
      :close-on-click-modal="false"
      destroy-on-close
    >
      <el-steps :active="currentStep" align-center finish-status="success" class="create-steps">
        <el-step title="选择商品" icon="ShoppingBag" />
        <el-step title="选择活动" icon="Grid" />
        <el-step title="完成发起" icon="CircleCheck" />
      </el-steps>

      <!-- 步骤1：选择商品 -->
      <div v-if="currentStep === 0" class="step-content">
        <div class="search-bar">
          <el-input
            v-model="productSearch"
            placeholder="搜索商品名称"
            clearable
            @input="handleProductSearch"
          >
            <template #prefix>
              <el-icon><Search /></el-icon>
            </template>
          </el-input>
        </div>

        <div class="products-grid" v-loading="productsLoading">
          <el-empty v-if="products.length === 0" description="暂无商品" />
          
          <div 
            v-for="product in products" 
            :key="product.productId"
            class="product-item"
            :class="{ selected: createForm.productId === product.productId }"
            @click="selectProduct(product)"
          >
            <el-image 
              :src="product.coverImg || '/placeholder-product.png'" 
              fit="cover"
              class="product-image"
            >
              <template #error>
                <div class="image-error">
                  <el-icon><Picture /></el-icon>
                </div>
              </template>
            </el-image>
            <div class="product-info">
              <div class="product-name">{{ product.productName }}</div>
              <div class="product-price">¥{{ product.price }}</div>
            </div>
            <div class="select-indicator" v-if="createForm.productId === product.productId">
              <el-icon><CircleCheck /></el-icon>
            </div>
          </div>
        </div>
      </div>

      <!-- 步骤2：选择活动 -->
      <div v-if="currentStep === 1" class="step-content">
        <el-alert
          title="请选择一个进行中的拼团活动"
          type="info"
          :closable="false"
          show-icon
          style="margin-bottom: 20px;"
        />

        <div class="activities-list" v-loading="activitiesLoading">
          <el-empty v-if="activities.length === 0" description="该商品暂无进行中的拼团活动" />
          
          <div 
            v-for="activity in activities" 
            :key="activity.activityId"
            class="activity-item"
            :class="{ selected: createForm.activityId === activity.activityId }"
            @click="selectActivity(activity)"
          >
            <div class="activity-header">
              <div class="activity-title">
                <el-tag type="success" size="large">拼团活动</el-tag>
                <span class="activity-id">活动ID: {{ activity.activityId }}</span>
              </div>
              <div class="select-indicator" v-if="createForm.activityId === activity.activityId">
                <el-icon><CircleCheck /></el-icon>
              </div>
            </div>

            <div class="activity-details">
              <div class="detail-row">
                <div class="detail-item">
                  <span class="detail-label">拼团价：</span>
                  <span class="detail-value price">¥{{ activity.groupPrice }}</span>
                </div>
                <div class="detail-item">
                  <span class="detail-label">原价：</span>
                  <span class="detail-value">¥{{ selectedProduct?.price }}</span>
                </div>
                <div class="detail-item">
                  <span class="detail-label">成团人数：</span>
                  <span class="detail-value">{{ activity.requiredNum }}人</span>
                </div>
              </div>

              <div class="detail-row">
                <div class="detail-item full-width">
                  <span class="detail-label">活动时间：</span>
                  <span class="detail-value">
                    {{ formatDateTime(activity.startTime) }} ~ {{ formatDateTime(activity.endTime) }}
                  </span>
                </div>
              </div>

              <!-- 已有团队 -->
              <div class="existing-teams" v-if="activity.teams && activity.teams.length > 0">
                <div class="teams-header">
                  <span class="teams-title">进行中的团（{{ activity.teams.length }}个）</span>
                </div>
                <div class="teams-scroll">
                  <div v-for="team in activity.teams" :key="team.teamId" class="mini-team">
                    <span class="team-leader">{{ team.leaderName }}</span>
                    <span class="team-community">{{ team.communityName }}</span>
                    <el-progress 
                      :percentage="Math.round(team.currentNum / team.requiredNum * 100)" 
                      :stroke-width="4"
                      :show-text="false"
                    />
                    <span class="team-count">{{ team.currentNum }}/{{ team.requiredNum }}</span>
                  </div>
                </div>
              </div>
            </div>
          </div>
        </div>
      </div>

      <!-- 步骤3：完成发起 -->
      <div v-if="currentStep === 2" class="step-content">
        <el-form :model="createForm" :rules="createRules" ref="createFormRef" label-width="120px">
          <el-divider content-position="left">拼团信息</el-divider>
          
          <el-form-item label="选择的商品">
            <div class="form-info">
              <el-image 
                :src="selectedProduct?.coverImg || '/placeholder-product.png'" 
                fit="cover"
                style="width: 60px; height: 60px; border-radius: 4px; margin-right: 12px;"
              >
                <template #error>
                  <div class="image-error" style="width: 60px; height: 60px; display: flex; align-items: center; justify-content: center; background: #f5f7fa;">
                    <el-icon><Picture /></el-icon>
                  </div>
                </template>
              </el-image>
              <div>
                <div style="font-weight: 500; margin-bottom: 4px;">{{ selectedProduct?.productName }}</div>
                <div style="color: #999; font-size: 13px;">原价: ¥{{ selectedProduct?.price }}</div>
              </div>
            </div>
          </el-form-item>

          <el-form-item label="拼团活动">
            <div class="form-info">
              <el-tag type="success">活动ID: {{ selectedActivity?.activityId }}</el-tag>
              <span style="margin-left: 12px;">
                拼团价: <strong style="color: #f56c6c; font-size: 16px;">¥{{ selectedActivity?.groupPrice }}</strong>
              </span>
              <span style="margin-left: 12px;">
                成团人数: {{ selectedActivity?.requiredNum }}人
              </span>
            </div>
          </el-form-item>

          <el-divider content-position="left">发起设置</el-divider>

          <el-form-item label="团购持续时间" prop="durationHours">
            <el-input-number
              v-model="createForm.durationHours"
              :min="1"
              :max="168"
              controls-position="right"
              style="width: 200px;"
            />
            <span style="margin-left: 12px; color: #999;">小时</span>
            <div class="form-tip">
              设置团购的持续时间，过期后自动结束拼团（1-168小时）
            </div>
          </el-form-item>

          <el-form-item label="是否立即参与" prop="joinImmediately">
            <el-switch 
              v-model="createForm.joinImmediately"
              active-text="立即参与拼团"
              inactive-text="仅发起团队"
              @change="handleJoinImmediatelyChange"
            />
            <div class="form-tip">
              开启后您将作为第一个成员加入该团，需要选择收货地址和数量
            </div>
          </el-form-item>

          <template v-if="createForm.joinImmediately">
            <!-- 团长团点地址提示 -->
            <el-form-item label="团点地址" v-if="leaderInfo">
              <el-alert
                type="info"
                :closable="false"
                show-icon
              >
                <template #title>
                  <div style="display: flex; flex-direction: column; gap: 4px;">
                    <div style="font-weight: 600;">{{ leaderInfo.storeName || '我的团点' }}</div>
                    <div style="font-size: 13px; color: #606266;">
                      <el-icon><Location /></el-icon>
                      {{ leaderInfo.address || '未设置地址' }}
                    </div>
                    <div style="font-size: 12px; color: #909399; margin-top: 4px;">
                      提示：建议团长使用团点地址作为收货地址，方便集中配送
                    </div>
                  </div>
                </template>
              </el-alert>
            </el-form-item>

            <el-form-item label="收货地址" prop="addressId">
              <el-select 
                v-model="createForm.addressId" 
                placeholder="请选择收货地址（建议选择团点地址）"
                style="width: 100%;"
              >
                <el-option
                  v-for="address in addresses"
                  :key="address.addressId"
                  :label="`${address.receiver} ${address.phone} - ${address.fullAddress || address.detail}`"
                  :value="address.addressId"
                >
                  <div style="display: flex; justify-content: space-between; align-items: center;">
                    <span>
                      <el-tag v-if="address.isDefault" size="small" type="success" style="margin-right: 8px;">默认</el-tag>
                      {{ address.receiver }} {{ address.phone }}
                    </span>
                    <span style="color: #999; font-size: 12px;">{{ address.fullAddress || address.detail }}</span>
                  </div>
                </el-option>
              </el-select>
              <el-link type="primary" :underline="false" @click="handleManageAddress" style="margin-top: 8px;">
                <el-icon><Plus /></el-icon>
                管理收货地址
              </el-link>
            </el-form-item>

            <el-form-item label="购买数量" prop="quantity">
              <el-input-number 
                v-model="createForm.quantity" 
                :min="1" 
                :max="99"
                controls-position="right"
              />
              <span style="margin-left: 12px; color: #999;">
                小计: ¥{{ ((selectedActivity?.groupPrice || 0) * createForm.quantity).toFixed(2) }}
              </span>
            </el-form-item>
          </template>

          <el-divider content-position="left">确认信息</el-divider>

          <div class="confirmation-info">
            <div class="confirm-item">
              <span class="confirm-label">团购持续时间：</span>
              <span class="confirm-value">{{ createForm.durationHours }}小时</span>
            </div>
            <div class="confirm-tip">
              团购将在发起后{{ createForm.durationHours }}小时自动结束
            </div>
          </div>

          <el-alert
            title="温馨提示"
            type="warning"
            :closable="false"
            show-icon
            style="margin-top: 20px;"
          >
            <ul style="margin: 0; padding-left: 20px;">
              <li>发起团队后，团队将在{{ createForm.durationHours }}小时内有效</li>
              <li>如果选择"立即参与"，系统将自动从您的账户余额扣款完成支付</li>
              <li>支付成功后您将成为该团的第一个成员，团的当前人数会更新</li>
              <li>建议使用团点地址作为收货地址，方便集中配送和用户自提</li>
              <li>您可以分享团队链接，邀请其他用户参与拼团</li>
              <li>成团人数达到要求后，拼团自动成功</li>
            </ul>
          </el-alert>
        </el-form>
      </div>

      <template #footer>
        <div class="dialog-footer">
          <el-button @click="handlePrevStep" v-if="currentStep > 0">
            <el-icon><ArrowLeft /></el-icon>
            上一步
          </el-button>
          <el-button type="primary" @click="handleNextStep" v-if="currentStep < 2">
            下一步
            <el-icon><ArrowRight /></el-icon>
          </el-button>
          <el-button type="primary" @click="handleSubmitCreate" :loading="submitLoading" v-if="currentStep === 2">
            <el-icon><CircleCheck /></el-icon>
            确认发起
          </el-button>
          <el-button @click="createDialogVisible = false">取消</el-button>
        </div>
      </template>
    </el-dialog>
    </div>
    <!-- 关闭teams-section -->

    <!-- 创建/编辑活动对话框 -->
    <el-dialog
      v-model="activityDialogVisible"
      :title="activityDialogTitle"
      width="800px"
      :close-on-click-modal="false"
      destroy-on-close
    >
      <!-- 步骤导航 -->
      <el-steps :active="activityStep" align-center finish-status="success" class="activity-steps" v-if="!isEditingActivity">
        <el-step title="选择商品" icon="ShoppingBag" />
        <el-step title="设置活动" icon="Setting" />
        <el-step title="确认创建" icon="CircleCheck" />
      </el-steps>

      <!-- 步骤1：选择商品 -->
      <div v-if="activityStep === 0 && !isEditingActivity" class="step-content">
        <div class="search-bar">
          <el-input
            v-model="activityProductSearch"
            placeholder="搜索商品名称"
            clearable
            @input="handleActivityProductSearch"
          >
            <template #prefix>
              <el-icon><Search /></el-icon>
            </template>
          </el-input>
        </div>

        <div class="products-grid" v-loading="productsLoading">
          <el-empty v-if="products.length === 0" description="暂无商品" />
          
          <div 
            v-for="product in products" 
            :key="product.productId"
            class="product-item"
            :class="{ selected: activityForm.productId === product.productId }"
            @click="selectActivityProduct(product)"
          >
            <el-image 
              :src="product.coverImg || '/placeholder-product.png'" 
              fit="cover"
              class="product-image"
            >
              <template #error>
                <div class="image-error">
                  <el-icon><Picture /></el-icon>
                </div>
              </template>
            </el-image>
            <div class="product-info">
              <div class="product-name">{{ product.productName }}</div>
              <div class="product-price">¥{{ product.price }}</div>
              <div class="product-stock">库存: {{ product.stock }}</div>
            </div>
            <div class="select-indicator" v-if="activityForm.productId === product.productId">
              <el-icon><CircleCheck /></el-icon>
            </div>
          </div>
        </div>
      </div>

      <!-- 步骤2：设置活动 -->
      <div v-if="activityStep === 1 && !isEditingActivity" class="step-content">
        <el-form :model="activityForm" :rules="activityRules" ref="activityFormRef" label-width="120px">
          <!-- 显示已选商品 -->
          <el-form-item label="选择的商品">
            <div class="selected-product-display">
              <el-image 
                :src="selectedActivityProduct?.coverImg || '/placeholder-product.png'" 
                fit="cover"
                style="width: 80px; height: 80px; border-radius: 8px; margin-right: 16px;"
              >
                <template #error>
                  <div class="image-error">
                    <el-icon><Picture /></el-icon>
                  </div>
                </template>
              </el-image>
              <div class="product-details">
                <h4>{{ selectedActivityProduct?.productName }}</h4>
                <p>商品价格: ¥{{ selectedActivityProduct?.price }}</p>
                <p>库存: {{ selectedActivityProduct?.stock }}件</p>
              </div>
            </div>
          </el-form-item>

        <el-form-item label="拼团价" prop="groupPrice">
          <el-input-number 
            v-model="activityForm.groupPrice" 
            :min="0.01" 
            :max="selectedActivityProduct?.price || 999999"
            :precision="2"
            :step="0.1"
            controls-position="right"
            style="width: 100%;"
          />
          <div class="form-tip" v-if="selectedActivityProduct">
            商品原价: ¥{{ selectedActivityProduct.price }}，请设置低于原价的拼团价
          </div>
        </el-form-item>

        <el-form-item label="成团人数" prop="requiredNum">
          <el-input-number 
            v-model="activityForm.requiredNum" 
            :min="2" 
            :max="100"
            controls-position="right"
            style="width: 100%;"
          />
          <div class="form-tip">
            2-100人，达到此人数即可成团
          </div>
        </el-form-item>

        <el-form-item label="最大人数" prop="maxNum">
          <el-input-number 
            v-model="activityForm.maxNum" 
            :min="activityForm.requiredNum || 2" 
            :max="999999"
            controls-position="right"
            style="width: 100%;"
            placeholder="留空表示无限制"
            clearable
          />
          <div class="form-tip">
            限制参与活动的总人数，留空表示无限制
          </div>
        </el-form-item>

        <el-form-item label="活动时间" required>
          <el-col :span="11">
            <el-form-item prop="startTime">
              <el-date-picker
                v-model="activityForm.startTime"
                type="datetime"
                placeholder="选择开始时间"
                format="YYYY-MM-DD HH:mm:ss"
                value-format="YYYY-MM-DDTHH:mm:ss"
                style="width: 100%;"
              />
            </el-form-item>
          </el-col>
          <el-col :span="2" style="text-align: center;">至</el-col>
          <el-col :span="11">
            <el-form-item prop="endTime">
              <el-date-picker
                v-model="activityForm.endTime"
                type="datetime"
                placeholder="选择结束时间"
                format="YYYY-MM-DD HH:mm:ss"
                value-format="YYYY-MM-DDTHH:mm:ss"
                style="width: 100%;"
              />
            </el-form-item>
          </el-col>
        </el-form-item>

        <el-alert
          title="设置提示"
          type="info"
          :closable="false"
          show-icon
        >
          <ul style="margin: 0; padding-left: 20px;">
            <li>拼团价必须低于商品原价</li>
            <li>成团人数建议2-10人</li>
            <li>活动结束时间必须晚于开始时间</li>
          </ul>
        </el-alert>
      </el-form>
      </div>

      <!-- 编辑模式的表单 -->
      <div v-if="isEditingActivity" class="step-content">
        <el-form :model="activityForm" :rules="activityRules" ref="activityFormRef" label-width="120px">
          <el-form-item label="商品信息">
            <div class="selected-product-display">
              <el-image 
                :src="selectedActivityProduct?.coverImg || '/placeholder-product.png'" 
                fit="cover"
                style="width: 60px; height: 60px; border-radius: 6px; margin-right: 12px;"
              />
              <div class="product-details">
                <h4>{{ selectedActivityProduct?.productName }}</h4>
                <p>原价: ¥{{ selectedActivityProduct?.price }}</p>
                <p style="color: #67c23a; font-weight: 600;">建议拼团价: ¥{{ selectedActivityProduct?.groupPrice }}</p>
              </div>
            </div>
          </el-form-item>

          <el-form-item label="拼团价" prop="groupPrice">
            <div style="display: flex; align-items: center;">
              <el-input-number 
                v-model="activityForm.groupPrice" 
                :min="selectedActivityProduct?.groupPrice || 0.01" 
                :max="selectedActivityProduct?.price || 999999"
                :precision="2"
                :step="0.1"
                controls-position="right"
                style="width: 200px;"
              />
              <el-button 
                type="success" 
                plain
                @click="useRecommendedPriceInEdit"
                style="margin-left: 12px;"
              >
                <el-icon><PriceTag /></el-icon>
                使用建议拼团价
              </el-button>
            </div>
            <div style="margin-top: 8px; font-size: 13px; color: #e6a23c;">
              <el-icon><Warning /></el-icon>
              拼团价不得低于建议拼团价 ¥{{ selectedActivityProduct?.groupPrice }}
            </div>
          </el-form-item>

          <el-form-item label="成团人数" prop="requiredNum">
            <el-input-number 
              v-model="activityForm.requiredNum" 
              :min="2" 
              :max="100"
              controls-position="right"
              style="width: 200px;"
            />
            <span style="margin-left: 12px; color: #999;">2-100人</span>
          </el-form-item>

          <el-form-item label="最大人数" prop="maxNum">
            <el-input-number 
              v-model="activityForm.maxNum" 
              :min="activityForm.requiredNum || 2" 
              :max="999999"
              controls-position="right"
              style="width: 200px;"
              clearable
            />
            <span style="margin-left: 12px; color: #999;">留空表示无限制</span>
          </el-form-item>

          <el-form-item label="活动时间" required>
            <el-col :span="11">
              <el-form-item prop="startTime">
                <el-date-picker
                  v-model="activityForm.startTime"
                  type="datetime"
                  placeholder="开始时间"
                  format="YYYY-MM-DD HH:mm:ss"
                  value-format="YYYY-MM-DDTHH:mm:ss"
                  style="width: 100%;"
                />
              </el-form-item>
            </el-col>
            <el-col :span="2" style="text-align: center;">~</el-col>
            <el-col :span="11">
              <el-form-item prop="endTime">
                <el-date-picker
                  v-model="activityForm.endTime"
                  type="datetime"
                  placeholder="结束时间"
                  format="YYYY-MM-DD HH:mm:ss"
                  value-format="YYYY-MM-DDTHH:mm:ss"
                  style="width: 100%;"
                />
              </el-form-item>
            </el-col>
          </el-form-item>
        </el-form>
      </div>

      <!-- 步骤3：确认创建 -->
      <div v-if="activityStep === 2 && !isEditingActivity" class="step-content">
        <div class="confirm-content">
          <el-result icon="success" title="请确认活动信息">
            <template #sub-title>
              <div class="confirm-details">
                <div class="confirm-section">
                  <h3><el-icon><ShoppingBag /></el-icon> 商品信息</h3>
                  <div class="info-row">
                    <el-image 
                      :src="selectedActivityProduct?.coverImg" 
                      fit="cover"
                      style="width: 100px; height: 100px; border-radius: 8px;"
                    />
                    <div style="margin-left: 20px;">
                      <p class="info-item"><strong>商品名称：</strong>{{ selectedActivityProduct?.productName }}</p>
                      <p class="info-item"><strong>商品价格：</strong>¥{{ selectedActivityProduct?.price }}</p>
                      <p class="info-item"><strong>库存数量：</strong>{{ selectedActivityProduct?.stock }}件</p>
                    </div>
                  </div>
                </div>

                <el-divider />

                <div class="confirm-section">
                  <h3><el-icon><Setting /></el-icon> 活动设置</h3>
                  <div class="info-grid">
                    <p class="info-item">
                      <strong>拼团价：</strong>
                      <span class="price-highlight">¥{{ activityForm.groupPrice }}</span>
                      <el-tag type="danger" size="small" style="margin-left: 8px;">
                        省¥{{ (selectedActivityProduct?.price - activityForm.groupPrice).toFixed(2) }}
                      </el-tag>
                    </p>
                    <p class="info-item"><strong>成团人数：</strong>{{ activityForm.requiredNum }}人</p>
                    <p class="info-item"><strong>最大人数：</strong>{{ activityForm.maxNum || '无限制' }}</p>
                    <p class="info-item">
                      <strong>活动时间：</strong><br/>
                      {{ formatDateTime(activityForm.startTime) }} ~ {{ formatDateTime(activityForm.endTime) }}
                    </p>
                  </div>
                </div>
              </div>
            </template>
            <template #extra>
              <el-alert 
                type="warning" 
                :closable="false"
                show-icon
              >
                <template #title>
                  <p style="margin: 0;">确认无误后点击"立即创建"，活动创建后可以编辑</p>
                </template>
              </el-alert>
            </template>
          </el-result>
        </div>
      </div>

      <template #footer>
        <div class="dialog-footer">
          <!-- 创建模式按钮 -->
          <template v-if="!isEditingActivity">
            <el-button @click="activityDialogVisible = false">取消</el-button>
            <el-button @click="handleActivityPrevStep" v-if="activityStep > 0">
              <el-icon><ArrowLeft /></el-icon>
              上一步
            </el-button>
            <el-button 
              type="primary" 
              @click="handleActivityNextStep" 
              v-if="activityStep < 2"
            >
              下一步
              <el-icon><ArrowRight /></el-icon>
            </el-button>
            <el-button 
              type="primary" 
              @click="handleSubmitActivity" 
              :loading="activitySubmitLoading"
              v-if="activityStep === 2"
            >
              <el-icon><CircleCheck /></el-icon>
              立即创建
            </el-button>
          </template>
          
          <!-- 编辑模式按钮 -->
          <template v-else>
            <el-button @click="activityDialogVisible = false">取消</el-button>
            <el-button type="primary" @click="handleSubmitActivity" :loading="activitySubmitLoading">
              <el-icon><CircleCheck /></el-icon>
              保存修改
            </el-button>
          </template>
        </div>
      </template>
    </el-dialog>

    <!-- 分享团队对话框 -->
    <el-dialog
      v-model="shareDialogVisible"
      title="分享拼团"
      width="500px"
    >
      <div class="share-content">
        <div class="share-info">
          <h3>{{ shareTeamData?.teamNo }}</h3>
          <p>还差 {{ shareTeamData?.remainNum }} 人成团</p>
        </div>
        
        <div class="share-link">
          <el-input
            v-model="shareLink"
            readonly
          >
            <template #append>
              <el-button @click="copyShareLink">
                <el-icon><DocumentCopy /></el-icon>
                复制
              </el-button>
            </template>
          </el-input>
        </div>

        <div class="share-qrcode">
          <!-- 这里可以集成二维码生成库 -->
          <p style="color: #999;">扫描二维码或复制链接分享给好友</p>
        </div>
      </div>
    </el-dialog>
    </div>
  </MainLayout>
</template>

<script setup>
import { ref, reactive, computed, onMounted, watch, nextTick } from 'vue'
import { useRouter } from 'vue-router'
import { useUserStore } from '@/stores/user'
import { ElMessage, ElMessageBox } from 'element-plus'
import MainLayout from '@/components/common/MainLayout.vue'
import {
  Grid, Plus, Loading, CircleCheck, CircleClose, DataLine,
  Search, RefreshLeft, Location, Clock, Timer, View, Share,
  ShoppingBag, Picture, ArrowLeft, ArrowRight, DocumentCopy,
  Edit, Delete, UserFilled, Promotion, User, Warning, Checked,
  Right, Setting, PriceTag
} from '@element-plus/icons-vue'
import { 
  getLeaderTeams, 
  launchTeam, 
  getProductGroupBuyActivities,
  getActivities,
  createActivity,
  updateActivity,
  deleteActivity as deleteActivityApi
} from '@/api/groupbuy'
import { getProductList } from '@/api/product'
import { getAddressList } from '@/api/user'
import { getMyLeaderInfo } from '@/api/leader'

// 防抖函数
const debounce = (fn, delay = 300) => {
  let timer = null
  return function (...args) {
    if (timer) clearTimeout(timer)
    timer = setTimeout(() => {
      fn.apply(this, args)
    }, delay)
  }
}

const router = useRouter()
const userStore = useUserStore()

// Tab切换
const activeTab = ref('activities') // activities: 活动管理, teams: 我的团队

// 数据定义
const loading = ref(false)
const teams = ref([])
const total = ref(0)
const queryForm = reactive({
  status: 0,  // 默认显示拼团中
  page: 0,  // ⭐后端使用 0-indexed（第一页是 0）
  limit: 12
})

// 活动管理数据
const activitiesTableLoading = ref(false)
const myActivities = ref([])
const activitiesTotal = ref(0)
const activitiesQuery = reactive({
  page: 1,
  limit: 10
})

// TAB状态切换
const activeStatusTab = ref('0') // 默认显示拼团中

// 统计数据 - 全局统计，切换标签时不重新计算
const stats = reactive({
  ongoing: 0,
  success: 0,
  failed: 0,
  total: 0
})

// 创建对话框
const createDialogVisible = ref(false)
const currentStep = ref(0)
const productsLoading = ref(false)
const activitiesLoading = ref(false)
const submitLoading = ref(false)
const products = ref([])
const activities = ref([])
const addresses = ref([])
const productSearch = ref('')
const leaderInfo = ref(null) // 团长团点信息

const createForm = reactive({
  productId: null,
  activityId: null,
  joinImmediately: false,
  addressId: null,
  quantity: 1,
  durationHours: 24
})

const createFormRef = ref(null)
const createRules = {
  addressId: [
    { required: true, message: '请选择收货地址', trigger: 'change' }
  ],
  quantity: [
    { required: true, message: '请输入购买数量', trigger: 'blur' },
    { type: 'number', min: 1, message: '数量至少为1', trigger: 'blur' }
  ],
  durationHours: [
    { required: true, message: '请输入团购持续时间', trigger: 'blur' },
    { type: 'number', min: 1, max: 168, message: '持续时间应在1-168小时之间', trigger: 'blur' }
  ]
}

// 活动对话框
const activityDialogVisible = ref(false)
const activityFormRef = ref(null)
const activitySubmitLoading = ref(false)
const isEditingActivity = ref(false)
const editingActivityId = ref(null)
const activityStep = ref(0) // 创建活动步骤：0-选择商品，1-设置活动，2-确认创建
const activityProductSearch = ref('') // 活动对话框商品搜索

const activityForm = reactive({
  productId: null,
  groupPrice: null,
  requiredNum: 3,
  maxNum: null,
  startTime: '',
  endTime: ''
})

// 活动统计数据
const activityStats = reactive({
  active: 0,      // 进行中的活动数
  totalTeams: 0,  // 总团数
  successTeams: 0 // 已成团数
})

const activityRules = {
  productId: [
    { required: true, message: '请选择商品', trigger: 'change' }
  ],
  groupPrice: [
    { required: true, message: '请输入拼团价', trigger: 'blur' },
    { type: 'number', min: 0.01, message: '拼团价必须大于0', trigger: 'blur' },
    {
      validator: (rule, value, callback) => {
        const product = selectedActivityProduct.value
        if (product && value < product.groupPrice) {
          callback(new Error(`拼团价不得低于建议拼团价 ¥${product.groupPrice}`))
        } else if (product && value >= product.price) {
          callback(new Error('拼团价必须低于商品原价'))
        } else {
          callback()
        }
      },
      trigger: 'blur'
    }
  ],
  requiredNum: [
    { required: true, message: '请输入成团人数', trigger: 'blur' },
    { type: 'number', min: 2, max: 100, message: '成团人数应在2-100之间', trigger: 'blur' }
  ],
  startTime: [
    { required: true, message: '请选择开始时间', trigger: 'change' }
  ],
  endTime: [
    { required: true, message: '请选择结束时间', trigger: 'change' }
  ]
}

const activityDialogTitle = computed(() => 
  isEditingActivity.value ? '编辑拼团活动' : '创建拼团活动'
)

const selectedActivityProduct = computed(() => 
  products.value.find(p => p.productId === activityForm.productId)
)

// 选中的商品和活动
const selectedProduct = computed(() => 
  products.value.find(p => p.productId === createForm.productId)
)

const selectedActivity = computed(() => 
  activities.value.find(a => a.activityId === createForm.activityId)
)

// 分享对话框
const shareDialogVisible = ref(false)
const shareTeamData = ref(null)
const shareLink = ref('')

// 方法定义
const loadTeams = async () => {
  try {
    loading.value = true
    const params = {
      leaderId: userStore.userInfo?.userId,
      ...queryForm
    }
    
    const res = await getLeaderTeams(params)
    if (res.code === 200) {
      teams.value = res.data?.list || []
      total.value = res.data?.total || 0

      // 注意：不再在这里更新全局统计，避免切换标签时数据变化
      // 全局统计在页面初始化时计算一次即可
    }
  } catch (error) {
    console.error('加载团列表失败:', error)
    ElMessage.error('加载失败，请稍后重试')
  } finally {
    loading.value = false
  }
}

// 全局统计计算 - 获取所有团队数据进行统计
const loadGlobalStats = async () => {
  try {
    const res = await getLeaderTeams({
      leaderId: userStore.userInfo?.userId,
      page: 0,
      limit: 1000  // 获取所有团队数据进行统计
    })

    if (res.code === 200) {
      const allTeams = res.data?.list || []

      // 基于所有团队数据计算全局统计
      stats.ongoing = allTeams.filter(t => t.teamStatus === 0).length
      stats.success = allTeams.filter(t => t.teamStatus === 1).length
      stats.failed = allTeams.filter(t => t.teamStatus === 2).length
      stats.total = allTeams.length

      console.log('✅ 全局统计更新:', stats)
    }
  } catch (error) {
    console.error('❌ 加载全局统计失败:', error)
    // 失败时保持现有数据不变
  }
}

// 不再使用的旧方法，保留用于兼容
const updateStats = () => {
  // 这个方法不再更新全局统计，只用于调试
  console.log('updateStats called, but global stats should not change on tab switch')
}

// 主TAB切换（活动管理/我的团队）- 只切换显示，不重新计算统计
const handleMainTabChange = (tabName) => {
  if (tabName === 'activities') {
    loadMyActivities()
  } else if (tabName === 'teams') {
    loadTeams()
  }
  // 注意：不再重新计算全局统计数据，避免切换标签时数据变化
}

// 状态筛选TAB切换
const handleStatusTabChange = (tabName) => {
  // 根据TAB切换设置查询参数
  if (tabName === 'all') {
    queryForm.status = null
  } else {
    // 确保tabName是有效的数字字符串
    const numValue = Number(tabName)
    if (isNaN(numValue)) {
      console.error('Invalid tabName, cannot convert to number:', tabName)
      return
    }
    queryForm.status = numValue
  }
  queryForm.page = 0  // ⭐重置为第一页（0-indexed）
  loadTeams()
}

const handleQuery = () => {
  queryForm.page = 0  // ⭐重置为第一页（0-indexed）
  loadTeams()
}

const handleReset = () => {
  activeStatusTab.value = '0' // 重置为默认TAB（拼团中）
  queryForm.status = 0
  handleQuery()
}

const handlePageChange = (page) => {
  queryForm.page = page - 1  // ⭐Element Plus 从 1 开始，转换为 0-indexed
  loadTeams()
}

const handleSizeChange = (size) => {
  queryForm.limit = size
  queryForm.page = 0  // ⭐重置为第一页（0-indexed）
  loadTeams()
}

const showCreateDialog = () => {
  createDialogVisible.value = true
  currentStep.value = 0
  resetCreateForm()
  loadProducts()
}

const resetCreateForm = () => {
  Object.assign(createForm, {
    productId: null,
    activityId: null,
    joinImmediately: false,
    addressId: null,
    quantity: 1,
    durationHours: 24
  })
  products.value = []
  activities.value = []
}

const loadProducts = async () => {
  try {
    productsLoading.value = true
    // API的page从0开始，size默认10，这里获取前100个商品
    const params = { 
      page: 0, 
      size: 100,
      sort: 'create_time'
    }
    
    // 如果有搜索关键词，添加到参数中
    if (productSearch.value && productSearch.value.trim()) {
      params.keyword = productSearch.value.trim()
    }
    
    const res = await getProductList(params)
    if (res.code === 200) {
      // 根据API_ProductService.md，商品列表在data.content字段中
      products.value = res.data?.content || []
      
      if (products.value.length === 0 && productSearch.value) {
        ElMessage.info('未找到相关商品')
      }
    }
  } catch (error) {
    console.error('加载商品失败:', error)
    ElMessage.error('加载商品失败')
  } finally {
    productsLoading.value = false
  }
}

// 使用防抖优化搜索
const handleProductSearch = debounce(() => {
  loadProducts()
}, 500)

const selectProduct = (product) => {
  createForm.productId = product.productId
}

const loadActivities = async () => {
  if (!createForm.productId) {
    ElMessage.warning('请先选择商品')
    return
  }

  try {
    activitiesLoading.value = true
    const params = {
      communityId: userStore.userInfo?.communityId
    }
    
    const res = await getProductGroupBuyActivities(createForm.productId, params)
    if (res.code === 200) {
      activities.value = res.data || []
      
      if (activities.value.length === 0) {
        ElMessage.warning('该商品暂无进行中的拼团活动')
      }
    }
  } catch (error) {
    console.error('加载活动失败:', error)
    ElMessage.error('加载活动失败')
  } finally {
    activitiesLoading.value = false
  }
}

const selectActivity = (activity) => {
  createForm.activityId = activity.activityId
}

// 加载团长信息
const loadLeaderInfo = async () => {
  try {
    const userId = userStore.userInfo?.userId
    if (!userId) return
    
    const res = await getMyLeaderInfo(userId)
    if (res.code === 200) {
      leaderInfo.value = res.data
    }
  } catch (error) {
    console.error('加载团长信息失败:', error)
  }
}

const loadAddresses = async () => {
  try {
    const userId = userStore.userInfo?.userId
    if (!userId) {
      ElMessage.warning('请先登录')
      return
    }
    
    const res = await getAddressList(userId)
    if (res.code === 200) {
      addresses.value = res.data || []
      
      // 自动选择默认地址
      const defaultAddress = addresses.value.find(a => a.isDefault)
      if (defaultAddress) {
        createForm.addressId = defaultAddress.addressId
      }
    }
  } catch (error) {
    console.error('加载地址失败:', error)
  }
}

const handleJoinImmediatelyChange = (value) => {
  if (value) {
    loadAddresses()
  }
}

const handleManageAddress = () => {
  window.open('/user/address', '_blank')
}

const handlePrevStep = () => {
  if (currentStep.value > 0) {
    currentStep.value--
  }
}

const handleNextStep = () => {
  if (currentStep.value === 0) {
    if (!createForm.productId) {
      ElMessage.warning('请选择商品')
      return
    }
    currentStep.value++
    loadActivities()
  } else if (currentStep.value === 1) {
    if (!createForm.activityId) {
      ElMessage.warning('请选择拼团活动')
      return
    }
    currentStep.value++
    
    // 如果开启了立即参与，加载地址
    if (createForm.joinImmediately) {
      loadAddresses()
    }
  }
}

const handleSubmitCreate = async () => {
  // 如果立即参与，验证表单
  if (createForm.joinImmediately) {
    const valid = await createFormRef.value?.validate().catch(() => false)
    if (!valid) return
  }

  try {
    submitLoading.value = true
    
    const data = {
      activityId: createForm.activityId,
      joinImmediately: createForm.joinImmediately,
      addressId: createForm.addressId,
      quantity: createForm.quantity,
      durationHours: createForm.durationHours
    }
    
    const res = await launchTeam(data)
    if (res.code === 200) {
      createDialogVisible.value = false
      
      // 如果立即参与，支付已自动完成
      if (createForm.joinImmediately) {
        ElMessageBox.alert(
          '团队创建成功！您已成功参与拼团并完成支付。',
          '发起成功',
          {
            confirmButtonText: '知道了',
            type: 'success'
          }
        ).then(() => {
          // 刷新列表和全局统计
          loadTeams()
          loadGlobalStats() // 更新统计数据
        })
      } else {
        ElMessage.success('发起成功！')
        // 刷新列表和全局统计
        loadTeams()
        loadGlobalStats() // 更新统计数据
        
        // 显示分享提示
        if (res.data?.shareLink) {
          ElMessageBox.alert(
            '团队创建成功！快去分享给好友一起拼团吧',
            '发起成功',
            {
              confirmButtonText: '去分享',
              type: 'success'
            }
          ).then(() => {
            shareTeam(res.data)
          })
        }
      }
    }
  } catch (error) {
    console.error('发起失败:', error)
    ElMessage.error(error.message || '发起失败，请稍后重试')
  } finally {
    submitLoading.value = false
  }
}

const viewTeamDetail = (teamId) => {
  router.push(`/leader/team/${teamId}`)
}

const shareTeam = (team) => {
  shareTeamData.value = team
  shareLink.value = team.shareLink || `${window.location.origin}/team/${team.teamId}`
  shareDialogVisible.value = true
}

const copyShareLink = () => {
  navigator.clipboard.writeText(shareLink.value).then(() => {
    ElMessage.success('链接已复制到剪贴板')
  }).catch(() => {
    ElMessage.error('复制失败，请手动复制')
  })
}

// 工具方法
const getStatusClass = (status) => {
  const map = {
    0: 'status-ongoing',
    1: 'status-success',
    2: 'status-failed'
  }
  return map[status] || ''
}

const getStatusText = (status) => {
  const map = {
    0: '拼团中',
    1: '已成团',
    2: '已失败'
  }
  return map[status] || '未知'
}

const getProgressPercentage = (team) => {
  return Math.round((team.currentNum / team.requiredNum) * 100)
}

const getProgressStatus = (status) => {
  const map = {
    0: null,
    1: 'success',
    2: 'exception'
  }
  return map[status]
}

const formatDateTime = (dateTime) => {
  if (!dateTime) return '-'
  const date = new Date(dateTime)
  const month = String(date.getMonth() + 1).padStart(2, '0')
  const day = String(date.getDate()).padStart(2, '0')
  const hours = String(date.getHours()).padStart(2, '0')
  const minutes = String(date.getMinutes()).padStart(2, '0')
  return `${month}-${day} ${hours}:${minutes}`
}

const formatExpireTime = (expireTime) => {
  if (!expireTime) return '未知'
  
  const now = new Date()
  const expire = new Date(expireTime)
  const diff = expire - now
  
  if (diff <= 0) return '已过期'
  
  const hours = Math.floor(diff / (1000 * 60 * 60))
  const minutes = Math.floor((diff % (1000 * 60 * 60)) / (1000 * 60))
  
  if (hours > 24) {
    const days = Math.floor(hours / 24)
    return `${days}天后过期`
  } else if (hours > 0) {
    return `${hours}小时${minutes}分钟后过期`
  } else {
    return `${minutes}分钟后过期`
  }
}


// 活动管理相关方法
const loadMyActivities = async () => {
  try {
    activitiesTableLoading.value = true
    const res = await getActivities()
    if (res.code === 200) {
      myActivities.value = res.data || []
      
      // 获取商品信息
      const productIds = [...new Set(myActivities.value.map(a => a.productId))]
      if (productIds.length > 0) {
        const productsRes = await getProductList({ page: 0, size: 100 })
        if (productsRes.code === 200) {
          const productsMap = {}
          productsRes.data?.content?.forEach(p => {
            productsMap[p.productId] = p
          })
          
          myActivities.value = myActivities.value.map(activity => ({
            ...activity,
            product: productsMap[activity.productId]
          }))
        }
      }
      
      // 注意：活动管理标签不重新计算全局统计，避免数据变化
      // 全局统计在页面初始化时已计算
      
      // 计算活动统计（进行中的活动数量）
      activityStats.active = myActivities.value.filter(a => a.status === 1).length
      
      activitiesTotal.value = myActivities.value.length
    }
  } catch (error) {
    console.error('加载活动列表失败:', error)
    ElMessage.error('加载活动列表失败')
  } finally {
    activitiesTableLoading.value = false
  }
}

/**
 * 加载团队统计数据（真实API）
 */
const loadTeamStats = async () => {
  try {
    // 调用团长团队列表API获取所有团队数据
    const res = await getLeaderTeams({
      leaderId: userStore.userInfo.userId,
      page: 0,
      limit: 1000  // 获取所有团队用于统计
    })
    
    if (res.code === 200) {
      const allTeams = res.data?.list || []
      
      // 统计总团数和已成团数
      activityStats.totalTeams = allTeams.length
      activityStats.successTeams = allTeams.filter(team => team.teamStatus === 1).length
      
      // 为每个活动计算团队统计
      const activityTeamsMap = {}
      allTeams.forEach(team => {
        if (!activityTeamsMap[team.activityId]) {
          activityTeamsMap[team.activityId] = {
            ongoing: 0,   // 拼团中
            success: 0,   // 已成团
            failed: 0,    // 已失败
            total: 0      // 总计
          }
        }
        
        activityTeamsMap[team.activityId].total++
        
        if (team.teamStatus === 0) {
          activityTeamsMap[team.activityId].ongoing++
        } else if (team.teamStatus === 1) {
          activityTeamsMap[team.activityId].success++
        } else if (team.teamStatus === 2) {
          activityTeamsMap[team.activityId].failed++
        }
      })
      
      // 将统计数据关联到活动
      myActivities.value = myActivities.value.map(activity => ({
        ...activity,
        teamStats: activityTeamsMap[activity.activityId] || {
          ongoing: 0,
          success: 0,
          failed: 0,
          total: 0
        }
      }))
      
      console.log('✅ 团队统计加载成功:', {
        totalTeams: activityStats.totalTeams,
        successTeams: activityStats.successTeams,
        activitiesWithStats: myActivities.value.length
      })
    }
  } catch (error) {
    console.error('❌ 加载团队统计失败:', error)
    // 不中断流程，使用默认值
    activityStats.totalTeams = 0
    activityStats.successTeams = 0
  }
}

const handleActivitiesPageChange = (page) => {
  activitiesQuery.page = page
  loadMyActivities()
}

const handleActivitiesSizeChange = (size) => {
  activitiesQuery.limit = size
  activitiesQuery.page = 1
  loadMyActivities()
}

const getActivityStatusType = (status) => {
  const map = {
    0: 'info',
    1: 'success',
    2: 'danger'
  }
  return map[status] || 'info'
}

const getActivityStatusText = (status) => {
  const map = {
    0: '未开始',
    1: '进行中',
    2: '已结束'
  }
  return map[status] || '未知'
}

// 跳转到创建活动页面
const showActivityDialog = () => {
  router.push('/leader/activity/create')
}

const resetActivityForm = () => {
  Object.assign(activityForm, {
    productId: null,
    groupPrice: null,
    requiredNum: 3,
    maxNum: null,
    startTime: '',
    endTime: ''
  })
  activityProductSearch.value = ''
  activityFormRef.value?.clearValidate()
}

// 选择活动商品
const selectActivityProduct = (product) => {
  activityForm.productId = product.productId
  // 自动填充拼团价（原价的80%）
  activityForm.groupPrice = Number((product.price * 0.8).toFixed(2))
}

// 活动对话框商品搜索（使用防抖）
const handleActivityProductSearch = debounce(() => {
  loadProducts()
}, 500)

const handleActivityProductChange = (productId) => {
  const product = products.value.find(p => p.productId === productId)
  if (product && !activityForm.groupPrice) {
    activityForm.groupPrice = Number((product.price * 0.8).toFixed(2))
  }
}

// 活动步骤导航
const handleActivityPrevStep = () => {
  if (activityStep.value > 0) {
    activityStep.value--
  }
}

const handleActivityNextStep = async () => {
  if (activityStep.value === 0) {
    // 步骤1验证：必须选择商品
    if (!activityForm.productId) {
      ElMessage.warning('请选择商品')
      return
    }
    activityStep.value = 1
  } else if (activityStep.value === 1) {
    // 步骤2验证：验证表单
    const valid = await activityFormRef.value?.validate().catch(() => false)
    if (!valid) return
    
    // 验证拼团价
    const product = selectedActivityProduct.value
    if (product && activityForm.groupPrice >= product.price) {
      ElMessage.warning('拼团价必须低于商品原价')
      return
    }
    
    // 验证时间
    if (activityForm.startTime >= activityForm.endTime) {
      ElMessage.warning('开始时间必须早于结束时间')
      return
    }
    
    activityStep.value = 2
  }
}

// 获取活动状态样式类
const getActivityStatusClass = (status) => {
  const map = {
    0: 'status-pending',
    1: 'status-active',
    2: 'status-ended'
  }
  return map[status] || ''
}

// 查看活动的团队
const viewActivityTeams = (activity) => {
  activeTab.value = 'teams'
  // 可以添加筛选逻辑，只显示该活动的团队
  ElMessage.info(`切换到团购管理，查看活动 ${activity.activityId} 的团队`)
}

// 编辑活动
const editActivity = (activity) => {
  isEditingActivity.value = true
  editingActivityId.value = activity.activityId
  
  Object.assign(activityForm, {
    productId: activity.productId,
    groupPrice: activity.groupPrice,
    requiredNum: activity.requiredNum,
    maxNum: activity.maxNum,
    startTime: activity.startTime,
    endTime: activity.endTime
  })
  
  activityDialogVisible.value = true
  
  // 加载商品列表
  if (products.value.length === 0) {
    loadProducts()
  }
}

const useRecommendedPriceInEdit = () => {
  const product = selectedActivityProduct.value
  if (product) {
    activityForm.groupPrice = product.groupPrice
    ElMessage.success(`已设置为建议拼团价 ¥${product.groupPrice}`)
  }
}

// 删除活动
const deleteActivity = (activity) => {
  ElMessageBox.confirm(
    `确定要删除活动ID ${activity.activityId} 吗？删除后将无法恢复。`,
    '确认删除',
    {
      confirmButtonText: '确定删除',
      cancelButtonText: '取消',
      type: 'warning'
    }
  ).then(async () => {
    try {
      const res = await deleteActivityApi(activity.activityId)
      if (res.code === 200) {
        ElMessage.success('删除成功')
        loadMyActivities()
        // 删除活动可能影响团队统计，需要刷新全局统计
        loadGlobalStats()
      }
    } catch (error) {
      console.error('删除失败:', error)
      ElMessage.error(error.message || '删除失败')
    }
  }).catch(() => {
    // 用户取消
  })
}

// 从活动发起团
const launchFromActivity = async (activity) => {
  // 切换到团队管理标签
  activeTab.value = 'teams'
  
  // 重置表单
  resetCreateForm()
  
  // 自动填充活动信息
  createForm.activityId = activity.activityId
  createForm.productId = activity.productId
  
  // 加载必要的数据
  await Promise.all([
    loadProducts(),
    loadActivities()
  ])
  
  // 显示发起团对话框，直接跳到步骤3（确认信息）
  createDialogVisible.value = true
  currentStep.value = 2
  
  ElMessage.success('已为您填充活动信息，请完成发起设置')
}

// 提交活动
const handleSubmitActivity = async () => {
  const valid = await activityFormRef.value?.validate().catch(() => false)
  if (!valid) return
  
  // 验证拼团价必须低于原价
  const product = selectedActivityProduct.value
  if (product && activityForm.groupPrice >= product.price) {
    ElMessage.warning('拼团价必须低于商品原价')
    return
  }
  
  // 验证时间范围
  if (activityForm.startTime >= activityForm.endTime) {
    ElMessage.warning('开始时间必须早于结束时间')
    return
  }
  
  try {
    activitySubmitLoading.value = true
    
    const data = {
      productId: activityForm.productId,
      groupPrice: activityForm.groupPrice,
      requiredNum: activityForm.requiredNum,
      maxNum: activityForm.maxNum || null,
      startTime: activityForm.startTime,
      endTime: activityForm.endTime
    }
    
    let res
    if (isEditingActivity.value) {
      res = await updateActivity(editingActivityId.value, data)
    } else {
      res = await createActivity(data)
    }
    
    if (res.code === 200) {
      ElMessage.success(isEditingActivity.value ? '修改成功' : '创建成功')
      activityDialogVisible.value = false
      loadMyActivities()
      // 活动变更可能影响团队统计
      loadGlobalStats()
    }
  } catch (error) {
    console.error('操作失败:', error)
    ElMessage.error(error.message || '操作失败')
  } finally {
    activitySubmitLoading.value = false
  }
}

// 加载页面数据
const loadPageData = async () => {
  // 确保用户信息已加载
  if (!userStore.isLogin || !userStore.userInfo?.userId) {
    console.log('用户信息未准备好，跳过数据加载')
    return
  }

  // 加载团长信息
  await loadLeaderInfo()

  // 先计算一次全局统计数据（基于所有团队数据）
  await loadGlobalStats()

  // 设置默认查询状态为拼团中
  activeStatusTab.value = '0'
  queryForm.status = 0

  // 等待DOM更新完成
  await nextTick()

  if (activeTab.value === 'activities') {
    loadMyActivities()
  } else {
    loadTeams()
  }
}

// 生命周期
onMounted(() => {
  loadPageData()
})

// 监听用户登录状态变化
watch(() => userStore.isLogin, (isLogin) => {
  if (isLogin) {
    loadPageData()
  }
})
</script>

<style scoped lang="scss">
.leader-groupbuy-manage {
  max-width: 1400px;
  margin: 0 auto;
  padding: 24px;
  min-height: calc(100vh - 60px);
  background: linear-gradient(to bottom, #f8f9fa 0%, #ffffff 300px);

  .page-header {
    display: flex;
    justify-content: space-between;
    align-items: center;
    margin-bottom: 32px;
    padding: 24px 32px;
    background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
    border-radius: 16px;
    box-shadow: 0 8px 24px rgba(102, 126, 234, 0.25);
    position: relative;
    overflow: hidden;

    &::before {
      content: '';
      position: absolute;
      top: -50%;
      right: -10%;
      width: 300px;
      height: 300px;
      background: radial-gradient(circle, rgba(255, 255, 255, 0.1) 0%, transparent 70%);
      border-radius: 50%;
    }

    .header-content {
      position: relative;
      z-index: 1;
      
      .page-title {
        display: flex;
        align-items: center;
        gap: 12px;
        font-size: 30px;
        font-weight: 700;
        color: #ffffff;
        margin: 0 0 8px 0;
        text-shadow: 0 2px 4px rgba(0, 0, 0, 0.1);

        .el-icon {
          font-size: 36px;
          color: #ffffff;
          filter: drop-shadow(0 2px 4px rgba(0, 0, 0, 0.1));
        }
      }

      .page-desc {
        margin: 0;
        color: rgba(255, 255, 255, 0.9);
        font-size: 15px;
        letter-spacing: 0.5px;
      }
    }

    .el-button {
      position: relative;
      z-index: 1;
      background: rgba(255, 255, 255, 0.95);
      border: none;
      color: #667eea;
      font-weight: 600;
      padding: 14px 28px;
      box-shadow: 0 4px 12px rgba(0, 0, 0, 0.15);
      transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1);

      &:hover {
        background: #ffffff;
        transform: translateY(-2px);
        box-shadow: 0 6px 20px rgba(0, 0, 0, 0.2);
      }

      &:active {
        transform: translateY(0);
      }
    }
  }

  // Tab导航样式
  .tab-card {
    margin-bottom: 28px;
    border-radius: 12px;
    border: none;
    box-shadow: 0 2px 12px rgba(0, 0, 0, 0.06);

    .tab-label {
      display: flex;
      align-items: center;
      gap: 8px;
      font-size: 15px;
      font-weight: 500;
      transition: all 0.3s ease;

      &:hover {
        color: var(--el-color-primary);
      }
    }

    :deep(.el-tabs__item.is-active) {
      font-weight: 600;
    }

    :deep(.el-tabs__active-bar) {
      height: 3px;
      border-radius: 3px 3px 0 0;
    }
  }

  // 活动管理区域
  .activities-section {
    animation: fadeIn 0.5s ease;

    // 活动统计
    .activity-stats {
      display: grid;
      grid-template-columns: repeat(auto-fit, minmax(220px, 1fr));
      gap: 20px;
      margin-bottom: 28px;

      .stat-item {
        border-radius: 12px;
        border: none;
        transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1);
        cursor: pointer;
        background: white;

        &:hover {
          transform: translateY(-6px);
          box-shadow: 0 12px 28px rgba(0, 0, 0, 0.12);
        }

        .stat-content {
          display: flex;
          align-items: center;
          gap: 18px;
          padding: 4px;

          .stat-icon {
            width: 64px;
            height: 64px;
            border-radius: 14px;
            display: flex;
            align-items: center;
            justify-content: center;
            font-size: 28px;
            position: relative;
            overflow: hidden;
            transition: transform 0.3s ease;

            &::before {
              content: '';
              position: absolute;
              top: -50%;
              left: -50%;
              width: 200%;
              height: 200%;
              background: radial-gradient(circle, rgba(255, 255, 255, 0.2) 0%, transparent 70%);
              opacity: 0;
              transition: opacity 0.3s ease;
            }

            &:hover::before {
              opacity: 1;
            }

            &.active {
              background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
              color: white;
              box-shadow: 0 8px 16px rgba(102, 126, 234, 0.3);
            }

            &.teams {
              background: linear-gradient(135deg, #f093fb 0%, #f5576c 100%);
              color: white;
              box-shadow: 0 8px 16px rgba(240, 147, 251, 0.3);
            }

            &.success {
              background: linear-gradient(135deg, #4facfe 0%, #00f2fe 100%);
              color: white;
              box-shadow: 0 8px 16px rgba(79, 172, 254, 0.3);
            }
          }

          .stat-info {
            flex: 1;

            .stat-value {
              font-size: 32px;
              font-weight: 700;
              background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
              -webkit-background-clip: text;
              -webkit-text-fill-color: transparent;
              background-clip: text;
              line-height: 1.2;
              margin-bottom: 8px;
            }

            .stat-label {
              font-size: 14px;
              color: #909399;
              font-weight: 500;
              letter-spacing: 0.5px;
            }
          }
        }

        &:hover .stat-icon {
          transform: scale(1.05) rotate(5deg);
        }
      }
    }

    .activities-list {
      .activities-grid {
        display: grid;
        grid-template-columns: repeat(auto-fill, minmax(480px, 1fr));
        gap: 24px;

        .activity-card {
          position: relative;
          border-radius: 16px;
          border: none;
          overflow: hidden;
          transition: all 0.4s cubic-bezier(0.4, 0, 0.2, 1);
          background: white;
          box-shadow: 0 4px 16px rgba(0, 0, 0, 0.08);

          &::before {
            content: '';
            position: absolute;
            top: 0;
            left: 0;
            right: 0;
            height: 4px;
            background: linear-gradient(90deg, #667eea 0%, #764ba2 50%, #f093fb 100%);
            transform: scaleX(0);
            transform-origin: left;
            transition: transform 0.4s ease;
          }

          &:hover {
            transform: translateY(-8px);
            box-shadow: 0 20px 40px rgba(0, 0, 0, 0.15);

            &::before {
              transform: scaleX(1);
            }
          }

          .activity-status-badge {
            position: absolute;
            top: 16px;
            right: 16px;
            padding: 8px 20px;
            border-radius: 24px;
            font-size: 13px;
            font-weight: 600;
            z-index: 2;
            backdrop-filter: blur(10px);
            box-shadow: 0 4px 12px rgba(0, 0, 0, 0.1);
            transition: all 0.3s ease;

            &.status-pending {
              background: linear-gradient(135deg, rgba(230, 247, 255, 0.95) 0%, rgba(186, 231, 255, 0.95) 100%);
              color: #1890ff;
              border: 1px solid rgba(24, 144, 255, 0.2);
            }

            &.status-active {
              background: linear-gradient(135deg, rgba(246, 255, 237, 0.95) 0%, rgba(183, 235, 143, 0.95) 100%);
              color: #52c41a;
              border: 1px solid rgba(82, 196, 26, 0.2);
              animation: pulse 2s infinite;
            }

            &.status-ended {
              background: linear-gradient(135deg, rgba(255, 241, 240, 0.95) 0%, rgba(255, 204, 199, 0.95) 100%);
              color: #ff4d4f;
              border: 1px solid rgba(255, 77, 79, 0.2);
            }

            &:hover {
              transform: scale(1.05);
            }
          }

          .activity-content {
            .product-section {
              display: flex;
              align-items: flex-start;
              padding-bottom: 16px;
              border-bottom: 1px solid #f0f0f0;
              margin-bottom: 16px;

              .product-image {
                width: 100px;
                height: 100px;
                border-radius: 8px;
                flex-shrink: 0;
              }

              .product-info {
                flex: 1;
                margin-left: 16px;

                .product-name {
                  font-size: 16px;
                  font-weight: 600;
                  color: #303133;
                  margin: 0 0 8px 0;
                  line-height: 1.4;
                }

                .product-meta {
                  display: flex;
                  flex-direction: column;
                  gap: 6px;

                  .meta-item {
                    display: flex;
                    align-items: center;
                    gap: 4px;
                    font-size: 13px;
                    color: #909399;

                    .el-icon {
                      font-size: 14px;
                    }
                  }
                }
              }

              .image-error {
                width: 100px;
                height: 100px;
                display: flex;
                align-items: center;
                justify-content: center;
                background: #f5f7fa;
                color: #c0c4cc;
                border-radius: 8px;
                font-size: 32px;
              }
            }

            .price-section {
              display: flex;
              align-items: center;
              justify-content: space-around;
              padding: 20px;
              background: linear-gradient(135deg, #fff5f7 0%, #ffe9f0 50%, #fff5f7 100%);
              border-radius: 12px;
              margin-bottom: 16px;
              position: relative;
              overflow: hidden;

              &::before {
                content: '';
                position: absolute;
                top: 0;
                left: 0;
                right: 0;
                bottom: 0;
                background: 
                  radial-gradient(circle at 20% 50%, rgba(255, 105, 135, 0.1) 0%, transparent 50%),
                  radial-gradient(circle at 80% 50%, rgba(255, 179, 186, 0.1) 0%, transparent 50%);
                pointer-events: none;
              }

              .price-item {
                display: flex;
                flex-direction: column;
                align-items: center;
                position: relative;
                z-index: 1;

                .price-label {
                  font-size: 13px;
                  color: #909399;
                  margin-bottom: 6px;
                  font-weight: 500;
                  letter-spacing: 0.5px;
                }

                .group-price {
                  font-size: 28px;
                  font-weight: 800;
                  background: linear-gradient(135deg, #ff6b81 0%, #ff4d4f 100%);
                  -webkit-background-clip: text;
                  -webkit-text-fill-color: transparent;
                  background-clip: text;
                  text-shadow: 0 2px 8px rgba(255, 77, 79, 0.2);
                  animation: priceGlow 2s ease-in-out infinite;
                }

                .original-price {
                  font-size: 18px;
                  color: #999;
                  text-decoration: line-through;
                  font-weight: 500;
                }
              }

              .price-divider {
                font-size: 24px;
                color: rgba(220, 223, 230, 0.6);
                position: relative;
                z-index: 1;
              }

              .discount-badge {
                padding: 6px 16px;
                background: linear-gradient(135deg, #ff6b81 0%, #ff4d4f 100%);
                color: white;
                border-radius: 20px;
                font-size: 14px;
                font-weight: 700;
                box-shadow: 0 4px 12px rgba(255, 77, 79, 0.3);
                position: relative;
                z-index: 1;
                animation: tada 2s ease infinite;
              }
            }

            .activity-details {
              margin-bottom: 16px;

              .detail-row {
                display: flex;
                gap: 20px;
                margin-bottom: 12px;

                &.time-info {
                  align-items: center;
                  background: #f5f7fa;
                  padding: 12px;
                  border-radius: 6px;

                  .time-separator {
                    color: #909399;
                    margin: 0 8px;
                  }
                }

                .detail-item {
                  display: flex;
                  align-items: center;
                  gap: 6px;
                  font-size: 14px;
                  color: #606266;

                  .el-icon {
                    font-size: 16px;
                    color: var(--el-color-primary);
                  }
                }

                .time-item {
                  display: flex;
                  align-items: center;
                  gap: 6px;
                  font-size: 13px;
                  color: #606266;

                  .el-icon {
                    font-size: 14px;
                  }
                }
              }
            }

            .team-stats {
              display: flex;
              gap: 12px;
              margin-bottom: 16px;

              .stat-badge {
                flex: 1;
                display: flex;
                flex-direction: column;
                align-items: center;
                padding: 12px;
                border-radius: 8px;
                transition: all 0.3s ease;

                &:hover {
                  transform: scale(1.05);
                }

                &.ongoing {
                  background: #e6f7ff;
                  border: 1px solid #91d5ff;
                }

                &.success {
                  background: #f6ffed;
                  border: 1px solid #b7eb8f;
                }

                &.total {
                  background: #fff7e6;
                  border: 1px solid #ffd591;
                }

                .badge-label {
                  font-size: 12px;
                  color: #606266;
                  margin-bottom: 4px;
                }

                .badge-value {
                  font-size: 20px;
                  font-weight: 700;
                  color: #303133;
                }
              }
            }

            .activity-actions {
              display: flex;
              gap: 8px;
              padding-top: 16px;
              border-top: 1px solid #f0f0f0;

              .el-button {
                flex: 1;
              }
            }
          }
        }
      }
    }
  }

  .stats-cards {
    display: grid;
    grid-template-columns: repeat(auto-fit, minmax(260px, 1fr));
    gap: 20px;
    margin-bottom: 28px;

    .stat-card {
      border-radius: 14px;
      border: none;
      box-shadow: 0 4px 16px rgba(0, 0, 0, 0.08);
      transition: all 0.4s cubic-bezier(0.4, 0, 0.2, 1);
      cursor: pointer;
      overflow: hidden;
      position: relative;

      &::after {
        content: '';
        position: absolute;
        top: 0;
        left: 0;
        right: 0;
        height: 3px;
        background: linear-gradient(90deg, transparent 0%, var(--accent-color) 50%, transparent 100%);
        transform: scaleX(0);
        transition: transform 0.4s ease;
      }

      &:hover {
        transform: translateY(-8px);
        box-shadow: 0 12px 32px rgba(0, 0, 0, 0.12);

        &::after {
          transform: scaleX(1);
        }

        .stat-icon {
          transform: scale(1.08);
        }
      }

      .stat-content {
        display: flex;
        align-items: center;
        gap: 18px;
        padding: 4px;

        .stat-icon {
          width: 68px;
          height: 68px;
          border-radius: 14px;
          display: flex;
          align-items: center;
          justify-content: center;
          font-size: 30px;
          transition: transform 0.3s ease;
          position: relative;
          overflow: hidden;

          &::before {
            content: '';
            position: absolute;
            inset: 0;
            background: radial-gradient(circle at center, rgba(255, 255, 255, 0.3) 0%, transparent 70%);
            opacity: 0;
            transition: opacity 0.3s ease;
          }

          &:hover::before {
            opacity: 1;
          }

          &.ongoing {
            background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
            color: white;
            box-shadow: 0 8px 16px rgba(102, 126, 234, 0.3);
            --accent-color: #667eea;
          }

          &.success {
            background: linear-gradient(135deg, #48c774 0%, #06b368 100%);
            color: white;
            box-shadow: 0 8px 16px rgba(72, 199, 116, 0.3);
            --accent-color: #48c774;
          }

          &.failed {
            background: linear-gradient(135deg, #f093fb 0%, #f5576c 100%);
            color: white;
            box-shadow: 0 8px 16px rgba(240, 147, 251, 0.3);
            --accent-color: #f093fb;
          }

          &.total {
            background: linear-gradient(135deg, #4facfe 0%, #00f2fe 100%);
            color: white;
            box-shadow: 0 8px 16px rgba(79, 172, 254, 0.3);
            --accent-color: #4facfe;
          }
        }

        .stat-info {
          flex: 1;

          .stat-value {
            font-size: 36px;
            font-weight: 800;
            color: #303133;
            line-height: 1.1;
            margin-bottom: 8px;
            background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
            -webkit-background-clip: text;
            -webkit-text-fill-color: transparent;
            background-clip: text;
          }

          .stat-label {
            font-size: 14px;
            color: #909399;
            font-weight: 500;
            letter-spacing: 0.5px;
          }
        }
      }
    }
  }

  .filter-tabs-card {
    margin-bottom: 28px;
    border-radius: 12px;
    border: none;
    box-shadow: 0 2px 12px rgba(0, 0, 0, 0.06);

    :deep(.el-tabs) {
      --el-tabs-header-height: 50px;
    }

    :deep(.el-tabs__header) {
      margin: 0;
      border-bottom: 2px solid #f0f0f0;
    }

    :deep(.el-tabs__nav-wrap::after) {
      display: none;
    }

    :deep(.el-tabs__item) {
      height: 50px;
      line-height: 48px;
      font-size: 15px;
      font-weight: 500;
      border: none;
      transition: all 0.3s ease;

      &:hover {
        color: var(--el-color-primary);
        background-color: rgba(64, 158, 255, 0.05);
      }

      &.is-active {
        color: var(--el-color-primary);
        font-weight: 600;
        background-color: rgba(64, 158, 255, 0.1);
        border-bottom: 3px solid var(--el-color-primary);
      }
    }

    .tab-label {
      display: flex;
      align-items: center;
      gap: 8px;

      .el-icon {
        font-size: 16px;
      }
    }

    .tab-badge {
      margin-left: 4px;
      font-size: 12px;
    }
  }

  .teams-section {
    animation: fadeIn 0.5s ease;
  }

  .teams-list {
    .teams-grid {
      display: grid;
      grid-template-columns: repeat(auto-fill, minmax(380px, 1fr));
      gap: 24px;
      margin-bottom: 28px;

      .team-card {
        cursor: pointer;
        position: relative;
        border-radius: 14px;
        border: none;
        overflow: hidden;
        background: white;
        box-shadow: 0 4px 16px rgba(0, 0, 0, 0.08);
        transition: all 0.4s cubic-bezier(0.4, 0, 0.2, 1);

        &::before {
          content: '';
          position: absolute;
          top: 0;
          left: 0;
          right: 0;
          height: 3px;
          background: linear-gradient(90deg, #667eea 0%, #764ba2 50%, #f093fb 100%);
          transform: scaleX(0);
          transform-origin: left;
          transition: transform 0.4s ease;
        }

        &:hover {
          transform: translateY(-8px);
          box-shadow: 0 16px 32px rgba(0, 0, 0, 0.15);

          &::before {
            transform: scaleX(1);
          }

          .team-status-badge {
            transform: scale(1.05);
          }
        }

        .team-status-badge {
          position: absolute;
          top: 70px;
          right: 16px;
          padding: 6px 16px;
          border-radius: 20px;
          font-size: 12px;
          font-weight: 600;
          z-index: 2;
          backdrop-filter: blur(8px);
          box-shadow: 0 4px 12px rgba(0, 0, 0, 0.1);
          transition: all 0.3s ease;

          &.status-ongoing {
            background: linear-gradient(135deg, rgba(230, 247, 255, 0.95) 0%, rgba(186, 231, 255, 0.95) 100%);
            color: #1890ff;
            border: 1px solid rgba(24, 144, 255, 0.2);
            animation: pulse 2s infinite;
          }

          &.status-success {
            background: linear-gradient(135deg, rgba(246, 255, 237, 0.95) 0%, rgba(183, 235, 143, 0.95) 100%);
            color: #52c41a;
            border: 1px solid rgba(82, 196, 26, 0.2);
          }

          &.status-failed {
            background: linear-gradient(135deg, rgba(255, 241, 240, 0.95) 0%, rgba(255, 204, 199, 0.95) 100%);
            color: #ff4d4f;
            border: 1px solid rgba(255, 77, 79, 0.2);
          }
        }

        .team-info {
          .team-header {
            display: flex;
            justify-content: space-between;
            align-items: flex-start;
            margin-bottom: 16px;

            .team-no {
              font-size: 18px;
              font-weight: 600;
              color: #303133;
              margin: 0;
            }

            .team-price {
              display: flex;
              flex-direction: column;
              align-items: flex-end;

              .price-label {
                font-size: 12px;
                color: #909399;
              }

              .price-value {
                font-size: 20px;
                font-weight: 700;
                color: #f56c6c;
              }
            }
          }

          .team-meta {
            display: flex;
            flex-direction: column;
            gap: 8px;
            margin-bottom: 16px;

            .meta-item {
              display: flex;
              align-items: center;
              gap: 6px;
              font-size: 13px;
              color: #606266;

              .el-icon {
                color: #909399;
              }
            }
          }

          .team-progress {
            margin-bottom: 12px;

            .progress-info {
              display: flex;
              justify-content: space-between;
              align-items: center;
              margin-bottom: 8px;
              font-size: 14px;

              .progress-text {
                color: #606266;

                strong {
                  color: var(--el-color-primary);
                  font-size: 16px;
                }
              }

              .remain-text {
                color: #f56c6c;
                font-size: 13px;
              }
            }
          }

          .expire-info,
          .success-info {
            display: flex;
            align-items: center;
            gap: 6px;
            padding: 8px 12px;
            background: #f5f7fa;
            border-radius: 6px;
            font-size: 13px;
            color: #606266;

            .el-icon {
              font-size: 16px;
            }
          }

          .expire-info {
            background: #fff7e6;
            color: #fa8c16;

            .el-icon {
              color: #fa8c16;
            }
          }

          .success-info {
            background: #f6ffed;
            color: #52c41a;

            .el-icon {
              color: #52c41a;
            }
          }
        }

        .team-actions {
          display: flex;
          justify-content: space-between;
          padding-top: 16px;
          margin-top: 16px;
          border-top: 1px solid #f0f0f0;
        }
      }
    }

    .pagination-wrapper {
      display: flex;
      justify-content: center;
      margin-top: 32px;
    }
  }

  // 创建对话框样式
  .create-steps, .activity-steps {
    margin-bottom: 32px;

    :deep(.el-step__title) {
      font-size: 14px;
      font-weight: 500;
    }

    :deep(.el-step__icon) {
      width: 32px;
      height: 32px;
    }
  }

  .step-content {
    min-height: 420px;
    padding: 24px 0;
    animation: slideInRight 0.4s ease;

    .form-tip {
      font-size: 12px;
      color: #909399;
      margin-top: 4px;
    }

    .form-info {
      display: flex;
      align-items: center;
    }

    .selected-product-display {
      display: flex;
      align-items: center;
      padding: 16px;
      background: #f5f7fa;
      border-radius: 8px;

      .product-details {
        h4 {
          margin: 0 0 8px 0;
          font-size: 16px;
          color: #303133;
        }

        p {
          margin: 4px 0;
          font-size: 14px;
          color: #606266;
        }
      }
    }

    // 确认页面样式
    .confirm-content {
      .confirm-details {
        .confirm-section {
          margin-bottom: 20px;

          h3 {
            display: flex;
            align-items: center;
            gap: 8px;
            font-size: 16px;
            color: #303133;
            margin: 0 0 16px 0;

            .el-icon {
              font-size: 18px;
              color: var(--el-color-primary);
            }
          }

          .info-row {
            display: flex;
            align-items: flex-start;
          }

          .info-grid {
            display: grid;
            gap: 12px;
          }

          .info-item {
            margin: 8px 0;
            font-size: 14px;
            color: #606266;
            line-height: 1.8;

            strong {
              color: #303133;
              margin-right: 8px;
            }

            .price-highlight {
              font-size: 20px;
              font-weight: 700;
              color: #f56c6c;
            }
          }
        }
      }
    }

    .search-bar {
      margin-bottom: 20px;
    }

    .products-grid {
      display: grid;
      grid-template-columns: repeat(auto-fill, minmax(160px, 1fr));
      gap: 18px;
      max-height: 520px;
      overflow-y: auto;
      padding: 4px;

      &::-webkit-scrollbar {
        width: 6px;
      }

      &::-webkit-scrollbar-track {
        background: #f5f7fa;
        border-radius: 3px;
      }

      &::-webkit-scrollbar-thumb {
        background: #dcdfe6;
        border-radius: 3px;

        &:hover {
          background: #c0c4cc;
        }
      }

      .product-item {
        position: relative;
        border: 2px solid #e4e7ed;
        border-radius: 12px;
        padding: 14px;
        cursor: pointer;
        background: white;
        transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1);
        overflow: hidden;

        &::before {
          content: '';
          position: absolute;
          top: 0;
          left: 0;
          right: 0;
          bottom: 0;
          background: linear-gradient(135deg, var(--el-color-primary-light-9) 0%, transparent 100%);
          opacity: 0;
          transition: opacity 0.3s ease;
        }

        &:hover {
          border-color: var(--el-color-primary-light-5);
          box-shadow: 0 6px 16px rgba(0, 0, 0, 0.12);
          transform: translateY(-4px);

          &::before {
            opacity: 0.5;
          }
        }

        &.selected {
          border-color: var(--el-color-primary);
          background: var(--el-color-primary-light-9);
          box-shadow: 0 8px 20px rgba(102, 126, 234, 0.2);

          &::before {
            opacity: 1;
          }
        }

        .product-image {
          width: 100%;
          height: 120px;
          border-radius: 6px;
          margin-bottom: 8px;

          .image-error {
            display: flex;
            align-items: center;
            justify-content: center;
            width: 100%;
            height: 100%;
            background: #f5f7fa;
            color: #c0c4cc;
            font-size: 32px;
          }
        }

        .product-info {
          .product-name {
            font-size: 14px;
            color: #303133;
            margin-bottom: 4px;
            overflow: hidden;
            text-overflow: ellipsis;
            white-space: nowrap;
          }

          .product-price {
            font-size: 16px;
            font-weight: 600;
            color: #f56c6c;
          }
        }

        .select-indicator {
          position: absolute;
          top: 10px;
          right: 10px;
          width: 28px;
          height: 28px;
          background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
          color: white;
          border-radius: 50%;
          display: flex;
          align-items: center;
          justify-content: center;
          font-size: 18px;
          box-shadow: 0 4px 12px rgba(102, 126, 234, 0.4);
          animation: scaleIn 0.3s ease;
          z-index: 1;
        }
      }
    }

    @keyframes scaleIn {
      from {
        transform: scale(0);
        opacity: 0;
      }
      to {
        transform: scale(1);
        opacity: 1;
      }
    }

    .activities-list {
      max-height: 500px;
      overflow-y: auto;

      .activity-item {
        border: 2px solid #e4e7ed;
        border-radius: 12px;
        padding: 20px;
        margin-bottom: 16px;
        cursor: pointer;
        transition: all 0.3s ease;

        &:hover {
          border-color: var(--el-color-primary-light-5);
          box-shadow: 0 4px 12px rgba(0, 0, 0, 0.08);
        }

        &.selected {
          border-color: var(--el-color-primary);
          background: var(--el-color-primary-light-9);
        }

        .activity-header {
          display: flex;
          justify-content: space-between;
          align-items: center;
          margin-bottom: 16px;

          .activity-title {
            display: flex;
            align-items: center;
            gap: 12px;

            .activity-id {
              font-size: 14px;
              color: #909399;
            }
          }

          .select-indicator {
            width: 32px;
            height: 32px;
            background: var(--el-color-primary);
            color: white;
            border-radius: 50%;
            display: flex;
            align-items: center;
            justify-content: center;
            font-size: 20px;
          }
        }

        .activity-details {
          .detail-row {
            display: flex;
            gap: 24px;
            margin-bottom: 12px;

            &:last-child {
              margin-bottom: 0;
            }

            .detail-item {
              display: flex;
              align-items: center;
              gap: 4px;

              &.full-width {
                flex: 1;
              }

              .detail-label {
                font-size: 13px;
                color: #909399;
              }

              .detail-value {
                font-size: 14px;
                color: #303133;
                font-weight: 500;

                &.price {
                  font-size: 18px;
                  color: #f56c6c;
                  font-weight: 700;
                }
              }
            }
          }

          .existing-teams {
            margin-top: 16px;
            padding-top: 16px;
            border-top: 1px dashed #e4e7ed;

            .teams-header {
              margin-bottom: 12px;

              .teams-title {
                font-size: 13px;
                color: #606266;
                font-weight: 500;
              }
            }

            .teams-scroll {
              max-height: 150px;
              overflow-y: auto;

              .mini-team {
                display: grid;
                grid-template-columns: 80px 100px 1fr 60px;
                align-items: center;
                gap: 12px;
                padding: 8px 12px;
                background: #f5f7fa;
                border-radius: 6px;
                margin-bottom: 8px;
                font-size: 12px;

                .team-leader {
                  color: #303133;
                  font-weight: 500;
                }

                .team-community {
                  color: #909399;
                }

                .team-count {
                  color: #606266;
                  text-align: right;
                }
              }
            }
          }
        }
      }
    }

    .form-info {
      display: flex;
      align-items: center;
    }

    .form-tip {
      font-size: 12px;
      color: #909399;
      margin-top: 4px;
    }

    .confirmation-info {
      background: #f5f7fa;
      padding: 16px;
      border-radius: 8px;
      border: 1px solid #e4e7ed;

      .confirm-item {
        display: flex;
        align-items: center;
        margin-bottom: 8px;

        .confirm-label {
          font-weight: 600;
          color: #303133;
          margin-right: 8px;
        }

        .confirm-value {
          color: #409eff;
          font-weight: 600;
          font-size: 16px;
        }
      }

      .confirm-tip {
        font-size: 12px;
        color: #909399;
      }
    }
  }

  .dialog-footer {
    display: flex;
    justify-content: center;
    gap: 16px;
    padding-top: 8px;

    .el-button {
      padding: 12px 32px;
      font-size: 14px;
      font-weight: 500;
      border-radius: 8px;
      transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1);

      &:hover {
        transform: translateY(-2px);
      }

      &:active {
        transform: translateY(0);
      }

      &--primary {
        background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
        border: none;
        box-shadow: 0 4px 12px rgba(102, 126, 234, 0.3);

        &:hover {
          box-shadow: 0 6px 16px rgba(102, 126, 234, 0.4);
        }
      }
    }
  }

  // 分享对话框样式
  .share-content {
    text-align: center;

    .share-info {
      margin-bottom: 24px;

      h3 {
        font-size: 20px;
        color: #303133;
        margin: 0 0 8px 0;
      }

      p {
        font-size: 14px;
        color: #909399;
        margin: 0;
      }
    }

    .share-link {
      margin-bottom: 24px;
    }

    .share-qrcode {
      padding: 24px;
      background: #f5f7fa;
      border-radius: 8px;
    }
  }

  // 关键帧动画
  @keyframes fadeIn {
    from {
      opacity: 0;
      transform: translateY(20px);
    }
    to {
      opacity: 1;
      transform: translateY(0);
    }
  }

  @keyframes pulse {
    0%, 100% {
      box-shadow: 0 4px 12px rgba(82, 196, 26, 0.2);
    }
    50% {
      box-shadow: 0 4px 20px rgba(82, 196, 26, 0.4);
    }
  }

  @keyframes priceGlow {
    0%, 100% {
      filter: brightness(1);
    }
    50% {
      filter: brightness(1.1);
    }
  }

  @keyframes tada {
    0% {
      transform: scale(1) rotate(0deg);
    }
    10%, 20% {
      transform: scale(0.95) rotate(-3deg);
    }
    30%, 50%, 70%, 90% {
      transform: scale(1.05) rotate(3deg);
    }
    40%, 60%, 80% {
      transform: scale(1.05) rotate(-3deg);
    }
    100% {
      transform: scale(1) rotate(0deg);
    }
  }

  @keyframes slideInRight {
    from {
      opacity: 0;
      transform: translateX(30px);
    }
    to {
      opacity: 1;
      transform: translateX(0);
    }
  }

  // 响应式适配
  @media (max-width: 1200px) {
    .activities-list .activities-grid {
      grid-template-columns: repeat(auto-fill, minmax(420px, 1fr));
    }

    .teams-list .teams-grid {
      grid-template-columns: repeat(auto-fill, minmax(340px, 1fr));
    }
  }

  @media (max-width: 768px) {
    padding: 16px;

    .page-header {
      flex-direction: column;
      align-items: flex-start;
      gap: 16px;
      padding: 20px 24px;

      .el-button {
        width: 100%;
      }
    }

    .activity-stats,
    .stats-cards {
      grid-template-columns: repeat(auto-fit, minmax(180px, 1fr));
      gap: 12px;
    }

    .activities-list .activities-grid,
    .teams-list .teams-grid {
      grid-template-columns: 1fr;
      gap: 16px;
    }

    .filter-card .filter-form {
      :deep(.el-form-item) {
        width: 100%;
      }
    }

    .products-grid {
      grid-template-columns: repeat(auto-fill, minmax(130px, 1fr));
      gap: 12px;
    }
  }

  @media (max-width: 480px) {
    padding: 12px;

    .page-header {
      padding: 16px 20px;
      margin-bottom: 20px;

      .page-title {
        font-size: 24px;
      }
    }

    .activity-stats,
    .stats-cards {
      grid-template-columns: 1fr;
    }

    .products-grid {
      grid-template-columns: repeat(2, 1fr);
    }
  }

  // 全局优化
  :deep(.el-loading-mask) {
    backdrop-filter: blur(2px);
    background-color: rgba(255, 255, 255, 0.8);
  }

  :deep(.el-button) {
    transition: all 0.3s ease;

    &:hover:not(:disabled) {
      transform: translateY(-1px);
    }

    &:active:not(:disabled) {
      transform: translateY(0);
    }
  }

  :deep(.el-card) {
    transition: all 0.3s ease;
  }

  :deep(.el-progress__text) {
    font-weight: 600;
  }

  :deep(.el-empty) {
    padding: 60px 0;

    .el-empty__description {
      margin-top: 16px;
      font-size: 15px;
      color: #909399;
    }

    .el-button {
      margin-top: 24px;
      padding: 14px 32px;
      font-size: 15px;
      font-weight: 600;
      border-radius: 10px;
      box-shadow: 0 4px 12px rgba(102, 126, 234, 0.3);

      &:hover {
        transform: translateY(-3px);
        box-shadow: 0 6px 16px rgba(102, 126, 234, 0.4);
      }
    }
  }
}
</style>

