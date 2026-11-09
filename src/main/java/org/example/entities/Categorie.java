package org.example.entities;

import javax.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.*;

@Entity
@Table(name = "categories")
public class Categorie {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 100)
    private String nom;

    @Column(length = 500)
    private String description;

    @OneToMany(mappedBy = "categorie", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Produit> produits = new ArrayList<>();

    public Categorie() {}
    public Long getId() { return id; }

    public String getNom() { return nom; }
    public void setNom(String nom) { this.nom = nom; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public Categorie(String nom) { this.nom = nom; }
    public List<Produit> getProduits() { return Collections.unmodifiableList(produits); }
    public void addProduit(Produit produit) {
        Objects.requireNonNull(produit, "Produit obligatoire");
        if (produit.getCategorie() != null && produit.getCategorie() != this)
            throw new IllegalArgumentException("Le produit appartient déjà à une catégorie");
        if (!produits.contains(produit)) produits.add(produit);
        produit.setCategorie(this);
    }
    public void removeProduit(Produit produit) {
        if (produits.remove(produit)) produit.setCategorie(null);
    }
    @Override public String toString() { return "Categorie{id=" + id + ", nom='" + nom + "'}"; }

}

