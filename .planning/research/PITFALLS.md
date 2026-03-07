# Domain Pitfalls

**Domain:** Android MQTT location service
**Researched:** 2026-03-08

## Critical Pitfalls

Mistakes that cause rewrites or major issues.

### Pitfall 1: Ignoring Android Background Execution Limits
**What goes wrong:** The location service works when the app is in foreground but stops functioning when the device enters Doze mode, App Standby, or when the screen turns off. Users report missing location data or complete service failures.

**Why it happens:** Android's power management features (Doze mode, App Standby) restrict network access and background execution. Many developers assume foreground services bypass all restrictions, but network access can still be throttled in Doze mode.

**Consequences:** 
- Loss of location data during critical periods
- MQTT connection drops without proper reconnection handling
- Poor user experience with inconsistent tracking
- Negative reviews due to perceived unreliability

**Prevention:** 
- Implement proper MQTT reconnection strategies with exponential backoff
- Use WorkManager for periodic location updates instead of relying solely on foreground services
- Request and properly handle "Allow all the time" location permissions
- Test thoroughly on different Android versions and OEM devices

**Detection:** Monitor MQTT connection logs and implement heartbeat mechanisms to detect when the service stops sending data.

**Phase:** Implementation phase - must be addressed during core service development

### Pitfall 2: Improper MQTT Connection Management
**What goes wrong:** MQTT connections drop frequently, especially when the device switches networks or enters power-saving modes. The client doesn't properly reconnect or handle session persistence.

**Why it happens:** 
- Not configuring proper keepalive intervals for mobile networks
- Failing to implement automatic reconnection with appropriate backoff strategies
- Incorrect session management leading to message loss
- Not handling network state changes properly

**Consequences:** 
- Lost location data during connection interruptions
- Increased battery drain from constant reconnection attempts
- Broker-side resource waste from abandoned sessions
- Inconsistent data delivery to subscribers

**Prevention:** 
- Use MQTT 5.0 features like session expiry and message expiry
- Implement intelligent reconnection strategies with exponential backoff
- Configure appropriate keepalive intervals (considering mobile network characteristics)
- Handle connection state changes gracefully with proper callbacks

**Detection:** Log connection state changes and implement connection health checks.

**Phase:** Implementation phase - core MQTT client setup

### Pitfall 3: Battery Drain from Aggressive Location Updates
**What goes wrong:** The app drains the battery quickly due to frequent GPS requests or inefficient location update strategies.

**Why it happens:** 
- Requesting high-frequency location updates without considering battery impact
- Not adapting location update frequency based on movement or importance
- Using high-accuracy GPS when lower accuracy would suffice
- Not properly managing location listeners when not needed

**Consequences:** 
- Users uninstall the app due to battery concerns
- Device battery optimization features kill the app
- Negative reviews and poor ratings
- Reduced adoption rates

**Prevention:** 
- Implement adaptive location update strategies (frequency based on movement)
- Use Fused Location Provider for optimal location sourcing
- Properly remove location updates when not needed
- Set appropriate timeouts for location requests

**Detection:** Monitor battery usage statistics and implement battery efficiency logging.

**Phase:** Implementation phase - location service optimization

## Moderate Pitfalls

### Pitfall 4: Inadequate Permission Handling
**What goes wrong:** The app fails to get necessary permissions or loses them due to Android updates or user changes.

**Why it happens:** 
- Not requesting background location permissions properly (especially Android 10+)
- Failing to handle permission revocation gracefully
- Not guiding users through vendor-specific battery optimization settings

**Consequences:** 
- Location service stops working completely
- App crashes due to permission exceptions
- Poor user experience requiring manual intervention

**Prevention:** 
- Implement proper permission request flows with explanations
- Handle permission denials gracefully with user guidance
- Check and request permissions at runtime when needed

**Phase:** Implementation phase - permission system setup

### Pitfall 5: Vendor-Specific Process Killing
**What goes wrong:** The service works on Google Pixel devices but gets aggressively killed on Samsung, Xiaomi, Huawei devices.

**Why it happens:** 
- OEMs implement aggressive battery saving features beyond Android standards
- Not accounting for vendor-specific background process limitations
- Failing to guide users to disable battery optimization for the app

**Consequences:** 
- Inconsistent behavior across devices
- Support burden from users reporting issues
- Reputation damage from negative reviews on specific devices

**Prevention:** 
- Test on multiple OEM devices
- Implement detection for known problematic vendors
- Provide clear user guidance for disabling battery optimization
- Use multiple approaches (foreground services + WorkManager + push notifications)

**Phase:** Testing phase - cross-device compatibility testing

## Minor Pitfalls

### Pitfall 6: Inefficient Data Serialization
**What goes wrong:** Location data takes too much bandwidth or processing time to serialize/deserialize.

**Why it happens:** 
- Using verbose formats like XML instead of compact formats like Protocol Buffers
- Sending redundant data with each location update
- Not compressing data for high-frequency updates

**Consequences:** 
- Increased data usage for users
- Slower transmission times affecting real-time performance
- Higher battery drain from network operations

**Prevention:** 
- Use efficient serialization formats (JSON/minimal structure or Protocol Buffers)
- Send only necessary data fields
- Implement data compression for high-frequency scenarios

**Phase:** Implementation phase - data handling optimization

### Pitfall 7: Lack of Error Handling and Recovery
**What goes wrong:** The service crashes or stops working when encountering unexpected situations.

**Why it happens:** 
- Not handling network errors, GPS errors, or MQTT errors properly
- Failing to recover from partial system failures
- Not implementing circuit breaker patterns for external dependencies

**Consequences:** 
- Complete service failure requiring manual restart
- Data loss during error conditions
- Poor reliability perception

**Prevention:** 
- Implement comprehensive error handling for all subsystems
- Add retry mechanisms with backoff for transient failures
- Implement graceful degradation when components fail

**Phase:** Implementation phase - robustness and error handling

## Phase-Specific Warnings

| Phase Topic | Likely Pitfall | Mitigation |
|-------------|---------------|------------|
| Core Service Implementation | Ignoring Android background execution limits | Implement WorkManager alongside foreground services |
| MQTT Integration | Improper connection management | Use HiveMQ client with automatic reconnection |
| Location Tracking | Battery drain from aggressive updates | Implement adaptive location strategies |
| Testing | Device-specific behavior differences | Test on multiple OEM devices |
| Deployment | Permission issues on newer Android versions | Implement proper permission flows |

## Sources

- Android Developer Documentation on Doze and App Standby
- HiveMQ MQTT Client documentation and best practices
- Stack Overflow discussions on Android MQTT service issues
- Medium articles on Android background service limitations (2025-2026)
- GitHub issues from popular MQTT Android client libraries
- "Don't kill my app!" website documenting OEM-specific issues