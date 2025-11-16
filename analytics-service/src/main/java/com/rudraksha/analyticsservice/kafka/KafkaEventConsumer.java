package com.rudraksha.analyticsservice.kafka;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.protobuf.InvalidProtocolBufferException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;
import patient.events.PatientEvent;

@Service
public class KafkaEventConsumer {

    private static final Logger log = LoggerFactory.getLogger(KafkaEventConsumer.class);

    @KafkaListener(topics = "patient")
    public void consumeEvent(byte[] event) {
        try {
            PatientEvent patientEvent = PatientEvent.parseFrom(event);
            log.info("Received patient event: {}", patientEvent.getEmail());
        } catch (InvalidProtocolBufferException e) {
            log.error("Error while parsing event {}", e.getMessage());
        }
    }
}
