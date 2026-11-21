package com.campus.erp.project;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 项目实体，对应 project 表。
 *
 * 三个金额的含义：
 *   available 可用：还能批给新申请的钱
 *   frozen    冻结：已提交/已批准但未结算的钱
 *   used      已使用：财务登记的实际花费
 *
 * 不变式（由数据库 CHECK 约束兜底）：
 *   available + frozen + used = totalBudget，且三项非负
 */
public class Project {

    private Long id;
    private String name;
    private BigDecimal totalBudget;   // total_budget
    private BigDecimal available;
    private BigDecimal frozen;
    private BigDecimal used;
    private LocalDateTime createdAt;  // created_at

    public Project() {
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public BigDecimal getTotalBudget() {
        return totalBudget;
    }

    public void setTotalBudget(BigDecimal totalBudget) {
        this.totalBudget = totalBudget;
    }

    public BigDecimal getAvailable() {
        return available;
    }

    public void setAvailable(BigDecimal available) {
        this.available = available;
    }

    public BigDecimal getFrozen() {
        return frozen;
    }

    public void setFrozen(BigDecimal frozen) {
        this.frozen = frozen;
    }

    public BigDecimal getUsed() {
        return used;
    }

    public void setUsed(BigDecimal used) {
        this.used = used;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
}
