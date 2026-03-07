# Feature Landscape

**Domain:** Android MQTT Location Service
**Researched:** 2026-03-08

## Table Stakes

Features users expect. Missing = product feels incomplete.

| Feature | Why Expected | Complexity | Notes |
|---------|--------------|------------|-------|
| Real-time Location Tracking | Core value proposition of any location tracking service | Medium | Must support high-frequency updates (second-level intervals) with GPS accuracy |
| Persistent MQTT Connection | Fundamental requirement for MQTT-based architecture | High | Must maintain connection despite network interruptions, device sleep, app backgrounding |
| Background Service Operation | Essential for continuous tracking without user intervention | High | Requires foreground service implementation to work on modern Android versions |
| Location History Storage | Users expect to review past locations | Medium | Local storage with export capabilities (GPX, JSON) |
| Geofencing/Region Monitoring | Standard feature in location tracking apps | Medium | Trigger events when entering/leaving defined areas |
| Authentication Mechanisms | Security requirement for MQTT brokers | Low | Username/password, TLS certificates, client IDs |
| Manual Location Publishing | Basic user control over location sharing | Low | Allow users to send current location on demand |
| Location Data Encryption | Privacy expectation in 2026 | Medium | End-to-end encryption of location payloads |
| Multi-device Support | Users have multiple devices | Medium | Handle multiple client connections with unique identifiers |
| Offline Location Queue | Network resilience requirement | Medium | Store locations locally when offline and send when reconnected |

## Differentiators

Features that set product apart. Not expected, but valued.

| Feature | Value Proposition | Complexity | Notes |
|---------|-------------------|------------|-------|
| Adaptive Tracking Frequency | Battery optimization based on movement/activity | High | Adjust update frequency based on user activity/motion |
| Cross-platform Friend Tracking | Share locations with contacts on different platforms | High | Integration with mapping services for cross-platform visualization |
| Beacon/iBeacon Support | Enhanced location precision indoors | Medium | Support for Bluetooth beacon proximity detection |
| Motion-based Tracking Modes | Different tracking strategies for different use cases | Medium | Move mode, significant changes mode, manual mode |
| Remote Configuration | Administer devices from central location | Medium | Push settings updates via MQTT commands |
| Payload Compression | Reduced bandwidth/data usage | Medium | Efficient transmission over limited networks |
| Advanced Geofencing | Complex region definitions and logic | High | Nested regions, time-based triggers, overlapping zones |
| Location Data Analytics | Insights from movement patterns | High | Trip detection, frequently visited locations, behavior analysis |
| Emergency/Panic Button | Immediate alert functionality | Medium | High-priority message with location during emergencies |
| Low Power Optimization | Extended battery life during tracking | High | Intelligent power management based on device state |

## Anti-Features

Features to explicitly NOT build.

| Anti-Feature | Why Avoid | What to Do Instead |
|--------------|-----------|-------------------|
| Continuous High-Frequency GPS | Drains battery quickly with minimal benefit | Use adaptive tracking based on movement |
| Unencrypted Location Transmission | Security risk for user privacy | Always implement end-to-end encryption |
| Aggressive Background Permissions | Violates user trust and privacy | Request minimal necessary permissions with clear justification |
| Complex UI Over Customization | Overwhelms users with options | Focus on sensible defaults with essential customization |
| iOS Platform Support | Out of current scope per project constraints | Focus exclusively on Android optimization |
| Video/Image Transmission | Excessive bandwidth and battery drain | Keep focused on efficient location data only |
| Social Media Integration | Privacy concerns and scope creep | Maintain focus on core location tracking functionality |

## Feature Dependencies

```
Persistent MQTT Connection → Real-time Location Tracking
Background Service Operation → Persistent MQTT Connection
Authentication Mechanisms → Persistent MQTT Connection
Location History Storage → Real-time Location Tracking
Geofencing/Region Monitoring → Real-time Location Tracking
Manual Location Publishing → Persistent MQTT Connection
Location Data Encryption → Persistent MQTT Connection
Multi-device Support → Authentication Mechanisms
Offline Location Queue → Persistent MQTT Connection
Adaptive Tracking Frequency → Real-time Location Tracking
Advanced Geofencing → Geofencing/Region Monitoring
Cross-platform Friend Tracking → Location History Storage
Emergency/Panic Button → Real-time Location Tracking
Remote Configuration → Persistent MQTT Connection
Location Data Analytics → Location History Storage
```

## MVP Recommendation

Prioritize:
1. Real-time Location Tracking
2. Persistent MQTT Connection
3. Background Service Operation
4. Authentication Mechanisms
5. Manual Location Publishing

Defer: Advanced Geofencing, Location Data Analytics, Cross-platform Friend Tracking - these are valuable but not essential for initial validation

## Sources

- OwnTracks Android/iOS App Documentation (https://owntracks.org/booklet/)
- Eclipse Paho MQTT Client Documentation
- Android Developer Documentation on Background Services
- PresencePublisher Android App (https://f-droid.org/packages/org.ostrya.presencepublisher)
- Industry analysis of GPS tracking apps in 2026