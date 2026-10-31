package de.ghostnet.web;

import java.math.BigDecimal;
import java.math.RoundingMode;

import jakarta.faces.application.FacesMessage;
import jakarta.faces.component.UIComponent;
import jakarta.faces.context.FacesContext;
import jakarta.faces.convert.Converter;
import jakarta.faces.convert.ConverterException;
import jakarta.faces.convert.FacesConverter;

@FacesConverter("koordinatenConverter")
public class KoordinatenConverter implements Converter<BigDecimal> {

    @Override
    public BigDecimal getAsObject(
            FacesContext context,
            UIComponent component,
            String eingabe) {

        if (eingabe == null || eingabe.isBlank()) {
            return null;
        }

        String wert = eingabe.trim();

        // Ein Dezimaltrennzeichen: Punkt ODER Komma.
        // Tausendertrennzeichen sind nicht erlaubt.
        if (!wert.matches("[+-]?\\d{1,3}([.,]\\d{1,15})?")) {
            throw new ConverterException(new FacesMessage(
                    FacesMessage.SEVERITY_ERROR,
                    "Ungültige Koordinate",
                    "Bitte einen Dezimalwert wie 43.738194 "
                            + "oder 43,738194 eingeben."
            ));
        }

        String normalisiert = wert.replace(',', '.');

        BigDecimal koordinate = new BigDecimal(normalisiert);

        // Vor dem Runden prüfen, damit z. B. 90.0000004
        // nicht fälschlich zu einer gültigen Breite wird.
        boolean breitengrad =
                "breitengrad".equals(component.getId());

        BigDecimal grenze = breitengrad
                ? new BigDecimal("90")
                : new BigDecimal("180");

        if (koordinate.abs().compareTo(grenze) > 0) {
            throw new ConverterException(new FacesMessage(
                    FacesMessage.SEVERITY_ERROR,
                    "Koordinate außerhalb des gültigen Bereichs",
                    breitengrad
                            ? "Der Breitengrad muss zwischen -90 und 90 liegen."
                            : "Der Längengrad muss zwischen -180 und 180 liegen."
            ));
        }

        // Unsere Datenbankspalten speichern sechs Nachkommastellen.
        return koordinate.setScale(6, RoundingMode.HALF_UP);
    }

    @Override
    public String getAsString(
            FacesContext context,
            UIComponent component,
            BigDecimal wert) {

        return wert == null ? "" : wert.toPlainString();
    }
}