package com.example.flickrsampledemo;

import android.app.Activity;
import android.content.pm.ActivityInfo;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.widget.Button;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserFactory;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;


public class MainActivity extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        setContentView(R.layout.activity_main);

        Button btn = (Button) findViewById(R.id.run_btn);
        btn.setOnClickListener(new View.OnClickListener() {
            Handler handler = new Handler();

            @Override
            public void onClick(View v) {
                Uri.Builder builder = new Uri.Builder();
                AsyncTask task = new AsyncTask(this)
                task.execute(builder);

                //Intent intent = new Intent(MainActivity.this, FlickrService.class);
                //startService(intent);

                /*
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        handler.post(new Runnable() {
                            @Override
                            public void run() {
                                //setContentView(new MainView(MainActivity.this));
                                surfaceChanged();
                            }
                        });
                    }
                }).start();
                */
            }
        });
    }

    public void surfaceChanged() {
        // サーバーに保存
        String imgURL = null;
        Bitmap bitmap = null;
        try {
            // Flickr接続用。APIのkeyとか検索タグとか。下のAPI keyは適当です。
            String API_KEY = "api_key=000";
            String API_URL = "http://api.flickr.com/services/rest/?method=flickr.photos.search";
            String API_TAG = "tags=cat";
            String PER_PAGE = "per_page=2";

            // APIに接続
            URL url = new URL(API_URL + "&" + API_KEY + "&" + API_TAG + "&" + PER_PAGE);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("POST");
            conn.setDoInput(true);
            conn.setUseCaches(false);
            conn.connect();

            // 結果を受信
            InputStream in = new BufferedInputStream(conn.getInputStream());
            BufferedReader reader = new BufferedReader(new InputStreamReader(in));

            // XMLをパース
            final XmlPullParserFactory factory = XmlPullParserFactory.newInstance();
            final XmlPullParser parser = factory.newPullParser();
            parser.setInput(reader);
            for (int type = parser.getEventType(); type != XmlPullParser.END_DOCUMENT; type = parser.next()) {
                if (type == XmlPullParser.START_TAG) {
                    String tagName = parser.getName();
                    if (tagName.equals("photo")) {
                        String id = parser.getAttributeValue(0);
                        String secret = parser.getAttributeValue(2);
                        String server = parser.getAttributeValue(3);
                        String farm = parser.getAttributeValue(4);
                        imgURL = "http://farm" + farm + ".static.flickr.com/" + server + "/" + id + "_" + secret + ".jpg";
                        break;
                    }
                }
            }

            // close
            reader.close();
            in.close();
            conn.disconnect();

            // 画像の読み込み
            if (imgURL != null) {
                url = new URL(imgURL);
                conn = (HttpURLConnection) url.openConnection();
                conn.setDoInput(true);
                conn.connect();
                in = new BufferedInputStream(conn.getInputStream());

                ByteArrayOutputStream bos = new ByteArrayOutputStream();
                byte[] w = new byte[1024];
                while (true) {
                    int ss = in.read(w);
                    if (ss <= 0) break;
                    bos.write(w, 0, ss);
                }
                ;
                bitmap = BitmapFactory.decodeByteArray(bos.toByteArray(), 0, bos.size());
                in.close();
                bos.close();
                conn.disconnect();

                /*
                // 描画
                Canvas canvas = holder.lockCanvas();
                if(bitmap != null)
                {
                    float scale = (float)(width) / (float)bitmap.getWidth();
                    canvas.scale(scale, scale);
                    canvas.drawBitmap(bitmap, 0, 0, null);
                }
                else
                {
                    Paint paint = new Paint();
                    paint.setColor(Color.WHITE);
                    paint.setAntiAlias(true);
                    canvas.scale(2, 2);
                    canvas.drawText("Image load error.", 10, 10, paint);
                }
                holder.unlockCanvasAndPost(canvas);
                */
            }
        } catch (Exception e) {
            Log.d("Program Error", e.getMessage());
        }
    }
}

