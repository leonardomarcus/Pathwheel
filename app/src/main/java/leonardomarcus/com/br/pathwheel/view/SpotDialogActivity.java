package leonardomarcus.com.br.pathwheel.view;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Base64;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import leonardomarcus.com.br.pathwheel.R;
import leonardomarcus.com.br.pathwheel.api.endpoint.FetchSpotListener;
import leonardomarcus.com.br.pathwheel.api.endpoint.ReportSpotListener;
import leonardomarcus.com.br.pathwheel.api.endpoint.SpotEndpoint;
import leonardomarcus.com.br.pathwheel.api.model.Spot;
import leonardomarcus.com.br.pathwheel.api.model.SpotReportType;
import leonardomarcus.com.br.pathwheel.api.model.SpotType;
import leonardomarcus.com.br.pathwheel.api.model.User;
import leonardomarcus.com.br.pathwheel.api.request.ReportSpotRequest;
import leonardomarcus.com.br.pathwheel.api.response.FetchSpotResponse;
import leonardomarcus.com.br.pathwheel.api.response.Response;
import leonardomarcus.com.br.pathwheel.service.PathwheelPreferences;

public class SpotDialogActivity extends AppCompatActivity {

    private ProgressDialog progress = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.activity_spot_dialog);


        if(getSupportActionBar() != null)
            getSupportActionBar().hide();
        if(getActionBar() != null)
            getActionBar().hide();


        final Spot spot = PathwheelPreferences.getSpot(getApplicationContext());
        Log.d("spot dialog",spot.toString());
        String title = "Ponto de Alerta";
        int markerResource = R.mipmap.ic_marker_alert;
        if(spot.getSpotType().getId() == SpotType.ALERT) {
            title = "Ponto de Alerta";
            markerResource = R.mipmap.ic_marker_alert;
        }
        else if(spot.getSpotType().getId() == SpotType.DANGER) {
            title = "Ponto de Perigo";
            markerResource = R.mipmap.ic_marker_danger;
        }
        else if(spot.getSpotType().getId() == SpotType.BARRIER) {
            title = "Ponto de Barreira";
            markerResource = R.mipmap.ic_marker_blocked;
        }

        TextView tvTitulo = (TextView) findViewById(R.id.textView_spot_titulo);
        tvTitulo.setText(title);

        ImageView imageViewMarker = (ImageView) findViewById(R.id.imageView_spot_marker);
        imageViewMarker.setImageResource(markerResource);

        TextView tvUser = (TextView) findViewById(R.id.textView_spot_user);
        tvUser.setText((spot.getUser().getUsername()== null ? "Anônimo" : spot.getUser().getUsername()) + " em "+spot.getRegistrationDate().substring(0,10));

        TextView tvComment = (TextView) findViewById(R.id.textView_spot_comment);
        tvComment.setText(spot.getComment());

        Button buttonOk = (Button) findViewById(R.id.button_spot_dialog_ok);
        buttonOk.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        SpotEndpoint endpoint = new SpotEndpoint();
        endpoint.fetch(spot.getId(), new FetchSpotListener() {
            @Override
            public void onFetchSpot(FetchSpotResponse response) {
                //Log.d("fetch", response.toString());
                if(response.getCode() == 200) {
                    if(response.getSpot().getPicture() != null && !response.getSpot().getPicture().equals("")) {
                        ImageView imageView = (ImageView) findViewById(R.id.imageView_spot_picture);
                        imageView.setVisibility(View.VISIBLE);
                        byte[] decodedString = Base64.decode(response.getSpot().getPicture(), Base64.DEFAULT);
                        Bitmap decodedByte = BitmapFactory.decodeByteArray(decodedString, 0, decodedString.length);
                        imageView.setImageBitmap(decodedByte);
                    }
                }
            }
        });

        final User user = PathwheelPreferences.getUser(getApplicationContext());
        if(user != null) {
            View viewHorizontalLine = (View) findViewById(R.id.view_horizontal_line);
            viewHorizontalLine.setVisibility(View.VISIBLE);

            Button buttonStillThere = (Button) findViewById(R.id.button_it_is_there);
            if(spot.getCountStillThere() > 0)
                buttonStillThere.setText(buttonStillThere.getText()+" ("+spot.getCountStillThere()+")");
            buttonStillThere.setVisibility(View.VISIBLE);
            buttonStillThere.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    new AlertDialog.Builder(SpotDialogActivity.this)
                            .setIcon(android.R.drawable.ic_dialog_alert)
                            .setTitle("Está aí?")
                            .setMessage("Confirma que este ponto de informação ainda existe?")
                            .setPositiveButton("Sim", new DialogInterface.OnClickListener()
                            {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    if (progress != null)
                                        progress.dismiss();
                                    else
                                        progress = new ProgressDialog(SpotDialogActivity.this);
                                    progress.setTitle("Aguarde");
                                    progress.setMessage("Processando...");
                                    progress.setCancelable(false);
                                    progress.show();

                                    SpotEndpoint endpoint = new SpotEndpoint();
                                    ReportSpotRequest request = new ReportSpotRequest();
                                    request.setSpotId(spot.getId());
                                    request.setUserId(user.getId());
                                    request.setSpotReportTypeId(SpotReportType.STILL_THERE);
                                    endpoint.report(request, new ReportSpotListener() {
                                        @Override
                                        public void onReportSpot(Response response) {

                                            if(progress!= null)
                                                progress.dismiss();
                                            progress = null;

                                            AlertDialog alertDialog = new AlertDialog.Builder(SpotDialogActivity.this).create();
                                            alertDialog.setButton(AlertDialog.BUTTON_NEUTRAL, "OK",
                                                    new DialogInterface.OnClickListener() {
                                                        public void onClick(DialogInterface dialog, int which) {
                                                            dialog.dismiss();
                                                            finish();
                                                        }
                                                    });
                                            if(response.getCode() == 200) {
                                                PathwheelPreferences.setRefreshOverview(SpotDialogActivity.this,true);
                                                alertDialog.setTitle("Sucesso");
                                                alertDialog.setMessage("Obrigado por colaborar!");
                                            }
                                            else if(response.getCode() == 401) {
                                                alertDialog.setTitle("Você já informou!");
                                                alertDialog.setMessage("Obrigado por colaborar, mas você já nos informou que esse ponto de informação está aí!");
                                            }
                                            else {
                                                alertDialog.setTitle("Ooops!");
                                                alertDialog.setMessage("Não foi possível registrar sua informação...");
                                            }
                                            alertDialog.show();
                                        }
                                    });
                                }

                            })
                            .setNegativeButton("Não", null)
                            .show();
                }
            });

            Button buttonNotThere = (Button) findViewById(R.id.button_it_is_not_there);
            if(spot.getCountNotThere() > 0)
                buttonNotThere.setText(buttonNotThere.getText()+" ("+spot.getCountNotThere()+")");
            buttonNotThere.setVisibility(View.VISIBLE);
            buttonNotThere.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    new AlertDialog.Builder(SpotDialogActivity.this)
                            .setIcon(android.R.drawable.ic_dialog_alert)
                            .setTitle("Não está aí?")
                            .setMessage("Confirma que este ponto de informação não existe?")
                            .setPositiveButton("Sim", new DialogInterface.OnClickListener()
                            {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    if (progress != null)
                                        progress.dismiss();
                                    else
                                        progress = new ProgressDialog(SpotDialogActivity.this);
                                    progress.setTitle("Aguarde");
                                    progress.setMessage("Processando...");
                                    progress.setCancelable(false);
                                    progress.show();

                                    SpotEndpoint endpoint = new SpotEndpoint();
                                    ReportSpotRequest request = new ReportSpotRequest();
                                    request.setSpotId(spot.getId());
                                    request.setUserId(user.getId());
                                    request.setSpotReportTypeId(SpotReportType.NOT_THERE);
                                    endpoint.report(request, new ReportSpotListener() {
                                        @Override
                                        public void onReportSpot(Response response) {
                                            if(progress != null)
                                                progress.dismiss();
                                            progress = null;
                                            AlertDialog alertDialog = new AlertDialog.Builder(SpotDialogActivity.this).create();
                                            alertDialog.setButton(AlertDialog.BUTTON_NEUTRAL, "OK",
                                                    new DialogInterface.OnClickListener() {
                                                        public void onClick(DialogInterface dialog, int which) {
                                                            dialog.dismiss();
                                                            finish();
                                                        }
                                                    });
                                            if(response.getCode() == 200) {
                                                PathwheelPreferences.setRefreshOverview(SpotDialogActivity.this,true);
                                                alertDialog.setTitle("Sucesso");
                                                alertDialog.setMessage("Obrigado por colaborar!");
                                            }
                                            else if(response.getCode() == 401) {
                                                alertDialog.setTitle("Você já informou!");
                                                alertDialog.setMessage("Obrigado por colaborar, mas você já nos informou que esse ponto de informação não está aí!");
                                            }
                                            else {
                                                alertDialog.setTitle("Ooops!");
                                                alertDialog.setMessage("Não foi possível registrar sua informação...");
                                            }
                                            alertDialog.show();
                                        }
                                    });
                                }

                            })
                            .setNegativeButton("Não", null)
                            .show();
                }
            });
        }
    }
}
