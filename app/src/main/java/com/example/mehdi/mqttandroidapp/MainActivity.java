package com.example.mehdi.mqttandroidapp;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.text.InputType;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ExpandableListAdapter;
import android.widget.ExpandableListView;
import android.widget.TextView;
import android.widget.Toast;

import org.eclipse.paho.client.mqttv3.IMqttDeliveryToken;
import org.eclipse.paho.client.mqttv3.MqttCallbackExtended;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttMessage;

import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {

    MQTTHelper mqttHelper;

    private int funcCodeDigit = 2;
    private int notCodeDigit = 2;
    private int conStatusDigit = 2;
    private int countDigit = 2;
    private int regAddressDigit = 2;
    private int valuesDigit = 5;

    private TextView functionMB, retuenValues,modConStatusTextView, intConStatusTextView;
    private Button buttonPublish, buttonOK;
    private EditText dataReceived, regAddressET,regCountET,regValueET;

    private String functionCode,regAddress,regCount,regValues,requestMsg, responseMsg, notCode, conStatus,
            responseFunCode, responseCount, responseAddress,resValues, temStr, requestMsgHex, responseMsgHex;
    private String[] responseValues;

    private char[] responseFunCodeChar = new char[funcCodeDigit];
    private char[] responseCountChar = new char[countDigit];
    private char[] responseAddressChar = new char[regAddressDigit];
    private char[] responseValuesChar = new char[valuesDigit];
    private char[] notCodeChar = new char[notCodeDigit];
    private char[] conStatusChar = new char[conStatusDigit];

    private int lengthofResponse , counter = 0;

    SharedPreferences modConnectionSatus;

    ExpandableListView myExpandableView;

    List<String> ChildList;
    Map<String, List<String>> ParentListItems;
    // Assign Parent list items here.
    List<String> ParentList = new ArrayList<String>();
    { ParentList.add("Function");}

    String [] childNames= {"Read Coil" , "Read Input Discrete" , "Read Input Register" , "Read Multiple Registers" ,
            "Write Single Coil" , "Write Multiple Coils" , "Write Single Register" , "Write Multiple Registers" , };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        modConnectionSatus = getSharedPreferences("modConnStatus", Context.MODE_PRIVATE);

        ParentListItems = new LinkedHashMap<String, List<String>>();

        for ( String HoldItem : ParentList) {
            if (HoldItem.equals("Function")) {
                loadChild(childNames);
            }
            ParentListItems.put(HoldItem, ChildList);
        }


        myExpandableView = (ExpandableListView) findViewById(R.id.funCode);

        functionMB = (TextView) findViewById(R.id.funCodeText);
        regAddressET = (EditText) findViewById(R.id.RegAddressEditText);
        regCountET = (EditText) findViewById(R.id.RegCountEditText);
        regValueET = (EditText) findViewById(R.id.RegValueEditText);

        modConStatusTextView = (TextView) findViewById(R.id.modConStatus);
        intConStatusTextView = (TextView) findViewById(R.id.intStatus);

        buttonPublish = (Button) findViewById(R.id.buttonPublish);
        buttonPublish.setOnClickListener(this);
        buttonPublish.setEnabled(false);
        buttonOK = (Button) findViewById(R.id.buttonOK);
        buttonOK.setOnClickListener(this);

        retuenValues = (TextView) findViewById(R.id.dataReceivedLabel);
        dataReceived = (EditText) findViewById(R.id.dataReceived);
        dataReceived.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_FLAG_MULTI_LINE);

        final ExpandableListAdapter myExpandableAdapter = new MyExpandableAdapter(this, ParentList, ParentListItems);
        myExpandableView.setAdapter(myExpandableAdapter);

        // Default of expandable list view
        functionCode = "16";
        functionMB.setText("Write Multiple Registers");
        regValueET.setEnabled(true);
        regCountET.setEnabled(true);

        myExpandableView.setOnChildClickListener(new ExpandableListView.OnChildClickListener() {

            @Override
            public boolean onChildClick(ExpandableListView parent, View v,
                                        int groupPosition, int childPosition, long id) {
                // TODO Auto-generated method stub

                final String selected = (String) myExpandableAdapter.getChild(
                        groupPosition, childPosition);

                switch (selected) {
                    case "Read Coil":
                        functionCode = "01";
                        functionMB.setText("Read Coil");
                        regValueET.setEnabled(false);
                        regCount = "01";
                        break;
                    case "Read Input Discrete":
                        functionCode = "02";
                        functionMB.setText("Read Input Discrete");
                        regValueET.setEnabled(false);
                        regCount = "01";
                        break;
                    case "Read Input Register":
                        functionCode = "04";
                        functionMB.setText("Read Input Register");
                        regValueET.setEnabled(false);
                        regCountET.setEnabled(false);
                        regCount = "01";
                        break;
                    case "Read Multiple Registers":
                        functionCode = "03";
                        functionMB.setText("Read Multiple Registers");
                        regCountET.setEnabled(true);
                        regValueET.setEnabled(false);
                        break;
                    case "Write Single Coil":
                        functionCode = "05";
                        functionMB.setText("Write Single Coil");
                        regValueET.setEnabled(true);
                        regCountET.setEnabled(false);
                        regCount = "01";
                        break;
                    case "Write Multiple Coils":
                        functionCode = "15";
                        functionMB.setText("Write Multiple Coils");
                        regValueET.setEnabled(true);
                        regCountET.setEnabled(true);
                        break;
                    case "Write Single Register":
                        functionCode = "06";
                        functionMB.setText("Write Single Register");
                        regValueET.setEnabled(true);
                        regCountET.setEnabled(false);
                        regCount = "01";
                        break;
                    case "Write Multiple Registers":
                        functionCode = "16";
                        functionMB.setText("Write Multiple Registers");
                        regValueET.setEnabled(true);
                        regCountET.setEnabled(true);
                        break;
                }
                return true;
            }
        });

       
        String modConnStatAtStart = modConnectionSatus.getString("connectionStatus","Default");

        SharedPreferences.Editor editor = modConnectionSatus.edit();
        if (modConnStatAtStart.equals("Connected"))
        {
            modConStatusTextView.setText("Connected");
            modConStatusTextView.setTextColor(0xff99cc00);
        } else
        {
            modConStatusTextView.setText("Disconnected");
            modConStatusTextView.setTextColor(0xffff4444);
        }

        intConStatusTextView.setText("Disconnected");
        intConStatusTextView.setTextColor(0xffff4444);

        startMqtt();
    }

    @Override
    public void onClick(View view) {

        if (view.getId() == buttonOK.getId()) {
            String countSt = regCountET.getText().toString();
            int countInt = new Integer(countSt).intValue();

            if (counter == 0) {
                regValues = String.format("%5S", regValueET.getText().toString().trim()).replace(' ', '0');
            } else {
                regValues = regValues + String.format("%5S", regValueET.getText().toString().trim()).replace(' ', '0');
            }
            regValueET.setText("");
            counter++;

            if (counter == countInt) {
                buttonOK.setEnabled(false);
                buttonPublish.setEnabled(true);
                Toast.makeText(getApplicationContext(), "Enough Data Has Been Given! Press Send Button", Toast.LENGTH_SHORT).show();
            }
        }

        if (view.getId() == buttonPublish.getId()) {

            if (functionCode.equals("03") || functionCode.equals("15") || functionCode.equals("16")){
                regCount = String.format("%2S",regCountET.getText().toString().trim()).replace(' ', '0');
            }else
            {
                regCount = "01";
            }

            // %2s --> because register addresses are supposed to have 2 digit
            regAddress = String.format("%2S",regAddressET.getText().toString().trim()).replace(' ', '0');

            requestMsg = functionCode + regCount + regAddress;

            if (functionCode.equals("05") || functionCode.equals("06") || functionCode.equals("15") || functionCode.equals("16")){

                requestMsg = requestMsg + regValues;
            }

            //requestMsgHex = convertStringToHex(requestMsg);

            Log.d("Result", requestMsg);

            if (regAddress.equals("") || regAddress.equals(null) || regCount.equals("") || regCount.equals(null) || regValues.equals("") || regValues.equals(null))
            {
                Toast.makeText(getApplicationContext(), "Please Specify All The Items!", Toast.LENGTH_LONG).show();
            }else {
                try {
                    mqttHelper.publishMessage(requestMsg, 1, "temp");
                    Toast.makeText(getApplicationContext(), "Sending ...", Toast.LENGTH_SHORT).show();
                    buttonOK.setEnabled(true);
                    buttonPublish.setEnabled(false);
                    regAddressET.setText("");
                    regCountET.setText("");

                    counter = 0;

                } catch (MqttException e) {
                    e.printStackTrace();
                } catch (UnsupportedEncodingException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    private void startMqtt() {
        mqttHelper = new MQTTHelper(getApplicationContext());
        mqttHelper.setCallback(new MqttCallbackExtended() {
            @Override
            public void connectComplete(boolean b, String s) {
                Toast.makeText(getApplicationContext(), "Connected to Broker!", Toast.LENGTH_SHORT).show();
		        try{
                    mqttHelper.publishMessage("990200", 1, "temp");
                }catch (MqttException e) {
                    e.printStackTrace();
                } catch (UnsupportedEncodingException e) {
                    e.printStackTrace();
                }

            }

            @Override
            public void connectionLost(Throwable throwable) {
                Toast.makeText(getApplicationContext(), "Connection to Broker Lost!", Toast.LENGTH_SHORT).show();

            }

            @Override
            public void messageArrived(String topic, MqttMessage mqttMessage) throws Exception {

                //responseMsgHex = mqttMessage.toString();
                //responseMsg = convertHexToString(responseMsgHex);
                responseMsg = mqttMessage.toString();

                lengthofResponse = responseMsg.length();

                responseValues = new String[(lengthofResponse - funcCodeDigit - regAddressDigit - countDigit) / valuesDigit];
                Log.w("Debug", responseMsg);
                //Log.w("Length ",Integer.toString(lengthofResponse));


                responseMsg.getChars(0, funcCodeDigit, responseFunCodeChar, 0);
                responseFunCode = new String(responseFunCodeChar);

                if (responseFunCode.equals("99")) {
                    responseMsg.getChars(funcCodeDigit, funcCodeDigit + notCodeDigit, notCodeChar, 0);
                    notCode = new String(notCodeChar);
                    if (notCode.equals("01"))
                    {
                        SharedPreferences.Editor editor = modConnectionSatus.edit();
                        responseMsg.getChars(funcCodeDigit + notCodeDigit, funcCodeDigit + notCodeDigit + conStatusDigit, conStatusChar, 0);
                        conStatus = new String(conStatusChar);

                        if (conStatus.equals("01"))
                        {
                            editor.putString("connectionStatus", "Connected");
                            modConStatusTextView.setText("Connected");
                            modConStatusTextView.setTextColor(0xff99cc00);
                        } else if (conStatus.equals("00"))
                        {
                            editor.putString("connectionStatus", "Disconnected");
                            modConStatusTextView.setText("Disconnected");
                            modConStatusTextView.setTextColor(0xffff4444);
                        }

                        editor.apply();
                    }else if (notCode.equals("02")) {
                        responseMsg.getChars(funcCodeDigit + notCodeDigit, funcCodeDigit + notCodeDigit + conStatusDigit, conStatusChar, 0);
                        conStatus = new String(conStatusChar);

                        if (conStatus.equals("01")) {

                            intConStatusTextView.setText("Connected");
                            intConStatusTextView.setTextColor(0xff99cc00);
                        }

                    }
                } else
                {
                    responseMsg.getChars(funcCodeDigit, funcCodeDigit + countDigit, responseCountChar, 0);
                    responseMsg.getChars(funcCodeDigit + countDigit, funcCodeDigit + countDigit + regAddressDigit, responseAddressChar, 0);


                    responseCount = new String(responseCountChar);
                    responseAddress = new String(responseAddressChar);

                    int resCountInt = new Integer(responseCount).intValue();

                    // Check to see if the received data is not corrupted
                    if (lengthofResponse < (resCountInt * funcCodeDigit + funcCodeDigit + countDigit + regAddressDigit)) {
                        Toast.makeText(getApplicationContext(), "Error in Number of Received Data!!", Toast.LENGTH_SHORT).show();
                    } else {

                        Log.w("FunCode", responseFunCode);
                        Log.w("Count", responseCount);
                        Log.w("Address", responseAddress);

                        int length = Integer.parseInt(responseCount);
                        Log.w("length", Integer.toString(length));

                        dataReceived.setLines(length + 1);
                        resValues = String.format("%-20s%17s\n", "Address", "Values");

                        for (int i = 0; i < length; i++) {
                            Log.w("Counter", Integer.toString(i));
                            responseMsg.getChars(i * valuesDigit + funcCodeDigit + countDigit + regAddressDigit, (i + 1) * valuesDigit + funcCodeDigit + countDigit + regAddressDigit, responseValuesChar, 0);
                            temStr = new String(responseValuesChar);
                            Log.w("TempRes", temStr);
                            responseValues[i] = temStr.replaceFirst("^0*", "");
                            Log.w("Response", responseValues[i]);
                            resValues = resValues + String.format("%-20s%20s\n", Integer.toString(i + Integer.parseInt(responseAddress)), responseValues[i]);
                        }
                        dataReceived.setText(resValues);

                        switch (responseFunCode) {
                            case "01":
                                retuenValues.setText("Coil Value: ");
                                break;
                            case "02":
                                retuenValues.setText("Input Discrete Value: ");
                                break;
                            case "04":
                                retuenValues.setText("Input Register Value: ");
                                break;
                            case "03":
                                retuenValues.setText("Input Registers Values: ");
                                break;
                            case "05":
                                retuenValues.setText("Coil Value: ");
                                break;
                            case "15":
                                retuenValues.setText("Coils Values: ");
                                break;
                            case "06":
                                retuenValues.setText("Register Value: ");
                                break;
                            case "16":
                                retuenValues.setText("Registers Values: ");
                                break;
                        }
                    }
                }
            }

            @Override
            public void deliveryComplete(IMqttDeliveryToken iMqttDeliveryToken) {

            }
        });
    }

    private void loadChild(String[] ParentElementsName) {
        ChildList = new ArrayList<String>();
        for (String model : ParentElementsName)
            ChildList.add(model);
    }

    public String convertStringToHex(String str){

        char[] chars = str.toCharArray();

        StringBuffer hex = new StringBuffer();
        for(int i = 0; i < chars.length; i++){
            hex.append(Integer.toHexString((int)chars[i]));
        }

        return hex.toString();
    }

    public String convertHexToString(String hex){

        StringBuilder sb = new StringBuilder();
        StringBuilder temp = new StringBuilder();

        //49204c6f7665204a617661 split into two characters 49, 20, 4c...
        for( int i=0; i<hex.length()-1; i+=2 ){

            //grab the hex in pairs
            String output = hex.substring(i, (i + 2));
            //convert hex to decimal
            int decimal = Integer.parseInt(output, 16);
            //convert the decimal to character
            sb.append((char)decimal);

            temp.append(decimal);
        }
        System.out.println("Decimal : " + temp.toString());

        return sb.toString();
    }
}
