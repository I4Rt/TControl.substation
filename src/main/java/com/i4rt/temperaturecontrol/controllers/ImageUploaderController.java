package com.i4rt.temperaturecontrol.controllers;

import com.i4rt.temperaturecontrol.additional.UploadedImageCounter;
import com.i4rt.temperaturecontrol.basic.FileUploadUtil;
import com.i4rt.temperaturecontrol.databaseInterfaces.ControlObjectRepo;
import com.i4rt.temperaturecontrol.databaseInterfaces.MeasurementRepo;
import com.i4rt.temperaturecontrol.databaseInterfaces.UserRepo;
import com.i4rt.temperaturecontrol.model.ControlObject;
import com.i4rt.temperaturecontrol.model.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.util.List;

@Controller
public class ImageUploaderController {


    @Autowired
    private ControlObjectRepo controlObjectRepo;
    @Autowired
    private final UserRepo userRepo;
    @Autowired
    private final MeasurementRepo measurementRepo;

    public ImageUploaderController(ControlObjectRepo controlObjectRepo, UserRepo userRepo, MeasurementRepo measurementRepo) {
        this.controlObjectRepo = controlObjectRepo;
        this.userRepo = userRepo;
        this.measurementRepo = measurementRepo;
    }

    @RequestMapping(value = "/setNewMap", method = RequestMethod.POST)
    public String setNewMap(@RequestParam("image") MultipartFile multipartFile, Model model) throws IOException {
        System.out.println(multipartFile.getName());

        List<ControlObject> controlObjects = controlObjectRepo.findAll();
        List<ControlObject> controlObjectsToDisplay = controlObjectRepo.getControlObjectsToDisplay();
        model.addAttribute("controlObjects", controlObjects);
        model.addAttribute("controlObjectsToDisplay", controlObjectsToDisplay);


        if (multipartFile != null){
            File previousMap = new File(System.getProperty("user.dir") + "/src/main/upload/static/img/bg/map"+UploadedImageCounter.getCurrentCounter()+".png");
            previousMap.delete();
            FileUploadUtil.saveFile("./src/main/upload/static/img/bg/", "map"+ UploadedImageCounter.increaseCounter()+".png", multipartFile);
        }
        model.addAttribute("src", "\"img/bg/map"+UploadedImageCounter.getCurrentCounter()+".png\"");

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


}
