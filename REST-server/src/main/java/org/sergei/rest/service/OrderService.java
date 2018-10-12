package org.sergei.rest.service;

import org.modelmapper.ModelMapper;
import org.sergei.rest.dao.CustomerDAO;
import org.sergei.rest.dao.OrderDAO;
import org.sergei.rest.dao.OrderDetailsDAO;
import org.sergei.rest.dao.ProductDAO;
import org.sergei.rest.dto.OrderDTO;
import org.sergei.rest.dto.OrderDetailsDTO;
import org.sergei.rest.model.Customer;
import org.sergei.rest.model.Order;
import org.sergei.rest.model.OrderDetails;
import org.sergei.rest.model.Product;
import org.sergei.rest.utils.ObjectMapperUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

@Service
public class OrderService {

    private final ModelMapper modelMapper;
    private final OrderDAO orderDAO;
    private final OrderDetailsDAO orderDetailsDAO;
    private final CustomerDAO customerDAO;
    private final ProductDAO productDAO;


    @Autowired
    public OrderService(ModelMapper modelMapper, OrderDAO orderDAO, OrderDetailsDAO orderDetailsDAO,
                        CustomerDAO customerDAO, ProductDAO productDAO) {
        this.modelMapper = modelMapper;
        this.orderDAO = orderDAO;
        this.orderDetailsDAO = orderDetailsDAO;
        this.customerDAO = customerDAO;
        this.productDAO = productDAO;
    }

    /**
     * Get all orders
     *
     * @return List of order DTOs
     */
    public List<OrderDTO> getAllOrders() {
        List<Order> orders = orderDAO.findAll();
        return getOrdersByListWithParam(orders);
    }

    /**
     * Get order by number
     *
     * @param orderNumber get order number as a parameter from REST controller
     * @return order DTO response
     */
    public OrderDTO getOrderByNumber(Long orderNumber) {
        Order order = orderDAO.findOne(orderNumber);
        // ModelMapper is used to avoid manual conversion from entity to DTO using setters and getters
        OrderDTO orderDTOResponse = modelMapper.map(order, OrderDTO.class);

        List<OrderDetails> orderDetailsList =
                orderDetailsDAO.findAllByOrderNumber(orderDTOResponse.getOrderNumber());

        List<OrderDetailsDTO> orderDetailsDTOS = new ArrayList<>();
        for (OrderDetails orderDetails : orderDetailsList) {
            // ModelMapper is used to avoid manual conversion from entity to DTO using setters and getters
            OrderDetailsDTO orderDetailsDTO = modelMapper.map(orderDetails, OrderDetailsDTO.class);
            orderDetailsDTOS.add(orderDetailsDTO);
        }

        orderDTOResponse.setOrderDetailsDTO(orderDetailsDTOS);

        return orderDTOResponse;
    }

    /**
     * Get order by customer and order numbers
     *
     * @param customerNumber Get customer number from the REST controller
     * @param orderNumber    Get order number from the REST controller
     * @return Return order DTO reponse
     */
    public OrderDTO getOrderByCustomerAndOrderNumbers(Long customerNumber, Long orderNumber) {
        /*if (!customerRepository.existsById(customerNumber)) {
            throw new RecordNotFoundException("No customer with this number found");
        }*/
        return getOrderByNumber(orderNumber);
    }

    /**
     * Get all orders by customer number
     *
     * @param customerNumber customer number form the REST controller
     * @return List of order DTOs
     */
    public List<OrderDTO> getAllOrdersByCustomerNumber(Long customerNumber) {
        /*if (!customerRepository.existsById(customerNumber)) {
            throw new RecordNotFoundException("No customer with this number found");
        }*/
        List<Order> orders = orderDAO.findAllByCustomerNumber(customerNumber);

        return getOrdersByListWithParam(orders);
    }

    /**
     * Get all orders by product code
     *
     * @param productCode get product code from the REST controller
     * @return return list of order DTOs
     */
    public List<OrderDTO> getAllByProductCode(String productCode) {
        List<Order> orders = orderDAO.findAllByProductCode(productCode);
        return getOrdersByListWithParam(orders);
    }

