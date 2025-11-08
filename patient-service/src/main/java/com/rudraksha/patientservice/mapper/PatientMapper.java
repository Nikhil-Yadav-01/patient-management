package com.rudraksha.patientservice.mapper;

import org.modelmapper.Converter;
import org.modelmapper.ModelMapper;
import org.modelmapper.spi.MappingContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.stereotype.Component;

import java.time.LocalDate;

@Configuration
public class PatientMapper {

    @Bean
    public ModelMapper modelMapper() {
        ModelMapper modelMapper = new ModelMapper();

        // Converter from String -> LocalDate
        Converter<String, LocalDate> toLocalDate = new Converter<>() {
            @Override
            public LocalDate convert(MappingContext<String, LocalDate> context) {
                return context.getSource() == null ? null : LocalDate.parse(context.getSource());
            }
        };

        // Register converters for date fields
        modelMapper.addConverter(toLocalDate);

        return modelMapper;
    }
}
