package shop_service;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

@RestController
@RequestMapping("/api/orders")
public class OrderController {

    private final CartController cartController;
    private final Map<Long, Map<String, Object>> orders = new ConcurrentHashMap<>();
    private final AtomicLong nextId = new AtomicLong(1);

    public OrderController(CartController cartController) {
        this.cartController = cartController;
    }

    @PostMapping
    public ResponseEntity<?> createOrder(@RequestBody Map<String, String> body) {
        Long userId = Long.valueOf(body.get("userId"));
        String paymentMethod = body.get("paymentMethod");
        String address = body.get("address");

        Cart cart = cartController.getCartInternal(userId);
        if (cart == null || cart.getItems().isEmpty()) {
            return ResponseEntity.badRequest().body(Map.of("error", "Cart is empty"));
        }

        if (paymentMethod == null || (!paymentMethod.equals("CARD") && !paymentMethod.equals("PAYPAL"))) {
            return ResponseEntity.badRequest().body(Map.of("error", "Invalid payment method"));
        }

        if (address == null || address.isBlank()) {
            return ResponseEntity.badRequest().body(Map.of("error", "Address required"));
        }

        Long orderId = nextId.getAndIncrement();
        Map<String, Object> order = new HashMap<>();
        order.put("id", orderId);
        order.put("userId", userId);
        order.put("items", cart.getItems());
        order.put("subtotal", cart.getSubtotal());
        order.put("tax", cart.getTax());
        order.put("total", cart.getTotal());
        order.put("paymentMethod", paymentMethod);
        order.put("address", address);
        order.put("status", "PAID");
        order.put("createdAt", LocalDateTime.now().toString());

        orders.put(orderId, order);
        cartController.clearCartInternal(userId);

        return ResponseEntity.ok(order);
    }

    @GetMapping("/{id}")
    public ResponseEntity<?> getOrder(@PathVariable Long id) {
        Map<String, Object> order = orders.get(id);
        if (order == null) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(order);
    }
}