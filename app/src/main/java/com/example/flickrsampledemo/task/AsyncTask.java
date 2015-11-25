package com.example.flickrsampledemo.task;

import android.app.Activity;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.util.Log;
import android.widget.TextView;

import com.example.flickrsampleapp.R;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserFactory;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

/**
 * Created by k on 2015/11/25.
 */
public class AsyncTask extends AsyncTask<Uri.Builder, Void, String> {

    private Activity mainActivity;

    public AsyncTask(Activity activity) {

        // 呼び出し元のアクティビティ
        this.mainActivity = activity;
    }

    // このメソッドは必ずオーバーライドする必要があるよ
    // ここが非同期で処理される部分みたいたぶん。
    @Override
    protected void doInBackground(Uri.Builder... builder) {
        // httpリクエスト投げる処理を書く。
        // ちなみに私はHttpClientを使って書きましたー

        surfaceChanged();
    }


    // このメソッドは非同期処理の終わった後に呼び出されます
    @Override
    protected void onPostExecute(String result) {

        // 取得した結果をテキストビューに入れちゃったり
        TextView tv = (TextView) mainActivity.findViewById(R.id.name);
        tv.setText(result);

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
            Log.d("Program Error", e.getClass().getName());
        }


    }

}