package fr.cned.emdsgil.suividevosfrais;

import android.content.Context;

import org.json.JSONObject;

public final class Controleur {
    private static Controleur instance = null ;
    private static Context contexte ;

    /**
     * Constructeur
     */
    private Controleur() {
        super();
    }

    /**
     * Création de l'instance unique
     * @return
     */
    public static final Controleur getInstance(Context contexte) {
        if (Controleur.instance == null) {
            Controleur.contexte = contexte;
            Controleur.instance = new Controleur() ;
        }
        return Controleur.instance ;
    }

    /**
     * Fait le lien avec le contexte pour exécuter la méthode d'affichage du message
     * de succès ou d'erreur
     * @param info contient les informations retournées par le serveur
     */
    public void setResult(JSONObject info) {
        ((MainActivity) contexte).resultTranfert(info);
    }
}
