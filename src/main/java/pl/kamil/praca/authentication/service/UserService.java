package pl.kamil.praca.authentication.service;

import javax.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import pl.kamil.praca.authentication.model.Role;
import pl.kamil.praca.authentication.model.User;
import pl.kamil.praca.authentication.repository.RoleRepository;
import pl.kamil.praca.authentication.repository.UserRepository;
import pl.kamil.praca.diet.model.Meal;

import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Optional;

@Service
@Transactional
@Slf4j
@RequiredArgsConstructor
public class UserService implements UserDetailsService {
    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final JavaMailSender javaMailSender;
    @Value("${spring.mail.username}")
    private String serviceEmail;

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
    public Optional<User> getUser(Long id) {
        return userRepository.findById(id);
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
        return userRepository.save(user);
    }

    public Role findRoleByName(String name) {
        return roleRepository.findByName(name);
    }

    public Double calculateCaloriesMan(User user) {
        double caloriesGoal = 66 + (13.7 * user.getWeight()) + (5 * user.getHeight()) - (6 * user.getAge());

        if (user.getGoal().equals("Redukcja")) {
            caloriesGoal -= 300;
            System.out.println("wybieram redukcje");
        } else if (user.getGoal().equals("Masa mięśniowa")) {
            caloriesGoal += 300;
            System.out.println("wybieram mase");
        } else if (user.getGoal().equals("Utrzymanie wagi")) {
            caloriesGoal += 0;
        }

        if (user.getActivity().equals("low")) {
            caloriesGoal *= 1.2;
            System.out.println("activity low");
        } else if (user.getActivity().equals("medium")) {
            caloriesGoal *= 1.375;
            System.out.println("activity medium");
        } else if (user.getActivity().equals("high")) {
            caloriesGoal *= 1.55;
            System.out.println("activity high");
        } else if (user.getActivity().equals("very-high")) {
            caloriesGoal *= 1.725;
            System.out.println("acitvity very-high");
        }

        return caloriesGoal;
    }

    public Double calculateCaloriesWoman(User user) {
        double caloriesGoal = 655 + (9.6 * user.getWeight()) + (1.8 * user.getHeight()) - (4.7 * user.getAge());

        if (user.getGoal().equals("Redukcja")) {
            caloriesGoal -= 300;
        } else if (user.getGoal().equals("Masa mięśniowa")) {
            caloriesGoal += 300;
        } else if (user.getGoal().equals("Utrzymanie wagi")) {
            caloriesGoal += 0;
        }

        if (user.getActivity().equals("low")) {
            caloriesGoal *= 1.2;
        } else if (user.getActivity().equals("medium")) {
            caloriesGoal *= 1.375;
        } else if (user.getActivity().equals("high")) {
            caloriesGoal *= 1.55;
        } else if (user.getActivity().equals("very-high")) {
            caloriesGoal *= 1.725;
        }

        return caloriesGoal;
    }

    public String randomPasswordGenerator() {
        final String CHARACTERS = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789";
        SecureRandom random = new SecureRandom();
        StringBuilder builder = new StringBuilder(10);

        for (int i = 0; i < 10; i++) {
            int randomIndex = random.nextInt(CHARACTERS.length());
            char randomChar = CHARACTERS.charAt(randomIndex);
            builder.append(randomChar);
        }

        return builder.toString();
    }

    public void sendEmail(String email, String newPassword) {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setFrom(serviceEmail);
        message.setTo(email);
        message.setText("Hasło zostało zmienione! Nowe hasło to: " + newPassword);
        message.setSubject("New password");
        javaMailSender.send(message);
    }
}
