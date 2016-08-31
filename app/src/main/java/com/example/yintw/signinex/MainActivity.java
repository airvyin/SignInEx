package com.example.yintw.signinex;

import android.app.ProgressDialog;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.RadioButton;
import android.widget.Spinner;
import android.widget.TextView;

import com.chinapower.qtx.util.SigUtil;

import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.HttpMethod;
import org.apache.commons.httpclient.HttpStatus;
import org.apache.commons.httpclient.methods.GetMethod;

import java.io.IOException;
import java.math.BigDecimal;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;

import static android.view.View.VISIBLE;

public class MainActivity extends AppCompatActivity {

    //    旧版本url
//    String url = "http://182.92.231.43:9000/ChinaPowerEHome/userauthority/rest/webservice/employee/{action}/116.381326/39.900113/{uid}/10000001?dosigninflag=1&selectType=&other=&ismakesure=&sig={sigMessage}";
//    201602 更新版本url
//    String url = "http://123.57.206.117:9000/ChinaPowerEHome/userauthority/rest/webservice/employee/{action}/116.381326/39.900113/{uid}/10000001?dosigninflag=1&selectType=&other=&ismakesure=&sig={sigMessage}";
    String url = "http://123.57.206.117:9000/ChinaPowerEHome/userauthority/rest/webservice/employee/{action}/{longitude}/{latitude}/{uid}/10000001?dosigninflag=1&selectType=&other=&ismakesure=&sig={sigMessage}";
    String trsMessage;
    List<String> userList = null;
    String userName;

    Button mButton = null;
    RadioButton rButtonSignIn = null;
    RadioButton rButtonChackOut = null;
    TextView resultView = null;
    Spinner mSpinner = null;
    ArrayAdapter<String> adapter = null;
    ProgressDialog pd = null;
    RadioButton rButtonFz = null;
    RadioButton rButtonYz = null;
    private Handler showResultHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case 1:
                    resultView.setText(String.valueOf(msg.obj));
                    break;
            }
        }
    };

    public static String dateFormat(Date paramDate, String paramString) {
        return new SimpleDateFormat(paramString, Locale.getDefault()).format(paramDate);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        userList = new ArrayList<>();
        userList.add("银天伟");
        userList.add("吴文彬");
        userList.add("岳峥");
        userList.add("吕新丽");
        mSpinner = (Spinner) findViewById(R.id.spinner);
        adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, userList);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        mSpinner.setAdapter(adapter);
        mSpinner.setOnItemSelectedListener(new OnItemSelectedListener() {

            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                userName = adapter.getItem(position);
                parent.setVisibility(VISIBLE);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                userName = "";
                parent.setVisibility(VISIBLE);
            }
        });
        mButton = (Button) findViewById(R.id.button);
        rButtonSignIn = (RadioButton) findViewById(R.id.signIn);
        rButtonChackOut = (RadioButton) findViewById(R.id.checkOut);
        rButtonFz = (RadioButton) findViewById(R.id.rButtonFz);
                rButtonYz = (RadioButton) findViewById(R.id.rButtonYz);
                resultView = (TextView) findViewById(R.id.resultView);

                mButton.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        trsMessage = getTrsMessage();
                        if ("error2".equals(trsMessage)) {
                            resultView.setText("请选择签到或签退;");
                        } else {
                /* 显示ProgressDialog */
                            pd = ProgressDialog.show(MainActivity.this, "正在处理", "加载中，请稍后……");
                            new SendMessage().start();
                            // 测试用
//                resultView.setText(String.valueOf(trsMessage));
                        }
            }
        });
    }

    private String getLongitude() {
        // 实际值为116.381664 因为加random，所以取小0.00001
        BigDecimal longitude;
        if (rButtonFz.isChecked()) {
            longitude = new BigDecimal(116.381614);
        } else if (rButtonYz.isChecked()) {
            // 亦庄
//            longitude = new BigDecimal(116.575907);
            // 白广路
            longitude = new BigDecimal(116.364941);
        } else {
            longitude = new BigDecimal(116.381614);
        }
        return countXY(longitude).setScale(6, BigDecimal.ROUND_HALF_UP).toString();
    }

    private String getLatitude() {
        // 实际值为39.90012 因为加random，所以取小0.00001
        BigDecimal latitude;
        if (rButtonFz.isChecked()) {
            latitude = new BigDecimal(39.900085);
        } else if (rButtonYz.isChecked()) {
            // 亦庄
//            latitude = new BigDecimal(39.789551);
            // 白广路
            latitude = new BigDecimal(39.892155);
        } else {
            latitude = new BigDecimal(39.900085);
        }
        return countXY(latitude).setScale(6, BigDecimal.ROUND_HALF_UP).toString();
    }

    private BigDecimal countXY(BigDecimal param) {
        BigDecimal randomValue = new BigDecimal(Math.random()).divide(new BigDecimal(10000), BigDecimal.ROUND_HALF_UP);
        return param.add(randomValue);
    }

    private String getUserId() {
        if ("银天伟".equals(userName)) {
            return "1045";
        } else if ("吴文彬".equals(userName)) {
            return "1605";
        } else if ("岳峥".equals(userName)) {
            return "1026";
        } else if ("吕新丽".equals(userName)) {
            return "266";
        } else {
            return "1045";
        }
    }

    private String getTrsMessage() {
        Calendar calendar = Calendar.getInstance();
        calendar.add(Calendar.SECOND, 1);
        String userId = getUserId();

        HashMap<String, String> localHashMap = new HashMap<>();
        String action;
        if (rButtonSignIn.isChecked()) {
            action = "dosignin";
            localHashMap.put("longitude", "117.236538");
            localHashMap.put("latitude", "39.132044");
            localHashMap.put("uid", userId);
            localHashMap.put("xtsj", MainActivity.dateFormat(calendar.getTime(), "yyyy-MM-dd"));
        } else if (rButtonChackOut.isChecked()) {
            action = "dosignout";
            localHashMap.put("longitude", "116.381699");
            localHashMap.put("latitude", "39.904046");
            localHashMap.put("qdqt", "34589");
            localHashMap.put("uid", userId);
            localHashMap.put("xtsj", MainActivity.dateFormat(calendar.getTime(), "yyyy-MM-dd"));
        } else {
            return "error2;";
        }
        String sigMessage = SigUtil.generateSig(localHashMap);
        return url.replace("{action}", action).replace("{longitude}", getLongitude()).replace("{latitude}", getLatitude()).replace("{uid}", userId).replace("{sigMessage}", sigMessage);
    }

    private class SendMessage extends Thread {

        @Override
        public void run() {
            String result = doHttpAction(trsMessage);
            Message message = showResultHandler.obtainMessage();
            message.what = 1;
            message.obj = result;
            showResultHandler.sendMessage(message);
        }

        public String doHttpAction(String url) {
            String response = null;
            HttpClient client = new HttpClient();
            HttpMethod method = new GetMethod(url);
            client.getHttpConnectionManager().getParams().setConnectionTimeout(20000);
            client.getHttpConnectionManager().getParams().setSoTimeout(20000);
            try {
                method.addRequestHeader("Content-Type",
                        "application/x-www-form-urlencoded; charset=" + "utf-8");
                client.executeMethod(method);
                if (method.getStatusCode() == HttpStatus.SC_OK) {
                    response = method.getResponseBodyAsString();
                } else {
                    response = "网络异常";
                }
            } catch (IOException e) {
                e.printStackTrace();
                response = "网络异常";
            } finally {
                method.releaseConnection();
                pd.dismiss();
            }

            return response;
        }
    }
}
