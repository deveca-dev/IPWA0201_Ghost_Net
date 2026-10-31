package de.ghostnet.web;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import jakarta.faces.application.FacesMessage;
import jakarta.faces.component.UIComponent;
import jakarta.faces.context.FacesContext;
import jakarta.faces.convert.Converter;
import jakarta.faces.convert.ConverterException;
import jakarta.faces.convert.FacesConverter;

@FacesConverter("koordinatenConverter")
public class KoordinatenConverter implements Converter<BigDecimal> {

    private static final Pattern DEZIMALGRAD = Pattern.compile(
            "^[+-]?\\d{1,3}(?:[.,]\\d{1,15})?$"
    );

    private static final Pattern GMS = Pattern.compile(
            "^\\s*(\\d{1,3})\\s*°\\s*"
            + "(\\d{1,2})\\s*['′]\\s*"
            + "(\\d{1,2}(?:[.,]\\d{1,15})?)\\s*[\"″]\\s*"
            + "([NSEW])\\s*$",
            Pattern.CASE_INSENSITIVE
    );

    @Override
    public BigDecimal getAsObject(
            FacesContext context,
            UIComponent component,
            String eingabe) {

        if (eingabe == null || eingabe.isBlank()) {
            return null;
        }

        String wert = eingabe.trim();

        boolean breitengrad;

        if ("breitengrad".equals(component.getId())) {
            breitengrad = true;
        } else if ("laengengrad".equals(component.getId())) {
            breitengrad = false;
        } else {
            throw fehler(
                    "Der Konverter ist keinem Koordinatenfeld zugeordnet."
            );
        }

        int maximaleGrad = breitengrad ? 90 : 180;

        // Variante 1: Dezimalgrad mit Punkt oder Komma.
        if (DEZIMALGRAD.matcher(wert).matches()) {
            BigDecimal dezimalgrad = new BigDecimal(
                    wert.replace(',', '.')
            );

            if (dezimalgrad.abs().compareTo(
                    BigDecimal.valueOf(maximaleGrad)) > 0) {

                throw fehler(breitengrad
                        ? "Der Breitengrad muss zwischen -90 und 90 liegen."
                        : "Der Längengrad muss zwischen -180 und 180 liegen.");
            }

            return dezimalgrad.setScale(
                    6,
                    RoundingMode.HALF_UP
            );
        }

        // Variante 2: Grad, Minuten und Sekunden mit Himmelsrichtung.
        Matcher matcher = GMS.matcher(wert);

        if (!matcher.matches()) {
            throw fehler(
                    "Bitte Dezimalgrad, z. B. 43.738194, "
                    + "oder GMS, z. B. 43°44'17.5\"N, eingeben."
            );
        }

        int grad = Integer.parseInt(matcher.group(1));
        int minuten = Integer.parseInt(matcher.group(2));

        BigDecimal sekunden = new BigDecimal(
                matcher.group(3).replace(',', '.')
        );

        String richtung = matcher.group(4)
                .toUpperCase(Locale.ROOT);

        if (breitengrad && !richtung.matches("[NS]")) {
            throw fehler(
                    "Für den Breitengrad bitte N oder S verwenden."
            );
        }

        if (!breitengrad && !richtung.matches("[EW]")) {
            throw fehler(
                    "Für den Längengrad bitte E oder W verwenden."
            );
        }

        if (grad > maximaleGrad
                || minuten >= 60
                || sekunden.compareTo(BigDecimal.valueOf(60)) >= 0
                || (grad == maximaleGrad
                    && (minuten != 0
                        || sekunden.compareTo(BigDecimal.ZERO) != 0))) {

            throw fehler(
                    "Die Grad-, Minuten- oder Sekundenangabe ist ungültig."
            );
        }

        // Erst alles in Bogensekunden umrechnen, dann einmal runden.
        BigDecimal gesamtSekunden = BigDecimal.valueOf(grad)
                .multiply(BigDecimal.valueOf(3600))
                .add(BigDecimal.valueOf(minuten)
                        .multiply(BigDecimal.valueOf(60)))
                .add(sekunden);

        BigDecimal dezimalgrad = gesamtSekunden.divide(
                BigDecimal.valueOf(3600),
                6,
                RoundingMode.HALF_UP
        );

        if (richtung.equals("S") || richtung.equals("W")) {
            dezimalgrad = dezimalgrad.negate();
        }

        return dezimalgrad;
    }

    @Override
    public String getAsString(
            FacesContext context,
            UIComponent component,
            BigDecimal wert) {

        return wert == null ? "" : wert.toPlainString();
    }

    private ConverterException fehler(String nachricht) {
        return new ConverterException(new FacesMessage(
                FacesMessage.SEVERITY_ERROR,
                nachricht,
                nachricht
        ));
    }
}