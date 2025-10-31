package com.bcu.edu.service;

import com.bcu.edu.common.dto.OperationLogDTO;
import com.bcu.edu.common.dto.OperationLogQuery;
import com.bcu.edu.common.entity.SysOperationLog;
import com.bcu.edu.common.repository.SysOperationLogRepository;
import com.bcu.edu.common.utils.ExcelUtil;
import com.github.pagehelper.PageInfo;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import jakarta.persistence.criteria.Predicate;
import jakarta.servlet.http.HttpServletResponse;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

/**
 * 日志服务
 * 负责保存操作日志到数据库，以及日志查询、导出等功能
 * 
 * @author AI Assistant
 * @since 2025-10-31
 */
@Service
@Slf4j
public class LogService {

    @Autowired
    private SysOperationLogRepository logRepository;

    /**
     * 保存操作日志（异步）
     * 供其他微服务通过Feign调用
     * 
     * @param logDTO 日志数据传输对象
     */
    @Async("logExecutor")
    @Transactional(rollbackFor = Exception.class)
    public void saveLog(OperationLogDTO logDTO) {
        try {
            // 转换DTO为实体
            SysOperationLog logEntity = new SysOperationLog();
            logEntity.setUserId(logDTO.getUserId());
            logEntity.setUsername(logDTO.getUsername());
            logEntity.setOperation(logDTO.getOperation());
            logEntity.setModule(logDTO.getModule());
            logEntity.setMethod(logDTO.getMethod());
            logEntity.setParams(logDTO.getParams());
            logEntity.setResult(logDTO.getResult());
            logEntity.setErrorMsg(logDTO.getErrorMsg());
            logEntity.setDuration(logDTO.getDuration());
            logEntity.setIp(logDTO.getIp());

            // 保存到数据库
            logRepository.save(logEntity);
            
            log.info("操作日志已保存: module={}, operation={}, user={}, result={}", 
                    logDTO.getModule(), 
                    logDTO.getOperation(), 
                    logDTO.getUsername(),
                    logDTO.getResult());
        } catch (Exception e) {
            log.error("保存操作日志失败: module={}, operation={}", 
                    logDTO.getModule(), logDTO.getOperation(), e);
            // 日志保存失败不应该影响业务，所以只记录错误
        }
    }

    /**
     * 同步保存日志（用于关键操作）
     * 
     * @param logDTO 日志数据传输对象
     */
    @Transactional(rollbackFor = Exception.class)
    public void saveLogSync(OperationLogDTO logDTO) {
        try {
            SysOperationLog logEntity = new SysOperationLog();
            logEntity.setUserId(logDTO.getUserId());
            logEntity.setUsername(logDTO.getUsername());
            logEntity.setOperation(logDTO.getOperation());
            logEntity.setModule(logDTO.getModule());
            logEntity.setMethod(logDTO.getMethod());
            logEntity.setParams(logDTO.getParams());
            logEntity.setResult(logDTO.getResult());
            logEntity.setErrorMsg(logDTO.getErrorMsg());
            logEntity.setDuration(logDTO.getDuration());
            logEntity.setIp(logDTO.getIp());

            logRepository.save(logEntity);
            
            log.info("操作日志已同步保存: module={}, operation={}", 
                    logDTO.getModule(), logDTO.getOperation());
        } catch (Exception e) {
            log.error("同步保存操作日志失败", e);
            throw e;
        }
    }

