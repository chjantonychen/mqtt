# Domain Pitfalls

**Domain:** Android MQTT位置服务系统
**Researched:** 2026-03-08

## Critical Pitfalls

Mistakes that cause rewrites or major issues.

### Pitfall 1: Incorrect Background Location Permissions Handling
**What goes wrong:** App fails to get "Allow all the time" permission or doesn't handle permission denial properly
**Why it happens:** Android 10+ requires explicit background location permission, and users often deny it due to privacy concerns
**Consequences:** Location tracking stops working when app is in background, leading to core functionality failure
**Prevention:** Implement proper permission request flow with clear rationale, guide users to settings if denied
**Detection:** Testing on Android 10+ devices with different permission scenarios

### Pitfall 2: Foreground Service Implementation Errors
**What goes wrong:** Foreground service doesn't start properly or gets killed by system
**Why it happens:** Missing permissions, incorrect service declaration, or not calling startForeground() quickly enough
**Consequences:** App can't track location in background, violating core requirement
**Prevention:** Follow Android's foreground service guidelines, handle all required permissions, test on multiple Android versions
**Detection:** Testing service lifecycle on different Android versions and manufacturer skins

### Pitfall 3: MQTT Connection Management Issues
**What goes wrong:** Connection drops frequently or doesn't reconnect automatically
**Why it happens:** Poor handling of network interruptions, not implementing proper reconnect logic, or incorrect keep-alive settings
**Consequences:** Location data loss, unreliable tracking service
**Prevention:** Implement robust reconnect logic with exponential backoff, proper session management, and offline message queuing
**Detection:** Network interruption testing, extended runtime testing

## Moderate Pitfalls

### Pitfall 1: Battery Drain from Continuous Location Updates
**What goes wrong:** App drains battery quickly due to inefficient location tracking
**Why it happens:** Requesting high-frequency updates without power management, not adapting to device state
**Consequences:** Users uninstall app due to battery concerns
**Prevention:** Implement adaptive location update intervals, use appropriate priority levels, handle Doze mode properly

### Pitfall 2: Data Loss During Network Outages
**What goes wrong:** Location data is lost when network is unavailable
**Why it happens:** Not implementing local storage for offline data or inadequate queue management
**Prevention:** Use Room database for local storage, implement message queuing with size limits

### Pitfall 3: UI Thread Blocking
**What goes wrong:** UI becomes unresponsive during MQTT operations or location processing
**Why it happens:** Performing network or database operations on main thread
**Prevention:** Use Kotlin Coroutines with proper dispatchers for background operations

## Minor Pitfalls

### Pitfall 1: Inadequate Error Handling
**What goes wrong:** App crashes or behaves unexpectedly when errors occur
**Why it happens:** Not handling all possible exception cases in location or MQTT operations
**Prevention:** Comprehensive try-catch blocks, proper error state management in UI

### Pitfall 2: Memory Leaks
**What goes wrong:** App consumes increasing memory over time
**Why it happens:** Not properly unregistering listeners or callbacks, holding context references incorrectly
**Prevention:** Use weak references where appropriate, properly unregister listeners in lifecycle methods

### Pitfall 3: Inconsistent Location Accuracy
**What goes wrong:** Location data varies significantly in accuracy
**Why it happens:** Not filtering location updates by accuracy, using inappropriate location provider settings
**Prevention:** Filter updates by accuracy threshold, use Fused Location Provider with proper priority settings

## Phase-Specific Warnings

| Phase Topic | Likely Pitfall | Mitigation |
|-------------|---------------|------------|
| 权限处理实现 | Background location permission rejection | Implement clear rationale and settings redirection |
| 前台服务开发 | Service not starting or getting killed | Follow Android foreground service best practices |
| MQTT连接管理 | Connection instability | Implement robust reconnect and offline queuing |
| 高频定位更新 | Excessive battery drain | Use adaptive intervals and power management |
| 数据持久化 | Performance issues with large datasets | Implement proper indexing and pagination |

## Sources

- Android Developers Documentation on background location permissions
- Android Background Execution Limits documentation
- Modern Android development best practices (2025-2026)
- Common issues reported in location tracking app development forums
- Android permission handling guidelines