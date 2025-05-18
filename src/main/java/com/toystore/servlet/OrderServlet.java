package com.toystore.servlet;

import com.toystore.model.Order;
import com.toystore.model.OrderItem;
import com.toystore.util.FileHandler;
import com.toystore.util.DiscountCalculator;
import java.io.IOException;
import java.util.UUID;
import java.util.List;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

@WebServlet("/order/*")
public class OrderServlet extends HttpServlet {
    
    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) 
            throws ServletException, IOException {
        String action = request.getPathInfo();
        
        switch (action) {
            case "/place":
                placeOrder(request, response);
                break;
            case "/update":
                updateOrder(request, response);
                break;
            case "/cancel":
                cancelOrder(request, response);
                break;
            default:
                response.sendError(HttpServletResponse.SC_BAD_REQUEST);
        }
    }
    
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) 
            throws ServletException, IOException {
        String action = request.getPathInfo();
        
        switch (action) {
            case "/history":
                viewOrderHistory(request, response);
                break;
            case "/details":
                viewOrderDetails(request, response);
                break;
            default:
                response.sendError(HttpServletResponse.SC_BAD_REQUEST);
        }
    }
    
    private void placeOrder(HttpServletRequest request, HttpServletResponse response) 
            throws ServletException, IOException {
        HttpSession session = request.getSession();
        String userId = (String) session.getAttribute("userId");
        String userType = (String) session.getAttribute("userType");
        
        if (userId == null) {
            response.sendRedirect("/login");
            return;
        }
        
        Order order = new Order();
        order.setOrderId(UUID.randomUUID().toString());
        order.setUserId(userId);
        order.setDeliveryAddress(request.getParameter("deliveryAddress"));
        order.setPaymentMethod(request.getParameter("paymentMethod"));
        
        // Add items to order (simplified for example)
        String[] toyIds = request.getParameterValues("toyId");
        String[] quantities = request.getParameterValues("quantity");
        
        for (int i = 0; i < toyIds.length; i++) {
            OrderItem item = new OrderItem(
                toyIds[i],
                request.getParameter("toyName_" + toyIds[i]),
                Double.parseDouble(request.getParameter("price_" + toyIds[i])),
                Integer.parseInt(quantities[i])
            );
            order.addItem(item);
        }
        
        // Apply discount
        DiscountCalculator calculator = DiscountCalculator.getDiscountCalculator(userType);
        double discount = calculator.calculateDiscount(order.getTotalAmount());
        order.setTotalAmount(order.getTotalAmount() - discount);
        
        FileHandler.saveOrder(order);
        response.sendRedirect("/order/history");
    }
    
    private void updateOrder(HttpServletRequest request, HttpServletResponse response) 
            throws ServletException, IOException {
        String orderId = request.getParameter("orderId");
        List<Order> orders = FileHandler.loadOrders();
        
        Order order = orders.stream()
                .filter(o -> o.getOrderId().equals(orderId))
                .findFirst()
                .orElse(null);
        
        if (order != null && "PENDING".equals(order.getStatus())) {
            order.setDeliveryAddress(request.getParameter("deliveryAddress"));
            order.setPaymentMethod(request.getParameter("paymentMethod"));
            
            // Update items
            order.getItems().clear();
            String[] toyIds = request.getParameterValues("toyId");
            String[] quantities = request.getParameterValues("quantity");
            
            for (int i = 0; i < toyIds.length; i++) {
                OrderItem item = new OrderItem(
                    toyIds[i],
                    request.getParameter("toyName_" + toyIds[i]),
                    Double.parseDouble(request.getParameter("price_" + toyIds[i])),
                    Integer.parseInt(quantities[i])
                );
                order.addItem(item);
            }
            
            FileHandler.updateOrder(order);
        }
        
        response.sendRedirect("/order/history");
    }
    
    private void cancelOrder(HttpServletRequest request, HttpServletResponse response) 
            throws ServletException, IOException {
        String orderId = request.getParameter("orderId");
        List<Order> orders = FileHandler.loadOrders();
        
        Order order = orders.stream()
                .filter(o -> o.getOrderId().equals(orderId))
                .findFirst()
                .orElse(null);
        
        if (order != null && "PENDING".equals(order.getStatus())) {
            order.setStatus("CANCELLED");
            FileHandler.updateOrder(order);
        }
        
        response.sendRedirect("/order/history");
    }
    
    private void viewOrderHistory(HttpServletRequest request, HttpServletResponse response) 
            throws ServletException, IOException {
        HttpSession session = request.getSession();
        String userId = (String) session.getAttribute("userId");
        
        if (userId == null) {
            response.sendRedirect("/login");
            return;
        }
        
        List<Order> orders = FileHandler.getOrdersByUser(userId);
        request.setAttribute("orders", orders);
        request.getRequestDispatcher("/OrderHistory.jsp")
                .forward(request, response);
    }
    
    private void viewOrderDetails(HttpServletRequest request, HttpServletResponse response) 
            throws ServletException, IOException {
        String orderId = request.getParameter("orderId");
        List<Order> orders = FileHandler.loadOrders();
        
        Order order = orders.stream()
                .filter(o -> o.getOrderId().equals(orderId))
                .findFirst()
                .orElse(null);
        
        if (order != null) {
            request.setAttribute("order", order);
            request.getRequestDispatcher("/EditOrder.jsp")
                    .forward(request, response);
        } else {
            response.sendRedirect("/order/history");
        }
    }
} 