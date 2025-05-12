package tn.esprit.models;

public class Materiel {
    private int id, quantite;
    private String nom, type;

    public Materiel() {}

    public Materiel(int id, String nom, String type, int quantite) {
        this.id = id;
        this.nom = nom;
        this.type = type;
        this.quantite = quantite;
    }

    public Materiel(String nom, String type, int quantite) {
        this.nom = nom;
        this.type = type;
        this.quantite = quantite;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getNom() {
        return nom;
    }

    public void setNom(String nom) {
        this.nom = nom;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public int getQuantite() {
        return quantite;
    }

    public void setQuantite(int quantite) {
        this.quantite = quantite;
    }

    @Override
    public String toString() {
        return "Materiel{" +
                "id=" + id +
                ", nom='" + nom + '\'' +
                ", type='" + type + '\'' +
                ", quantite=" + quantite +
                "}\n";
    }
}
