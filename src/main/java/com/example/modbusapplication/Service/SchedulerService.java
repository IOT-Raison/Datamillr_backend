package com.example.modbusapplication.Service;

import com.example.modbusapplication.Entity.DeviceMapping;
import com.example.modbusapplication.Entity.WhatsAppScheduler;
import com.example.modbusapplication.Model.BatchReportDTO;
import com.example.modbusapplication.Model.SchedulerRequestDTO;
import com.example.modbusapplication.Repository.DeviceMappingRepository;
import com.example.modbusapplication.Repository.FlowRepository;
import com.example.modbusapplication.Repository.SchedulerRepository;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import java.sql.SQLException;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
public class SchedulerService {

    private final SchedulerRepository schedulerRepository;
    private final DeviceMappingRepository deviceMappingRepository;
    private final FlowRepository flowRepository;
    private final ModbusNotificationService notificationService;

    DateTimeFormatter formatter = DateTimeFormatter.ofPattern("HH:mm");

    public SchedulerService(
            SchedulerRepository schedulerRepository,
            DeviceMappingRepository deviceMappingRepository,
            FlowRepository flowRepository,
            ModbusNotificationService notificationService
    ) {
        this.schedulerRepository = schedulerRepository;
        this.deviceMappingRepository = deviceMappingRepository;
        this.flowRepository = flowRepository;
        this.notificationService = notificationService;
    }

    // Scheduled to run at 00, 15, 30, and 45 minutes of every hour
    @Scheduled(cron = "0 0,15,30,45 * * * *")
    public void checkSchedules() {
        LocalTime now = LocalTime.now();
        String currentTime = now.format(formatter);

        System.out.println("Scheduler Running at: " + currentTime);

        List<WhatsAppScheduler> schedules = schedulerRepository.findAll();

        for (WhatsAppScheduler s : schedules) {
            if (matches(currentTime, s.getTime1()) ||
                matches(currentTime, s.getTime2()) ||
                matches(currentTime, s.getTime3()) ||
                matches(currentTime, s.getTime4())) {

                System.out.println("Time matched for user: " + s.getUserKey());
                sendUserReports(s.getUserKey());
            }
        }
    }

    private boolean matches(String current, LocalTime target) {
        if (target == null) return false;
        return current.equals(target.format(formatter));
    }

    private void sendUserReports(int userKey) {
        List<DeviceMapping> devices = deviceMappingRepository.findByUserKey((short) userKey);

        for (DeviceMapping mapping : devices) {
            try {
                
        BatchReportDTO dto = flowRepository.getLastRowForSchedular(mapping.getDeviceId());
    if (dto != null) {

    dto.setDeviceId(mapping.getDeviceId());
    
    System.out.println("Sending WhatsApp for device: " + mapping.getDeviceName());
    notificationService.sendSchedulerTemplate(dto);
} else {
    System.out.println("No batch data found for device: " + mapping.getDeviceName());
}

            } catch (SQLException e) {
                System.err.println("SQL Error: " + e.getMessage());
            } catch (Exception e) {
                System.err.println("WhatsApp Error: " + e.getMessage());
            }
        }
    }

    // Add or remove a user’s schedule dynamically
    public String updateSchedule(SchedulerRequestDTO request) {
        Optional<WhatsAppScheduler> optional = schedulerRepository.findByUserKey(request.getUserKey());
        WhatsAppScheduler scheduler = optional.orElseGet(() -> new WhatsAppScheduler(request.getUserKey()));

        // Add a new schedule time
        if (request.getTime() != null && !request.getTime().isEmpty()) {
            LocalTime newTime = LocalTime.parse(request.getTime(), formatter);

            if (scheduler.getTime1() == null) scheduler.setTime1(newTime);
            else if (scheduler.getTime2() == null) scheduler.setTime2(newTime);
            else if (scheduler.getTime3() == null) scheduler.setTime3(newTime);
            else if (scheduler.getTime4() == null) scheduler.setTime4(newTime);
            else return "All 4 time slots are already filled.";

            schedulerRepository.save(scheduler);
            return "Added schedule " + request.getTime() + " for user " + request.getUserKey();
        }

        // Remove an existing schedule
        if (request.getRemovetime() != null && !request.getRemovetime().isEmpty()) {
            LocalTime removeTime = LocalTime.parse(request.getRemovetime(), formatter);

            List<LocalTime> times = new ArrayList<>();
            if (scheduler.getTime1() != null) times.add(scheduler.getTime1());
            if (scheduler.getTime2() != null) times.add(scheduler.getTime2());
            if (scheduler.getTime3() != null) times.add(scheduler.getTime3());
            if (scheduler.getTime4() != null) times.add(scheduler.getTime4());

            boolean removed = times.removeIf(t -> t.equals(removeTime));

            if (removed) {
                // Reassign remaining times sequentially (shift up)
                scheduler.setTime1(times.size() > 0 ? times.get(0) : null);
                scheduler.setTime2(times.size() > 1 ? times.get(1) : null);
                scheduler.setTime3(times.size() > 2 ? times.get(2) : null);
                scheduler.setTime4(times.size() > 3 ? times.get(3) : null);

                schedulerRepository.save(scheduler);
                return "Removed schedule " + request.getRemovetime() + " for user " + request.getUserKey();
            } else {
                return "Time " + request.getRemovetime() + " not found for user " + request.getUserKey();
            }
        }

        return "No valid time or removetime provided.";
    }
}
