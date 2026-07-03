package com.university.unify.activities;

import android.database.Cursor;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.android.volley.DefaultRetryPolicy;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.university.unify.R;
import com.university.unify.SqLite.DatabaseHelper;
import com.university.unify.adapter.StudentChatBotAdapter;
import com.university.unify.model.ChatBotMessageModel;
import com.university.unify.network.ApiConfig;
import com.university.unify.BuildConfig;
import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class StudentChatBotActivity extends AppCompatActivity {

    /*
        University demo mode only.
        Do NOT use direct Android API key in production.
    */
    private static final String OPEN_ROUTER_API_KEY =
            BuildConfig.OPENROUTER_API_KEY;
    private static final String OPEN_ROUTER_URL =
            "https://openrouter.ai/api/v1/chat/completions";

    private static final String AI_MODEL =
            "deepseek/deepseek-v4-flash";


    private DatabaseHelper databaseHelper;
    private ImageButton buttonBack;
    private RecyclerView recyclerMessages;
    private EditText editMessage;
    private Button buttonSend;
    private ProgressBar progressBar;

    private StudentChatBotAdapter adapter;
    private RequestQueue queue;

    private final ArrayList<ChatBotMessageModel> messages = new ArrayList<>();
    private String currentUserId = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_student_chatbot);

        queue = Volley.newRequestQueue(this);

        databaseHelper = new DatabaseHelper(this);

        bindViews();
        loadCurrentUser();
        setupRecycler();
        loadSavedChatMessages();
        setupClicks();

        if (messages.isEmpty()) {
            addWelcomeMessage();
        }
    }

    private void loadSavedChatMessages() {
        Cursor cursor = databaseHelper.getChatBotMessages(currentUserId);

        if (cursor != null) {
            while (cursor.moveToNext()) {
                String message = cursor.getString(0);
                String senderType = cursor.getString(1);

                if ("USER".equals(senderType)) {
                    messages.add(new ChatBotMessageModel(message, ChatBotMessageModel.TYPE_USER));
                } else {
                    messages.add(new ChatBotMessageModel(message, ChatBotMessageModel.TYPE_BOT));
                }
            }

            cursor.close();
        }

        if (adapter != null) {
            adapter.notifyDataSetChanged();
            scrollToBottom();
        }
    }

    private void bindViews() {
        buttonBack = findViewById(R.id.buttonBackChatBot);
        recyclerMessages = findViewById(R.id.recyclerChatBotMessages);
        editMessage = findViewById(R.id.editChatBotMessage);
        buttonSend = findViewById(R.id.buttonSendChatBot);
        progressBar = findViewById(R.id.progressChatBot);
    }

    private void loadCurrentUser() {
        DatabaseHelper db = new DatabaseHelper(this);
        currentUserId = safe(db.getLoggedInUserId());

        if (TextUtils.isEmpty(currentUserId)) {
            Toast.makeText(this, getString(R.string.error_user_not_found), Toast.LENGTH_LONG).show();
            finish();
        }
    }

    private void setupRecycler() {
        adapter = new StudentChatBotAdapter(this, messages);

        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        layoutManager.setStackFromEnd(true);

        recyclerMessages.setLayoutManager(layoutManager);
        recyclerMessages.setAdapter(adapter);
    }

    private void setupClicks() {
        buttonBack.setOnClickListener(v -> finish());
        buttonSend.setOnClickListener(v -> sendCurrentMessage());

        editMessage.setOnEditorActionListener((textView, actionId, event) -> {
            if (actionId == EditorInfo.IME_ACTION_SEND) {
                sendCurrentMessage();
                return true;
            }
            return false;
        });
    }

    private void addWelcomeMessage() {
        addBotMessage(getString(R.string.student_assistant_welcome));
    }

    private void sendCurrentMessage() {
        String message = getText(editMessage);

        if (TextUtils.isEmpty(message)) {
            editMessage.setError(getString(R.string.error_required));
            return;
        }

        if (OPEN_ROUTER_API_KEY.equals("PASTE_YOUR_OPENROUTER_KEY_HERE")) {
            addBotMessage("OpenRouter API key is missing. Add it inside StudentChatBotActivity first.");
            return;
        }

        addUserMessage(message);
        editMessage.setText("");

        loadStudentContextThenAskAI(message);
    }

    private void loadStudentContextThenAskAI(final String userMessage) {
        setLoading(true);

        StringRequest request = new StringRequest(
                Request.Method.POST,
                ApiConfig.STUDENT_AI_CONTEXT,
                response -> {
                    String context = "";

                    try {
                        JSONObject obj = new JSONObject(cleanJson(response));

                        if (obj.optBoolean("success", false)) {
                            context = obj.optString("context", "");
                        }

                    } catch (Exception ignored) {
                        context = "";
                    }

                    askAI(userMessage, context);
                },
                error -> {
                    setLoading(false);
                    addBotMessage("I could not load your app data right now. Please check your internet connection and try again.");
                }
        ) {
            @Override
            protected Map<String, String> getParams() {
                Map<String, String> params = new HashMap<>();
                params.put("user_id", currentUserId);
                return params;
            }
        };

        request.setRetryPolicy(new DefaultRetryPolicy(
                15000,
                1,
                DefaultRetryPolicy.DEFAULT_BACKOFF_MULT
        ));

        queue.add(request);
    }

    private void askAI(final String userMessage, final String studentContext) {
        try {
            String prompt = buildPrompt(userMessage, studentContext);

            JSONArray messagesArray = new JSONArray();

            JSONObject systemMessage = new JSONObject();
            systemMessage.put("role", "system");
            systemMessage.put("content", "You are the Unify university mobile app AI assistant.");

            JSONObject userMsg = new JSONObject();
            userMsg.put("role", "user");
            userMsg.put("content", prompt);

            messagesArray.put(systemMessage);
            messagesArray.put(userMsg);

            JSONObject body = new JSONObject();
            body.put("model", AI_MODEL);
            body.put("messages", messagesArray);
            body.put("temperature", 0.3);
            body.put("max_tokens", 700);

            JsonObjectRequest request = new JsonObjectRequest(
                    Request.Method.POST,
                    OPEN_ROUTER_URL,
                    body,
                    response -> {
                        setLoading(false);

                        String reply = extractAIReply(response);

                        if (TextUtils.isEmpty(reply)) {
                            addBotMessage("I could not understand the AI response. Please try again.");
                        } else {
                            addBotMessage(reply);
                        }
                    },
                    error -> {
                        setLoading(false);

                        String errorMessage = "AI is temporarily unavailable.";

                        if (error.networkResponse != null) {
                            errorMessage += "\nCode: " + error.networkResponse.statusCode;

                            if (error.networkResponse.data != null) {
                                errorMessage += "\n" + new String(error.networkResponse.data);
                            }
                        }

                        addBotMessage(errorMessage);
                    }
            ) {
                @Override
                public Map<String, String> getHeaders() {
                    Map<String, String> headers = new HashMap<>();

                    headers.put("Authorization", "Bearer " + OPEN_ROUTER_API_KEY);
                    headers.put("Content-Type", "application/json");

                    /*
                        Optional but recommended by OpenRouter.
                    */
                    headers.put("HTTP-Referer", "https://unify-university-demo.com");
                    headers.put("X-Title", "Unify University App");

                    return headers;
                }
            };

            request.setRetryPolicy(new DefaultRetryPolicy(
                    30000,
                    1,
                    DefaultRetryPolicy.DEFAULT_BACKOFF_MULT
            ));

            queue.add(request);

        } catch (Exception e) {
            setLoading(false);
            addBotMessage("AI is temporarily unavailable. Please try again.");
        }
    }

    private String buildPrompt(String userMessage, String studentContext) {
        String context = safe(studentContext);
        String history = buildShortConversationHistory();

        return "You are the Unify university mobile app AI assistant.\n" +
                "Answer shortly and naturally.\n" +
                "Understand English, Arabic, Lebanese Arabic, and Arabizi.\n" +
                "If the student writes Arabizi, answer in simple Arabizi.\n" +
                "Use ONLY the app data below. Do not invent data.\n\n" +

                "Arabizi hints:\n" +
                "shu=what, kif=how, ade=how much, mwed/mawed=courses, " +
                "msajla=registered/enrolled, sajel=enroll, dr=instructor, " +
                "aleme/grade=grade, mnha/good=good, material=materials, " +
                "fini/fne=can I, wen=where, emta=when.\n\n" +

                "App data:\n" +
                context + "\n\n" +

                "Recent chat:\n" +
                history + "\n\n" +

                "Student question:\n" +
                userMessage + "\n\n" +

                "Answer:";
    }

    private String buildShortConversationHistory() {
        StringBuilder history = new StringBuilder();

        int start = Math.max(0, messages.size() - 2);

        for (int i = start; i < messages.size(); i++) {
            ChatBotMessageModel msg = messages.get(i);

            if (msg == null || TextUtils.isEmpty(msg.getMessage())) {
                continue;
            }

            String text = msg.getMessage();

            if (text.length() > 120) {
                text = text.substring(0, 120);
            }

            if (msg.getType() == ChatBotMessageModel.TYPE_USER) {
                history.append("Student: ").append(text).append("\n");
            } else {
                history.append("Assistant: ").append(text).append("\n");
            }
        }

        return history.toString();
    }

    private String extractAIReply(JSONObject response) {
        try {
            JSONArray choices = response.optJSONArray("choices");
            if (choices == null || choices.length() == 0) {
                return "";
            }

            JSONObject firstChoice = choices.optJSONObject(0);
            if (firstChoice == null) {
                return "";
            }

            JSONObject message = firstChoice.optJSONObject("message");
            if (message == null) {
                return "";
            }

            return safe(message.optString("content", ""));

        } catch (Exception e) {
            return "";
        }
    }

    private void addUserMessage(String text) {
        messages.add(new ChatBotMessageModel(text, ChatBotMessageModel.TYPE_USER));
        adapter.notifyItemInserted(messages.size() - 1);

        if (!TextUtils.isEmpty(currentUserId)) {
            databaseHelper.saveChatBotMessage(currentUserId, text, "USER");
        }

        scrollToBottom();
    }

    private void addBotMessage(String text) {
        messages.add(new ChatBotMessageModel(text, ChatBotMessageModel.TYPE_BOT));
        adapter.notifyItemInserted(messages.size() - 1);

        if (!TextUtils.isEmpty(currentUserId)) {
            databaseHelper.saveChatBotMessage(currentUserId, text, "BOT");
        }

        scrollToBottom();
    }

    private void scrollToBottom() {
        recyclerMessages.post(() -> {
            if (!messages.isEmpty()) {
                recyclerMessages.smoothScrollToPosition(messages.size() - 1);
            }
        });
    }

    private void setLoading(boolean loading) {
        progressBar.setVisibility(loading ? View.VISIBLE : View.GONE);
        buttonSend.setEnabled(!loading);
        buttonSend.setAlpha(loading ? 0.6f : 1f);
    }

    private String getText(EditText editText) {
        if (editText == null || editText.getText() == null) {
            return "";
        }

        return editText.getText().toString().trim();
    }

    private String safe(String value) {
        if (value == null || value.equalsIgnoreCase("null")) {
            return "";
        }

        return value.trim();
    }

    private String cleanJson(String response) {
        if (response == null) {
            return "{}";
        }

        String clean = response.trim();
        int start = clean.indexOf("{");
        int end = clean.lastIndexOf("}");

        if (start != -1 && end != -1 && end > start) {
            return clean.substring(start, end + 1);
        }

        return clean;
    }
}