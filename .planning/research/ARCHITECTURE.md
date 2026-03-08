# Architecture Patterns

**Domain:** Android MQTT位置服务系统
**Researched:** 2026-03-08

## Recommended Architecture

The Android MQTT位置服务系统 follows a layered architecture with clear separation of concerns:

```
┌─────────────────────────────────────────────────────────────┐
│                    Presentation Layer                       │
│                   (Jetpack Compose UI)                      │
├─────────────────────────────────────────────────────────────┤
│                    Business Logic Layer                     │
│              (ViewModel + Use Cases + State)                │
├─────────────────────────────────────────────────────────────┤
│                   Data & Services Layer                     │
│         (Repository + Location Service + MQTT Client)       │
├─────────────────────────────────────────────────────────────┤
│                    Platform Layer                           │
│     (Android Framework + Foreground Service + Room)         │
└─────────────────────────────────────────────────────────────┘
```

### Component Boundaries

| Component | Responsibility | Communicates With |
|-----------|---------------|-------------------|
| MainActivity/NavHost | Hosts the Compose UI and manages app navigation | ViewModel, Composables |
| LocationScreen | Displays location data and settings UI | ViewModel, Composables |
| LocationViewModel | Manages UI state and exposes business logic | UI, LocationUseCases |
| StartTrackingUseCase | Encapsulates logic for starting location tracking | ViewModel, LocationService |
| StopTrackingUseCase | Encapsulates logic for stopping location tracking | ViewModel, LocationService |
| LocationService | Foreground service that manages continuous location updates | FusedLocationProvider, MqttClient |
| LocationRepository | Coordinates location data flow between sources | LocationService, RoomDatabase |
| MqttClientWrapper | Handles MQTT connection and message exchange | HiveMQ MQTT Client, Repository |
| LocationDatabase | Local storage for location history | Room, Repository |
| AppModule | Provides dependency injection bindings | All components via Hilt |

### Data Flow

1. User interacts with UI (Jetpack Compose) to start tracking
2. UI calls ViewModel which executes StartTrackingUseCase
3. UseCase communicates with LocationService through LocationRepository
4. LocationService (Foreground Service) requests location updates from FusedLocationProvider
5. Location updates are sent to MqttClientWrapper for publishing via HiveMQ MQTT Client
6. Location data is also saved to LocationDatabase via Room
7. UI observes state changes through ViewModel and updates accordingly

## Patterns to Follow

### Pattern 1: Foreground Service for Background Location Tracking
**What:** Use Android's Foreground Service to maintain continuous location tracking in background
**When:** Whenever continuous location updates are required regardless of app visibility
**Example:**
```kotlin
class LocationTrackingService : Service(), LifecycleService() {
    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private lateinit var mqttClient: MqttClient
    
    override fun onCreate() {
        super.onCreate()
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)
        mqttClient = HiveMqMqttClient.builder().build()
        startForeground(NOTIFICATION_ID, createNotification())
    }
    
    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        startLocationUpdates()
        return START_STICKY
    }
    
    private fun startLocationUpdates() {
        val locationRequest = LocationRequest.create().apply {
            interval = 1000 // 1 second
            fastestInterval = 500 // 0.5 second
            priority = Priority.PRIORITY_HIGH_ACCURACY
        }
        
        fusedLocationClient.requestLocationUpdates(
            locationRequest,
            locationCallback,
            Looper.getMainLooper()
        )
    }
}
```

### Pattern 2: Repository Pattern for Data Management
**What:** Centralize data operations through a repository that abstracts data sources
**When:** Managing data from multiple sources (local database, remote services)
**Example:**
```kotlin
class LocationRepository @Inject constructor(
    private val locationDao: LocationDao,
    private val mqttClient: MqttClientWrapper
) {
    fun saveLocation(location: LocationEntity) = viewModelScope.launch {
        // Save to local database
        locationDao.insertLocation(location)
        
        // Publish to MQTT broker
        mqttClient.publishLocation(location)
    }
    
    fun getLocationHistory(): Flow<List<LocationEntity>> {
        return locationDao.getLocationHistory()
    }
}
```

### Pattern 3: ViewModel for UI State Management
**What:** Manage UI-related data in a lifecycle-conscious way
**When:** Handling UI state that needs to survive configuration changes
**Example:**
```kotlin
@HiltViewModel
class LocationViewModel @Inject constructor(
    private val locationRepository: LocationRepository
) : ViewModel() {
    
    private val _uiState = mutableStateOf(LocationUiState())
    val uiState: State<LocationUiState> = _uiState
    
    fun startTracking() {
        // Execute use case to start tracking
        // Update UI state accordingly
        _uiState.value = _uiState.value.copy(isTracking = true)
    }
    
    fun stopTracking() {
        // Execute use case to stop tracking
        _uiState.value = _uiState.value.copy(isTracking = false)
    }
}
```

## Anti-Patterns to Avoid

### Anti-Pattern 1: Direct Context Access in Data Layer
**What:** Accessing Android Context directly from repositories or data sources
**Why bad:** Creates tight coupling, makes testing difficult, violates separation of concerns
**Instead:** Use dependency injection to provide required dependencies

### Anti-Pattern 2: Blocking Operations on Main Thread
**What:** Performing database or network operations directly on the UI thread
**Why bad:** Causes UI freezing and ANR errors
**Instead:** Use Kotlin Coroutines with proper dispatchers for background operations

### Anti-Pattern 3: Monolithic Service Implementation
**What:** Putting all location and MQTT logic in a single service class
**Why bad:** Difficult to maintain, test, and extend
**Instead:** Separate concerns into distinct components with clear responsibilities

## Scalability Considerations

| Concern | At 100 users | At 10K users | At 1M users |
|---------|--------------|--------------|-------------|
| 电池消耗优化 | Standard location request intervals | Adaptive frequency based on movement | Predictive power management |
| 数据存储 | Local Room database sufficient | Consider local caching strategy | Hybrid local/cloud storage |
| 网络带宽 | Direct MQTT connection | Connection pooling, compression | Edge computing, data aggregation |
| 并发连接 | Single MQTT broker | Clustered brokers | Distributed MQTT infrastructure |
| 位置精度 | Standard GPS accuracy | Differential GPS corrections | Multi-sensor fusion |

## Sources

- Android Developers Documentation on Foreground Services
- Google's recommendations for location tracking apps
- Jetpack documentation on architecture components
- HiveMQ MQTT Client documentation
- Android Background Execution Limits documentation