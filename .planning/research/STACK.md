# Technology Stack

**Project:** Android MQTT位置服务系统
**Researched:** 2026-03-08

## Recommended Stack

### Core Framework
| Technology | Version | Purpose | Why |
|------------|---------|---------|-----|
| Kotlin | 1.9+ | Primary language | Modern, concise, null-safe language with full Android support and excellent interoperability with Java libraries |
| Android SDK | API 34+ (Android 14) | Platform foundation | Latest Android features, improved background location handling, and better privacy controls |
| Jetpack Compose | 1.5+ | UI framework | Modern declarative UI toolkit that simplifies development and improves performance compared to traditional View system |
| Android Gradle Plugin | 8.0+ | Build system | Required for latest Android features and Kotlin Symbol Processing (KSP) |

### Location Services
| Technology | Version | Purpose | Why |
|------------|---------|---------|-----|
| Fused Location Provider API | Latest | Location tracking | Google's recommended approach for location services, intelligently manages power and accuracy by combining multiple location sources |
| Foreground Service | Android SDK | Background location tracking | Required for continuous location tracking in background on Android 8.0+, shows persistent notification to user for transparency |

### MQTT Communication
| Technology | Version | Purpose | Why |
|------------|---------|---------|-----|
| HiveMQ MQTT Client | 1.3.3 | MQTT communication | Modern, feature-rich MQTT 5.0/3.1.1 client with reactive API support, better performance than Paho, and active maintenance |
| OkHttp | 4.12+ | HTTP client (for MQTT over WebSocket) | Robust HTTP client with excellent performance, connection pooling, and automatic retries |

### Data Persistence
| Technology | Version | Purpose | Why |
|------------|---------|---------|-----|
| Room Persistence Library | 2.6+ | Local data storage | Official Android ORM with compile-time verification, LiveData/Flow integration, and excellent performance |
| Kotlin Coroutines | 1.7+ | Asynchronous operations | Modern approach to handling background tasks with structured concurrency and better performance than RxJava |

### Dependency Injection
| Technology | Version | Purpose | Why |
|------------|---------|---------|-----|
| Hilt | 2.50+ | Dependency injection | Official Android DI solution built on Dagger, provides compile-time code generation and simplified setup |

### Supporting Libraries
| Library | Version | Purpose | When to Use |
|---------|---------|---------|-------------|
| Lifecycle Components | 2.7+ | Lifecycle-aware components | Handling Android component lifecycles (Activity, Fragment, Service) to prevent memory leaks |
| ViewModel | 2.7+ | UI-related data holder | Managing UI-related data in a lifecycle-conscious way |
| WorkManager | 2.9+ | Deferrable background work | For periodic tasks that don't require constant location tracking |

## Alternatives Considered

| Category | Recommended | Alternative | Why Not |
|----------|-------------|-------------|---------|
| MQTT Client | HiveMQ MQTT Client | Eclipse Paho | Paho is older, less actively maintained, lacks MQTT 5.0 features, and has a more complex API |
| Location Services | Fused Location Provider + Foreground Service | LocationManager | LocationManager is outdated, less power-efficient, and doesn't intelligently combine location sources |
| UI Framework | Jetpack Compose | Traditional View System | Compose is Google's recommended approach, offers better performance, and reduces boilerplate |
| HTTP Client | OkHttp | Retrofit | Retrofit is primarily for REST APIs, while we need a general-purpose MQTT client that can work over WebSocket |

## Installation

```bash
# Core dependencies in build.gradle (Module: app)
dependencies {
    // Kotlin
    implementation "org.jetbrains.kotlin:kotlin-stdlib:1.9.22"
    implementation "org.jetbrains.kotlinx:kotlinx-coroutines-android:1.7.3"
    
    // Android UI
    implementation "androidx.compose.ui:ui:1.5.4"
    implementation "androidx.compose.material3:material3:1.1.2"
    implementation "androidx.compose.ui:ui-tooling-preview:1.5.4"
    implementation "androidx.activity:activity-compose:1.8.2"
    
    // Android Lifecycle
    implementation "androidx.lifecycle:lifecycle-runtime-ktx:2.7.0"
    implementation "androidx.lifecycle:lifecycle-viewmodel-compose:2.7.0"
    
    // Location Services
    implementation "com.google.android.gms:play-services-location:21.0.1"
    
    // MQTT Client
    implementation "com.hivemq:hivemq-mqtt-client:1.3.3"
    
    // HTTP Client (for MQTT over WebSocket)
    implementation "com.squareup.okhttp3:okhttp:4.12.0"
    
    // Room Database
    implementation "androidx.room:room-runtime:2.6.1"
    implementation "androidx.room:room-ktx:2.6.1"
    ksp "androidx.room:room-compiler:2.6.1"
    
    // Hilt for DI
    implementation "com.google.dagger:hilt-android:2.50"
    ksp "com.google.dagger:hilt-compiler:2.50"
    
    // WorkManager
    implementation "androidx.work:work-runtime-ktx:2.9.0"
    
    // Permissions
    implementation "androidx.activity:activity-compose:1.8.2"
}
```

## Sources

- Android Developers Documentation (developer.android.com)
- HiveMQ MQTT Client Documentation (hivemq.github.io/hivemq-mqtt-client)
- Google Play Services Location API Documentation
- Android Jetpack Documentation
- Context7 documentation for HiveMQ and Android libraries