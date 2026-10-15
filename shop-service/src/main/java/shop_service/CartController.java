package shop_service;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.ResourceAccessException;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@RestController
@RequestMapping("/api/carts")
public class CartController {

    private final ProductServiceClient productServiceClient;
    private final Map<Long, Cart> carts = new ConcurrentHashMap<>();

    public CartController(ProductServiceClient productServiceClient) {
        this.productServiceClient = productServiceClient;
    }

    @PostMapping("/{userId}/items")
    public ResponseEntity<?> addItem(@PathVariable Long userId,
                                     @RequestBody Map<String, Object> body) {
        Long productId = Long.valueOf(body.get("productId").toString());
        int quantity = Integer.parseInt(body.get("quantity").toString());

        ProductDto product;
        try {
            product = productServiceClient.getProductById(productId);
        } catch (ResourceAccessException e) {
            return ResponseEntity.status(503).body(Map.of("ошибка", "Сервис товаров не доступен"));
        }

        if (product == null) {
            return ResponseEntity.status(404).body(Map.of("ошибка", "Продукт не найден"));
        }

        Cart cart = carts.computeIfAbsent(userId, Cart::new);

        CartItem existing = cart.getItems().stream()
                .filter(i -> i.getProductId().equals(productId))
                .findFirst().orElse(null);

        if (existing != null) {
            existing.setQuantity(existing.getQuantity() + quantity);
        } else {
            cart.getItems().add(new CartItem(productId, product.getName(), product.getPrice(), quantity));
        }

        return ResponseEntity.ok(cart);
    }

    @GetMapping("/{userId}")
    public ResponseEntity<Cart> getCart(@PathVariable Long userId) {
        Cart cart = carts.get(userId);
        if (cart == null) {
            cart = new Cart(userId);
        }
        return ResponseEntity.ok(cart);
    }

    @DeleteMapping("/{userId}/items/{productId}")
    public ResponseEntity<Cart> removeItem(@PathVariable Long userId, @PathVariable Long productId) {
        Cart cart = carts.get(userId);
        if (cart != null) {
            cart.getItems().removeIf(i -> i.getProductId().equals(productId));
        }
        return ResponseEntity.ok(cart);
    }

    @DeleteMapping("/{userId}")
    public ResponseEntity<?> clearCart(@PathVariable Long userId) {
        carts.remove(userId);
        return ResponseEntity.ok(Map.of("ошибка", "Корзина очищена"));
    }


    public Cart getCartInternal(Long userId) {
        return carts.get(userId);
    }

    public void clearCartInternal(Long userId) {
        carts.remove(userId);
    }
}
