package com.university.unify.network;

import com.android.volley.AuthFailureError;
import com.android.volley.NetworkResponse;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.toolbox.HttpHeaderParser;

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.util.HashMap;
import java.util.Map;

public class VolleyMultipartRequest extends Request<NetworkResponse> {

    private final Response.Listener<NetworkResponse> listener;
    private final Response.ErrorListener errorListener;
    private final Map<String, String> headers;

    private final String boundary = "apiclient-" + System.currentTimeMillis();

    public VolleyMultipartRequest(
            int method,
            String url,
            Response.Listener<NetworkResponse> listener,
            Response.ErrorListener errorListener
    ) {
        super(method, url, errorListener);
        this.listener = listener;
        this.errorListener = errorListener;
        this.headers = new HashMap<>();
    }

    @Override
    public Map<String, String> getHeaders() throws AuthFailureError {
        return headers;
    }

    @Override
    public String getBodyContentType() {
        return "multipart/form-data;boundary=" + boundary;
    }

    @Override
    public byte[] getBody() throws AuthFailureError {
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        DataOutputStream dos = new DataOutputStream(bos);

        try {
            Map<String, String> textParams = getParams();

            if (textParams != null && textParams.size() > 0) {
                for (Map.Entry<String, String> entry : textParams.entrySet()) {
                    buildTextPart(dos, entry.getKey(), entry.getValue());
                }
            }

            Map<String, DataPart> dataParams = getByteData();

            if (dataParams != null && dataParams.size() > 0) {
                for (Map.Entry<String, DataPart> entry : dataParams.entrySet()) {
                    buildDataPart(dos, entry.getKey(), entry.getValue());
                }
            }

            dos.writeBytes("--" + boundary + "--\r\n");

            return bos.toByteArray();

        } catch (Exception e) {
            e.printStackTrace();
        }

        return null;
    }

    private void buildTextPart(DataOutputStream dos, String parameterName, String value)
            throws Exception {

        dos.writeBytes("--" + boundary + "\r\n");
        dos.writeBytes(
                "Content-Disposition: form-data; name=\"" +
                        parameterName +
                        "\"\r\n\r\n"
        );
        dos.writeBytes(value + "\r\n");
    }

    private void buildDataPart(DataOutputStream dos, String parameterName, DataPart dataFile)
            throws Exception {

        dos.writeBytes("--" + boundary + "\r\n");

        dos.writeBytes(
                "Content-Disposition: form-data; name=\"" +
                        parameterName +
                        "\"; filename=\"" +
                        dataFile.getFileName() +
                        "\"\r\n"
        );

        dos.writeBytes("Content-Type: " + dataFile.getType() + "\r\n\r\n");
        dos.write(dataFile.getContent());
        dos.writeBytes("\r\n");
    }

    protected Map<String, DataPart> getByteData() throws AuthFailureError {
        return null;
    }

    @Override
    protected Response<NetworkResponse> parseNetworkResponse(NetworkResponse response) {
        return Response.success(response, HttpHeaderParser.parseCacheHeaders(response));
    }

    @Override
    protected void deliverResponse(NetworkResponse response) {
        listener.onResponse(response);
    }

    @Override
    public void deliverError(com.android.volley.VolleyError error) {
        errorListener.onErrorResponse(error);
    }

    public static class DataPart {
        private final String fileName;
        private final byte[] content;
        private final String type;

        public DataPart(String fileName, byte[] content, String type) {
            this.fileName = fileName;
            this.content = content;
            this.type = type;
        }

        public String getFileName() {
            return fileName;
        }

        public byte[] getContent() {
            return content;
        }

        public String getType() {
            return type;
        }
    }
}