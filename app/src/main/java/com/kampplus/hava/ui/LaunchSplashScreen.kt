package com.kampplus.hava.ui

import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun LaunchSplashScreen() {
    val floatAnimation = rememberInfiniteTransition(label = "splash-cloud")
    val cloudOffset by floatAnimation.animateFloat(
        initialValue = -7f,
        targetValue = 7f,
        animationSpec = infiniteRepeatable(tween(1500), RepeatMode.Reverse),
        label = "cloud-offset"
    )
    val glow by floatAnimation.animateFloat(
        initialValue = 0.72f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(tween(1800), RepeatMode.Reverse),
        label = "logo-glow"
    )

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    listOf(Color(0xFF7698AA), Color(0xFF405F78), Color(0xFF172B45))
                )
            )
    ) {
        Box(
            modifier = Modifier
                .align(Alignment.TopEnd)
                .offset(x = 90.dp, y = 100.dp)
                .size(260.dp)
                .background(Color.White.copy(alpha = 0.055f), CircleShape)
        )
        Box(
            modifier = Modifier
                .align(Alignment.BottomStart)
                .offset(x = (-90).dp, y = 70.dp)
                .size(280.dp)
                .background(Color(0xFF8FE5EE).copy(alpha = 0.08f), CircleShape)
        )

        Column(
            modifier = Modifier.align(Alignment.Center).padding(horizontal = 28.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Box(contentAlignment = Alignment.Center) {
                Box(
                    modifier = Modifier
                        .size(158.dp)
                        .graphicsLayer { alpha = glow }
                        .background(Color(0xFFB9F4F5).copy(alpha = 0.14f), CircleShape)
                )
                Surface(
                    modifier = Modifier.size(118.dp),
                    shape = RoundedCornerShape(38.dp),
                    color = Color.White.copy(alpha = 0.16f)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Text("☀️", fontSize = 54.sp, modifier = Modifier.offset(x = (-8).dp, y = (-9).dp))
                        Text("☁️", fontSize = 47.sp, modifier = Modifier.offset(x = 12.dp, y = 17.dp + cloudOffset.dp))
                    }
                }
            }

            Spacer(Modifier.height(30.dp))
            Text(
                "Havadis",
                color = Color.White,
                fontSize = 36.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = 0.4.sp
            )
            Spacer(Modifier.height(8.dp))
            Text(
                "Bugün gökyüzünde ne var?",
                color = Color.White.copy(alpha = 0.83f),
                fontSize = 16.sp
            )
            Spacer(Modifier.height(30.dp))
            Surface(
                shape = RoundedCornerShape(50),
                color = Color.White.copy(alpha = 0.12f)
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 10.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("🌦️", fontSize = 16.sp)
                    Text("Tahmin • Şehirler • Yol önerisi", color = Color.White.copy(alpha = 0.9f), fontSize = 12.sp)
                }
            }
        }

        Text(
            "Günün havası, bir adım önünde",
            modifier = Modifier.align(Alignment.BottomCenter).padding(bottom = 42.dp),
            color = Color.White.copy(alpha = 0.62f),
            fontSize = 12.sp
        )
    }
}
