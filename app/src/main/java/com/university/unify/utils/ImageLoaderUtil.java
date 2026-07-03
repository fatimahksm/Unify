package com.university.unify.utils;

import android.content.Context;
import android.graphics.Bitmap;
import android.text.TextUtils;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.android.volley.toolbox.ImageRequest;
import com.android.volley.toolbox.Volley;
import com.university.unify.R;

public class ImageLoaderUtil {

    private static final String BASE_URL = "http://collegeapp.atwebpages.com/";
    private static final String PROFILE_IMAGE_SCRIPT = BASE_URL + "profile/show_profile_image.php?name=";

    /**
     * Simple loader — older API, still supported.
     */
    public static void loadImage(Context context, ImageView imageView, String imageUrl) {
        loadImage(context, imageView, imageUrl, true);
    }

    public static void loadImage(Context context, ImageView imageView, String imageUrl, boolean hideIfEmpty) {
        String cleanUrl = normalizeImageUrl(imageUrl);

        if (TextUtils.isEmpty(cleanUrl)) {
            imageView.setImageDrawable(null);
            if (hideIfEmpty) {
                imageView.setVisibility(View.GONE);
            } else {
                imageView.setVisibility(View.VISIBLE);
                imageView.setImageResource(R.drawable.baseline_person_24);
            }
            return;
        }

        imageView.setVisibility(View.VISIBLE);
        imageView.setImageDrawable(null);

        ImageRequest request = new ImageRequest(
                cleanUrl,
                bitmap -> {
                    imageView.setVisibility(View.VISIBLE);
                    imageView.setImageBitmap(bitmap);
                },
                0,
                0,
                ImageView.ScaleType.CENTER_CROP,
                Bitmap.Config.RGB_565,
                error -> {
                    imageView.setImageDrawable(null);
                    if (hideIfEmpty) {
                        imageView.setVisibility(View.GONE);
                    } else {
                        imageView.setVisibility(View.VISIBLE);
                        imageView.setImageResource(R.drawable.baseline_person_24);
                    }
                }
        );

        Volley.newRequestQueue(context).add(request);
    }

    /**
     * Avatar binder — shows the profile image if available, otherwise shows
     * the user's first initial in the same circle.
     *
     * Pass either:
     *   - both views (ImageView for photo + TextView for initial), or
     *   - just an ImageView (pass null for textInitial); a default "person" icon
     *     is shown when no photo is available.
     *
     * Always pass the full name so the initial can be computed.
     */
    public static void bindAvatar(Context context,
                                  ImageView imageAvatar,
                                  TextView textInitial,
                                  String fullName,
                                  String imageUrl) {
        String cleanUrl = normalizeImageUrl(imageUrl);
        String initial = computeInitial(fullName);

        if (TextUtils.isEmpty(cleanUrl)) {
            // No photo — show initial (or icon fallback)
            if (imageAvatar != null) {
                if (textInitial != null) {
                    imageAvatar.setVisibility(View.GONE);
                } else {
                    imageAvatar.setVisibility(View.VISIBLE);
                    imageAvatar.setImageResource(R.drawable.baseline_person_24);
                }
            }
            if (textInitial != null) {
                textInitial.setVisibility(View.VISIBLE);
                textInitial.setText(initial);
            }
            return;
        }

        // Photo available — show it, hide initial
        if (textInitial != null) {
            textInitial.setVisibility(View.GONE);
        }
        if (imageAvatar != null) {
            imageAvatar.setVisibility(View.VISIBLE);
            loadImage(context, imageAvatar, cleanUrl, false);
        }
    }

    private static String computeInitial(String fullName) {
        if (TextUtils.isEmpty(fullName)) return "?";
        String trimmed = fullName.trim();
        if (trimmed.isEmpty()) return "?";
        return trimmed.substring(0, 1).toUpperCase();
    }

    public static String normalizeImageUrl(String value) {
        if (value == null) return "";

        String url = value.trim();

        if (url.isEmpty()
                || url.equalsIgnoreCase("null")
                || url.equalsIgnoreCase("NULL")
                || url.equals("-")) {
            return "";
        }

        url = url.replace("\\/", "/");
        url = url.replace(" ", "%20");

        /*
            Old broken value:  http://collegeapp.atwebpages.com/profile/uploads/profile_13_xxx.jpg
            New working value: http://collegeapp.atwebpages.com/profile/show_profile_image.php?name=profile_13_xxx.jpg
        */
        if (url.contains("/profile/uploads/")) {
            String fileName = url.substring(url.lastIndexOf('/') + 1);
            return PROFILE_IMAGE_SCRIPT + fileName;
        }

        if (url.startsWith("profile/uploads/")) {
            String fileName = url.substring(url.lastIndexOf('/') + 1);
            return PROFILE_IMAGE_SCRIPT + fileName;
        }

        if (url.startsWith("uploads/profile_") || url.startsWith("profile_")) {
            String fileName = url.substring(url.lastIndexOf('/') + 1);
            return PROFILE_IMAGE_SCRIPT + fileName;
        }

        if (url.startsWith("http://") || url.startsWith("https://")) {
            return url;
        }

        if (url.startsWith("/")) {
            return BASE_URL + url.substring(1);
        }

        return BASE_URL + url;
    }
}
