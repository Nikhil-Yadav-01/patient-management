package com.rudraksha.patientservice.service;

import com.rudraksha.patientservice.dto.PatientRequestDTO;
import com.rudraksha.patientservice.dto.PatientResponseDTO;
import com.rudraksha.patientservice.exception.EmailAlreadyExistsException;
import com.rudraksha.patientservice.exception.PatientNotFoundException;
import com.rudraksha.patientservice.grpc.BillingServiceGrpcClient;
import com.rudraksha.patientservice.model.Patient;
import com.rudraksha.patientservice.repository.PatientRepository;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@Service
@AllArgsConstructor
@Slf4j
public class PatientService {
    private final PatientRepository patientRepository;
    private final ModelMapper modelMapper;
    private final BillingServiceGrpcClient billingServiceGrpcClient;

    public List<PatientResponseDTO> getPatients() {
        List<Patient> patients = patientRepository.findAll();

        return patients.stream().map(patient ->
                modelMapper.map(patient, PatientResponseDTO.class)
        ).toList();
    }

    public PatientResponseDTO createPatient(PatientRequestDTO patientRequestDTO) {
        if (patientRepository.existsByEmail(patientRequestDTO.getEmail())) {
            throw new EmailAlreadyExistsException("A patient with this email already exists: " + patientRequestDTO.getEmail()
            );
        }

        Patient newPatient = patientRepository.save(modelMapper.map(patientRequestDTO, Patient.class));

        billingServiceGrpcClient.createBillingAccount(
                newPatient.getId().toString(), newPatient.getName(), newPatient.getEmail()
        );

        log.info("✅ Calling KafkaProducer.sendEvent for patient {}", newPatient.getId());

        return modelMapper.map(newPatient, PatientResponseDTO.class);
    }

    public PatientResponseDTO updatePatient(UUID id, PatientRequestDTO dto) {

        Patient existing = patientRepository.findById(id)
                .orElseThrow(() -> new PatientNotFoundException("Patient not found with ID: " + id));

        if (patientRepository.existsByEmailAndIdNot(dto.getEmail(), id)) {
            throw new EmailAlreadyExistsException(
                    "A patient with this email already exists: " + dto.getEmail());
        }

        // Update fields manually or conditionally
        existing.setName(dto.getName());
        existing.setEmail(dto.getEmail());
        existing.setAddress(dto.getAddress());
        existing.setDateOfBirth(LocalDate.parse(dto.getDateOfBirth()));

        // Only update registeredDate if provided
        if (dto.getRegisteredDate() != null && !dto.getRegisteredDate().isBlank()) {
            existing.setRegisteredDate(LocalDate.parse(dto.getRegisteredDate()));
        }

        Patient updated = patientRepository.save(existing);
        return modelMapper.map(updated, PatientResponseDTO.class);
    }


    public void deletePatient(UUID id) {
        patientRepository.deleteById(id);
    }
}
