package de.ghostnet.web;

import java.io.Serializable;
import java.util.List;

import de.ghostnet.domain.Geisternetz;
import de.ghostnet.domain.NetStatus;
import jakarta.enterprise.context.SessionScoped;
import jakarta.inject.Named;

@Named
@SessionScoped
public class NetzListeBean implements Serializable {

    private static final long serialVersionUID = 1L;

    private String statusFilter = "ZU_BERGEN";

    public List<Geisternetz> getGeisternetze() {
        // TODO: Geisternetze über den Geisternetz-Service laden und mit passtZumFilter einschränken.
        List<Geisternetz> geladen = List.of();
        return geladen.stream().filter(this::passtZumFilter).toList();
    }

    public boolean passtZumFilter(Geisternetz netz) {
        if (netz == null || netz.getStatus() == null) {
            return false;
        }
        String filter = statusFilter == null || statusFilter.isBlank() ? "ZU_BERGEN" : statusFilter;
        return switch (filter) {
            case "ALLE" -> true;
            case "ZU_BERGEN" -> netz.getStatus() == NetStatus.GEMELDET
                    || netz.getStatus() == NetStatus.BERGUNG_BEVORSTEHEND;
            case "GEMELDET", "BERGUNG_BEVORSTEHEND", "GEBORGEN", "VERSCHOLLEN" ->
                    netz.getStatus().name().equals(filter);
            default -> false;
        };
    }

    public String statusText(NetStatus status) {
        if (status == null) {
            return "Unbekannt";
        }
        return switch (status) {
            case GEMELDET -> "Gemeldet";
            case BERGUNG_BEVORSTEHEND -> "Bergung bevorstehend";
            case GEBORGEN -> "Geborgen";
            case VERSCHOLLEN -> "Verschollen";
        };
    }

    public List<FilterOption> getFilterOptionen() {
        return List.of(
                new FilterOption("ZU_BERGEN", "Noch zu bergende Netze"),
                new FilterOption("GEMELDET", "Gemeldet"),
                new FilterOption("BERGUNG_BEVORSTEHEND", "Bergung bevorstehend"),
                new FilterOption("GEBORGEN", "Geborgen"),
                new FilterOption("VERSCHOLLEN", "Verschollen"),
                new FilterOption("ALLE", "Alle Status"));
    }

    public String getBackendHinweis() {
        return "Die Geisternetze werden noch nicht aus der Datenbank geladen, weil der Geisternetz-Service nicht angebunden ist. Die Liste bleibt daher leer.";
    }

    public String getStatusFilter() {
        return statusFilter;
    }

    public void setStatusFilter(String statusFilter) {
        this.statusFilter = statusFilter;
    }

    public static final class FilterOption implements Serializable {

        private static final long serialVersionUID = 1L;

        private final String wert;
        private final String bezeichnung;

        public FilterOption(String wert, String bezeichnung) {
            this.wert = wert;
            this.bezeichnung = bezeichnung;
        }

        public String getWert() {
            return wert;
        }

        public String getBezeichnung() {
            return bezeichnung;
        }
    }
}
