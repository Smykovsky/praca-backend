package pl.kamil.praca.diet.model;

import javax.persistence.*;
import lombok.*;

import java.time.LocalDate;
import java.util.List;

@AllArgsConstructor
@NoArgsConstructor
@Data
@Table(name = "meal")
@Entity
@Getter
@Setter
public class Meal {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String name;
    @Column(name = "created_at")
    private LocalDate createdAt;
    @Column(name = "updated_at")
    private LocalDate updatedAt;
    @OneToMany(fetch = FetchType.LAZY, cascade = CascadeType.ALL)
    private List<FoodItem> foodItems;

    public Meal(String name) {
        this.name = name;
    }

    @PrePersist
    void prePersist() {
        this.createdAt = LocalDate.now();
    }

    @PreUpdate
    void preUpdate() {
        this.updatedAt = LocalDate.now();
    }

    public void addFoodItems(FoodItem foodItem) {
        this.foodItems.add(foodItem);
    }

    public void removeFoodItems(FoodItem foodItem) {
        this.foodItems.remove(foodItem);
    }

    public void removeFoodItems(Long id) {
        this.foodItems.removeIf(foodItem -> foodItem.getId().equals(id));
    }

    public FoodItem getFoodItem(Long id) {
        return this.foodItems.stream().filter(foodItem -> foodItem.getId().equals(id)).findFirst().orElse(null);
    }

}
