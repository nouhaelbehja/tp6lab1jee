package org.example.entities;

import javax.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.*;

@Entity
@Table(name = "fournisseurs")
public class Fournisseur extends User {
    @Column(length = 300)
    private String adresse;

    @Column(name = "raison_sociale", length = 150)
    private String raisonSociale;

    public Fournisseur() {}
    public String getAdresse() { return adresse; }
    public void setAdresse(String adresse) { this.adresse = adresse; }

    public String getRaisonSociale() { return raisonSociale; }
    public void setRaisonSociale(String raisonSociale) { this.raisonSociale = raisonSociale; }


}

