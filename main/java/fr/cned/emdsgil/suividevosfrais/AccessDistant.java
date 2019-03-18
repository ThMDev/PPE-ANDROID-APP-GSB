package fr.cned.emdsgil.suividevosfrais;

import android.util.Log;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class AccessDistant implements AsyncResponse {

    // constante
    private static final String SERVERADDR = ""; // Attention, remplir avec l'adresse du serveur distant
    private Controleur controle ;

    /**
     * Constructeur
     */
    public AccessDistant(){
        controle = Controleur.getInstance(null);
    }


    /**
     * Traitement des informations qui viennent du serveur distant
     * @param output
     */
    @Override
    public void processFinish(String output) {
        // contenu du retour du serveur, pour contrôle dans la console
        Log.d("serveur", "************" + output);
        // découpage du message reçu
        String[] message = output.split("%");
        // contrôle si le serveur a retourné une information
        if(message.length>0){
            try {
                JSONObject info = new JSONObject(message[0]);
                // on récupére l'action sollicité par le serveur pour afficher une réponse à
                // l'utilisateur en fonction de ce qui a été retourné
                switch (info.get("action").toString()){
                    case "enregistrer":
                        // on appelle le contrôleur pour faire le lien avec un contexte
                        controle.setResult(info);
                        break;
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }

    }

    /**
     * Envoi d'informations vers le serveur distant
     * @param action contient l'action à réaliser
     * @param lesDonneesJSON contient les données à envoyer
     */
    public void envoi(String action, JSONArray lesDonneesJSON){
        AccessHTTP accesDonnees = new AccessHTTP();
        // permet de faire le lien asynchrone avec AccesHTTP
        accesDonnees.delegate = this;
        // paramètres POST pour l'envoi vers le serveur distant
        accesDonnees.addParam("action", action);
        accesDonnees.addParam("lesdonnees", lesDonneesJSON.toString());
        // appel du serveur
        accesDonnees.execute(SERVERADDR);
    }

}
