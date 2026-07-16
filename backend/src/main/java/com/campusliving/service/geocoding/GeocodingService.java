package com.campusliving.service.geocoding;

import java.util.Optional;

/**
 * Converte um endereço textual em coordenadas geográficas (RF-16).
 */
public interface GeocodingService {

    Optional<Coordenadas> geocodificar(String enderecoCompleto);

    record Coordenadas(double latitude, double longitude) {}
}
