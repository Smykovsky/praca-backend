package pl.kamil.praca.authentication.service;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import pl.kamil.praca.authentication.model.User;
import pl.kamil.praca.authentication.repository.RoleRepository;
import pl.kamil.praca.authentication.repository.UserRepository;
import pl.kamil.praca.diet.model.Meal;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

@Service
public class UserService implements UserDetailsService {
    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;

    public UserService(UserRepository userRepository, RoleRepository roleRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.roleRepository = roleRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        User user = userRepository.findByUsername(username);
        if (user == null) {
            throw new UsernameNotFoundException("User not found");
        }

        Collection<SimpleGrantedAuthority> authorities = new ArrayList<>();
        user.getRoles().forEach(role -> {
            authorities.add(new SimpleGrantedAuthority(role.getName()));
        });
        return new org.springframework.security.core.userdetails.User(user.getUsername(), user.getPassword(), authorities);
    }

    public User getUser(String username) {
        return userRepository.findByUsername(username);
    }

    public User getUser(String username, String email) {
        return userRepository.findByUsernameOrEmail(username, email);
    }

    public List<User> getUsers() {
        return userRepository.findAll();
    }

    public UserDetails getUserDetails(String username) {
        final User user = this.getUser(username);
        if (user == null) {
            return null;
        }
        return new org.springframework.security.core.userdetails.User(user.getUsername(), user.getPassword(), user.roleToAuthority());
    }

    public UserDetails getUserDetails(User user) {
        return new org.springframework.security.core.userdetails.User(user.getUsername(), user.getPassword(), user.roleToAuthority());

    }

    public boolean existsByUsernameOrEmail(final String email, final String username) {
        return this.userRepository.existsByUsernameOrEmail(email, username);
    }

    public User saveUser(User user) {
        user.setPassword(passwordEncoder.encode(user.getPassword()));
        return userRepository.save(user);
    }

    public Double calculateCaloriesMan(User user) {
        double caloriesGoal = 66 + (13.7 * user.getWeight()) + (5 * user.getHeight()) - (6 * user.getAge());

        if (user.getGoal().equals("Schudn????")) {
            return caloriesGoal - 300;
        } else if (user.getGoal().equals("Zbudowa?? mas??")) {
            return caloriesGoal + 300;
        } else return caloriesGoal;
    }

    public Double calculateCaloriesWomen(User user) {
        double caloriesGoal = 655 + (9.6 * user.getWeight()) + (1.8 * user.getHeight()) - (4.7 * user.getAge());

        if (user.getGoal().equals("Redukcja")) {
            return caloriesGoal - 300;
        } else if (user.getGoal().equals("Masa mi????niowa")) {
            return caloriesGoal + 300;
        } else return caloriesGoal;
    }

    public Double getCaloriesFromUser(Authentication authentication) {
        if (authentication == null || !authentication.isAuthenticated()) {
            return null;
        }
        User user = getUser(authentication.getName());
        List<Meal> meals = user.getMeals();
        double calories = meals.stream()
                .map(Meal::getFoodItems)
                .map(foodItems -> foodItems.stream().map(foodItem -> foodItem.getCalories()).reduce(0.0, Double::sum))
                .reduce(0.0, Double::sum);

        double x;

        return calories;
    }
}
