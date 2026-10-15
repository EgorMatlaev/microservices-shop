package product_service;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/products")
public class ProductController {
    private final List<Product> products = new ArrayList<>();

    public ProductController(){
        products.add(new Product(1L, "Ноутбук", "Электроника", 50000));
        products.add(new Product(2L, "Мышь", "Электроника", 1500));
        products.add(new Product(3L, "Книга Java", "Книги", 2000));
        products.add(new Product(4L, "Футболка", "Одежда", 1200));
        products.add(new Product(5L, "Смартфон", "Электроника", 30000));
    }
    @GetMapping
    public ResponseEntity<List<Product>>search(
            @RequestParam(required = false) String name,
            @RequestParam(required = false) String category,
            @RequestParam(required = false) Double minPrice,
            @RequestParam(required = false) Double maxPrice){

        List<Product>result = products.stream()
                .filter(p ->name == null || p.getName().toLowerCase().contains(name.toLowerCase()))
                .filter(p -> category == null || p.getCategory().equalsIgnoreCase(category))
                .filter(p -> minPrice == null || p.getPrice() >= minPrice)
                .filter(p -> maxPrice == null || p.getPrice() <= maxPrice)
                .sorted((a, b) -> Double.compare(a.getPrice(), b.getPrice()))
                .collect(Collectors.toList());

        return ResponseEntity.ok(result);

    }
    @GetMapping("/{id}")
    public ResponseEntity<Product>getById(@PathVariable Long id){
        return products.stream()
                .filter(p -> p.getId().equals(id))
                .findFirst()
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

}
