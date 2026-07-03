package com.university.unify.helpers;

import android.app.AlertDialog;
import android.content.Context;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONObject;

import java.nio.charset.StandardCharsets;

public class EmailJsSender {

    private static final String SERVICE_ID = "service_luf5tz1";
    private static final String TEMPLATE_ID = "template_td7iyty";
    private static final String PUBLIC_KEY = "DpUKrt0vdYeqkwCQP";

    private static final String EMAILJS_URL =
            "https://api.emailjs.com/api/v1.0/email/send";

    public static void sendInstructorCredentials(
            Context context,
            String toEmail,
            String temporaryPassword,
            String role
    ) {
        try {
            JSONObject templateParams = new JSONObject();
            templateParams.put("to_email", toEmail);
            templateParams.put("user_email", toEmail);
            templateParams.put("temporary_password", temporaryPassword);
            templateParams.put("role", role);
            templateParams.put("app_name", "Unify University");

            JSONObject body = new JSONObject();
            body.put("service_id", SERVICE_ID);
            body.put("template_id", TEMPLATE_ID);
            body.put("user_id", PUBLIC_KEY);
            body.put("template_params", templateParams);

            StringRequest request = new StringRequest(
                    Request.Method.POST,
                    EMAILJS_URL,
                    response -> showDialog(context, "EmailJS", "Credentials email sent."),
                    error -> {
                        String msg = "EmailJS failed";

                        if (error.networkResponse != null) {
                            msg += "\nCode: " + error.networkResponse.statusCode;

                            if (error.networkResponse.data != null) {
                                msg += "\n\n" + new String(error.networkResponse.data, StandardCharsets.UTF_8);
                            }
                        } else if (error.getMessage() != null) {
                            msg += "\n" + error.getMessage();
                        }

                        showDialog(context, "EmailJS Error", msg);
                    }
            ) {
                @Override
                public String getBodyContentType() {
                    return "application/json; charset=UTF-8";
                }

                @Override
                public byte[] getBody() {
                    return body.toString().getBytes(StandardCharsets.UTF_8);
                }
            };

            RequestQueue queue = Volley.newRequestQueue(context.getApplicationContext());
            queue.add(request);

        } catch (Exception e) {
            showDialog(context, "EmailJS Error", "Email setup error:\n" + e.getMessage());
        }
    }

    private static void showDialog(Context context, String title, String message) {
        new AlertDialog.Builder(context)
                .setTitle(title)
                .setMessage(message)
                .setPositiveButton("OK", null)
                .show();
    }
}