package pl.kamil.praca.diet.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import pl.kamil.praca.authentication.model.User;
import pl.kamil.praca.authentication.service.UserService;
import pl.kamil.praca.authentication.view.UserDietViewModel;
import pl.kamil.praca.authentication.view.UserViewModel;
import pl.kamil.praca.diet.dto.PersonalizeRequest;
import pl.kamil.praca.diet.model.UserProgress;
import pl.kamil.praca.diet.service.FoodItemService;

import javax.validation.Valid;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/user")
public class UserController {
    private final UserService userService;
    private final FoodItemService foodItemService;


    @PostMapping("/personalize")
    public ResponseEntity<?> personalizeUser(@RequestBody @Valid PersonalizeRequest personalizeRequest, Authentication authentication) {
        Map<String, Object> responseMap = new HashMap<>();
        final String MAN = "Mężczyzna";
        final String WOMEN = "Kobieta";

        if (authentication == null || !authentication.isAuthenticated()) {
            responseMap.put("error", true);
            responseMap.put("message", "Użytkownik nie jest zautoryzowany!");
            return ResponseEntity.status(500).body(responseMap);
        }

        final User user = userService.getUser(authentication.getName());
        if (user == null) {
            return ResponseEntity.notFound().build();
        }
        user.setAge(personalizeRequest.getAge());
        user.setWeight(personalizeRequest.getWeight());
        user.setHeight(personalizeRequest.getHeight());
        user.setGender(personalizeRequest.getGender());
        user.setGoal(personalizeRequest.getGoal());

        user.addUserProgress(new UserProgress(personalizeRequest.getWeight()));

        if (personalizeRequest.getGender().equals(MAN)) {
            user.setCalorieIntakeGoal(userService.calculateCaloriesMan(user));
            userService.saveUser(user);
        } else
            user.setCalorieIntakeGoal(userService.calculateCaloriesWomen(user));
        userService.saveUser(user);



        responseMap.put("error", false);
        responseMap.put("user", user);
        responseMap.put("message", "Pomyślnie spersonalizowano użytkownika :)");
        return ResponseEntity.ok(responseMap);
    }

        @GetMapping("/data")
        public ResponseEntity<?> getUserData(Authentication authentication) {
            Map<String, Object> responseMap = new HashMap<>();
            if (authentication == null || !authentication.isAuthenticated()) {
                responseMap.put("error", true);
                responseMap.put("message", "Użytkownik nie jest zautoryzowany!");
                return ResponseEntity.status(500).body(responseMap);
            }

            final User user = userService.getUser(authentication.getName());
            if (user == null) {
                return ResponseEntity.notFound().build();
            }

            return ResponseEntity.ok(new UserDietViewModel(user));
        }
}
