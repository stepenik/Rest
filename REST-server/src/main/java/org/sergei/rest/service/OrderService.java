package org.sergei.rest.service;

import org.sergei.rest.dto.OrderDTO;
import org.sergei.rest.dto.OrderDetailsDTO;
import org.sergei.rest.exceptions.ResourceNotFoundException;
import org.sergei.rest.model.Customer;
import org.sergei.rest.model.Order;
import org.sergei.rest.model.OrderDetails;
import org.sergei.rest.model.Product;
import org.sergei.rest.repository.CustomerRepository;
import org.sergei.rest.repository.OrderDetailsRepository;
import org.sergei.rest.repository.OrderRepository;
import org.sergei.rest.repository.ProductRepository;
import org.sergei.rest.util.ObjectMapperUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

/**
 * @author Sergei Visotsky, 2018
 */
@Service
public class OrderService<T> {

    protected static final String CUSTOMER_NOT_FOUND = "Customer with this ID not found";
    protected static final String ORDER_NOT_FOUND = "Order with this ID not found";
    private static final String PRODUCT_NOT_FOUND = "Product with this ID not found";

    protected final OrderRepository orderRepository;
    protected final OrderDetailsRepository orderDetailsRepository;
    protected final CustomerRepository customerRepository;
    private final ProductRepository productRepository;

    @Autowired
    public OrderService(OrderRepository orderRepository, OrderDetailsRepository orderDetailsRepository,
                        CustomerRepository customerRepository, ProductRepository productRepository) {
        this.orderRepository = orderRepository;
        this.orderDetailsRepository = orderDetailsRepository;
        this.customerRepository = customerRepository;
        this.productRepository = productRepository;
    }

    /**
     * Get order by number
     *
     * @param orderId get order number as a parameter from REST controller
     * @return order DTO response
     */
    private OrderDTO findOne(Long orderId) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(
                        () -> new ResourceNotFoundException(ORDER_NOT_FOUND)
                );
        // ModelMapper is used to avoid manual conversion from entity to DTO using setters and getters
        OrderDTO orderDTO = ObjectMapperUtil.map(order, OrderDTO.class);

        List<OrderDetails> orderDetailsList =
                orderDetailsRepository.findAllByOrderId(orderDTO.getOrderId());

        List<OrderDetailsDTO> orderDetailsDTOList = new ArrayList<>();
        orderDetailsList.forEach(orderDetails ->
                orderDetailsDTOList.add(
                        ObjectMapperUtil.map(orderDetails, OrderDetailsDTO.class)
                )
        );

        orderDTO.setOrderDetailsDTO(orderDetailsDTOList);

