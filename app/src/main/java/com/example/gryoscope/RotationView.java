package com.example.gryoscope;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.RectF;
import android.graphics.Camera;
import android.util.AttributeSet;
import android.view.View;

import java.util.Locale;

public class RotationView extends View {

    private Paint paint;
    private Camera camera;

    // Current rotation angles in degrees

    private float angleX = 0f;
    private float angleY = 0f;
    private float angleZ = 0f;

    // Previous sensor timestamp

    private long lastTimestamp = 0;

    private float density;

    public RotationView(Context context) {
        super(context);
        initialize();
    }

    public RotationView(Context context, AttributeSet attrs) {
        super(context, attrs);
        initialize();
    }

    public RotationView(Context context,
                        AttributeSet attrs,
                        int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        initialize();
    }

    // Initialize drawing tools

    private void initialize() {

        paint = new Paint(Paint.ANTI_ALIAS_FLAG);

        camera = new Camera();

        density = getResources()
                .getDisplayMetrics().density;

        setLayerType(View.LAYER_TYPE_SOFTWARE, null);
    }

    // Receive gyroscope readings

    public void updateRotation(float x,
                               float y,
                               float z,
                               long timestamp) {

        if (lastTimestamp == 0) {
            lastTimestamp = timestamp;
            return;
        }

        // Calculate time difference in seconds

        float deltaTime =
                (timestamp - lastTimestamp) / 1000000000f;

        lastTimestamp = timestamp;

        // Avoid unusually large time intervals

        if (deltaTime <= 0 || deltaTime > 0.1f) {
            return;
        }

        // Convert angular velocity into rotation angle

        angleX += (float) Math.toDegrees(x * deltaTime);

        angleY += (float) Math.toDegrees(y * deltaTime);

        angleZ += (float) Math.toDegrees(z * deltaTime);

        // Keep angles within a manageable range

        angleX = normalize(angleX);
        angleY = normalize(angleY);
        angleZ = normalize(angleZ);

        // Redraw the view

        invalidate();
    }

    private float normalize(float angle) {

        angle = angle % 360f;

        if (angle > 180f) {
            angle -= 360f;
        }

        if (angle < -180f) {
            angle += 360f;
        }

        return angle;
    }

    // Reset orientation

    public void resetRotation() {

        angleX = 0f;
        angleY = 0f;
        angleZ = 0f;

        lastTimestamp = 0;

        invalidate();
    }

    // Display the 3D phone illustration

    @Override
    protected void onDraw(Canvas canvas) {

        super.onDraw(canvas);

        float centerX = getWidth() / 2f;
        float centerY = getHeight() / 2f;

        canvas.save();

        // Move drawing origin to the center

        canvas.translate(centerX, centerY);

        // Create 3D rotation transformation

        camera.save();

        camera.rotateX(angleX);
        camera.rotateY(angleY);
        camera.rotateZ(angleZ);

        android.graphics.Matrix matrix =
                new android.graphics.Matrix();

        camera.getMatrix(matrix);

        camera.restore();

        // Center the rotation around the phone

        matrix.preTranslate(-85f * density,
                -150f * density);

        matrix.postTranslate(85f * density,
                150f * density);

        canvas.concat(matrix);

        // Draw phone body

        paint.setColor(Color.rgb(40, 55, 80));
        paint.setStyle(Paint.Style.FILL);

        RectF phoneBody = new RectF(
                0,
                0,
                170f * density,
                300f * density
        );

        canvas.drawRoundRect(
                phoneBody,
                25f * density,
                25f * density,
                paint
        );

        // Draw phone border

        paint.setColor(Color.rgb(90, 170, 255));
        paint.setStyle(Paint.Style.STROKE);
        paint.setStrokeWidth(3f * density);

        canvas.drawRoundRect(
                phoneBody,
                25f * density,
                25f * density,
                paint
        );

        // Draw phone screen

        paint.setStyle(Paint.Style.FILL);
        paint.setColor(Color.rgb(12, 25, 45));

        RectF screen = new RectF(
                10f * density,
                12f * density,
                160f * density,
                288f * density
        );

        canvas.drawRoundRect(
                screen,
                18f * density,
                18f * density,
                paint
        );

        // Draw top speaker

        paint.setColor(Color.rgb(100, 120, 150));

        canvas.drawRoundRect(
                new RectF(
                        65f * density,
                        18f * density,
                        105f * density,
                        23f * density
                ),
                4f * density,
                4f * density,
                paint
        );

        // Draw center gyroscope symbol

        paint.setColor(Color.rgb(60, 180, 255));
        paint.setStyle(Paint.Style.STROKE);
        paint.setStrokeWidth(3f * density);

        canvas.drawCircle(
                85f * density,
                145f * density,
                32f * density,
                paint
        );

        // Draw X axis

        paint.setColor(Color.rgb(255, 100, 100));
        paint.setStrokeWidth(4f * density);

        canvas.drawLine(
                85f * density,
                145f * density,
                125f * density,
                145f * density,
                paint
        );

        // Draw Y axis

        paint.setColor(Color.rgb(80, 230, 160));

        canvas.drawLine(
                85f * density,
                145f * density,
                85f * density,
                105f * density,
                paint
        );

        // Draw center circle

        paint.setColor(Color.WHITE);
        paint.setStyle(Paint.Style.FILL);

        canvas.drawCircle(
                85f * density,
                145f * density,
                6f * density,
                paint
        );

        // Draw Z axis indicator

        paint.setColor(Color.rgb(80, 170, 255));
        paint.setStyle(Paint.Style.STROKE);
        paint.setStrokeWidth(3f * density);

        canvas.drawCircle(
                85f * density,
                145f * density,
                18f * density,
                paint
        );

        // Restore canvas

        canvas.restore();

        // Display rotation values outside the phone

        paint.setStyle(Paint.Style.FILL);
        paint.setTextAlign(Paint.Align.CENTER);
        paint.setTextSize(13f * density);

        paint.setColor(Color.rgb(255, 110, 110));

        canvas.drawText(
                String.format(Locale.US, "X: %.1f°", angleX),
                centerX,
                getHeight() - 40f * density,
                paint
        );

        paint.setColor(Color.rgb(80, 230, 160));

        canvas.drawText(
                String.format(Locale.US, "Y: %.1f°", angleY),
                centerX,
                getHeight() - 23f * density,
                paint
        );

        paint.setColor(Color.rgb(100, 180, 255));

        canvas.drawText(
                String.format(Locale.US, "Z: %.1f°", angleZ),
                centerX,
                getHeight() - 6f * density,
                paint
        );
    }
}

