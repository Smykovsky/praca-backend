package pl.kamil.praca.authentication.model;

import javax.persistence.*;
import lombok.*;
import org.apache.tomcat.jni.Local;
import org.springframework.lang.Nullable;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import pl.kamil.praca.diet.model.FoodItem;
import pl.kamil.praca.diet.model.Meal;
import pl.kamil.praca.diet.model.ShoppingItem;
import pl.kamil.praca.diet.model.UserProgress;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

@Entity
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Builder
@Data
@Table(name = "user")
public class User {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String username;
    private String email;
    private String password;
    private boolean isBlocked;

    @Nullable
    private int age;
    @Nullable
    private String gender;
    @Nullable
    private Double weight;
    @Nullable
    private Double height;
    @Nullable
    private String goal;
    @Nullable
    private String activity;
    @Nullable
    private Double calorieIntakeGoal;
    @Nullable
    private Double caloriesEaten;
    @Column(name = "created_at")
    private LocalDate createdAt;
    @Column(name = "updated_at")
    private LocalDate updatedAt;

    @ManyToMany(fetch = FetchType.EAGER)
    private Collection<Role> roles = new ArrayList<>();

    @OneToMany(fetch = FetchType.LAZY, cascade = CascadeType.ALL)
    private List<Meal> meals; // meals list -> breakfast, second_breakfast, lunch, dinner

    @OneToMany(fetch = FetchType.LAZY, cascade = CascadeType.ALL)
    private List<UserProgress> progressList; // user's progress list

    @OneToMany(fetch = FetchType.LAZY, cascade = CascadeType.ALL)
    private List<ShoppingItem> shoppingList;

    @PrePersist
    void prePersist() {
        this.createdAt = LocalDate.now();
    }

    @PreUpdate
    void preUpdate() {
        this.updatedAt = LocalDate.now();
    }

    public Collection<GrantedAuthority> roleToAuthority() {
        final Collection<GrantedAuthority> grantedAuthorities = new ArrayList<>();
        this.roles.forEach(role -> grantedAuthorities.add(new SimpleGrantedAuthority(role.getName())));
        return grantedAuthorities;
    }

    public void addRole(Role role) {
        roles.add(role);
    }

    public void removeRole(Role role) {
        roles.remove(role);
    }

    //meals methods
    public void addMeal(Meal meal) {
        this.meals.add(meal);
    }

    public void removeMeal(Meal meal) {
        this.meals.remove(meal);
    }

    public void removeMeal(Long id) {
        this.meals.removeIf(meal -> meal.getId().equals(id));
    }

    public Meal getMeal(Long id) {
        return this.meals.stream().filter(meal -> meal.getId().equals(id)).findFirst().orElse(null);
    }

    //user progress methods
    public void addUserProgress(UserProgress userProgress) {
        this.progressList.add(userProgress);
    }

    public void removeUserProgress(UserProgress userProgress) {
        this.progressList.remove(userProgress);
    }

    public void removeUserProgress(Long id) {
        this.progressList.removeIf(progress -> progress.getId().equals(id));
    }

    public UserProgress getUserProgress(Long id) {
        return this.progressList.stream().filter(progress -> progress.getId().equals(id)).findFirst().orElse(null);
    }

    public List<Meal> getMealsPerDay() {
        return meals.stream()
                .filter(meal -> meal.getCreatedAt().isEqual(LocalDate.now()))
                .collect(Collectors.toList());
    }

    public List<Meal> getMealsPerDayByDate(LocalDate date) {
        return meals.stream()
                .filter(meal -> meal.getCreatedAt().isEqual(date))
                .collect(Collectors.toList());
    }

    public List<FoodItem> getFoodItemsPerDay() {
        return meals.stream()
                .flatMap(meal -> meal.getFoodItems().stream())
                .filter(foodItem -> foodItem.getCreatedAt().isEqual(LocalDate.now()))
                .collect(Collectors.toList());
    }

    public Double getEatenCalories() {
        return meals.stream()
                .filter(meal -> meal.getCreatedAt().equals(LocalDate.now()))
                .map(Meal::getFoodItems).map(foodItems -> foodItems.stream()
                        .filter(foodItem -> foodItem.getCreatedAt().equals(LocalDate.now()))
                        .map(FoodItem::getCalories)
                        .reduce(0.0, Double::sum))
                .reduce(0.0, Double::sum);
    }

    public Double getEatenProteins() {
        return meals.stream()
                .filter(meal -> meal.getCreatedAt().equals(LocalDate.now()))
                .map(Meal::getFoodItems).map(foodItems -> foodItems.stream()
                        .filter(foodItem -> foodItem.getCreatedAt().equals(LocalDate.now()))
                        .map(FoodItem::getProtein)
                        .reduce(0.0, Double::sum))
                .reduce(0.0, Double::sum);
    }

    public Double getEatenCarbohydrates() {
        return meals.stream()
                .filter(meal -> meal.getCreatedAt().equals(LocalDate.now()))
                .map(Meal::getFoodItems).map(foodItems -> foodItems.stream()
                        .filter(foodItem -> foodItem.getCreatedAt().equals(LocalDate.now()))
                        .map(FoodItem::getCarbohydrates)
                        .reduce(0.0, Double::sum))
                .reduce(0.0, Double::sum);
    }

    public Double getEatenFats() {
        return meals.stream()
                .filter(meal -> meal.getCreatedAt().equals(LocalDate.now()))
                .map(Meal::getFoodItems).map(foodItems -> foodItems.stream()
                        .filter(foodItem -> foodItem.getCreatedAt().equals(LocalDate.now()))
                        .map(FoodItem::getFat)
                        .reduce(0.0, Double::sum))
                .reduce(0.0, Double::sum);
    }

    public void addShoppingItem(ShoppingItem item) {
        this.shoppingList.add(item);
    }

    public void removeShoppingItem(ShoppingItem item) {
        this.shoppingList.remove(item);
    }
    public void removeAllShoppingItems() {
        this.shoppingList.clear();
    }

    public List<ShoppingItem> getShoppingItemList() {
        return shoppingList;
    }
}