    /**
     * 分页查询操作日志
     * 
     * @param query 查询条件
     * @param page 页码
     * @param size 每页大小
     * @return 分页结果
     */
    public PageInfo<SysOperationLog> getOperationLogs(OperationLogQuery query, Integer page, Integer size) {
        try {
            // 构建查询规范
            Specification<SysOperationLog> spec = (root, criteriaQuery, criteriaBuilder) -> {
                List<Predicate> predicates = new ArrayList<>();

                // 用户ID条件
                if (query.getUserId() != null) {
                    predicates.add(criteriaBuilder.equal(root.get("userId"), query.getUserId()));
                }

                // 用户名模糊查询
                if (query.getUsername() != null && !query.getUsername().isEmpty()) {
                    predicates.add(criteriaBuilder.like(root.get("username"), "%" + query.getUsername() + "%"));
                }

                // 模块精确查询
                if (query.getModule() != null && !query.getModule().isEmpty()) {
                    predicates.add(criteriaBuilder.equal(root.get("module"), query.getModule()));
                }

                // 开始时间
                if (query.getStartDate() != null) {
                    predicates.add(criteriaBuilder.greaterThanOrEqualTo(root.get("createTime"), query.getStartDate()));
                }

                // 结束时间
                if (query.getEndDate() != null) {
                    predicates.add(criteriaBuilder.lessThanOrEqualTo(root.get("createTime"), query.getEndDate()));
                }

                // 关键词模糊查询（操作内容）
                if (query.getKeyword() != null && !query.getKeyword().isEmpty()) {
                    predicates.add(criteriaBuilder.like(root.get("operation"), "%" + query.getKeyword() + "%"));
                }

                return criteriaBuilder.and(predicates.toArray(new Predicate[0]));
            };

            // 排序：按创建时间倒序
            Sort sort = Sort.by(Sort.Direction.DESC, "createTime");

            // 查询（使用JPA的Pageable，然后转换为PageInfo）
            org.springframework.data.domain.Pageable pageable = 
                    org.springframework.data.domain.PageRequest.of(page - 1, size, sort);
            
            org.springframework.data.domain.Page<SysOperationLog> pageResult = 
                    logRepository.findAll(spec, pageable);

            // 转换为PageInfo
            PageInfo<SysOperationLog> pageInfo = new PageInfo<>();
            pageInfo.setList(pageResult.getContent());
            pageInfo.setTotal(pageResult.getTotalElements());
            pageInfo.setPageNum(page);
            pageInfo.setPageSize(size);
            pageInfo.setPages(pageResult.getTotalPages());
            pageInfo.setHasNextPage(pageResult.hasNext());
            pageInfo.setHasPreviousPage(pageResult.hasPrevious());
            pageInfo.setIsFirstPage(pageResult.isFirst());
            pageInfo.setIsLastPage(pageResult.isLast());

            log.info("查询操作日志完成: total={}, page={}, size={}", 
                    pageInfo.getTotal(), page, size);

            return pageInfo;
        } catch (Exception e) {
            log.error("查询操作日志失败", e);
            throw e;
        }
    }

    /**
     * 导出操作日志
     * 
     * @param query 查询条件
     * @param response HTTP响应
     */
    public void exportOperationLogs(OperationLogQuery query, HttpServletResponse response) {
        try {
            // 构建查询规范（复用分页查询的逻辑）
            Specification<SysOperationLog> spec = (root, criteriaQuery, criteriaBuilder) -> {
                List<Predicate> predicates = new ArrayList<>();

                if (query.getUserId() != null) {
                    predicates.add(criteriaBuilder.equal(root.get("userId"), query.getUserId()));
                }
                if (query.getModule() != null && !query.getModule().isEmpty()) {
                    predicates.add(criteriaBuilder.equal(root.get("module"), query.getModule()));
                }
                if (query.getStartDate() != null) {
                    predicates.add(criteriaBuilder.greaterThanOrEqualTo(root.get("createTime"), query.getStartDate()));
                }
                if (query.getEndDate() != null) {
                    predicates.add(criteriaBuilder.lessThanOrEqualTo(root.get("createTime"), query.getEndDate()));
                }

                return criteriaBuilder.and(predicates.toArray(new Predicate[0]));
            };

            // 排序：按创建时间倒序
            Sort sort = Sort.by(Sort.Direction.DESC, "createTime");

            // 查询全部（最多10000条）
            org.springframework.data.domain.Pageable pageable = 
                    org.springframework.data.domain.PageRequest.of(0, 10000, sort);
            
            List<SysOperationLog> logs = logRepository.findAll(spec, pageable).getContent();

            if (logs.size() >= 10000) {
                log.warn("导出日志数量达到上限: 10000条");
            }

            // 使用ExcelUtil导出
            byte[] excelBytes = ExcelUtil.exportOperationLogs(logs);

            // 设置响应头
            String fileName = "operation_logs_" + 
                    java.time.LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss")) + ".xlsx";
            
            response.setContentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
            response.setHeader("Content-Disposition", "attachment;filename=" + fileName);
            response.setContentLength(excelBytes.length);

            // 写入响应
            response.getOutputStream().write(excelBytes);
            response.getOutputStream().flush();

            log.info("导出操作日志完成: count={}", logs.size());
        } catch (Exception e) {
            log.error("导出操作日志失败", e);
            throw new RuntimeException("导出Excel失败", e);
        }
    }

    /**
     * 获取所有操作模块列表
     * 
     * @return 模块列表
     */
    public List<String> getLogModules() {
        try {
            List<String> modules = logRepository.findAllModules();
            log.debug("获取操作模块列表完成: count={}", modules.size());
            return modules;
        } catch (Exception e) {
            log.error("获取操作模块列表失败", e);
            throw e;
        }
    }
}

