package org.manusmith.shell.util;

import javafx.animation.*;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.ProgressIndicator;
import javafx.scene.effect.DropShadow;
import javafx.scene.effect.Glow;
import javafx.scene.paint.Color;
import javafx.scene.transform.Scale;
import javafx.util.Duration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

// Runnable is in java.lang, not java.util.function

/**
 * Utility class for smooth animations and micro-interactions in the application.
 * Provides Material Design-inspired animations and transitions.
 */
public class AnimationUtil {
    
    private static final Logger logger = LoggerFactory.getLogger(AnimationUtil.class);
    
    // Standard durations for consistent animations
    public static final Duration FAST = Duration.millis(150);
    public static final Duration NORMAL = Duration.millis(250);
    public static final Duration SLOW = Duration.millis(400);
    
    /**
     * Fade in animation for nodes
     */
    public static void fadeIn(Node node, Duration duration, Runnable onFinished) {
        if (node == null) return;
        
        FadeTransition fade = new FadeTransition(duration, node);
        fade.setFromValue(0.0);
        fade.setToValue(1.0);
        fade.setInterpolator(Interpolator.EASE_OUT);
        
        if (onFinished != null) {
            fade.setOnFinished(e -> onFinished.run());
        }
        
        node.setOpacity(0.0);
        node.setVisible(true);
        fade.play();
    }
    
    /**
     * Fade out animation for nodes
     */
    public static void fadeOut(Node node, Duration duration, Runnable onFinished) {
        if (node == null) return;
        
        FadeTransition fade = new FadeTransition(duration, node);
        fade.setFromValue(node.getOpacity());
        fade.setToValue(0.0);
        fade.setInterpolator(Interpolator.EASE_IN);
        
        fade.setOnFinished(e -> {
            node.setVisible(false);
            if (onFinished != null) {
                onFinished.run();
            }
        });
        
        fade.play();
    }
    
    /**
     * Scale animation for button press effect
     */
    public static void scaleButton(Node button, double fromScale, double toScale, Duration duration) {
        if (button == null) return;
        
        Scale scale = new Scale();
        scale.setPivotX(button.getBoundsInLocal().getWidth() / 2);
        scale.setPivotY(button.getBoundsInLocal().getHeight() / 2);
        
        if (!button.getTransforms().contains(scale)) {
            button.getTransforms().add(scale);
        }
        
        Timeline timeline = new Timeline(
            new KeyFrame(Duration.ZERO, 
                new KeyValue(scale.xProperty(), fromScale, Interpolator.EASE_OUT),
                new KeyValue(scale.yProperty(), fromScale, Interpolator.EASE_OUT)
            ),
            new KeyFrame(duration,
                new KeyValue(scale.xProperty(), toScale, Interpolator.EASE_OUT),
                new KeyValue(scale.yProperty(), toScale, Interpolator.EASE_OUT)
            )
        );
        
        timeline.play();
    }
    
    /**
     * Hover effect for buttons and interactive elements
     */
    public static void addHoverEffect(Node node) {
        if (node == null) return;
        
        DropShadow shadow = new DropShadow();
        shadow.setColor(Color.color(0, 0, 0, 0.2));
        shadow.setRadius(5.0);
        shadow.setOffsetX(2.0);
        shadow.setOffsetY(2.0);
        
        node.setOnMouseEntered(e -> {
            scaleButton(node, 1.0, 1.05, FAST);
            node.setEffect(shadow);
        });
        
        node.setOnMouseExited(e -> {
            scaleButton(node, 1.05, 1.0, FAST);
            node.setEffect(null);
        });
        
        if (node instanceof Button) {
            node.setOnMousePressed(e -> scaleButton(node, 1.05, 0.95, Duration.millis(50)));
            node.setOnMouseReleased(e -> scaleButton(node, 0.95, 1.05, Duration.millis(100)));
        }
    }
    
    /**
     * Theme transition animation
     */
    public static void animateThemeTransition(Node root, Runnable themeChangeAction) {
        if (root == null || themeChangeAction == null) return;
        
        logger.debug("Starting theme transition animation");
        
        // Fade out
        fadeOut(root, FAST, () -> {
            // Change theme during fade
            themeChangeAction.run();
            
            // Fade back in
            fadeIn(root, FAST, () -> 
                logger.debug("Theme transition animation completed")
            );
        });
    }
    
    /**
     * Success feedback animation with glow effect
     */
    public static void showSuccessFeedback(Node node) {
        if (node == null) return;
        
        Glow glow = new Glow(0.0);
        node.setEffect(glow);
        
        Timeline glowAnimation = new Timeline(
            new KeyFrame(Duration.ZERO, new KeyValue(glow.levelProperty(), 0.0)),
            new KeyFrame(NORMAL, new KeyValue(glow.levelProperty(), 0.8)),
            new KeyFrame(NORMAL.multiply(2), new KeyValue(glow.levelProperty(), 0.0))
        );
        
        glowAnimation.setOnFinished(e -> node.setEffect(null));
        glowAnimation.play();
        
        // Also add a subtle scale effect
        scaleButton(node, 1.0, 1.1, NORMAL);
        
        PauseTransition pause = new PauseTransition(NORMAL);
        pause.setOnFinished(e -> scaleButton(node, 1.1, 1.0, NORMAL));
        pause.play();
    }
    
