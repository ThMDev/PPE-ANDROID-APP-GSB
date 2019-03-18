package fr.cned.emdsgil.suividevosfrais;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Hashtable;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        setTitle("GSB : Suivi des frais");
        // récupération des informations sérialisées
        recupSerialize();
        // chargement des méthodes événementielles
        cmdMenu_clic(((ImageButton) findViewById(R.id.cmdKm)), KmActivity.class);
        cmdMenu_clic(((ImageButton) findViewById(R.id.cmdRepas)), FraisRepasActivity.class);
        cmdMenu_clic(((ImageButton) findViewById(R.id.cmdNuitee)), FraisNuiteesActivity.class);
        cmdMenu_clic(((ImageButton) findViewById(R.id.cmdEtape)), FraisEtapesActivity.class);
        cmdMenu_clic(((ImageButton) findViewById(R.id.cmdHf)), HfActivity.class);
        cmdMenu_clic(((ImageButton) findViewById(R.id.cmdHfRecap)), HfRecapActivity.class);
        cmdTransfert_clic();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.main, menu);
        return true;
    }

    /**
     * Récupère la sérialisation si elle existe
     */
    private void recupSerialize() {
        /* Pour éviter le warning "Unchecked cast from Object to Hash" produit par un casting direct :
         * Global.listFraisMois = (Hashtable<Integer, FraisMois>) Serializer.deSerialize(Global.filename, MainActivity.this);
         * On créé un Hashtable générique <?,?> dans lequel on récupère l'Object retourné par la méthode deSerialize, puis
         * on cast chaque valeur dans le type attendu.
         * Seulement ensuite on affecte cet Hastable à Global.listFraisMois.
        */
        Hashtable<?, ?> monHash = (Hashtable<?, ?>) Serializer.deSerialize(MainActivity.this);
        if (monHash != null) {
            Hashtable<Integer, FraisMois> monHashCast = new Hashtable<>();
            for (Hashtable.Entry<?, ?> entry : monHash.entrySet()) {
                monHashCast.put((Integer) entry.getKey(), (FraisMois) entry.getValue());
            }
            Global.listFraisMois = monHashCast;
        }
        // si rien n'a été récupéré, il faut créer la liste
        if (Global.listFraisMois == null) {
            Global.listFraisMois = new Hashtable<>();
            /* Retrait du type de l'HashTable (Optimisation Android Studio)
			 * Original : Typage explicit =
			 * Global.listFraisMois = new Hashtable<Integer, FraisMois>();
			*/

        }
    }

    /**
     * Sur la sélection d'un bouton dans l'activité principale ouverture de l'activité correspondante
     */
    private void cmdMenu_clic(ImageButton button, final Class classe) {
        button.setOnClickListener(new Button.OnClickListener() {
            public void onClick(View v) {
                // ouvre l'activité
                Intent intent = new Intent(MainActivity.this, classe);
                startActivity(intent);
            }
        });
    }

    /**
     * Cas particulier du bouton pour le transfert d'informations vers le serveur
     */
    private void cmdTransfert_clic() {
        findViewById(R.id.cmdTransfert).setOnClickListener(new Button.OnClickListener() {
            public void onClick(View v) {
                (findViewById(R.id.lytIdentification)).setVisibility(View.VISIBLE);
                cmdTransfertIdentification_clic();
                cmdRetourIdentification_clic();
            }
        });
    }

    /**
     * écoute sur le clic du bouton retour dans le layout d'identification pour cacher le formulaire
     */
    private void cmdRetourIdentification_clic() {
        findViewById(R.id.cmdRetourIdentifcation).setOnClickListener(new Button.OnClickListener() {
            public void onClick(View v) {
                (findViewById(R.id.lytIdentification)).setVisibility(View.GONE);
            }
        });
    }

    /**
     *
     */
    private void cmdTransfertIdentification_clic() {
        findViewById(R.id.cmdTransfertIdentification).setOnClickListener(new Button.OnClickListener() {
            public void onClick(View v) {
                String login,mdp;
                login = ((EditText) findViewById(R.id.editTextLogin)).getText().toString();
                mdp = ((EditText) findViewById(R.id.editTextMotDePasse)).getText().toString();
                // vérification  du remplissage des champs
                if(login.length() > 0 && mdp.length() > 0) {
                    // suppression de l'écoute du clic d'envoi
                    findViewById(R.id.cmdTransfertIdentification).setOnClickListener(new Button.OnClickListener() {
                        public void onClick(View v) {
                            // information d'attendre
                            Toast.makeText(MainActivity.this,getString(R.string.txt_attente),Toast.LENGTH_LONG).show();
                        }
                    });
                    // envoi les informations sérialisées vers le serveur
                    Controleur.getInstance(MainActivity.this);
                    AccessDistant accessDistant = new AccessDistant();
                    ArrayList arrayList = new ArrayList();
                    arrayList.add(login);
                    arrayList.add(mdp);
                    arrayList.addAll(Global.convertList());
                    JSONArray jsonArray = new JSONArray(arrayList);
                    accessDistant.envoi("enregistrer", jsonArray);
                }else{
                    // information de l'erreur
                    Toast.makeText(MainActivity.this,getString(R.string.txt_erreur_champs_non_remplis),Toast.LENGTH_LONG).show();
                }
            }
        });
    }

    /**
     * Méthode sollicité par le controleur à la suite d'une réponse du serveur
     * @param info contient les infos retournées par le serveur
     */
    public void resultTranfert(JSONObject info) {
        try {
            // si le couple d'identification renvoie à un utilisateur enregistré on cache le formulaire
            if(info.get("estConnecte").equals("true")){
                // on cache le formulaire d'identification
                (findViewById(R.id.lytIdentification)).setVisibility(View.GONE);
                // si tout s'est bien passé côté serveur on informe l'utilisateur du succès...
                if(info.get("statut").equals("true")) {
                    Toast.makeText(MainActivity.this, getString(R.string.txt_succes_transfert), Toast.LENGTH_LONG).show();
                }else{
                    // ... sinon de l'échec
                    Toast.makeText(MainActivity.this, getString(R.string.txt_erreur), Toast.LENGTH_LONG).show();
                }
            }else{
                // si le couple d'identification ne renvoie pas à un utilisateur enregistré on laisse le
                // formulaire visible
                // réinitialisation de l'ecoute
                cmdTransfertIdentification_clic();
                // information de l'erreur
                Toast.makeText(MainActivity.this,getString(R.string.txt_erreur_identification),Toast.LENGTH_LONG).show();
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }
}
