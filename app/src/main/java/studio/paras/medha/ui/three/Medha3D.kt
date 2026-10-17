package studio.paras.medha.ui.three

import androidx.compose.foundation.layout.Box
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.withFrameNanos
import androidx.compose.ui.Modifier
import io.github.sceneview.SceneView
import io.github.sceneview.SurfaceType
import io.github.sceneview.math.Position
import io.github.sceneview.math.Rotation
import io.github.sceneview.rememberCameraNode
import io.github.sceneview.rememberEngine
import io.github.sceneview.rememberEnvironment
import io.github.sceneview.rememberEnvironmentLoader
import io.github.sceneview.rememberModelInstance
import io.github.sceneview.rememberModelLoader
import studio.paras.medha.core.design.LocalReducedMotion

/** 3D models bundled in assets/models (built by tools/models/make_models.py). */
object Medha3D {
    const val Coin = "models/coin.glb"
    const val Trophy = "models/trophy.glb"
}

/**
 * A real-time 3D model (gold PBR, lit by SceneView's studio light) that turns slowly.
 * The background is transparent, so it sits on top of any screen.
 *
 * With "reduce motion" on it holds still at a three-quarter angle.
 */
@Composable
fun Spinning3DModel(
    asset: String,
    modifier: Modifier = Modifier,
    degreesPerSecond: Float = 70f,
    tiltDegrees: Float = 8f,
) {
    val calm = LocalReducedMotion.current
    var angle by remember { mutableFloatStateOf(25f) }
    LaunchedEffect(calm, degreesPerSecond) {
        if (calm) return@LaunchedEffect
        var last = 0L
        while (true) {
            withFrameNanos { now ->
                if (last != 0L) {
                    angle = (angle + (now - last) / 1_000_000_000f * degreesPerSecond) % 360f
                }
                last = now
            }
        }
    }
    Box(modifier) {
        val engine = rememberEngine()
        val modelLoader = rememberModelLoader(engine)
        val environmentLoader = rememberEnvironmentLoader(engine)
        val model = rememberModelInstance(modelLoader, assetFileLocation = asset)
        val cameraNode = rememberCameraNode(engine) {
            position = Position(x = 0f, y = 0f, z = 1.9f)
            lookAt(Position(0f, 0f, 0f))
        }
        SceneView(
            modifier = Modifier.matchParentSize(),
            // TextureSurface blends with Compose, so the scene has a see-through background.
            surfaceType = SurfaceType.TextureSurface,
            engine = engine,
            modelLoader = modelLoader,
            environmentLoader = environmentLoader,
            isOpaque = false,
            environment = rememberEnvironment(environmentLoader, isOpaque = false),
            cameraNode = cameraNode,
            // Display only: no orbit or pinch gestures.
            cameraManipulator = null,
            onGestureListener = null,
        ) {
            model?.let {
                ModelNode(
                    modelInstance = it,
                    scaleToUnits = 1f,
                    centerOrigin = Position(0f, 0f, 0f),
                    rotation = Rotation(x = tiltDegrees, y = angle, z = 0f),
                )
            }
        }
    }
}
