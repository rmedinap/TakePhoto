package pe.hypergis.takephoto;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.AsyncTask;

/**
 * Created by Marlon on 1/12/2017.
 */

public class UploadTask extends AsyncTask<String,Void,Boolean> {
    private ProgressDialog progressDialog;
    AlertDialog.Builder builder;
    private Context context;

    /**Constructor de clase */
    public UploadTask(Context context) {
        this.context = context;
        builder = new AlertDialog.Builder(context);
    }
    /**
     * Antes de comenzar la tarea muestra el progressDialog
     * */
    @Override
    protected void onPreExecute() {
        super.onPreExecute();
        progressDialog = ProgressDialog.show(context, "Por favor espere", "Subiendo...");
    }

    /**
     * @param
     * */
    @Override
    protected Boolean doInBackground(String... params) {
        Boolean r = false;
        ApiRest apiRest = new ApiRest(this.context.getString(R.string.url_api));
        try {
            r = apiRest.uploadPhoto(params[0]);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return r;
    }

    /**
     * Cuando se termina de ejecutar, cierra el progressDialog y avisa
     * **/
    @Override
    protected void onPostExecute(Boolean resul) {
        progressDialog.dismiss();
        if( resul )
        {
            builder.setMessage("Imagen subida al servidor, visita " + this.context.getString(R.string.url_upload))
                    .setTitle("Information")
                    .setNeutralButton("Aceptar", new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog,int which) {
                            dialog.cancel();
                        }
                    }).create().show();
        }
        else
        {
            builder.setMessage("No se pudo subir la imagen")
                    .setTitle("Information")
                    .setNeutralButton("Aceptar", new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog,int which) {
                            dialog.cancel();
                        }
                    }).create().show();
        }
    }
}
