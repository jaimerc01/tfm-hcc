package com.hcc.tfm_hcc.converter;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.hcc.tfm_hcc.dto.HistorialClinicoDTO;
import com.hcc.tfm_hcc.model.Alergia;
import com.hcc.tfm_hcc.model.AntecedenteClinico;
import com.hcc.tfm_hcc.model.DatoClinico;
import com.hcc.tfm_hcc.model.HistorialClinico;
import com.hcc.tfm_hcc.model.Rango;

class HistorialClinicoConverterTest {

    private HistorialClinicoConverter converter;

    @BeforeEach
    void setUp() {
        converter = new HistorialClinicoConverter(new RangoConverter(), new AlergiaConverter(), new AntecedenteClinicoConverter());
    }

    private DatoClinico datoClinico(String tipo) {
        DatoClinico dato = new DatoClinico();
        dato.setId(UUID.randomUUID());
        dato.setTipo(tipo);
        dato.setValor("120");
        dato.setUnidad("mg/dL");
        return dato;
    }

    @Test
    void toDto_clasificaLosDatosClinicosPorTipoIgnorandoMayusculas() {
        HistorialClinico historial = new HistorialClinico();
        List<DatoClinico> datos = List.of(
                datoClinico("Glucosa"),
                datoClinico("frecuencia cardiaca"),
                datoClinico("PH ORINA")
        );

        HistorialClinicoDTO dto = converter.toDto(historial, datos);

        assertEquals(1, dto.getAnalisisSangre().size());
        assertEquals(1, dto.getSignosVitales().size());
        assertEquals(1, dto.getAnalisisOrina().size());
    }

    @Test
    void toDto_conListaDeDatosVacia_devuelveListasVacias() {
        HistorialClinicoDTO dto = converter.toDto(new HistorialClinico(), List.of());

        assertTrue(dto.getAnalisisSangre().isEmpty());
        assertTrue(dto.getSignosVitales().isEmpty());
        assertTrue(dto.getAnalisisOrina().isEmpty());
    }

    @Test
    void toDto_conListaDeDatosNula_devuelveListasVacias() {
        HistorialClinicoDTO dto = converter.toDto(new HistorialClinico(), null);

        assertTrue(dto.getAnalisisSangre().isEmpty());
    }

    @Test
    void toDto_conTipoDesconocido_loClasificaComoAnalisisDeSangre() {
        HistorialClinicoDTO dto = converter.toDto(new HistorialClinico(), List.of(datoClinico("Tipo Inventado")));

        assertEquals(1, dto.getAnalisisSangre().size());
    }

    @Test
    void toDatoClinicoDto_conRangoAsociado_incluyeElRangoConvertido() {
        DatoClinico dato = datoClinico("Glucosa");
        Rango rango = new Rango();
        rango.setNombre("Glucosa - Normal");
        dato.setRango(rango);

        var dto = converter.toDatoClinicoDto(dato);

        assertNotNull(dto.getRango());
        assertEquals("Glucosa - Normal", dto.getRango().getNombre());
    }

    @Test
    void toDatoClinicoDto_sinRango_dejaElCampoRangoNulo() {
        var dto = converter.toDatoClinicoDto(datoClinico("Glucosa"));

        assertEquals(null, dto.getRango());
    }

    @Test
    void toDtoCompleto_incluyeAlergiasYAntecedentesConvertidos() {
        Alergia alergia = new Alergia();
        alergia.setDescripcion("Alergia al polen");
        AntecedenteClinico antecedente = new AntecedenteClinico();
        antecedente.setCategoria(AntecedenteClinico.Categoria.PERSONAL);
        antecedente.setDescripcion("Hipertensión");

        HistorialClinicoDTO dto = converter.toDto(new HistorialClinico(), List.of(), List.of(alergia), List.of(antecedente));

        assertEquals(1, dto.getAlergias().size());
        assertEquals("Alergia al polen", dto.getAlergias().get(0).getDescripcion());
        assertEquals(1, dto.getAntecedentes().size());
        assertEquals("PERSONAL", dto.getAntecedentes().get(0).getCategoria());
    }

    @Test
    void toDtoCompleto_conAlergiasYAntecedentesNulos_devuelveListasVacias() {
        HistorialClinicoDTO dto = converter.toDto(new HistorialClinico(), List.of(), null, null);

        assertTrue(dto.getAlergias().isEmpty());
        assertTrue(dto.getAntecedentes().isEmpty());
    }

    @Test
    void toDtoSimple_devuelveUnDtoSinPoblarDatosDeColecciones() {
        HistorialClinicoDTO dto = converter.toDto(new HistorialClinico());

        assertEquals(null, dto.getAlergias());
        assertEquals(null, dto.getAnalisisSangre());
    }
}
