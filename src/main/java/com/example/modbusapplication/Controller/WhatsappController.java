package com.example.modbusapplication.Controller;

import com.example.modbusapplication.Model.SchedulerRequestDTO;
import com.example.modbusapplication.Model.UpdateWhatsAppDAO;
import com.example.modbusapplication.Repository.SchedulerRepository;
import com.example.modbusapplication.Service.SchedulerService;
import com.example.modbusapplication.Service.WhatsAppService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.time.format.DateTimeFormatter;
import java.util.Map;

@RestController
@RequestMapping("/api/whatsapp")
@CrossOrigin
public class WhatsappController {

    

    @Autowired
    private SchedulerRepository schedulerRepository;
    @Autowired
    private WhatsAppService whatsAppService;
    @Autowired
    private SchedulerService schedulerService; 

    DateTimeFormatter formatter = DateTimeFormatter.ofPattern("HH:mm");

@PostMapping("/send-otp")
public ResponseEntity<?> sendOtp(@RequestParam String whatsappNumber) {
    int otp = (int)(Math.random() * 9000) + 1000;

    try {
        whatsAppService.sendotpTemplateMessage(whatsappNumber, "HX4ccf69236a581187d0688ef03b62fb1c", otp);
        return ResponseEntity.ok(Map.of(
                "status", "sent",
                "otp", otp,
                "number", whatsappNumber
        ));
    } catch (IllegalArgumentException e) {
        return ResponseEntity.badRequest().body(Map.of(
                "status", "failed",
                "error", e.getMessage()
        ));
    } catch (RuntimeException e) {
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of(
                "status", "failed",
                "error", e.getMessage()
        ));
    }
}


    @GetMapping("/get-user-whatsapp/{userKey}")
    public ResponseEntity<?> getUserWhatsAppDetails(@PathVariable short userKey) {
        return whatsAppService.getUserWhatsAppDetails(userKey);
    }

    @PostMapping("/update-whatsapp")
    public ResponseEntity<?> updateWhatsApp(@RequestBody UpdateWhatsAppDAO request) {
        return whatsAppService.updateWhatsAppSettings(request);
    }

    @PostMapping("/update")
    public String updateSchedule(@RequestBody SchedulerRequestDTO request) {
        return schedulerService.updateSchedule(request);
    }

    //View schedule by userKey
    @GetMapping("/{userKey}")
    public Object getByUserKey(@PathVariable int userKey) {
        return schedulerRepository.findByUserKey(userKey);
    }

    @DeleteMapping("/delete-number")
    public ResponseEntity<?> deleteWhatsAppNumber(
        @RequestParam short userKey,
        @RequestParam String whatsappNumber) {

    return whatsAppService.deleteWhatsAppNumber(userKey, whatsappNumber);
}


    
}
