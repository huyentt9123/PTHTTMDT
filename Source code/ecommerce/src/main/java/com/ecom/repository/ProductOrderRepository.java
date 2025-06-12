package com.ecom.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.ecom.model.ProductOrder;

public interface ProductOrderRepository extends JpaRepository<ProductOrder, Integer> {

	List<ProductOrder> findByUserId(Integer userId);

	ProductOrder findByOrderId(String orderId);
	ProductOrder findTopByUserIdOrderByIdDesc(Integer userId);

	@Query("SELECT SUM(o.price * o.quantity) FROM ProductOrder o WHERE o.status = 'Delivered'")
	Double getTotalRevenue();

	@Query("SELECT o.product.title, SUM(o.quantity) as total FROM ProductOrder o WHERE o.status = 'Delivered' GROUP BY o.product.id, o.product.title ORDER BY total DESC LIMIT 1")
	Object[] getBestSellingProduct();

	@Query("SELECT COUNT(o) FROM ProductOrder o")
	Long getTotalOrderCount();

	@Query("SELECT o.orderDate, SUM(o.price * o.quantity) FROM ProductOrder o WHERE o.status = 'Delivered' GROUP BY o.orderDate ORDER BY o.orderDate")
	List<Object[]> getRevenueByDay();

	@Query("SELECT FUNCTION('YEAR', o.orderDate), FUNCTION('MONTH', o.orderDate), SUM(o.price * o.quantity) FROM ProductOrder o WHERE o.status = 'Delivered' GROUP BY FUNCTION('YEAR', o.orderDate), FUNCTION('MONTH', o.orderDate) ORDER BY FUNCTION('YEAR', o.orderDate), FUNCTION('MONTH', o.orderDate)")
	List<Object[]> getRevenueByMonth();

	@Query("SELECT FUNCTION('YEAR', o.orderDate), SUM(o.price * o.quantity) FROM ProductOrder o WHERE o.status = 'Delivered' GROUP BY FUNCTION('YEAR', o.orderDate) ORDER BY FUNCTION('YEAR', o.orderDate)")
	List<Object[]> getRevenueByYear();
}