        return orderDTO;
    }

    /**
     * Get order by customer and order numbers
     *
     * @param customerId Get customer number from the REST controller
     * @param orderId    Get order number from the REST controller
     * @return Return order DTO reponse
     */
    public OrderDTO findOneByCustomerIdAndOrderId(Long customerId, Long orderId) {
        customerRepository.findById(customerId)
                .orElseThrow(
                        () -> new ResourceNotFoundException(CUSTOMER_NOT_FOUND)
                );
        return findOne(orderId);
    }

    /**
     * Get all orders by customer number
     *
     * @param customerId customer number form the REST controller
     * @return List of order DTOs
     */
    public List<OrderDTO> findAllByCustomerId(Long customerId) {
        List<Order> orders = orderRepository.findAllByCustomerId(customerId);
        if (orders == null) {
            throw new ResourceNotFoundException(ORDER_NOT_FOUND);
        }

        return findOrdersByListWithParam(orders);
    }

    /**
     * Get all orders by product code
     *
     * @param productCode get product code from the REST controller
     * @return return list of order DTOs
     */
    public List<OrderDTO> findAllByProductCode(String productCode) {
        List<Order> orders = orderRepository.findAllByProductCode(productCode);
        if (orders == null) {
            throw new ResourceNotFoundException(ORDER_NOT_FOUND);
        }
        return findOrdersByListWithParam(orders);
    }

    /**
     * Save order
     *
     * @param customerId Get customer number from the REST controller
     * @param orderDTO   Get order DTO request body
     * @return return order DTO as a response
     */
    public OrderDTO saveByCustomerId(Long customerId, OrderDTO orderDTO) {
        Customer customer = customerRepository.findById(customerId).orElseThrow(
                () -> new ResourceNotFoundException(CUSTOMER_NOT_FOUND)
        );

        Order order = ObjectMapperUtil.map(orderDTO, Order.class);
        order.setCustomer(customer);

        // Maps each member of collection containing requests to the class
        List<OrderDetails> orderDetailsList = ObjectMapperUtil
                .mapAll(orderDTO.getOrderDetailsDTO(), OrderDetails.class);

        int counter = 0;
        for (OrderDetails orderDetails : orderDetailsList) {
            Product product = productRepository.findByProductCode(orderDTO.getOrderDetailsDTO().get(counter).getProductCode())
                    .orElseThrow(
                            () -> new ResourceNotFoundException(PRODUCT_NOT_FOUND)
                    );
            orderDetails.setOrder(order);
            orderDetails.setProduct(product);
            orderDetails.setQuantityOrdered(orderDTO.getOrderDetailsDTO().get(counter).getQuantityOrdered());
            orderDetails.setPrice(orderDTO.getOrderDetailsDTO().get(counter).getPrice());
            counter++;
        }

        order.setOrderDetails(orderDetailsList);

        Order savedOrder = orderRepository.save(order);
        return ObjectMapperUtil.map(savedOrder, OrderDTO.class);
    }

    /**
     * Update order by customer and order numbers
     *
     * @param customerId get customer number form the REST controller
     * @param orderId    get order number form the REST controller
     * @param orderDTO   Get order DTO request body
     * @return return order DTO as a response
     */
    public OrderDTO updateByCustomerId(Long customerId, Long orderId, OrderDTO orderDTO) {
        Customer customer = customerRepository.findById(customerId).orElseThrow(
                () -> new ResourceNotFoundException(CUSTOMER_NOT_FOUND)
        );
        Order order = orderRepository.findById(orderId)
                .orElseThrow(
                        () -> new ResourceNotFoundException(ORDER_NOT_FOUND)
                );
        order.setCustomer(customer);
        order.setOrderDate(orderDTO.getOrderDate());
        order.setRequiredDate(orderDTO.getRequiredDate());
        order.setShippedDate(orderDTO.getShippedDate());
        order.setStatus(orderDTO.getStatus());

        // Maps each member of collection containing requests to the class
        List<OrderDetails> orderDetailsList = ObjectMapperUtil
                .mapAll(orderDTO.getOrderDetailsDTO(), OrderDetails.class);

        int counter = 0;
        for (OrderDetails orderDetails : orderDetailsList) {
            Product product = productRepository.findByProductCode(orderDTO.getOrderDetailsDTO().get(counter).getProductCode())
                    .orElseThrow(
                            () -> new ResourceNotFoundException(PRODUCT_NOT_FOUND)
                    );
            orderDetails.setOrder(order);
            orderDetails.setProduct(product);
            orderDetails.setQuantityOrdered(orderDTO.getOrderDetailsDTO().get(counter).getQuantityOrdered());
            orderDetails.setPrice(orderDTO.getOrderDetailsDTO().get(counter).getPrice());
            counter++;
        }

        order.setOrderDetails(orderDetailsList);

        orderRepository.save(order);

        return orderDTO;
    }

    /**
     * Delete order method
     *
     * @param customerId get customer number form the REST controller
     * @param orderId    get order number form the REST controller
     */
    public void deleteByCustomerIdAndOrderId(Long customerId, Long orderId) {
        orderRepository.deleteByCustomerIdAndOrderId(customerId, orderId);
    }

    /**
     * Util method to get order by specific parameter
     *
     * @param orders Gets list of the order entities
     * @return List of the order DTOs
     */
    private List<OrderDTO> findOrdersByListWithParam(List<Order> orders) {
        List<OrderDTO> orderDTOList = new LinkedList<>();

        for (Order order : orders) {
            // ModelMapper is used to avoid manual conversion from entity to DTO using setters and getters
            OrderDTO orderDTO = ObjectMapperUtil.map(order, OrderDTO.class);

            List<OrderDetails> orderDetailsList =
                    orderDetailsRepository.findAllByOrderId(orderDTO.getOrderId());

            List<OrderDetailsDTO> orderDetailsDTOList = new ArrayList<>();
            orderDetailsList.forEach(orderDetails ->
                    orderDetailsDTOList.add(
                            ObjectMapperUtil.map(orderDetails, OrderDetailsDTO.class)
                    )
            );
            for (OrderDetails orderDetails : orderDetailsList) {
                // ModelMapper is used to avoid manual conversion from entity to DTO using setters and getters
                OrderDetailsDTO orderDetailsDTO = ObjectMapperUtil.map(orderDetails, OrderDetailsDTO.class);
                orderDetailsDTOList.add(orderDetailsDTO);
            }

            orderDTO.setOrderDetailsDTO(orderDetailsDTOList);
            orderDTOList.add(orderDTO);
        }

        return orderDTOList;
    }
}