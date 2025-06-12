package com.ecom.service.impl;

import java.time.LocalDate;

import java.util.List;
import java.util.Optional;
import java.util.Random;


import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import com.ecom.model.Cart;
import com.ecom.model.OrderAddress;
import com.ecom.model.OrderRequest;
import com.ecom.model.ProductOrder;
import com.ecom.repository.CartRepository;
import com.ecom.repository.ProductOrderRepository;
import com.ecom.service.CartService;
import com.ecom.service.OrderService;
import com.ecom.util.CommonUtil;
import com.ecom.util.OrderStatus;

@Service
public class OrderServiceImpl implements OrderService {

	@Autowired
	private ProductOrderRepository orderRepository;

	@Autowired
	private CartRepository cartRepository;

	@Autowired
	private CommonUtil commonUtil;

	@Autowired
	private CartService cartService;

	@Override
	public void saveOrder(Integer userid, OrderRequest orderRequest) throws Exception {

		List<Cart> carts = cartRepository.findByUserId(userid);

		for (Cart cart : carts) {

			ProductOrder order = new ProductOrder();
			int orderId = 100000 + new Random().nextInt(900000); // Tạo số ngẫu nhiên từ 100000 đến 999999
			order.setOrderId(String.valueOf(orderId));
			// order.setOrderId(UUID.randomUUID().toString());
			order.setOrderDate(LocalDate.now());

			order.setProduct(cart.getProduct());
			order.setPrice(cart.getProduct().getDiscountPrice());

			order.setQuantity(cart.getQuantity());
			order.setUser(cart.getUser());

			order.setStatus(OrderStatus.IN_PROGRESS.getName());
			order.setPaymentType(orderRequest.getPaymentType());

			OrderAddress address = new OrderAddress();
			address.setFirstName(orderRequest.getFirstName());
			address.setLastName(orderRequest.getLastName());
			address.setEmail(orderRequest.getEmail());
			address.setMobileNo(orderRequest.getMobileNo());
			address.setAddress(orderRequest.getAddress());
			address.setCity(orderRequest.getCity());
			address.setState(orderRequest.getState());
			address.setPincode(orderRequest.getPincode());

			order.setOrderAddress(address);

			// ProductOrder saveOrder = //
			orderRepository.save(order);
			// commonUtil.sendMailForProductOrder(saveOrder, "success");
		}
		cartService.clearCartByUserId(userid);
	}

	@Override
	public List<ProductOrder> getOrdersByUser(Integer userId) {
		List<ProductOrder> orders = orderRepository.findByUserId(userId);
		return orders;
	}

	@Override
	public ProductOrder updateOrderStatus(Integer id, String status) {
		Optional<ProductOrder> findById = orderRepository.findById(id);
		if (findById.isPresent()) {
			ProductOrder productOrder = findById.get();
			productOrder.setStatus(status);
			ProductOrder updateOrder = orderRepository.save(productOrder);
			return updateOrder;
		}
		return null;
	}

	@Override
	public List<ProductOrder> getAllOrders() {
		return orderRepository.findAll();
	}

	@Override
	public Page<ProductOrder> getAllOrdersPagination(Integer pageNo, Integer pageSize) {
		Pageable pageable = PageRequest.of(pageNo, pageSize);
		return orderRepository.findAll(pageable);

	}

	@Override
	public ProductOrder getOrdersByOrderId(String orderId) {
		return orderRepository.findByOrderId(orderId);
	}

	@Override
	public boolean requestReturn(Integer orderId) {
		Optional<ProductOrder> orderOpt = orderRepository.findById(orderId);
		if (orderOpt.isPresent()) {
			ProductOrder order = orderOpt.get();
			if (OrderStatus.DELIVERED.getName().equals(order.getStatus())) {
				order.setStatus(OrderStatus.RETURN_REQUESTED.getName());
				orderRepository.save(order);
				return true;
			}
		}
		return false;
	}

	@Override
	public Double getTotalRevenue() {
		Double revenue = orderRepository.getTotalRevenue();
		return revenue != null ? revenue : 0.0;
	}

	@Override
	public Object[] getBestSellingProduct() {
		return orderRepository.getBestSellingProduct();
	}

	@Override
	public Long getTotalOrderCount() {
		Long count = orderRepository.getTotalOrderCount();
		return count != null ? count : 0L;
	}

	@Override
	public List<Object[]> getRevenueByDay() {
		return orderRepository.getRevenueByDay();
	}

	@Override
	public List<Object[]> getRevenueByMonth() {
		return orderRepository.getRevenueByMonth();
	}

	@Override
	public List<Object[]> getRevenueByYear() {
		return orderRepository.getRevenueByYear();
	}

}
