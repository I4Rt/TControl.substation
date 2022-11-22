package com.i4rt.temperaturecontrol.basic;

import com.i4rt.temperaturecontrol.databaseInterfaces.*;
import com.i4rt.temperaturecontrol.model.ControlObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.File;
import java.io.FilenameFilter;
import java.util.Arrays;

@Component
public class FolderManager {

    @Autowired
    private final ControlObjectRepo controlObjectRepo;

    public FolderManager(ControlObjectRepo controlObjectRepo){
        this.controlObjectRepo = controlObjectRepo;
    }

    public void renameFolders(ControlObject controlObject, String newName){
            String lastName = controlObject.getName();
            File file = new File(System.getProperty("user.dir")+"\\src\\main\\upload\\static\\imgData");
            String[] directories = file.list();

            for (String area: directories){
                File folder = new File(System.getProperty("user.dir")+"\\src\\main\\upload\\static\\imgData\\" + area);
                String[] areas = folder.list();
                for (String folderArea: areas){
                    File nameArea = new File(System.getProperty("user.dir")+"\\src\\main\\upload\\static\\imgData\\"
                            + area + "\\" + folderArea);
                    if (folderArea.equals(lastName)) {
                        File newNameArea = new File(System.getProperty("user.dir")+"\\src\\main\\upload\\static\\imgData\\"
                                + area + "\\" + newName);
                        nameArea.renameTo(newNameArea);
                    }
                }
                System.out.println(Arrays.toString(areas));
            }


//        String[] directories = file.list(new FilenameFilter() {
//            @Override
//            public boolean accept(File current, String name) {
//                return new File(current, name).isDirectory();
//            }
//        });
//        for (String area: directories){
//            File file1 = new File(System.getProperty("user.dir")+"\\src\\main\\upload\\static\\imgData\\" + area);
//            String[] file1Directories = file1.list(new FilenameFilter() {
//                @Override
//                public boolean accept(File current, String name) {
//                    return new File(current, name).isDirectory();
//                }
//            });
//            System.out.println(Arrays.toString(file1Directories));
//        }

    }
    // remove all folders before date

    // rename all folders in parent folders
}
