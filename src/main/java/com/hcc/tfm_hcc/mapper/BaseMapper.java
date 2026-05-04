package com.hcc.tfm_hcc.mapper;

import java.util.UUID;

import org.mapstruct.Mapper;
import org.mapstruct.Named;

@Mapper(componentModel = "spring")
public interface BaseMapper {

    @Named("uuidToString")
    static String uuidToString(UUID uuid) {
        return uuid != null ? uuid.toString() : null;
    }

    @Named("stringToUuid")
    static UUID stringToUuid(String uuidString) {
        return uuidString != null ? UUID.fromString(uuidString) : null;
    }
}