    /**
     * Save order
     *
     * @param customerNumber      Get customer number from the REST controller
     * @param orderDTORequestBody Get order DTO request body
     * @return return order DTO as a response
     */
    public OrderDTO saveOrder(Long customerNumber, OrderDTO orderDTORequestBody) {
        Customer customer = customerDAO.findOne(customerNumber);

        Order order = modelMapper.map(orderDTORequestBody, Order.class);
        order.setCustomer(customer);

        // Maps each member of collection containing requests to the class
        List<OrderDetails> orderDetailsList = ObjectMapperUtils
                .mapAll(orderDTORequestBody.getOrderDetailsDTO(), OrderDetails.class);

        int counter = 0;
        for (OrderDetails orderDetails : orderDetailsList) {
            Product product = productDAO.findByCode(orderDTORequestBody.getOrderDetailsDTO().get(counter).getProductCode());
            orderDetails.setOrder(order);
            orderDetails.setProduct(product);
            orderDetails.setQuantityOrdered(orderDTORequestBody.getOrderDetailsDTO().get(counter).getQuantityOrdered());
            orderDetails.setPrice(orderDTORequestBody.getOrderDetailsDTO().get(counter).getPrice());
            counter++;
        }

        order.setOrderDetails(orderDetailsList);

        orderDAO.save(order);

        return orderDTORequestBody;
    }

    /**
     * Update order by customer and order numbers
     *
     * @param customerNumber      get customer number form the REST controller
     * @param orderNumber         get order number form the REST controller
     * @param orderDTORequestBody Get order DTO request body
     * @return return order DTO as a response
     */
    public OrderDTO updateOrder(Long customerNumber, Long orderNumber, OrderDTO orderDTORequestBody) {
        Customer customer = customerDAO.findOne(customerNumber);

        Order order = orderDAO.findOne(orderNumber);
        order.setOrderNumber(orderDTORequestBody.getOrderNumber());
        order.setCustomer(customer);
        order.setOrderDate(orderDTORequestBody.getOrderDate());
        order.setRequiredDate(orderDTORequestBody.getRequiredDate());
        order.setShippedDate(orderDTORequestBody.getShippedDate());
        order.setStatus(orderDTORequestBody.getStatus());

        // Maps each member of collection containing requests to the class
        List<OrderDetails> orderDetailsList = ObjectMapperUtils
                .mapAll(orderDTORequestBody.getOrderDetailsDTO(), OrderDetails.class);

        int counter = 0;
        for (OrderDetails orderDetails : orderDetailsList) {
            Product product = productDAO.findByCode(orderDTORequestBody.getOrderDetailsDTO().get(counter).getProductCode());
            orderDetails.setOrder(order);
            orderDetails.setProduct(product);
            orderDetails.setQuantityOrdered(orderDTORequestBody.getOrderDetailsDTO().get(counter).getQuantityOrdered());
            orderDetails.setPrice(orderDTORequestBody.getOrderDetailsDTO().get(counter).getPrice());
            counter++;
        }

        order.setOrderDetails(orderDetailsList);

        // FIXME: order_number in order_details is null while update is performed
        orderDAO.update(order);

        return orderDTORequestBody;
    }

    /**
     * Method to delete order by number taken from the REST controller
     *
     * @param orderNumber get oder number from th REST controller
     * @return Order entity as a response
     */
    // FIXME: order_number in order_details is null while delete is performed
    public OrderDTO deleteOrderByNumber(Long orderNumber) {
        Order order = orderDAO.findOne(orderNumber);
        orderDAO.delete(order);
        return modelMapper.map(order, OrderDTO.class);
    }

    /**
     * Delete order method
     *
     * @param customerNumber get customer number form the REST controller
     * @param orderNumber    get order number form the REST controller
     * @return Order entity as a response
     */
    // FIXME: So that it was able to delete entity by customer and order numbers
    public OrderDTO deleteOrderByCustomerIdAndOrderId(Long customerNumber, Long orderNumber) {

        Order order = orderDAO.findOne(orderNumber);
        orderDAO.delete(order);

        return modelMapper.map(order, OrderDTO.class);
    }

    /**
     * Util method to get order by specific parameter
     *
     * @param orders Gets list of the order entities
     * @return List of the order DTOs
     */
    private List<OrderDTO> getOrdersByListWithParam(List<Order> orders) {
        List<OrderDTO> response = new LinkedList<>();

        for (Order order : orders) {
            // ModelMapper is used to avoid manual conversion from entity to DTO using setters and getters
            OrderDTO orderDTO = modelMapper.map(order, OrderDTO.class);

            List<OrderDetails> orderDetailsList =
                    orderDetailsDAO.findAllByOrderNumber(orderDTO.getOrderNumber());

            List<OrderDetailsDTO> orderDetailsDTOS = new ArrayList<>();
            for (OrderDetails orderDetails : orderDetailsList) {
                // ModelMapper is used to avoid manual conversion from entity to DTO using setters and getters
                OrderDetailsDTO orderDetailsDTO = modelMapper.map(orderDetails, OrderDetailsDTO.class);
                orderDetailsDTOS.add(orderDetailsDTO);
            }

            orderDTO.setOrderDetailsDTO(orderDetailsDTOS);
            response.add(orderDTO);
        }

        return response;
    }
}