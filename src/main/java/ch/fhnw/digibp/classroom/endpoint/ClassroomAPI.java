/*
 * Copyright (c) 2019. University of Applied Sciences and Arts Northwestern Switzerland FHNW.
 * All rights reserved.
 */

package ch.fhnw.digibp.classroom.endpoint;

import ch.fhnw.digibp.classroom.config.ClassroomProperties;
import ch.fhnw.digibp.classroom.service.TenantService;
import ch.fhnw.digibp.classroom.service.UserService;
import org.camunda.bpm.engine.IdentityService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;

@RestController
@RequestMapping(path = "/classroom")
public class ClassroomAPI {

    @Autowired
    private UserService userService;

    @Autowired
    private TenantService tenantService;

    @Autowired
    private ClassroomProperties classroomProperties;

    @Autowired
    private IdentityService identityService;

    @GetMapping(path = "/generator/user", produces = "application/json")
    public ResponseEntity<List<String>> getNewUserAndTenant(@RequestParam(value = "prefix", required = false, defaultValue = "") String prefix, @RequestParam(value = "firstId") Integer firstId, @RequestParam(value = "lastId") Integer lastId, @RequestParam(value = "suffix", required = false, defaultValue = "") String suffix){
        if(!isAdminAuthentication()){
            return new ResponseEntity<>(HttpStatus.UNAUTHORIZED);
        }
        List<String> response = new ArrayList<>();
        String id;
        for (int number=firstId; number<=lastId; number++) {
            String instanceId = prefix + number + suffix;
            try {
                id = tenantService.addTenant(instanceId, instanceId);
                response.add("Tenant with ID " + id + " generated.");

                id = userService.addUser(instanceId+"giulia", "password", instanceId+" Giulia", "Ricci", "", new String[]{"owner"}, instanceId);
                response.add("User with ID " + id + " generated.");
                id = userService.addUser(instanceId+"martina", "password", instanceId+" Martina", "Russo", "", new String[]{"manager"}, instanceId);
                response.add("User with ID " + id + " generated.");
                id = userService.addUser(instanceId+"sofia", "password", instanceId+" Sofia", "Conti", "", new String[]{"analyst"}, instanceId);
                response.add("User with ID " + id + " generated.");
                id = userService.addUser(instanceId+"chiara", "password", instanceId+" Chiara", "Lombardi", "", new String[]{"engineer"}, instanceId);
                response.add("User with ID " + id + " generated.");
                id = userService.addUser(instanceId+"beppe", "password", instanceId+" Beppe", "Ferrari", "", new String[]{"initiator", "assistant"}, instanceId);
                response.add("User with ID " + id + " generated.");
                id = userService.addUser(instanceId+"matteo", "password", instanceId+" Matteo", "Alfonsi", "", new String[]{"worker", "chef"}, instanceId);
                response.add("User with ID " + id + " generated.");
                id = userService.addUser(instanceId+"silvio", "password", instanceId+" Silvio", "Esposito", "", new String[]{"worker", "courier"}, instanceId);
                response.add("User with ID " + id + " generated.");
            } catch (Exception e) {
                response.add("Error: " + e.getMessage());
                return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
            }
        }
        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }

    @DeleteMapping(path = "/generator/user")
    public ResponseEntity<List<String>> deleteUserAndTenant(@RequestParam(value = "prefix", required = false, defaultValue = "") String prefix, @RequestParam(value = "firstId") Integer firstId, @RequestParam(value = "lastId") Integer lastId, @RequestParam(value = "suffix", required = false, defaultValue = "") String suffix){
        if(!isAdminAuthentication()){
            return new ResponseEntity<>(HttpStatus.UNAUTHORIZED);
        }
        List<String> response = new ArrayList<>();
        String id;
        for (int number=firstId; number<=lastId; number++) {
            String instanceId = prefix + number + suffix;
            id = userService.removeUser(instanceId+"giulia");
            response.add("User with ID " + id + " removed.");
            id = userService.removeUser(instanceId+"martina");
            response.add("User with ID " + id + " removed.");
            id = userService.removeUser(instanceId+"sofia");
            response.add("User with ID " + id + " removed.");
            id = userService.removeUser(instanceId+"chiara");
            response.add("User with ID " + id + " removed.");
            id = userService.removeUser(instanceId+"beppe");
            response.add("User with ID " + id + " removed.");
            id = userService.removeUser(instanceId+"matteo");
            response.add("User with ID " + id + " removed.");
            id = userService.removeUser(instanceId+"silvio");
            response.add("User with ID " + id + " removed.");

            try {
                tenantService.removeTenant(instanceId);
                response.add("Tenant with ID " + instanceId + " removed.");
            } catch (Exception e) {
                response.add("Error: " + e.getMessage());
                return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
            }
        }
        return new ResponseEntity<>(response, HttpStatus.ACCEPTED);
    }

    @GetMapping(path = "/properties")
    public ResponseEntity<ClassroomProperties> getClassroomProperties(){
        return new ResponseEntity<>(this.classroomProperties, HttpStatus.ACCEPTED);
    }

    @PutMapping(path = "/properties")
    public ResponseEntity<ClassroomProperties> putClassroomProperties(@RequestBody ClassroomProperties classroomProperties){
        if(!isAdminAuthentication()){
            return new ResponseEntity<>(HttpStatus.UNAUTHORIZED);
        }
        this.classroomProperties = classroomProperties;
        return new ResponseEntity<>(this.classroomProperties, HttpStatus.ACCEPTED);
    }

    protected Boolean isAdminAuthentication(){
        return identityService.getCurrentAuthentication().getGroupIds().contains("camunda-admin");
    }
}
