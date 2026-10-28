package de.ghostnet.persistence;

import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;

import de.ghostnet.domain.Geisternetz;
import de.ghostnet.domain.NetStatus;
import jakarta.enterprise.context.RequestScoped;
import jakarta.inject.Inject;
import jakarta.persistence.EntityManager;

@RequestScoped
public class NetzRepository {

    private static final String SELECT = "select n from Geisternetz n "
            + "left join fetch n.meldendePerson left join fetch n.bergendePerson ";

    @Inject
    private EntityManager entityManager;

    public NetzRepository() {
    }

    public NetzRepository(EntityManager entityManager) {
        this.entityManager = Objects.requireNonNull(entityManager);
    }

    public Optional<Geisternetz> findById(Long id) {
        if (id == null) {
            return Optional.empty();
        }
        return entityManager.createQuery(SELECT + "where n.id = :id", Geisternetz.class)
                .setParameter("id", id).getResultStream().findFirst();
    }

    public List<Geisternetz> findAll() {
        return entityManager.createQuery(SELECT + "order by n.gemeldetAm desc, n.id desc", Geisternetz.class)
                .getResultList();
    }

    public List<Geisternetz> findByStatus(Set<NetStatus> status) {
        Objects.requireNonNull(status);
        if (status.isEmpty()) {
            return List.of();
        }
        return entityManager.createQuery(SELECT
                + "where n.status in :status order by n.gemeldetAm desc, n.id desc", Geisternetz.class)
                .setParameter("status", status).getResultList();
    }

    public List<Geisternetz> findOpen() {
        return findByStatus(Set.of(NetStatus.GEMELDET, NetStatus.BERGUNG_BEVORSTEHEND));
    }

    public void persist(Geisternetz netz) {
        if (!entityManager.getTransaction().isActive()) {
            throw new IllegalStateException("Speichern benötigt eine aktive Transaktion.");
        }
        entityManager.persist(Objects.requireNonNull(netz));
    }
}
