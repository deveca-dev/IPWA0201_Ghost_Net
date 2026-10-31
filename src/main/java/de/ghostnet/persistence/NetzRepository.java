package de.ghostnet.persistence;

import de.ghostnet.domain.Geisternetz;
import jakarta.enterprise.context.RequestScoped;
import jakarta.inject.Inject;
import jakarta.persistence.EntityManager;
import java.util.List;

@RequestScoped
public class NetzRepository {

    @Inject
    private EntityManager entityManager;

    public void speichern(Geisternetz netz) {
        entityManager.persist(netz);
    };

    public List<Geisternetz> alleLaden() {
        return entityManager.createQuery(
                "SELECT n FROM Geisternetz n "
                + "LEFT JOIN FETCH n.meldendePerson "
                + "LEFT JOIN FETCH n.bergendePerson "
                + "ORDER BY n.gemeldetAm DESC",
                Geisternetz.class
        ).getResultList();
    };

    public Geisternetz anhandIdLaden(Long id) {
        if (id == null) {
            return null;
        }
    
        return entityManager.find(Geisternetz.class, id);
    };
}