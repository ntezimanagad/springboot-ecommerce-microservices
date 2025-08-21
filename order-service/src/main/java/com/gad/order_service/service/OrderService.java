// package com.gad.order_service.service;

// import org.springframework.stereotype.Service;

// import com.gad.order_service.client.ProductClient;
// import com.gad.order_service.client.UserClient;
// import com.gad.order_service.dto.ProductDto;
// import com.gad.order_service.dto.UserDto;
// import com.gad.order_service.exception.ProductNotFoundException;
// import com.gad.order_service.exception.UserNotFoundException;
// import com.gad.order_service.model.Order;
// import com.gad.order_service.repository.OrderRepository;

// import feign.FeignException;
// import lombok.RequiredArgsConstructor;

// @Service
// @RequiredArgsConstructor
// public class OrderService {

//     private final OrderRepository orderRepository;
//     private final UserClient userClient;
//     private final ProductClient productClient;

//     public Order placeOrder(Long userId, Long productId, int quantity) {
//         UserDto user;
//         ProductDto product;

//         try {
//             user = userClient.getUserById(userId);
//         } catch (FeignException.NotFound e) {
//             throw new UserNotFoundException(userId);
//         }

//         try {
//             product = productClient.getProductById(productId);
//         } catch (FeignException.NotFound e) {
//             throw new ProductNotFoundException(productId);
//         }

//         // Create order
//         Order order = new Order();
//         order.setUserId(user.getId());
//         order.setProductId(product.getId());
//         order.setQuantity(quantity);
//         order.setTotalPrice(product.getPrice() * quantity);

//         return orderRepository.save(order);
//     }
// }
package com.gad.order_service.service;

import org.springframework.stereotype.Service;

import com.gad.order_service.client.ProductClient;
import com.gad.order_service.client.UserClient;
import com.gad.order_service.dto.ProductDto;
import com.gad.order_service.dto.UserDto;
import com.gad.order_service.exception.ProductNotFoundException;
import com.gad.order_service.exception.UserNotFoundException;
import com.gad.order_service.model.Order;
import com.gad.order_service.repository.OrderRepository;

import feign.FeignException;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import io.github.resilience4j.retry.annotation.Retry;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class OrderService {

    private final OrderRepository orderRepository;
    private final UserClient userClient;
    private final ProductClient productClient;

    private static final String USER_SERVICE = "userService";
    private static final String PRODUCT_SERVICE = "productService";

    // Protected call to UserService
    @CircuitBreaker(name = USER_SERVICE, fallbackMethod = "userFallback")
    @Retry(name = USER_SERVICE)
    public UserDto getUser(Long userId) {
        try {
            return userClient.getUserById(userId);
        } catch (FeignException.NotFound e) {
            throw new UserNotFoundException(userId);
        }
    }

    // Protected call to ProductService
    @CircuitBreaker(name = PRODUCT_SERVICE, fallbackMethod = "productFallback")
    @Retry(name = PRODUCT_SERVICE)
    public ProductDto getProduct(Long productId) {
        try {
            return productClient.getProductById(productId);
        } catch (FeignException.NotFound e) {
            throw new ProductNotFoundException(productId);
        }
    }

    // Main placeOrder method
    public Order placeOrder(Long userId, Long productId, int quantity) {
        UserDto user = getUser(userId); // Resilience4j protects this call
        ProductDto product = getProduct(productId); // Resilience4j protects this call

        Order order = new Order();
        order.setUserId(user.getId());
        order.setProductId(product.getId());
        order.setQuantity(quantity);
        order.setTotalPrice(product.getPrice() * quantity);

        return orderRepository.save(order);
    }

    // Fallback methods
    public UserDto userFallback(Long userId, Throwable ex) {
        throw new RuntimeException("User Service is currently unavailable. Please try again later.");
    }

    public ProductDto productFallback(Long productId, Throwable ex) {
        throw new RuntimeException("Product Service is currently unavailable. Please try again later.");
    }
}
