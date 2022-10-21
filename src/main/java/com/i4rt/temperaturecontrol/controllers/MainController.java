package com.i4rt.temperaturecontrol.controllers;

import com.i4rt.temperaturecontrol.additional.GotPicImageCounter;
import com.i4rt.temperaturecontrol.additional.UploadedImageCounter;
import com.i4rt.temperaturecontrol.databaseInterfaces.ControlObjectRepo;
import com.i4rt.temperaturecontrol.databaseInterfaces.MeasurementRepo;
import com.i4rt.temperaturecontrol.databaseInterfaces.ThermalImagerRepo;
import com.i4rt.temperaturecontrol.databaseInterfaces.UserRepo;
import com.i4rt.temperaturecontrol.device.ThermalImager;
import com.i4rt.temperaturecontrol.model.ControlObject;
import com.i4rt.temperaturecontrol.model.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.factory.PasswordEncoderFactories;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import javax.xml.bind.SchemaOutputResolver;
import java.io.IOException;
import java.util.Collection;
import java.util.List;

@Controller
public class MainController {

    @Autowired
    private ControlObjectRepo controlObjectRepo;
    @Autowired
    private final MeasurementRepo measurementRepo;
    @Autowired
    private final ThermalImagerRepo thermalImagerRepo;
    @Autowired
    private final UserRepo userRepo;


    public MainController(ControlObjectRepo controlObjectRepo, MeasurementRepo measurementRepo, ThermalImagerRepo thermalImagerRepo, UserRepo userRepo) {

        this.controlObjectRepo = controlObjectRepo;
        this.measurementRepo = measurementRepo;
        this.thermalImagerRepo = thermalImagerRepo;
        this.userRepo = userRepo;
    }

    @GetMapping("/")
    public String gotoMain(){
        return "redirect:/main";
    }

    @GetMapping("main")
    public String getMainPage(Model model) throws IOException {

        List<ControlObject> controlObjects = controlObjectRepo.findAll();
        List<ControlObject> controlObjectsToDisplay = controlObjectRepo.getControlObjectsToDisplay();
        model.addAttribute("controlObjects", controlObjects);
        model.addAttribute("controlObjectsToDisplay", controlObjectsToDisplay);

        model.addAttribute("src", "\"img/bg/map"+ UploadedImageCounter.getCurrentCounter() +".png\""); //Make getCurrentCounter method do not throw exception (try/catch)

        User user = userRepo.getByUserLogin(SecurityContextHolder.getContext().getAuthentication().getName());
        model.addAttribute("user", user);

        user.setThermalImagerGrabbed(false);

        userRepo.save(user);

        if(user.getRole().equals("ADMIN")){
            model.addAttribute("adderClass", "");
        }
        else{
            model.addAttribute("adderClass", "hidden");
        }

        return "mainWindow";
    }

    @GetMapping("adding")
    public String getAddingPage(Model model) throws IOException {

        List<ControlObject> controlObjects = controlObjectRepo.findAll();
        model.addAttribute("controlObjects", controlObjects);

        ThermalImager ti = thermalImagerRepo.getById(Long.valueOf(1));

        model.addAttribute("horizontal", ti.getCurHorizontal());
        model.addAttribute("vertical", ti.getCurVertical());
        model.addAttribute("focusing", 500); // not work (non functionality)
        model.addAttribute("src", "img/loading.gif");


        User user = userRepo.getByUserLogin(SecurityContextHolder.getContext().getAuthentication().getName());
        model.addAttribute("user", user);

        user.setThermalImagerGrabbed(true);
        userRepo.save(user);


        if(user.getRole().equals("ADMIN")){
            model.addAttribute("adderClass", "");
        }
        else{
            model.addAttribute("adderClass", "hidden");
        }

        return "addingWindow";

    }


    @GetMapping("area")
    public String getAreaPage(@RequestParam Long id, Model model){
        ControlObject controlObject =  controlObjectRepo.getById(id);

        model.addAttribute("controlObject", controlObject);
        model.addAttribute("coordinatesString",  controlObject.getCoordinatesString());

        User user = userRepo.getByUserLogin(SecurityContextHolder.getContext().getAuthentication().getName());
        model.addAttribute("user", user);

        user.setThermalImagerGrabbed(false);
        userRepo.save(user);

        if(user.getRole().equals("ADMIN")){
            model.addAttribute("adderClass", "");
        }
        else{
            model.addAttribute("adderClass", "hidden");
        }

        return "areaWindow";
    }

    @GetMapping("newArea")
    public String addingAreaPage(Model model){
        ControlObject controlObject =  new ControlObject();
        System.out.println(controlObjectRepo.getLastObjectId());



        model.addAttribute("controlObject", controlObject);
        model.addAttribute("coordinatesString",  "");

        User user = userRepo.getByUserLogin(SecurityContextHolder.getContext().getAuthentication().getName());
        model.addAttribute("user", user);

        user.setThermalImagerGrabbed(false);
        userRepo.save(user);

        if(user.getRole().equals("ADMIN")){
            model.addAttribute("adderClass", "");
        }
        else{
            model.addAttribute("adderClass", "hidden");
        }

        return "areaWindow";
    }


    @GetMapping("/register")
    public String registerPage(Model model){

        User user = new User();

        User curUser = userRepo.getByUserLogin(SecurityContextHolder.getContext().getAuthentication().getName());
        curUser.setThermalImagerGrabbed(false);
        userRepo.save(curUser);

        model.addAttribute("user",  user);
        model.addAttribute("message",  "");

        return "register";
    }

    @PostMapping("/registerUser")
    public String registerUser(@ModelAttribute User user,Model model){

        User curUser = userRepo.getByUserLogin(SecurityContextHolder.getContext().getAuthentication().getName());
        curUser.setThermalImagerGrabbed(false);
        userRepo.save(curUser);

        model.addAttribute("user",  user);

        if(userRepo.getByUserLogin(user.getLogin()) == null){
            if(user.getPassword().length() >= 10){
                if(user.getPassword().equals(user.getPasswordRepeat())){
                    user.setPassword(PasswordEncoderFactories.createDelegatingPasswordEncoder().encode(user.getPassword()));
                    user.setPasswordRepeat(PasswordEncoderFactories.createDelegatingPasswordEncoder().encode(user.getPasswordRepeat()));
                    user.setRole("OPERATOR");

                    System.out.println(user);

                    userRepo.save(user);
                    model.addAttribute("message",  "Пользователь создан");
                }
                else{
                    model.addAttribute("message",  "Пароли не совпали");
                }
            }
            else{
                model.addAttribute("message",  "Длина пароля минимум 10 символов");
            }

        }
        else{
            model.addAttribute("message",  "Пользователь с таким логином существует");
        }

        return "register";
    }

    @GetMapping("/login")
    public String loginPage(Model model){



        User user = new User();

        model.addAttribute("user", user);
        model.addAttribute("message", "");
        return "login";
    }





    @PostMapping("/authorizeUser")
    public String authorizeUser(@ModelAttribute User user, Model model){

        User curUser = userRepo.getByUserLogin(user.getLogin());

        model.addAttribute("message", "Неверные данные входа");
        return "login";
    }


}