    /**
     * Error shake animation
     */
    public static void showErrorFeedback(Node node) {
        if (node == null) return;
        
        double originalX = node.getTranslateX();
        
        Timeline shake = new Timeline(
            new KeyFrame(Duration.ZERO, new KeyValue(node.translateXProperty(), originalX)),
            new KeyFrame(Duration.millis(50), new KeyValue(node.translateXProperty(), originalX - 10)),
            new KeyFrame(Duration.millis(100), new KeyValue(node.translateXProperty(), originalX + 10)),
            new KeyFrame(Duration.millis(150), new KeyValue(node.translateXProperty(), originalX - 5)),
            new KeyFrame(Duration.millis(200), new KeyValue(node.translateXProperty(), originalX + 5)),
            new KeyFrame(Duration.millis(250), new KeyValue(node.translateXProperty(), originalX))
        );
        
        shake.play();
        
        // Add red glow effect
        DropShadow redShadow = new DropShadow();
        redShadow.setColor(Color.color(1.0, 0.2, 0.2, 0.6));
        redShadow.setRadius(10.0);
        node.setEffect(redShadow);
        
        PauseTransition pause = new PauseTransition(Duration.millis(1000));
        pause.setOnFinished(e -> node.setEffect(null));
        pause.play();
    }
    
    /**
     * Loading animation for progress indicators
     */
    public static void animateProgress(ProgressIndicator progress, boolean show) {
        if (progress == null) return;
        
        if (show) {
            progress.setProgress(ProgressIndicator.INDETERMINATE_PROGRESS);
            fadeIn(progress, FAST, null);
            
            // Add rotation animation for visual appeal
            RotateTransition rotate = new RotateTransition(Duration.seconds(2), progress);
            rotate.setByAngle(360);
            rotate.setCycleCount(Animation.INDEFINITE);
            rotate.setInterpolator(Interpolator.LINEAR);
            rotate.play();
            
            progress.setUserData(rotate); // Store for later cleanup
        } else {
            Animation rotation = (Animation) progress.getUserData();
            if (rotation != null) {
                rotation.stop();
            }
            
            fadeOut(progress, FAST, () -> {
                progress.setProgress(0);
                progress.setUserData(null);
            });
        }
    }
    
    /**
     * Slide in animation from direction
     */
    public static void slideIn(Node node, Direction direction, Duration duration, Runnable onFinished) {
        if (node == null) return;
        
        double originalX = node.getTranslateX();
        double originalY = node.getTranslateY();
        
        // Set initial position based on direction
        switch (direction) {
            case LEFT:
                node.setTranslateX(originalX - 200);
                break;
            case RIGHT:
                node.setTranslateX(originalX + 200);
                break;
            case TOP:
                node.setTranslateY(originalY - 200);
                break;
            case BOTTOM:
                node.setTranslateY(originalY + 200);
                break;
        }
        
        node.setOpacity(0.0);
        node.setVisible(true);
        
        // Animate to original position
        Timeline slide = new Timeline(
            new KeyFrame(Duration.ZERO,
                new KeyValue(node.translateXProperty(), node.getTranslateX()),
                new KeyValue(node.translateYProperty(), node.getTranslateY()),
                new KeyValue(node.opacityProperty(), 0.0)
            ),
            new KeyFrame(duration,
                new KeyValue(node.translateXProperty(), originalX, Interpolator.EASE_OUT),
                new KeyValue(node.translateYProperty(), originalY, Interpolator.EASE_OUT),
                new KeyValue(node.opacityProperty(), 1.0, Interpolator.EASE_OUT)
            )
        );
        
        if (onFinished != null) {
            slide.setOnFinished(e -> onFinished.run());
        }
        
        slide.play();
    }
    
    /**
     * Create a pulsing animation for attention-grabbing elements
     */
    public static Timeline createPulseAnimation(Node node, Duration duration) {
        if (node == null) return null;
        
        Timeline pulse = new Timeline(
            new KeyFrame(Duration.ZERO, new KeyValue(node.opacityProperty(), 1.0)),
            new KeyFrame(duration.divide(2), new KeyValue(node.opacityProperty(), 0.5)),
            new KeyFrame(duration, new KeyValue(node.opacityProperty(), 1.0))
        );
        
        pulse.setCycleCount(Animation.INDEFINITE);
        pulse.setAutoReverse(false);
        
        return pulse;
    }
    
    /**
     * Direction enumeration for slide animations
     */
    public enum Direction {
        LEFT, RIGHT, TOP, BOTTOM
    }
}
