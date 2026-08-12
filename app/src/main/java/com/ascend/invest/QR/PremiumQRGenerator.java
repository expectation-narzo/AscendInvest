package com.ascend.invest.QR;


import android.graphics.*;
import com.google.zxing.*;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.qrcode.decoder.ErrorCorrectionLevel;
import java.util.HashMap;
import java.util.Map;

public class PremiumQRGenerator {

    public static Bitmap generate(String text, int size, Bitmap logo) throws WriterException {

        Map<EncodeHintType, Object> hints = new HashMap<>();
        hints.put(EncodeHintType.ERROR_CORRECTION, ErrorCorrectionLevel.H);
        hints.put(EncodeHintType.MARGIN, 0);

        BitMatrix matrix = new MultiFormatWriter()
                .encode(text, BarcodeFormat.QR_CODE, size, size, hints);

        int count = matrix.getWidth();
        float module = (float) size / count;

        Bitmap bitmap = Bitmap.createBitmap(size, size, Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(bitmap);
        canvas.drawColor(Color.WHITE);

        Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);
        paint.setColor(Color.BLACK);
        paint.setStyle(Paint.Style.FILL);

        for (int x = 0; x < count; x++) {
            for (int y = 0; y < count; y++) {

                if (!matrix.get(x, y)) continue;
                if (isFinder(x, y, count)) continue;

                float left = x * module;
                float top = y * module;
                float right = left + module;
                float bottom = top + module;

                boolean topN = y > 0 && matrix.get(x, y - 1);
                boolean bottomN = y < count - 1 && matrix.get(x, y + 1);
                boolean leftN = x > 0 && matrix.get(x - 1, y);
                boolean rightN = x < count - 1 && matrix.get(x + 1, y);

                float radius = module / 2f;

                float tl = (!topN && !leftN) ? radius : 0;
                float tr = (!topN && !rightN) ? radius : 0;
                float br = (!bottomN && !rightN) ? radius : 0;
                float bl = (!bottomN && !leftN) ? radius : 0;

                RectF rect = new RectF(left, top, right, bottom);

                Path path = new Path();
                path.addRoundRect(rect,
                        new float[]{
                                tl, tl,
                                tr, tr,
                                br, br,
                                bl, bl
                        },
                        Path.Direction.CW);

                canvas.drawPath(path, paint);
            }
        }

        drawFinder(canvas, module, 0, 0);
        drawFinder(canvas, module, count - 7, 0);
        drawFinder(canvas, module, 0, count - 7);

        if (logo != null) {

            int logoSize = size / 5;   // smaller logo (was /4)
            int padding = 30;          // clean white spacing

            Bitmap scaledLogo = Bitmap.createScaledBitmap(logo, logoSize, logoSize, true);

            int left = (size - logoSize) / 2;
            int top = (size - logoSize) / 2;

            // 🔥 STEP 1: CLEAR QR AREA COMPLETELY (no modules behind logo)
            Paint clearPaint = new Paint();
            clearPaint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.CLEAR));

            canvas.drawRoundRect(
                    left - padding,
                    top - padding,
                    left + logoSize + padding,
                    top + logoSize + padding,
                    60,
                    60,
                    clearPaint
            );

            clearPaint.setXfermode(null);

            // 🔥 STEP 2: Draw clean white rounded background
            Paint whitePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
            whitePaint.setColor(Color.WHITE);

            canvas.drawRoundRect(
                    left - padding,
                    top - padding,
                    left + logoSize + padding,
                    top + logoSize + padding,
                    60,
                    60,
                    whitePaint
            );

            // 🔥 STEP 3: Draw logo on top
            canvas.drawBitmap(scaledLogo, left, top, null);
        }

        return bitmap;
    }

    private static boolean isFinder(int x, int y, int size) {
        return (x < 7 && y < 7) ||
                (x > size - 8 && y < 7) ||
                (x < 7 && y > size - 8);
    }

    private static void drawFinder(Canvas canvas, float module, int startX, int startY) {

        Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);
        paint.setColor(Color.BLACK);

        float size = module * 7;
        float left = startX * module;
        float top = startY * module;

        RectF outer = new RectF(left, top, left + size, top + size);
        canvas.drawRoundRect(outer, 50, 50, paint);

        Paint white = new Paint(Paint.ANTI_ALIAS_FLAG);
        white.setColor(Color.WHITE);

        RectF innerWhite = new RectF(
                left + module,
                top + module,
                left + size - module,
                top + size - module
        );
        canvas.drawRoundRect(innerWhite, 40, 40, white);

        RectF center = new RectF(
                left + 2.5f * module,
                top + 2.5f * module,
                left + size - 2.5f * module,
                top + size - 2.5f * module
        );
        canvas.drawRoundRect(center, 30, 30, paint);
    }
}