package ru.yandex.practicum.mymarket.model;

import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;

import java.math.BigDecimal;

@Table("orders")
public class CustomerOrder {

    @Id
    private Long id;

    @Column("total_sum")
    private BigDecimal totalSum;

    @Column("user_id")
    private Long userId;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public BigDecimal getTotalSum() {
        return totalSum;
    }

    public void setTotalSum(BigDecimal totalSum) {
        this.totalSum = totalSum;
    }

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }
}
