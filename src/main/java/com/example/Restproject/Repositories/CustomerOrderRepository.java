package com.example.Restproject.Repositories;

import com.example.Restproject.Model.CustomerOrder;
import org.springframework.data.jpa.repository.JpaRepository;

public interface  CustomerOrderRepository extends JpaRepository<CustomerOrder, Long> {
}
