package org.example.entities;

import javax.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.*;

@Entity
@Table(name = "produits")
@NamedQueries({
    @NamedQuery(name = "Produit.findAll", query = "SELECT p FROM Produit p JOIN FETCH p.categorie ORDER BY p.id"),
    @NamedQuery(name = "Produit.findByCategorie", query = "SELECT p FROM Produit p JOIN FETCH p.categorie WHERE p.categorie.id = :catId ORDER BY p.id"),
    @NamedQuery(name = "Produit.searchByLibelle", query = "SELECT p FROM Produit p JOIN FETCH p.categorie WHERE LOWER(p.libelle) LIKE LOWER(CONCAT('%', :term, '%')) ORDER BY p.id")
})
public class Produit {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 150)
    private String libelle;

    @Column(nullable = false, unique = true, length = 50)
    private String reference;

    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal prix;

    @Column(nullable = false)
    private int stock = 0;

    @Column(nullable = false)
    private boolean actif = true;

    @Column(name = "date_creation", nullable = false)
    private LocalDate dateCreation = LocalDate.now();

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "categorie_id", nullable = false)
    private Categorie categorie;

    public Produit() {}
    public Long getId() { return id; }

    public String getLibelle() { return libelle; }
    public void setLibelle(String libelle) { this.libelle = libelle; }

    public String getReference() { return reference; }
    public void setReference(String reference) { this.reference = reference; }

    public BigDecimal getPrix() { return prix; }
    public void setPrix(BigDecimal prix) { this.prix = prix; }

    public int getStock() { return stock; }
    public void setStock(int stock) { this.stock = stock; }

    public boolean getActif() { return actif; }
    public void setActif(boolean actif) { this.actif = actif; }

    public LocalDate getDateCreation() { return dateCreation; }
    public void setDateCreation(LocalDate dateCreation) { this.dateCreation = dateCreation; }

    public Categorie getCategorie() { return categorie; }
    public void setCategorie(Categorie categorie) { this.categorie = categorie; }

    public Produit(String libelle, String reference, BigDecimal prix, Categorie categorie) {
        this.libelle = libelle; this.reference = reference; this.prix = prix; this.categorie = categorie;
    }
    @Override public String toString() { return "Produit{id=" + id + ", reference='" + reference + "', libelle='" + libelle + "', prix=" + prix + ", stock=" + stock + "}"; }

}

