package com.rudraksha.patientservice.kafka;

import com.rudraksha.patientservice.model.Patient;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaHandler;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;
import patient.events.PatientEvent;

@Service
@Slf4j
public class KafkaEventProducer {

    private final KafkaTemplate<String, byte[]> kafkaTemplate;

    public KafkaEventProducer(KafkaTemplate<String, byte[]> kafkaTemplate) {
        this.kafkaTemplate = kafkaTemplate;
    }

    public void sendEvent(Patient patient) {
        log.info("🟢 Entered KafkaProducer.sendEvent()");
        try {
            PatientEvent patientEvent = PatientEvent.newBuilder()
                    .setPatientId(patient.getId().toString())
                    .setName(patient.getName())
                    .setEmail(patient.getEmail())
                    .setEventType("PATIENT_CREATED")
                    .build();

            kafkaTemplate.send("patient", patientEvent.toByteArray());
            log.info("Sent event");
        } catch (Exception e) {
            log.error("Error sending PatientCreated event: {}", e.getMessage());
        }
    }
}
