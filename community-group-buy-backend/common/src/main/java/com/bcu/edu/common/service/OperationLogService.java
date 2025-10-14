package com.bcu.edu.common.service;

import com.bcu.edu.common.dto.OperationLogDTO;
import com.bcu.edu.common.dto.OperationLogQuery;
import com.bcu.edu.common.entity.SysOperationLog;
import com.bcu.edu.common.exception.BusinessException;
import com.bcu.edu.common.repository.SysOperationLogRepository;
import com.bcu.edu.common.result.PageResult;
import com.bcu.edu.common.utils.ExcelUtil;
import jakarta.persistence.criteria.Predicate;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 操作日志服务类
 */
@Service
@Slf4j
public class OperationLogService {

    @Autowired
    private SysOperationLogRepository repository;

    /**
     * 分页查询操作日志
     */
    public PageResult<OperationLogDTO> queryLogs(OperationLogQuery query) {
        // 构建查询条件
        Specification<SysOperationLog> spec = buildSpecification(query);

        // 构建分页参数（按创建时间倒序）
        Pageable pageable = PageRequest.of(
                query.getPage() - 1,
                query.getSize(),
                Sort.by(Sort.Direction.DESC, "createTime")
        );

        // 执行查询
        Page<SysOperationLog> page = repository.findAll(spec, pageable);

        // 转换为DTO
        List<OperationLogDTO> dtoList = page.getContent().stream()
                .map(OperationLogDTO::fromEntity)
                .collect(Collectors.toList());

        // 构建分页结果
        PageResult<OperationLogDTO> result = new PageResult<>();
        result.setTotal(page.getTotalElements());
        result.setPageNum(query.getPage());
        result.setPageSize(query.getSize());
        result.setPages(page.getTotalPages());
        result.setList(dtoList);

        return result;
    }

    /**
     * 导出操作日志为Excel
     */
    public byte[] exportLogs(OperationLogQuery query) throws IOException {
        // 构建查询条件
        Specification<SysOperationLog> spec = buildSpecification(query);

        // 查询所有数据（按创建时间倒序）
        Sort sort = Sort.by(Sort.Direction.DESC, "createTime");
        List<SysOperationLog> logs = repository.findAll(spec, sort);

        // 限制导出数量
        if (logs.size() > 10000) {
            throw new BusinessException("导出数据量过大，请缩小查询范围（最大10000条）");
        }

        // 转换为DTO
        List<OperationLogDTO> dtoList = logs.stream()
                .map(OperationLogDTO::fromEntity)
                .collect(Collectors.toList());

        // 导出Excel
        return ExcelUtil.exportOperationLogs(dtoList);
    }

    /**
     * 获取所有操作模块列表
     */
    public List<String> getModules() {
        return repository.findAllModules();
    }

    /**
     * 构建查询条件（JPA Specification）
     */
    private Specification<SysOperationLog> buildSpecification(OperationLogQuery query) {
        return (root, criteriaQuery, criteriaBuilder) -> {
            List<Predicate> predicates = new ArrayList<>();

            // 按用户ID查询
            if (query.getUserId() != null) {
                predicates.add(criteriaBuilder.equal(root.get("userId"), query.getUserId()));
            }

            // 按用户名模糊查询
            if (StringUtils.hasText(query.getUsername())) {
                predicates.add(criteriaBuilder.like(root.get("username"), "%" + query.getUsername() + "%"));
            }

            // 按模块查询
            if (StringUtils.hasText(query.getModule())) {
                predicates.add(criteriaBuilder.equal(root.get("module"), query.getModule()));
            }

            // 按时间范围查询
            if (query.getStartDate() != null) {
                predicates.add(criteriaBuilder.greaterThanOrEqualTo(root.get("createTime"), query.getStartDate()));
            }
            if (query.getEndDate() != null) {
                predicates.add(criteriaBuilder.lessThanOrEqualTo(root.get("createTime"), query.getEndDate()));
            }

            // 按关键词模糊查询（操作内容）
            if (StringUtils.hasText(query.getKeyword())) {
                predicates.add(criteriaBuilder.like(root.get("operation"), "%" + query.getKeyword() + "%"));
            }

            // 如果没有指定时间范围，默认查询最近30天
            if (query.getStartDate() == null && query.getEndDate() == null) {
                predicates.add(criteriaBuilder.greaterThanOrEqualTo(
                        root.get("createTime"),
                        java.time.LocalDateTime.now().minusDays(30)
                ));
            }

            return criteriaBuilder.and(predicates.toArray(new Predicate[0]));
        };
    }
}

