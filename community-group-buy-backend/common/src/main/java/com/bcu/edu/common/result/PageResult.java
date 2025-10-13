package com.bcu.edu.common.result;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.List;

/**
 * 分页结果封装类
 *
 * @author 耿康瑞
 * @date 2025-10-12
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class PageResult<T> implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 当前页码
     */
    private Integer pageNum;

    /**
     * 每页大小
     */
    private Integer pageSize;

    /**
     * 总记录数
     */
    private Long total;

    /**
     * 总页数
     */
    private Integer pages;

    /**
     * 数据列表
     */
    private List<T> list;

    /**
     * 构造分页结果
     *
     * @param pageNum 当前页码
     * @param pageSize 每页大小
     * @param total 总记录数
     * @param list 数据列表
     */
    public PageResult(Integer pageNum, Integer pageSize, Long total, List<T> list) {
        this.pageNum = pageNum;
        this.pageSize = pageSize;
        this.total = total;
        this.list = list;
        // 计算总页数
        this.pages = (int) Math.ceil((double) total / pageSize);
    }

    /**
     * 构造分页结果（兼容FeedbackService的调用方式）
     *
     * @param total 总记录数
     * @param totalPages 总页数
     * @param currentPage 当前页码
     * @param pageSize 每页大小
     * @param list 数据列表
     */
    public PageResult(Long total, Long totalPages, Long currentPage, Long pageSize, List<T> list) {
        this.total = total;
        this.pages = totalPages.intValue();
        this.pageNum = currentPage.intValue();
        this.pageSize = pageSize.intValue();
        this.list = list;
    }

    /**
     * 创建分页结果
     */
    public static <T> PageResult<T> of(Integer pageNum, Integer pageSize, Long total, List<T> list) {
        return new PageResult<>(pageNum, pageSize, total, list);
    }

    /**
     * 是否有下一页
     */
    public boolean hasNext() {
        return this.pageNum < this.pages;
    }

    /**
     * 是否有上一页
     */
    public boolean hasPrevious() {
        return this.pageNum > 1;
    }

    /**
     * 是否为第一页
     */
    public boolean isFirst() {
        return this.pageNum == 1;
    }

    /**
     * 是否为最后一页
     */
    public boolean isLast() {
        return this.pageNum.equals(this.pages);
    }
}

