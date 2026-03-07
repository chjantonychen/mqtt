# Architecture Patterns

**Domain:** Android MQTT location service
**Researched:** 2026-03-08

## Recommended Architecture

Typical Android MQTT location service systems follow a component-based architecture with clear separation of concerns. The architecture consists of:

1. **UI Layer** - MainActivity that handles user interactions and displays location/MQTT status
2. **Location Service Layer** - Background location tracking using Android's LocationManager or FusedLocationProvider
3. **MQTT Communication Layer** - Handles connection to MQTT broker and message publishing/subscribing
4. **Data Model Layer** - Represents location data structures for transmission
5. **Permission Management Layer** - Handles runtime permissions for location and network access

```
┌─────────────────┐    ┌──────────────────┐    ┌──────────────────┐
│   UI Layer      │    │ Location Service │    │  MQTT Layer      │
│  (MainActivity) │◄──►│   (Location      │◄──►│ (MqttHandler)    │
│                 │    │    Updater)      │    │                  │
└─────────────────┘    └──────────────────┘    └──────────────────┘
         ▲                       ▲                       ▲
         │                       │                       │
         ▼                       ▼                       ▼
┌─────────────────┐    ┌──────────────────┐    ┌──────────────────┐
│ Permission      │    │ Data Model       │    │ Android System   │
│ Management      │    │ (StateData)      │    │ Services         │
│ (Location       │    │                  │    │ (LocationManager,│
│ Permission      │    │                  │    │  MqttService)    │
│ Manager)        │    │                  │    │                  │
└─────────────────┘    └──────────────────┘    └──────────────────┘
```

### Component Boundaries

| Component | Responsibility | Communicates With |
|-----------|---------------|-------------------|
| MainActivity | UI presentation, user interaction handling, component coordination | LocationUpdater, MqttHandler, LocationPermissionManager |
| LocationUpdater | Continuous location tracking and updates | MainActivity (callback), Android LocationManager |
| MqttHandler | MQTT connection management, message publishing/subscribing | MainActivity, Eclipse Paho MQTT library |
| LocationPermissionManager | Runtime permission handling for location services | MainActivity |
| StateData | Data model representing location information for MQTT transmission | MqttHandler |
| Android LocationManager | System-level location services | LocationUpdater |
| Eclipse Paho MQTT Service | System-level MQTT communication | MqttHandler |

### Data Flow

1. **Application Startup**: MainActivity initializes and requests location permissions via LocationPermissionManager
2. **Location Tracking Initiation**: MainActivity creates LocationUpdater which registers with Android LocationManager for location updates
3. **MQTT Connection**: User initiates MQTT connection through UI, MainActivity creates MqttHandler which connects to broker
4. **Location Data Processing**: LocationUpdater receives location updates from LocationManager and passes to MainActivity via callback
5. **MQTT Publishing**: MainActivity receives location data and forwards to MqttHandler for JSON serialization and publishing
6. **Data Transmission**: MqttHandler formats StateData as JSON and publishes to MQTT broker via Paho library
7. **System Integration**: Android LocationManager and MqttService handle low-level communication with system services

## Patterns to Follow

### Pattern 1: Component Separation
**What:** Separate location tracking, MQTT communication, and UI concerns into distinct components
**When:** Always in Android MQTT location services to maintain clean architecture and testability
**Example:**
```kotlin
// Location tracking separated from MQTT communication
class LocationUpdater(context: Context, private val locationChangeHandler: (Location) -> Unit) {
    private val locationManager = context.getSystemService(LOCATION_SERVICE) as LocationManager
    
    fun start() {
        locationManager.requestLocationUpdates(
            LocationManager.GPS_PROVIDER,
            2000L,  // Update interval
            1f,     // Distance filter
            locationListener
        )
    }
}

class MqttHandler(context: Context, private var clientId: String) {
    private var mqttClient: MqttAndroidClient
    
    fun connect(callback: (Boolean) -> Unit) {
        // MQTT connection logic
    }
    
    fun sendMessage(deviceName: String, topic: String, message: String, lat: String, lon: String) {
        // Message publishing logic
    }
}
```

### Pattern 2: Callback-Based Communication
**What:** Use callback interfaces for inter-component communication to maintain loose coupling
**When:** When components need to communicate without tight dependencies
**Example:**
```kotlin
class LocationUpdater(context: Context, private val locationChangeHandler: (Location) -> Unit) {
    private val locationListener = android.location.LocationListener { location ->
        locationChangeHandler(location)  // Callback to MainActivity
    }
}
```

### Pattern 3: Foreground Service for Persistent Tracking
**What:** Use Android foreground services for continuous location tracking to ensure app remains active
**When:** When continuous location tracking is required even when app is in background
**Example:**
```xml
<!-- In AndroidManifest.xml -->
<uses-permission android:name="android.permission.FOREGROUND_SERVICE" />
<service android:name="org.eclipse.paho.android.service.MqttService" 
         android:foregroundServiceType="location" />
```

## Anti-Patterns to Avoid

### Anti-Pattern 1: Monolithic Activity
**What:** Putting all location tracking and MQTT logic directly in MainActivity
**Why bad:** Leads to tightly coupled, hard-to-maintain code that's difficult to test
**Instead:** Separate concerns into dedicated components (LocationUpdater, MqttHandler, etc.)

### Anti-Pattern 2: Blocking Main Thread
**What:** Performing MQTT operations or location processing on the UI thread
**Why bad:** Causes UI freezing and ANR (Application Not Responding) errors
**Instead:** Use background threads for MQTT operations and location processing

### Anti-Pattern 3: Ignoring Android Permissions
**What:** Not properly handling runtime permissions for location and network access
**Why bad:** App crashes or fails to function on Android 6.0+ devices
**Instead:** Implement proper permission checking and request flows

## Scalability Considerations

| Concern | At 100 users | At 10K users | At 1M users |
|---------|--------------|--------------|-------------|
| Location update frequency | 2-second intervals acceptable | May need adaptive throttling | Smart sampling based on movement |
| MQTT connection management | Direct broker connections | Connection pooling recommended | Dedicated MQTT gateway layer |
| Data storage | In-memory or local storage | Database persistence needed | Distributed database with caching |
| Battery optimization | Standard Android optimizations | Adaptive location sampling | AI-driven power management |
| Network resilience | Basic retry logic | Exponential backoff | Sophisticated offline buffering |

## Sources

- GitHub repository examples (gurkanucar/mqtt-example, Ayasha01/LocateMeAndroid)
- Android Developer documentation on location services and foreground services
- Eclipse Paho MQTT client documentation
- Modern Android architecture guidelines for background processing